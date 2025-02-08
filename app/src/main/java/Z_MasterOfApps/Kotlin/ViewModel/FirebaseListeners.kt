package Z_MasterOfApps.Kotlin.ViewModel

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model.C_GrossistsDataBase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirebaseListeners {
    private const val TAG = "FirebaseListeners"
    private var productsListener: ValueEventListener? = null
    private var clientsListener: ValueEventListener? = null
    private var grossistsListener: ValueEventListener? = null
    val firebaseDatabase = Firebase.database
    private val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")
    private var jetPackExportListener: ValueEventListener? = null
    private val refColorsArticles = firebaseDatabase.getReference("H_ColorsArticles")

    fun setupRealtimeListeners(viewModel: ViewModelInitApp) {
        Log.d(TAG, "Setting up real-time listeners...")
        setupProductsListener(viewModel)
        setupClientsListener(viewModel)
        setupGrossistsListener(viewModel)
        setupJetPackExportListener() // Add this line
    }

    private fun setupJetPackExportListener() {
        jetPackExportListener?.let { refDBJetPackExport.removeEventListener(it) }

        jetPackExportListener = object : ValueEventListener {
            private val lastKnownValues = mutableMapOf<String, Pair<Double, Double>>()

            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { productSnapshot ->
                    val productId = productSnapshot.key ?: return@forEach
                    val newPrixAchat = productSnapshot.child("monPrixAchat").getValue(Double::class.java) ?: 0.0
                    val newPrixVent = productSnapshot.child("monPrixVent").getValue(Double::class.java) ?: 0.0

                    // Check if values have actually changed before updating
                    val lastValues = lastKnownValues[productId]
                    if (lastValues?.first != newPrixAchat || lastValues.second != newPrixVent) {
                        // Find the corresponding product in the database
                        _ModelAppsFather.produitsFireBaseRef.child(productId).get()
                            .addOnSuccessListener { productDbSnapshot ->
                                val product = productDbSnapshot.getValue(A_ProduitModel::class.java)
                                if (product != null) {
                                    // Handle color updates
                                    val updatedProduct = handleColorUpdate(productSnapshot, product)

                                    // Update prices
                                    updatedProduct.statuesBase.infosCoutes.monPrixAchat = newPrixAchat
                                    updatedProduct.statuesBase.infosCoutes.monPrixVent = newPrixVent

                                    // Save updates to Firebase
                                    _ModelAppsFather.produitsFireBaseRef.child(productId)
                                        .setValue(updatedProduct)
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Updated product $productId: PrixAchat=$newPrixAchat, PrixVent=$newPrixVent")
                                            lastKnownValues[productId] = Pair(newPrixAchat, newPrixVent)
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e(TAG, "Error updating product $productId", e)
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error fetching product $productId", e)
                            }
                    } else {
                        Log.d(TAG, "No changes detected for product $productId, skipping update")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "JetPackExport listener cancelled: ${error.message}")
            }
        }

        refDBJetPackExport.addValueEventListener(jetPackExportListener!!)
    }
    fun handleColorUpdate(
        productSnapshot: DataSnapshot,
        product: A_ProduitModel
    ): A_ProduitModel {
        // Get all potential color IDs from the snapshot
        val colorNames = listOfNotNull(
            productSnapshot.child("couleur1").getValue(String::class.java),
            productSnapshot.child("couleur2").getValue(String::class.java),
            productSnapshot.child("couleur3").getValue(String::class.java),
            productSnapshot.child("couleur4").getValue(String::class.java),

        )

        // Process each color ID
        colorNames.forEach { color ->
            // Check if this color already exists in the product's colors
         //   val colorExists = product.statuesBase.coloursEtGoutsIds.any { it == color }

        }

        // Update the product's status to indicate it has colors with images if any colors exist
        product.statuesBase.ilAUneCouleurAvecImage = product.coloursEtGouts.isNotEmpty()

        return product
    }
    private fun setupProductsListener(viewModel: ViewModelInitApp) {
        productsListener?.let { _ModelAppsFather.produitsFireBaseRef.removeEventListener(it) }

        productsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val products = mutableListOf<A_ProduitModel>()
                        snapshot.children.forEach { snap ->
                            val map = snap.value as? Map<*, *> ?: return@forEach
                            val prod = A_ProduitModel(
                                id = snap.key?.toLongOrNull() ?: return@forEach,
                                itsTempProduit = map["itsTempProduit"] as? Boolean ?: false,
                                init_nom = map["nom"] as? String ?: "",
                                init_besoin_To_Be_Updated = map["besoin_To_Be_Updated"] as? Boolean ?: false,
                                initialNon_Trouve = map["non_Trouve"] as? Boolean ?: false,
                                init_visible = map["isVisible"] as? Boolean ?: false
                            ).apply {
                                // Load StatuesBase
                                snap.child("statuesBase").getValue(A_ProduitModel.StatuesBase::class.java)?.let {
                                    statuesBase = it
                                    statuesBase.imageGlidReloadTigger = 0
                                }

                                // Load ColoursEtGouts
                                snap.child("coloursEtGoutsList").children.forEach { colorSnap ->
                                    colorSnap.getValue(A_ProduitModel.ColourEtGout_Model::class.java)?.let {
                                        coloursEtGouts.add(it)
                                    }
                                }

                                // Load current BonCommend
                                snap.child("bonCommendDeCetteCota").getValue(A_ProduitModel.GrossistBonCommandes::class.java)?.let {
                                    bonCommendDeCetteCota = it
                                }

                                // Load BonsVentDeCetteCota
                                snap.child("bonsVentDeCetteCotaList").children.forEach { bonVentSnap ->
                                    bonVentSnap.getValue(A_ProduitModel.ClientBonVentModel::class.java)?.let {
                                        bonsVentDeCetteCota.add(it)
                                    }
                                }

                                // Load HistoriqueBonsVents
                                snap.child("historiqueBonsVentsList").children.forEach { historySnap ->
                                    historySnap.getValue(A_ProduitModel.ClientBonVentModel::class.java)?.let {
                                        historiqueBonsVents.add(it)
                                    }
                                }

                                // Load HistoriqueBonsCommend
                                snap.child("historiqueBonsCommendList").children.forEach { historySnap ->
                                    historySnap.getValue(A_ProduitModel.GrossistBonCommandes::class.java)?.let {
                                        historiqueBonsCommend.add(it)
                                    }
                                }
                            }
                            products.add(prod)
                        }

                        viewModel.modelAppsFather.produitsMainDataBase.apply {
                            clear()
                            addAll(products)
                        }
                        Log.d(TAG, "Products updated: ${products.size} items")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating products", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Products listener cancelled: ${error.message}")
            }
        }

        _ModelAppsFather.produitsFireBaseRef.addValueEventListener(productsListener!!)
    }

    private fun setupClientsListener(viewModel: ViewModelInitApp) {
        clientsListener?.let { B_ClientsDataBase.refClientsDataBase.removeEventListener(it) }

        clientsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val clients = mutableListOf<B_ClientsDataBase>()
                        snapshot.children.forEach { snap ->
                            val map = snap.value as? Map<*, *> ?: return@forEach
                            B_ClientsDataBase(
                                id = snap.key?.toLongOrNull() ?: return@forEach,
                                nom = map["nom"] as? String ?: ""
                            ).apply {
                                snap.child("statueDeBase").getValue(B_ClientsDataBase.StatueDeBase::class.java)?.let {
                                    statueDeBase = it
                                }
                                snap.child("gpsLocation").getValue(B_ClientsDataBase.GpsLocation::class.java)?.let {
                                    gpsLocation = it
                                }
                                clients.add(this)
                            }
                        }

                        viewModel.modelAppsFather.clientDataBase.apply {
                            clear()
                            addAll(clients)
                        }
                        Log.d(TAG, "Clients updated: ${clients.size} items")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating clients", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Clients listener cancelled: ${error.message}")
            }
        }

        B_ClientsDataBase.refClientsDataBase.addValueEventListener(clientsListener!!)
    }

    private fun setupGrossistsListener(viewModel: ViewModelInitApp) {
        grossistsListener?.let { _ModelAppsFather.ref_HeadOfModels.removeEventListener(it) }

        grossistsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val grossists = mutableListOf<C_GrossistsDataBase>()
                        snapshot.child("C_GrossistsDataBase").children.forEach { grossistSnapshot ->
                            try {
                                val grossistMap = grossistSnapshot.value as? Map<*, *> ?: return@forEach
                                C_GrossistsDataBase(
                                    id = grossistSnapshot.key?.toLongOrNull() ?: return@forEach,
                                    nom = grossistMap["nom"] as? String ?: "Non Defini"
                                ).apply {
                                    grossistSnapshot.child("statueDeBase")
                                        .getValue(C_GrossistsDataBase.StatueDeBase::class.java)?.let {
                                            statueDeBase = it
                                        }
                                    grossists.add(this)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing grossist ${grossistSnapshot.key}", e)
                            }
                        }

                        viewModel.modelAppsFather.grossistsDataBase.apply {
                            clear()
                            addAll(grossists)
                        }
                        Log.d(TAG, "Grossists updated: ${grossists.size} items")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating grossists", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Grossists listener cancelled: ${error.message}")
            }
        }

        _ModelAppsFather.ref_HeadOfModels.addValueEventListener(grossistsListener!!)
    }
}
