package Z_MasterOfApps.Kotlin.ViewModel.Startup.FirebaseListeners

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.D_CouleursEtGoutesProduitsInfos
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.Startup.FirebaseListeners.CurrentModels.setupCurrentModels
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object AncienDataBase {
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
                    )

                    val lastState = lastKnownValues[productId]
                    val pricesChanged = lastState?.prixAchat != newPrixAchat ||
                            lastState.prixVent != newPrixVent
                    val colorsChanged = lastState?.colors != newColors

                    if (pricesChanged || colorsChanged) {
                        _ModelAppsFather.produitsFireBaseRef.child(productId).get()
                            .addOnSuccessListener { productDbSnapshot ->
                                val product = productDbSnapshot.getValue(A_ProduitModel::class.java)
                                if (product != null) {
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
                                }
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
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


}
