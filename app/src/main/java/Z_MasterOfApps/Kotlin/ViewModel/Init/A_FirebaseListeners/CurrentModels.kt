package Z_MasterOfApps.Kotlin.ViewModel.Init.A_FirebaseListeners

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model.C_GrossistsDataBase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object CurrentModels {
    private var productsListener: ValueEventListener? = null
    private var clientsListener: ValueEventListener? = null
    private var grossistsListener: ValueEventListener? = null

    fun setupCurrentModels(viewModel: ViewModelInitApp) {
        setupProductsListener(viewModel)
        setupClientsListener(viewModel)
        setupGrossistsListener(viewModel)

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
                                snap.child("statuesBase").getValue(A_ProduitModel.StatuesBase::class.java)?.let {
                                    statuesBase = it
                                    statuesBase.imageGlidReloadTigger = 0
                                }

                                snap.child("coloursEtGoutsList").children.forEach { colorSnap ->
                                    colorSnap.getValue(A_ProduitModel.ColourEtGout_Model::class.java)?.let {
                                        coloursEtGouts.add(it)
                                    }
                                }

                                snap.child("bonCommendDeCetteCota").getValue(A_ProduitModel.GrossistBonCommandes::class.java)?.let {
                                    bonCommendDeCetteCota = it
                                }

                                snap.child("bonsVentDeCetteCotaList").children.forEach { bonVentSnap ->
                                    bonVentSnap.getValue(A_ProduitModel.ClientBonVentModel::class.java)?.let {
                                        bonsVentDeCetteCota.add(it)
                                    }
                                }

                                snap.child("historiqueBonsVentsList").children.forEach { historySnap ->
                                    historySnap.getValue(A_ProduitModel.ClientBonVentModel::class.java)?.let {
                                        historiqueBonsVents.add(it)
                                    }
                                }

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
                    } catch (e: Exception) {
                        // Handle error if needed
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
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
                    } catch (e: Exception) {
                        // Handle error if needed
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
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
                            }
                        }

                        viewModel.modelAppsFather.grossistsDataBase.apply {
                            clear()
                            addAll(grossists)
                        }
                    } catch (e: Exception) {
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        _ModelAppsFather.ref_HeadOfModels.addValueEventListener(grossistsListener!!)
    }
}
