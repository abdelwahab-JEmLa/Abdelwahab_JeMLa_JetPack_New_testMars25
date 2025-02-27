package Z_MasterOfApps.Kotlin.ViewModel.Init.A_FirebaseListeners

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model.C_GrossistsDataBase
import Z_MasterOfApps.Kotlin.Model.D_CouleursEtGoutesProduitsInfos
import Z_MasterOfApps.Kotlin.Model.E_AppsOptionsStates
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object CurrentModels {
    private const val TAG = "CurrentModels"
    private val listeners = mutableListOf<ListenerInfo>()

    private data class ListenerInfo(
        val listener: ValueEventListener,
        val path: String
    )

    fun setupCurrentModels(viewModel: ViewModelInitApp) {
        Log.d(TAG, "Setting up current models")
        cleanup()
        setupProductsListener(viewModel)
        setupClientsListener(viewModel)
        setupGrossistsListener(viewModel)
        setupCouleursListener(viewModel)
        setupApplicationEstInstalleListener(viewModel)
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up listeners")
        listeners.forEach { info ->
            when (info.path) {
                "products" -> _ModelAppsFather.produitsFireBaseRef.removeEventListener(info.listener)
                "clients" -> B_ClientsDataBase.refClientsDataBase.removeEventListener(info.listener)
                "grossists" -> _ModelAppsFather.ref_HeadOfModels.removeEventListener(info.listener)
                "couleurs" -> D_CouleursEtGoutesProduitsInfos.caReference.removeEventListener(info.listener)
                "appInstalle" -> E_AppsOptionsStates.caReference.removeEventListener(info.listener)
            }
        }
        listeners.clear()
        Log.d(TAG, "All listeners cleaned up")
    }

    private fun setupProductsListener(viewModel: ViewModelInitApp) {
        Log.d(TAG, "Setting up products listener")

        _ModelAppsFather.produitsFireBaseRef.keepSynced(true)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "Products data change triggered. Children count: ${snapshot.childrenCount}")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val products = mutableListOf<A_ProduitModel>()

                        snapshot.children.forEach { snap ->
                            try {
                                val map = snap.value as? Map<*, *>
                                if (map == null) {
                                    Log.w(TAG, "Invalid product data format: ${snap.key}")
                                    return@forEach
                                }

                                val productId = snap.key?.toLongOrNull()
                                if (productId == null) {
                                    Log.w(TAG, "Invalid product ID: ${snap.key}")
                                    return@forEach
                                }

                                val prod = A_ProduitModel(
                                    id = productId,
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
                                    coloursEtGoutsList = snap.child("coloursEtGoutsList").children
                                        .mapNotNull { it.getValue(A_ProduitModel.ColourEtGout_Model::class.java) }

                                    // Load BonCommend
                                    snap.child("bonCommendDeCetteCota").getValue(A_ProduitModel.GrossistBonCommandes::class.java)?.let { bonCommend ->
                                        snap.child("bonCommendDeCetteCota/mutableBasesStates")
                                            .getValue(A_ProduitModel.GrossistBonCommandes.MutableBasesStates::class.java)?.let {
                                                bonCommend.mutableBasesStates = it
                                            }
                                        bonCommendDeCetteCota = bonCommend
                                    }

                                    // Load BonsVent
                                    bonsVentDeCetteCotaList = snap.child("bonsVentDeCetteCotaList").children
                                        .mapNotNull { it.getValue(A_ProduitModel.ClientBonVentModel::class.java) }

                                    // Load Historique
                                    historiqueBonsVentsList = snap.child("historiqueBonsVentsList").children
                                        .mapNotNull { it.getValue(A_ProduitModel.ClientBonVentModel::class.java) }
                                }

                                products.add(prod)
                                Log.d(TAG, "Processed product: ${prod.id}, name: ${prod.nom}")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing product ${snap.key}", e)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            viewModel.modelAppsFather.produitsMainDataBase.apply {
                                clear()
                                addAll(products)
                            }
                            Log.d(TAG, "Updated UI with ${products.size} products")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in products listener", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Products listener cancelled", error.toException())
            }
        }

        listeners.add(ListenerInfo(listener, "products"))
        _ModelAppsFather.produitsFireBaseRef.addValueEventListener(listener)
    }

    private fun setupClientsListener(viewModel: ViewModelInitApp) {
        Log.d(TAG, "Setting up clients listener")

        B_ClientsDataBase.refClientsDataBase.keepSynced(true)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val clients = mutableListOf<B_ClientsDataBase>()

                        snapshot.children.forEach { snap ->
                            try {
                                val map = snap.value as? Map<*, *> ?: return@forEach
                                val clientId = snap.key?.toLongOrNull() ?: return@forEach

                                B_ClientsDataBase(
                                    id = clientId,
                                    nom = map["nom"] as? String ?: ""
                                ).apply {
                                    snap.child("statueDeBase")
                                        .getValue(B_ClientsDataBase.StatueDeBase::class.java)?.let {
                                            statueDeBase = it
                                        }
                                    snap.child("gpsLocation")
                                        .getValue(B_ClientsDataBase.GpsLocation::class.java)?.let {
                                            gpsLocation = it
                                        }
                                    clients.add(this)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing client ${snap.key}", e)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            viewModel.modelAppsFather.clientDataBase.apply {
                                clear()
                                addAll(clients)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in clients listener", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Clients listener cancelled", error.toException())
            }
        }

        listeners.add(ListenerInfo(listener, "clients"))
        B_ClientsDataBase.refClientsDataBase.addValueEventListener(listener)
    }

    private fun setupGrossistsListener(viewModel: ViewModelInitApp) {
        Log.d(TAG, "Setting up grossists listener")

        _ModelAppsFather.ref_HeadOfModels.keepSynced(true)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val grossists = mutableListOf<C_GrossistsDataBase>()

                        val grossistsNode = snapshot.child("C_GrossistsDataBase")
                        if (!grossistsNode.exists()) {
                            grossists.add(C_GrossistsDataBase(
                                id = 1,
                                nom = "Default Grossist",
                                statueDeBase = C_GrossistsDataBase.StatueDeBase(
                                    cUnClientTemporaire = true
                                )
                            ))
                        } else {
                            grossistsNode.children.forEach { grossistSnapshot ->
                                try {
                                    val grossistMap = grossistSnapshot.value as? Map<*, *> ?: return@forEach
                                    val grossistId = grossistSnapshot.key?.toLongOrNull() ?: return@forEach

                                    C_GrossistsDataBase(
                                        id = grossistId,
                                        nom = grossistMap["nom"] as? String ?: "Non Defini"
                                    ).apply {
                                        grossistSnapshot.child("statueDeBase")
                                            .getValue(C_GrossistsDataBase.StatueDeBase::class.java)?.let {
                                                statueDeBase = it
                                            }
                                        grossists.add(this)
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error processing grossist ${grossistSnapshot.key}", e)
                                }
                            }
                        }

                        withContext(Dispatchers.Main) {
                            viewModel.modelAppsFather.grossistsDataBase.apply {
                                clear()
                                addAll(grossists)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in grossists listener", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Grossists listener cancelled", error.toException())
            }
        }

        listeners.add(ListenerInfo(listener, "grossists"))
        _ModelAppsFather.ref_HeadOfModels.addValueEventListener(listener)
    }

    private fun setupCouleursListener(viewModel: ViewModelInitApp) {
        Log.d(TAG, "Setting up couleurs listener")

        D_CouleursEtGoutesProduitsInfos.caReference.keepSynced(true)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val couleurs = mutableListOf<D_CouleursEtGoutesProduitsInfos>()

                        snapshot.children.forEach { snap ->
                            try {
                                val couleurId = snap.key?.toLongOrNull() ?: return@forEach

                                D_CouleursEtGoutesProduitsInfos(
                                    id = couleurId
                                ).apply {
                                    snap.child("infosDeBase")
                                        .getValue(D_CouleursEtGoutesProduitsInfos.InfosDeBase::class.java)?.let {
                                            infosDeBase = it
                                        }
                                    snap.child("statuesMutable")
                                        .getValue(D_CouleursEtGoutesProduitsInfos.StatuesMutable::class.java)?.let {
                                            statuesMutable = it
                                        }
                                    couleurs.add(this)
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing couleur ${snap.key}", e)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            viewModel.modelAppsFather.couleursProduitsInfos.apply {
                                clear()
                                addAll(couleurs)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in couleurs listener", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Couleurs listener cancelled", error.toException())
            }
        }

        listeners.add(ListenerInfo(listener, "couleurs"))
        D_CouleursEtGoutesProduitsInfos.caReference.addValueEventListener(listener)
    }
    private fun setupApplicationEstInstalleListener(viewModel: ViewModelInitApp) {
        Log.d(TAG, "Setting up application installation status listener")

        E_AppsOptionsStates.caReference.keepSynced(true)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val appInstallations = mutableListOf<E_AppsOptionsStates.ApplicationEstInstalleDonTelephone>()

                        snapshot.children.forEach { snap ->
                            try {
                                val appInstall = E_AppsOptionsStates.ApplicationEstInstalleDonTelephone().apply {
                                    id = snap.key?.toIntOrNull() ?: 0
                                    nom = snap.child("nom").getValue(String::class.java) ?: ""
                                    widthScreen = snap.child("widthScreen").getValue(Int::class.java)
                                        ?: E_AppsOptionsStates.ApplicationEstInstalleDonTelephone.metricsWidthPixels
                                }
                                appInstallations.add(appInstall)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing application installation ${snap.key}", e)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            viewModel.modelAppsFather.applicationEstInstalleDonTelephone.apply {
                                clear()
                                addAll(appInstallations)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error in application installation listener", e)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Application installation listener cancelled", error.toException())
            }
        }

        listeners.add(ListenerInfo(listener, "appInstalle"))
        E_AppsOptionsStates.caReference.addValueEventListener(listener)
    }
}
