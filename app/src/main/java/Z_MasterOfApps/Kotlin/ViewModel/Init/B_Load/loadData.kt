package Z_MasterOfApps.Kotlin.ViewModel.Init.B_Load

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model.C_GrossistsDataBase
import Z_MasterOfApps.Kotlin.Model.D_CouleursEtGoutesProduitsInfos
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.Init.A_FirebaseListeners.AncienDataBase
import Z_MasterOfApps.Kotlin.ViewModel.Init.C_Compare.CompareUpdate
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private var isInitialized = false

fun initializeFirebase(app: FirebaseApp) {
    if (!isInitialized) {
        try {
            FirebaseDatabase.getInstance(app).apply {
                setPersistenceEnabled(true)
                setPersistenceCacheSizeBytes(100L * 1024L * 1024L)
            }
            isInitialized = true
        } catch (_: Exception) {}
    }
}

suspend fun loadData(viewModel: ViewModelInitApp) {
    try {
        viewModel.loadingProgress = 0.1f

        val refs = listOf(
            _ModelAppsFather.ref_HeadOfModels,
            _ModelAppsFather.produitsFireBaseRef,
            B_ClientsDataBase.refClientsDataBase
        ).onEach { it.keepSynced(true) }

        val isOnline = withTimeoutOrNull(3000L) {
            refs[1].child("test").setValue(true).await()
            refs[1].child("test").removeValue().await()
            true
        } ?: false

        val snapshots = if (isOnline) {
            AncienDataBase.setupRealtimeListeners(viewModel)
            CompareUpdate.setupeCompareUpdateAncienModels()
            refs.map { it.get().await() }
        } else {
            FirebaseDatabase.getInstance().goOffline()
            refs.map { ref ->
                withTimeoutOrNull(5000L) {
                    ref.get().await()
                }
            }.also { FirebaseDatabase.getInstance().goOnline() }
        }

        val (headModels, products, clients) = snapshots

        withContext(Dispatchers.Main) {
            viewModel.modelAppsFather.apply {
                produitsMainDataBase.clear()
                products?.children?.forEach { snap ->
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
                        val coloursEtGoutsList = mutableListOf<A_ProduitModel.ColourEtGout_Model>()
                        snap.child("coloursEtGoutsList").children.forEach { colorSnap ->
                            colorSnap.getValue(A_ProduitModel.ColourEtGout_Model::class.java)?.let {
                                coloursEtGoutsList.add(it)
                            }
                        }
                        this.coloursEtGoutsList = coloursEtGoutsList

                        // Load current BonCommend with MutableBasesStates
                        snap.child("bonCommendDeCetteCota").getValue(A_ProduitModel.GrossistBonCommandes::class.java)?.let { bonCommend ->
                            // Load MutableBasesStates
                            snap.child("bonCommendDeCetteCota/mutableBasesStates")
                                .getValue(A_ProduitModel.GrossistBonCommandes.MutableBasesStates::class.java)?.let {
                                    bonCommend.mutableBasesStates = it
                                }
                            bonCommendDeCetteCota = bonCommend
                        }

                        // Load BonsVentDeCetteCota with proper initialization
                        val bonsVent = mutableListOf<A_ProduitModel.ClientBonVentModel>()
                        snap.child("bonsVentDeCetteCotaList").children.forEach { bonVentSnap ->
                            bonVentSnap.getValue(A_ProduitModel.ClientBonVentModel::class.java)?.let {
                                bonsVent.add(it)
                            }
                        }
                        bonsVentDeCetteCotaList = bonsVent

                        // Load HistoriqueBonsVents
                        val historique = mutableListOf<A_ProduitModel.ClientBonVentModel>()
                        snap.child("historiqueBonsVentsList").children.forEach { historySnap ->
                            historySnap.getValue(A_ProduitModel.ClientBonVentModel::class.java)?.let {
                                historique.add(it)
                            }
                        }
                        historiqueBonsVentsList = historique
                    }
                    produitsMainDataBase.add(prod)
                }

                clientDataBase.clear()
                clients?.children?.forEach { snap ->
                    val map = snap.value as? Map<*, *> ?: return@forEach
                    B_ClientsDataBase(
                        id = snap.key?.toLongOrNull() ?: return@forEach,
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
                        clientDataBase.add(this)
                    }
                }

                grossistsDataBase.clear()
                if (headModels != null) {
                    val grossistsNode = headModels.child("C_GrossistsDataBase")
                    if (!grossistsNode.exists()) {
                        grossistsDataBase.add(
                            C_GrossistsDataBase(
                            id = 1,
                            nom = "Default Grossist",
                            statueDeBase = C_GrossistsDataBase.StatueDeBase(
                                cUnClientTemporaire = true
                            )
                        )
                        )
                    } else {
                        grossistsNode.children.forEach { snap ->
                            try {
                                val map = snap.value as? Map<*, *> ?: return@forEach
                                C_GrossistsDataBase(
                                    id = snap.key?.toLongOrNull() ?: return@forEach,
                                    nom = map["nom"] as? String ?: "Non Defini"
                                ).apply {
                                    snap.child("statueDeBase")
                                        .getValue(C_GrossistsDataBase.StatueDeBase::class.java)?.let {
                                            statueDeBase = it
                                        }
                                    grossistsDataBase.add(this)
                                }
                            } catch (e: Exception) {
                                // Silent catch to skip invalid entries
                            }
                        }
                    }
                }
                
                couleursProduitsInfos.clear()
                if (headModels != null) {
                    val node = headModels.child("D_CouleursEtGoutesProduitsInfos")
                    if (!node.exists()) {
                        couleursProduitsInfos.add(
                            D_CouleursEtGoutesProduitsInfos(
                                id = 1,
                            )
                        )
                    } else {
                        node.children.forEach { snap ->
                            try {
                                D_CouleursEtGoutesProduitsInfos(
                                    id = snap.key?.toLongOrNull() ?: return@forEach,
                                ).apply {
                                    snap.child("infosDeBase")
                                        .getValue(D_CouleursEtGoutesProduitsInfos.InfosDeBase::class.java)?.let { infosDeBase ->
                                            this.infosDeBase = infosDeBase
                                        }
                                    snap.child("statuesMutable")
                                        .getValue(D_CouleursEtGoutesProduitsInfos.StatuesMutable::class.java)?.let { statuesMutable ->
                                            this.statuesMutable = statuesMutable
                                        }
                                    couleursProduitsInfos.add(this)
                                }
                            } catch (_: Exception){
                            }
                        }
                    }
                }
            }
            viewModel.loadingProgress = 1.0f
        }
    } catch (e: Exception) {
        viewModel.loadingProgress = -1f
        throw e
    }
}
