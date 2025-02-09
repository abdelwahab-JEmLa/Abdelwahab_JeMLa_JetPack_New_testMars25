package Z_MasterOfApps.Kotlin.ViewModel.Init.B_Load

import Z_MasterOfApps.Kotlin.Model.*
import Z_MasterOfApps.Kotlin.ViewModel.Init.A_FirebaseListeners.FromAncienDataBase
import Z_MasterOfApps.Kotlin.ViewModel.Init.C_Compare.CompareUpdate
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import com.google.android.gms.common.GoogleApiAvailability
import android.content.Context

private var isInitialized = false
private var connectivityCheckJob: Job? = null

fun initializeFirebase(app: FirebaseApp, context: Context) {
    if (!isInitialized) {
        try {
            // Check Google Play Services availability
            val availability = GoogleApiAvailability.getInstance()
            val resultCode = availability.isGooglePlayServicesAvailable(context)
            if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
                availability.makeGooglePlayServicesAvailable(context)          //->
                //TODO(FIXME):Fix erreur Type mismatch.
                //Required:
                //Activity
                //Found:
                //Context
                return
            }

            FirebaseDatabase.getInstance(app).apply {
                setPersistenceEnabled(true)
                setPersistenceCacheSizeBytes(100L * 1024L * 1024L)
            }
            isInitialized = true
        } catch (e: Exception) {
            // Log the error but don't crash
            e.printStackTrace()
        }
    }
}

private suspend fun checkConnectivity(context: Context): Boolean = withTimeoutOrNull(3000L) {
    try {
        // Verify Google Play Services first
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(context)
        if (resultCode != com.google.android.gms.common.ConnectionResult.SUCCESS) {
            return@withTimeoutOrNull false
        }

        val testRef = _ModelAppsFather.produitsFireBaseRef.child("connectivity_test")
        testRef.setValue(System.currentTimeMillis()).await()
        testRef.removeValue().await()
        true
    } catch (e: Exception) {
        false
    }
} ?: false

suspend fun loadData(viewModel: ViewModelInitApp, context: Context) {
    var errorOccurred = false

    try {
        viewModel.loadingProgress = 0.1f

        val refs = listOf(
            _ModelAppsFather.ref_HeadOfModels,
            _ModelAppsFather.produitsFireBaseRef,
            B_ClientsDataBase.refClientsDataBase
        )

        // Initialize data structures before loading
        viewModel.modelAppsFather.apply {
            produitsMainDataBase.clear()
            clientDataBase.clear()
            grossistsDataBase.clear()
            couleursProduitsInfos.clear()
        }

        // Safe reference keeping
        withContext(Dispatchers.IO) {
            refs.forEach { ref ->
                try {
                    ref.keepSynced(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val isOnline = checkConnectivity(context)

        val snapshots = if (isOnline) {
            try {
                // Setup listeners with error handling
                withContext(Dispatchers.IO) {
                    FromAncienDataBase.setupRealtimeListeners(viewModel)
                    CompareUpdate.setupeCompareUpdateAncienModels()
                }

                refs.map { ref ->
                    try {
                        withTimeout(5000L) {
                            ref.get().await()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            try {
                FirebaseDatabase.getInstance().goOffline()
                refs.map { ref ->
                    try {
                        withTimeout(5000L) {
                            ref.get().await()
                        }
                    } catch (e: Exception) {
                        null
                    }
                }.also { FirebaseDatabase.getInstance().goOnline() }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        if (snapshots == null) {
            viewModel.loadingProgress = -1f
            return
        }

        val (headModels, products, clients) = snapshots

        withContext(Dispatchers.Main) {
            viewModel.modelAppsFather.apply {
                // Load products safely
                products?.children?.forEach { snap ->
                    try {
                        val map = snap.value as? Map<*, *> ?: return@forEach
                        val prod = A_ProduitModel(
                            id = snap.key?.toLongOrNull() ?: return@forEach,
                            itsTempProduit = map["itsTempProduit"] as? Boolean ?: false,
                            init_nom = map["nom"] as? String ?: "",
                            init_besoin_To_Be_Updated = map["besoin_To_Be_Updated"] as? Boolean ?: false,
                            initialNon_Trouve = map["non_Trouve"] as? Boolean ?: false,
                            init_visible = map["isVisible"] as? Boolean ?: false
                        )
                        produitsMainDataBase.add(prod)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Load clients safely
                clients?.children?.forEach { snap ->
                    try {
                        val map = snap.value as? Map<*, *> ?: return@forEach
                        val client = B_ClientsDataBase(
                            id = snap.key?.toLongOrNull() ?: return@forEach,
                            nom = map["nom"] as? String ?: ""
                        )
                        clientDataBase.add(client)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Load grossists safely
                if (headModels != null) {
                    try {
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
                                    val grossist = C_GrossistsDataBase(
                                        id = snap.key?.toLongOrNull() ?: return@forEach,
                                        nom = map["nom"] as? String ?: "Non Defini"
                                    )
                                    grossistsDataBase.add(grossist)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (!errorOccurred) {
                viewModel.loadingProgress = 1.0f
            }
        }
    } catch (e: Exception) {
        errorOccurred = true
        viewModel.loadingProgress = -1f
        e.printStackTrace()
    } finally {
        connectivityCheckJob?.cancel()
    }
}
