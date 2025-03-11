package Z_MasterOfApps.Kotlin.ViewModel.Init.B_Load

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_CodePartageEntreApps.Model.B_ClientsDataBase
import Z_CodePartageEntreApps.Model.C_GrossistsDataBase
import Z_CodePartageEntreApps.Model.D_CouleursEtGoutesProduitsInfos
import Z_CodePartageEntreApps.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.Init.C_Compare.CompareUpdate
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

private var isInitialized = false
private const val TAG = "ConnectivityMonitor"
private const val CHECK_INTERVAL = 3000L
private const val CHECK_TIMEOUT = 10000L // Increase to 10 seconds
 private const val CACHE_SIZE_BYTES = 100L * 1024L * 1024L // 100MB
private const val OFFLINE_TIMEOUT = 5000L

class ConnectivityMonitor(private val scope: CoroutineScope) {
    private var isOnline = false
    private var lastCheckTime = 0L
    private var connectivityCheckJob: Job? = null
    private var lastNotifiedState: Boolean? = null
    private var onConnectivityChanged: ((Boolean) -> Unit)? = null

    init {
        Log.d(TAG, "ConnectivityMonitor initialized")
    }

    suspend fun checkConnectivity(): Boolean {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastCheckTime < CHECK_INTERVAL && lastNotifiedState != null) {
            Log.d(TAG, "Using cached connectivity state: $isOnline")
            return isOnline
        }

        return try {
            Log.d(TAG, "Performing new connectivity check")

            val result = withTimeoutOrNull(CHECK_TIMEOUT) {
                try {
                    // Using Google's DNS server to check connectivity
                    withContext(Dispatchers.IO) {
                        val socket = java.net.Socket()
                        val socketAddress = java.net.InetSocketAddress("8.8.8.8", 53)

                        try {
                            socket.connect(socketAddress, 3000) // 3 seconds timeout
                            socket.close()
                            true
                        } catch (e: Exception) {
                            Log.e(TAG, "Socket connection failed", e)
                            false
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Google connectivity check failed", e)
                    false
                }
            } ?: false

            isOnline = result
            lastCheckTime = currentTime

            if (lastNotifiedState != result) {
                Log.d(TAG, "Connectivity state changed: $result")
                lastNotifiedState = result
                onConnectivityChanged?.invoke(result)
            }

            Log.d(TAG, "Connectivity check complete - Online: $result")
            result

        } catch (e: Exception) {
            Log.e(TAG, "Connectivity check failed with exception", e)
            false
        }
    }

    // Rest of the class remains unchanged
    fun startMonitoring(onChange: (Boolean) -> Unit) {
        Log.d(TAG, "Starting connectivity monitoring")

        onConnectivityChanged = onChange
        connectivityCheckJob?.cancel()

        connectivityCheckJob = scope.launch {
            try {
                val initialState = checkConnectivity()
                Log.d(TAG, "Initial connectivity state: $initialState")
                onChange(initialState)

                while (isActive) {
                    delay(CHECK_INTERVAL)
                    checkConnectivity()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Monitoring loop failed", e)
            }
        }.also {
            it.invokeOnCompletion { throwable ->
                if (throwable != null) {
                    Log.e(TAG, "Monitoring job completed with error", throwable)
                } else {
                    Log.d(TAG, "Monitoring job completed normally")
                }
            }
        }
    }

    fun stopMonitoring() {
        Log.d(TAG, "Stopping connectivity monitoring")
        connectivityCheckJob?.cancel()
        connectivityCheckJob = null
        onConnectivityChanged = null
        lastNotifiedState = null
    }
}
fun initializeFirebase(app: FirebaseApp) {
    if (!isInitialized) {
        try {
            Log.d(TAG, "Initializing Firebase")
            FirebaseDatabase.getInstance(app).apply {
                setPersistenceEnabled(true)
                setPersistenceCacheSizeBytes(CACHE_SIZE_BYTES)
            }
            isInitialized = true
            Log.d(TAG, "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
        }
    }
}

suspend fun loadData(viewModel: ViewModelInitApp) {
    try {
        Log.d(TAG, "Starting data loading process")
        viewModel.loadingProgress = 0.1f

        val connectivityMonitor = ConnectivityMonitor(viewModel.viewModelScope)

        val refs = setupDatabaseRefs()
        val isOnline = connectivityMonitor.checkConnectivity()

        setupConnectivityMonitoring(connectivityMonitor, refs, viewModel)

        if (isOnline) {
            CompareUpdate.setupeCompareUpdateAncienModels()
        }

        loadDataFromRefs(refs, isOnline, viewModel)

        viewModel.loadingProgress = 1.0f
        Log.d(TAG, "Data loading completed successfully")

    } catch (e: Exception) {
        Log.e(TAG, "Data loading failed", e)
        viewModel.loadingProgress = -1f
        throw e
    }
}

private fun setupDatabaseRefs(): List<DatabaseReference> {
    return listOf(
        _ModelAppsFather.ref_HeadOfModels,
        _ModelAppsFather.produitsFireBaseRef,
        B_ClientsDataBase.refClientsDataBase
    ).onEach {
        it.keepSynced(true)
        Log.d(TAG, "Database reference synced: ${it.key}")
    }
}

private fun setupConnectivityMonitoring(
    connectivityMonitor: ConnectivityMonitor,
    refs: List<DatabaseReference>,
    viewModel: ViewModelInitApp
) {
    connectivityMonitor.startMonitoring { newState ->
        viewModel.viewModelScope.launch {
            if (newState) {
                Log.d(TAG, "Device went online")
                FirebaseDatabase.getInstance().goOnline()
                loadDataFromRefs(refs, true, viewModel)
            } else {
                Log.d(TAG, "Device went offline")
                FirebaseDatabase.getInstance().goOffline()
            }
        }
    }
}



private suspend fun loadDataFromRefs(
    refs: List<DatabaseReference>,
    isOnline: Boolean,
    viewModel: ViewModelInitApp
) {
    try {
        Log.d(TAG, "Loading data from refs - Online mode: $isOnline")

        val snapshots = fetchSnapshots(refs, isOnline)
        val (headModels, products, clients) = snapshots

        withContext(Dispatchers.Main) {
            viewModel.modelAppsFather.apply {
                updateProducts(products)
                updateClients(clients)
                updateGrossists(headModels)
                updateCouleurs(headModels)
            }
        }

        Log.d(TAG, "Data loading from refs completed")
    } catch (e: Exception) {
        Log.e(TAG, "Failed to load data from refs", e)
        withContext(Dispatchers.Main) {
            viewModel.loadingProgress = -1f
        }
        throw e
    }
}

private suspend fun fetchSnapshots(
    refs: List<DatabaseReference>,
    isOnline: Boolean
): List<DataSnapshot?> {
    return if (isOnline) {
        refs.map { ref ->
            try {
                ref.get().await()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch online snapshot for ${ref.key}", e)
                null
            }
        }
    } else {
        refs.map { ref ->
            try {
                withTimeoutOrNull(OFFLINE_TIMEOUT) {
                    ref.get().await()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch offline snapshot for ${ref.key}", e)
                null
            }
        }
    }
}

private suspend fun _ModelAppsFather.updateProducts(snapshot: DataSnapshot?) {
    produitsMainDataBase.clear()
    snapshot?.children?.forEach { snap ->
        try {
            createProduct(snap)?.let { produitsMainDataBase.add(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update product: ${snap.key}", e)
        }
    }
}

private fun createProduct(snap: DataSnapshot): A_ProduitModel? {
    val map = snap.value as? Map<*, *> ?: return null
    return A_ProduitModel(
        id = snap.key?.toLongOrNull() ?: return null,
        itsTempProduit = map["itsTempProduit"] as? Boolean ?: false,
        init_nom = map["nom"] as? String ?: "",
        init_besoin_To_Be_Updated = map["besoin_To_Be_Updated"] as? Boolean ?: false,
        initialNon_Trouve = map["non_Trouve"] as? Boolean ?: false,
        init_visible = map["isVisible"] as? Boolean ?: false
    ).apply {
        loadProductDetails(snap)
    }
}

private fun A_ProduitModel.loadProductDetails(snap: DataSnapshot) {
    snap.child("statuesBase").getValue(A_ProduitModel.StatuesBase::class.java)?.let {
        statuesBase = it
        statuesBase.imageGlidReloadTigger = 0
    }

    coloursEtGoutsList = snap.child("coloursEtGoutsList").children
        .mapNotNull { it.getValue(A_ProduitModel.ColourEtGout_Model::class.java) }
        .toMutableList()

    loadBonCommend(snap)
    loadBonsVent(snap)
    loadHistorique(snap)
}

private fun A_ProduitModel.loadBonCommend(snap: DataSnapshot) {
    snap.child("bonCommendDeCetteCota").getValue(A_ProduitModel.GrossistBonCommandes::class.java)?.let { bonCommend ->
        snap.child("bonCommendDeCetteCota/mutableBasesStates")
            .getValue(A_ProduitModel.GrossistBonCommandes.MutableBasesStates::class.java)?.let {
                bonCommend.mutableBasesStates = it
            }
        bonCommendDeCetteCota = bonCommend
    }
}

private fun A_ProduitModel.loadBonsVent(snap: DataSnapshot) {
    bonsVentDeCetteCotaList = snap.child("bonsVentDeCetteCotaList").children
        .mapNotNull { it.getValue(A_ProduitModel.ClientBonVentModel::class.java) }
        .toMutableList()
}

private fun A_ProduitModel.loadHistorique(snap: DataSnapshot) {
    historiqueBonsVentsList = snap.child("historiqueBonsVentsList").children
        .mapNotNull { it.getValue(A_ProduitModel.ClientBonVentModel::class.java) }
        .toMutableList()
}

private suspend fun _ModelAppsFather.updateClients(snapshot: DataSnapshot?) {
    clientDataBase.clear()
    snapshot?.children?.forEach { snap ->
        try {
            createClient(snap)?.let { clientDataBase.add(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update client: ${snap.key}", e)
        }
    }
}

private fun createClient(snap: DataSnapshot): B_ClientsDataBase? {
    val map = snap.value as? Map<*, *> ?: return null
    return B_ClientsDataBase(
        id = snap.key?.toLongOrNull() ?: return null,
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
    }
}

private suspend fun _ModelAppsFather.updateGrossists(snapshot: DataSnapshot?) {
    grossistsDataBase.clear()
    val grossistsNode = snapshot?.child("C_GrossistsDataBase")

    if (grossistsNode == null || !grossistsNode.exists()) {
        grossistsDataBase.add(createDefaultGrossist())
        return
    }

    grossistsNode.children.forEach { snap ->
        try {
            createGrossist(snap)?.let { grossistsDataBase.add(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update grossist: ${snap.key}", e)
        }
    }
}

private fun createDefaultGrossist() = C_GrossistsDataBase(
    id = 1,
    nom = "Default Grossist",
    statueDeBase = C_GrossistsDataBase.StatueDeBase(
        cUnClientTemporaire = true
    )
)

private fun createGrossist(snap: DataSnapshot): C_GrossistsDataBase? {
    val map = snap.value as? Map<*, *> ?: return null
    return C_GrossistsDataBase(
        id = snap.key?.toLongOrNull() ?: return null,
        nom = map["nom"] as? String ?: "Non Defini"
    ).apply {
        snap.child("statueDeBase")
            .getValue(C_GrossistsDataBase.StatueDeBase::class.java)?.let {
                statueDeBase = it
            }
    }
}

private suspend fun _ModelAppsFather.updateCouleurs(snapshot: DataSnapshot?) {
    couleursProduitsInfos.clear()
    val couleursNode = snapshot?.child("D_CouleursEtGoutesProduitsInfos")

    if (couleursNode == null || !couleursNode.exists()) {
        couleursProduitsInfos.add(D_CouleursEtGoutesProduitsInfos(id = 1))
        return
    }

    couleursNode.children.forEach { snap ->
        try {
            createCouleur(snap)?.let { couleursProduitsInfos.add(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update couleur: ${snap.key}", e)
        }
    }
}

private fun createCouleur(snap: DataSnapshot): D_CouleursEtGoutesProduitsInfos? {
    return D_CouleursEtGoutesProduitsInfos(
        id = snap.key?.toLongOrNull() ?: return null
    ).apply {
        snap.child("infosDeBase")
            .getValue(D_CouleursEtGoutesProduitsInfos.InfosDeBase::class.java)?.let {
                infosDeBase = it
            }
        snap.child("statuesMutable")
            .getValue(D_CouleursEtGoutesProduitsInfos.StatuesMutable::class.java)?.let {
                statuesMutable = it
            }
    }
}
