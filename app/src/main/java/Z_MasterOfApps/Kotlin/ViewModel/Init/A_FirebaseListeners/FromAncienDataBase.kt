package Z_MasterOfApps.Kotlin.ViewModel.Init.A_FirebaseListeners

import Z_MasterOfApps.Kotlin.Model.I_CategoriesProduits
import Z_MasterOfApps.Kotlin.Model.I_CategoriesRepository
import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.C_GrossistsDataBase
import Z_MasterOfApps.Kotlin.Model.D_CouleursEtGoutesProduitsInfos
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.Init.A_FirebaseListeners.CurrentModels.setupCurrentModels
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Archives.CategoriesTabelleECB
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.TabelleSuppliersSA
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FromAncienDataBase {
    private val firebaseDatabase = Firebase.database
    private val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")
    private var jetPackExportListener: ValueEventListener? = null
    private val refColorsArticles = firebaseDatabase.getReference("H_ColorsArticles")
    private var colorsArticlesListener: ValueEventListener? = null
    private val lastKnownColorValues = mutableMapOf<Long, D_CouleursEtGoutesProduitsInfos>()

    fun setupRealtimeListeners(viewModel: ViewModelInitApp) {
        setupJetPackExportListener()
        setupColorsArticlesListener(viewModel)
        setupCurrentModels(viewModel)
        syncOldSuppliers(viewModel)
   //     syncOldCategories(viewModel) // Add this line to sync old categories

    }

    data class ProductState(
        val prixAchat: Double,
        val prixVent: Double,
        val colors: List<Long>
    )

    private fun setupJetPackExportListener() {
        jetPackExportListener?.let { refDBJetPackExport.removeEventListener(it) }

        jetPackExportListener = object : ValueEventListener {
            private val lastKnownValues = mutableMapOf<String, ProductState>()

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { productSnapshot ->
                    val productId = productSnapshot.key ?: return@forEach

                    val newPrixAchat = productSnapshot.child("monPrixAchat").getValue(Double::class.java) ?: 0.0
                    val newPrixVent = productSnapshot.child("monPrixVent").getValue(Double::class.java) ?: 0.0
                    val newColors = listOfNotNull(
                        productSnapshot.child("idcolor1").getValue(Long::class.java),
                        productSnapshot.child("idcolor2").getValue(Long::class.java),
                        productSnapshot.child("idcolor3").getValue(Long::class.java),
                        productSnapshot.child("idcolor4").getValue(Long::class.java)
                    ).filter { it > 0 } // Filter out invalid color IDs

                    val lastState = lastKnownValues[productId]
                    val pricesChanged = lastState?.prixAchat != newPrixAchat ||
                            lastState?.prixVent != newPrixVent
                    val colorsChanged = lastState?.colors != newColors

                    _ModelAppsFather.produitsFireBaseRef.child(productId).get()
                        .addOnSuccessListener { productDbSnapshot ->
                            val product = productDbSnapshot.getValue(A_ProduitModel::class.java)

                            if (product != null) {
                                // Existing product update logic
                                var updated = false

                                if (colorsChanged) {
                                    val beforeColors = product.statuesBase.coloursEtGoutsIds.toList()
                                    val updatedProduct = handleColorUpdate(productSnapshot, product)
                                    val afterColors = updatedProduct.statuesBase.coloursEtGoutsIds.toList()

                                    if (beforeColors != afterColors) {
                                        updated = true
                                    }
                                }

                                if (pricesChanged) {
                                    product.statuesBase.infosCoutes.monPrixAchat = newPrixAchat
                                    product.statuesBase.infosCoutes.monPrixVent = newPrixVent
                                    updated = true
                                }

                                if (updated) {
                                    _ModelAppsFather.produitsFireBaseRef.child(productId)
                                        .setValue(product)
                                        .addOnSuccessListener {
                                            lastKnownValues[productId] = ProductState(
                                                newPrixAchat,
                                                newPrixVent,
                                                newColors
                                            )
                                        }
                                }
                            } else {
                                // Implementation of TODO(1): Create a new product if it doesn't exist
                                val newProductId = productId.toLongOrNull() ?: return@addOnSuccessListener

                                val newProduct = A_ProduitModel(
                                    id = newProductId,
                                    init_nom = productSnapshot.child("nomArticle").getValue(String::class.java) ?: "Imported Product"
                                ).apply {
                                    // Set prices from old database
                                    statuesBase.infosCoutes.monPrixAchat = newPrixAchat
                                    statuesBase.infosCoutes.monPrixVent = newPrixVent

                                    // Set colors from old database
                                    statuesBase.coloursEtGoutsIds = newColors

                                    // Mark as needing update for any potential missing fields
                                    besoinToBeUpdated = true
                                }

                                // Save the new product to Firebase
                                _ModelAppsFather.produitsFireBaseRef.child(productId)
                                    .setValue(newProduct)
                                    .addOnSuccessListener {
                                        Log.d("Import", "Successfully imported product $productId from old database")
                                        lastKnownValues[productId] = ProductState(
                                            newPrixAchat,
                                            newPrixVent,
                                            newColors
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("Import", "Failed to import product $productId", e)
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Error checking product $productId", e)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Database error: ${error.message}")
            }
        }

        refDBJetPackExport.addValueEventListener(jetPackExportListener!!)
    }

    private fun setupColorsArticlesListener(viewModel: ViewModelInitApp) {
        colorsArticlesListener?.let { refColorsArticles.removeEventListener(it) }

        colorsArticlesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val colors = mutableListOf<D_CouleursEtGoutesProduitsInfos>()
                        snapshot.children.forEach { colorSnap ->
                            val colorId = colorSnap.key?.toLongOrNull() ?: return@forEach
                            val lastKnownColor = lastKnownColorValues[colorId]
                            val rawName = colorSnap.child("nameColore").getValue(String::class.java)
                            val rawIcon = colorSnap.child("iconColore").getValue(String::class.java)
                            val rawClassement = colorSnap.child("classementColore").getValue(Long::class.java)

                            val colorInfo = D_CouleursEtGoutesProduitsInfos(
                                id = colorId,
                                infosDeBase = D_CouleursEtGoutesProduitsInfos.InfosDeBase(
                                    nom = rawName?.takeIf { it.isNotBlank() }
                                        ?: lastKnownColor?.infosDeBase?.nom
                                        ?: "Non Defini",
                                    imogi = rawIcon?.takeIf { it.isNotBlank() }
                                        ?: lastKnownColor?.infosDeBase?.imogi
                                        ?: "🎨"
                                ),
                                statuesMutable = D_CouleursEtGoutesProduitsInfos.StatuesMutable(
                                    classmentDonsParentList = rawClassement
                                        ?: lastKnownColor?.statuesMutable?.classmentDonsParentList
                                        ?: 0,
                                    sonImageNeExistPas = false,
                                    caRefDonAncienDataBase = "H_ColorsArticles"
                                )
                            )

                            lastKnownColorValues[colorId] = colorInfo
                            colors.add(colorInfo)

                            try {
                                val existingColorTask = D_CouleursEtGoutesProduitsInfos.caReference
                                    .child(colorId.toString())
                                    .get()
                                    .await()

                                if (existingColorTask.exists()) {
                                    val existingNom = existingColorTask.child("infosDeBase/nom")
                                        .getValue(String::class.java)

                                    if (existingNom == "Non Defini" && rawName?.isNotBlank() == true) {
                                        D_CouleursEtGoutesProduitsInfos.caReference
                                            .child(colorId.toString())
                                            .setValue(colorInfo)
                                            .await()
                                    }
                                } else {
                                    D_CouleursEtGoutesProduitsInfos.caReference
                                        .child(colorId.toString())
                                        .setValue(colorInfo)
                                        .await()
                                }
                            } catch (e: Exception) {
                                // Handle error if needed
                            }
                        }

                        viewModel.modelAppsFather.couleursProduitsInfos.apply {
                            clear()
                            addAll(colors)
                        }
                    } catch (e: Exception) {
                        // Handle error if needed
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        }

        refColorsArticles.addValueEventListener(colorsArticlesListener!!)
    }


    fun handleColorUpdate(
        productSnapshot: DataSnapshot,
        product: A_ProduitModel
    ): A_ProduitModel {
        // Get current colors as a mutable list for easy modification
        val currentColors = product.statuesBase.coloursEtGoutsIds.toMutableList()

        // Extract new colors from snapshot, maintaining order
        val newColors = listOfNotNull(
            productSnapshot.child("idcolor1").getValue(Long::class.java),
            productSnapshot.child("idcolor2").getValue(Long::class.java),
            productSnapshot.child("idcolor3").getValue(Long::class.java),
            productSnapshot.child("idcolor4").getValue(Long::class.java)
        ).filter { it > 0 } // Filter out invalid color IDs

        // Update colors at their respective positions
        newColors.forEachIndexed { index, colorId ->
            if (index < currentColors.size) {
                // Update existing color at index
                currentColors[index] = colorId
            } else {
                // Add new color if we have room
                currentColors.add(colorId)
            }
        }

        // Trim any excess colors if new list is shorter
        while (currentColors.size > newColors.size) {
            currentColors.removeAt(currentColors.lastIndex)
        }

        // Convert to SnapshotStateList and update the product
        product.statuesBase.coloursEtGoutsIds = currentColors

        return product
    }
    private fun syncOldSuppliers(viewModel: ViewModelInitApp) {
        val newRef = firebaseDatabase.getReference("0_UiState_3_Host_Package_3_Prototype11Dec/C_GrossistsDataBase")
        val oldRef = Firebase.database.getReference("F_Suppliers")

        oldRef.addValueEventListener(object : ValueEventListener {
            private var lastKnownIds = mutableSetOf<Long>()

            override fun onDataChange(snapshot: DataSnapshot) {
                // Get current supplier IDs
                val currentIds = snapshot.children
                    .mapNotNull { it.getValue(TabelleSuppliersSA::class.java)?.idSupplierSu }
                    .toSet()

                // Handle deletions
                lastKnownIds.filter { it !in currentIds }.forEach { deletedId ->
                    newRef.child(deletedId.toString()).removeValue()
                    viewModel._modelAppsFather.grossistsDataBase.removeAll { it.id == deletedId }
                }

                // Update or add suppliers
                snapshot.children.forEach { snap ->
                    val supplier = snap.getValue(TabelleSuppliersSA::class.java) ?: return@forEach
                    val grossist = C_GrossistsDataBase(
                        id = supplier.idSupplierSu,
                        nom = supplier.nomSupplierSu.ifBlank { supplier.nameInFrenche.ifBlank { "Non Defini" } },
                        statueDeBase = C_GrossistsDataBase.StatueDeBase(couleur = supplier.couleurSu)
                    )

                    // Update local list
                    val index = viewModel._modelAppsFather.grossistsDataBase.indexOfFirst { it.id == grossist.id }
                    if (index != -1) {
                        viewModel._modelAppsFather.grossistsDataBase[index] = grossist
                    } else {
                        viewModel._modelAppsFather.grossistsDataBase.add(grossist)
                    }

                    // Update Firebase
                    newRef.child(supplier.idSupplierSu.toString()).setValue(grossist)
                }

                // Update last known IDs
                lastKnownIds = currentIds.toMutableSet()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Sync", error.message)
            }
        })
    }

    // Add this function to sync categories from the old database to the new database
    private fun syncOldCategories(viewModel: ViewModelInitApp) {
        val oldCategoriesRef = I_CategoriesRepository.ancienBaseDonneRef
        val newCategoriesRef = I_CategoriesRepository.caReference

        oldCategoriesRef.addValueEventListener(object : ValueEventListener {
            private var lastKnownIds = mutableSetOf<Long>()

            override fun onDataChange(snapshot: DataSnapshot) {
                // Get current category IDs
                val currentIds = snapshot.children
                    .mapNotNull { it.getValue(CategoriesTabelleECB::class.java)?.idCategorieInCategoriesTabele }
                    .toSet()

                // Handle deletions
                lastKnownIds.filter { it !in currentIds }.forEach { deletedId ->
                    newCategoriesRef.child(deletedId.toString()).removeValue()
                    viewModel._modelAppsFather.i_CategoriesProduits.removeAll { it.id == deletedId }
                }

                // Update or add categories
                snapshot.children.forEach { snap ->
                    val oldCategory = snap.getValue(CategoriesTabelleECB::class.java) ?: return@forEach
                    val newCategory = I_CategoriesProduits(
                        id = oldCategory.idCategorieInCategoriesTabele,
                        infosDeBase = I_CategoriesProduits.InfosDeBase(
                            nom = oldCategory.nomCategorieInCategoriesTabele
                        ),
                        statuesMutable = I_CategoriesProduits.StatuesMutable(
                            indexDonsParentList = oldCategory.idClassementCategorieInCategoriesTabele.toLong()
                        )
                    )

                    // Update local list
                    val index = viewModel._modelAppsFather.i_CategoriesProduits.indexOfFirst { it.id == newCategory.id }
                    if (index != -1) {
                        viewModel._modelAppsFather.i_CategoriesProduits[index] = newCategory
                    } else {
                        viewModel._modelAppsFather.i_CategoriesProduits.add(newCategory)
                    }

                    // Update Firebase
                    newCategoriesRef.child(oldCategory.idCategorieInCategoriesTabele.toString()).setValue(newCategory)
                }

                // Update last known IDs
                lastKnownIds = currentIds.toMutableSet()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Sync", error.message)
            }
        })
    }

}
