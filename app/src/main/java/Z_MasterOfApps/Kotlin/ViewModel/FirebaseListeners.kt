package Z_MasterOfApps.Kotlin.ViewModel

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model.C_GrossistsDataBase
import Z_MasterOfApps.Kotlin.Model.D_CouleursEtGoutesProduitsInfos
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirebaseListeners {
    private var productsListener: ValueEventListener? = null
    private var clientsListener: ValueEventListener? = null
    private var grossistsListener: ValueEventListener? = null
    private val firebaseDatabase = Firebase.database
    private val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")
    private var jetPackExportListener: ValueEventListener? = null
    private val refColorsArticles = firebaseDatabase.getReference("H_ColorsArticles")
    private var colorsArticlesListener: ValueEventListener? = null
    private val lastKnownColorValues = mutableMapOf<Long, D_CouleursEtGoutesProduitsInfos>()

    fun setupRealtimeListeners(viewModel: ViewModelInitApp) {
        setupProductsListener(viewModel)
        setupClientsListener(viewModel)
        setupGrossistsListener(viewModel)
        setupJetPackExportListener()
        setupColorsArticlesListener(viewModel)
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
        val currentColors = product.statuesBase.coloursEtGoutsIds.toSet()
        val colorsId = listOfNotNull(
            productSnapshot.child("idcolor1").getValue(Long::class.java),
            productSnapshot.child("idcolor2").getValue(Long::class.java),
            productSnapshot.child("idcolor3").getValue(Long::class.java),
            productSnapshot.child("idcolor4").getValue(Long::class.java)
        )

        colorsId.forEach { colorId ->
            if (colorId > 0 && !currentColors.contains(colorId)) {
                product.statuesBase.coloursEtGoutsIds += colorId
            }
        }

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
