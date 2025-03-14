package Z_CodePartageEntreApps.Model

import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class J_AppInstalleDonTelephone(
    var id: Long = 0,
) {
    var infosDeBase by mutableStateOf(InfosDeBase())

    @IgnoreExtraProperties
    class InfosDeBase {
        var nom by mutableStateOf("Non Defini")
        var widthScreen by mutableIntStateOf(0)
        var itsTablette by mutableStateOf(false)
    }

    var etatesMutable by mutableStateOf(EtatesMutable())

    @IgnoreExtraProperties
    class EtatesMutable {
        var itsReciverTelephone by mutableStateOf(false)
        var indexDonsParentList by mutableLongStateOf(0)
        var nearbyWifiAdressIpConexion by mutableStateOf("")
    }
}

interface J_AppInstalleDonTelephoneRepository {
    var modelDatas: SnapshotStateList<J_AppInstalleDonTelephone>
    val progressRepo: MutableStateFlow<Float>
        get() = MutableStateFlow(0f)

    suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<J_AppInstalleDonTelephone>, Flow<Float>>
    suspend fun updateDatas(datas: SnapshotStateList<J_AppInstalleDonTelephone>)
    fun updatePhones()

    companion object {
        // Convert width pixels to dp
        private val displayMetrics = Resources.getSystem().displayMetrics
        private val widthPixels = displayMetrics.widthPixels
        val metricsWidthPixels = widthPixels.pixelsToDp(displayMetrics)

        val caReference = _ModelAppsFather.ref_HeadOfModels.child("J_AppInstalleDonTelephone")

        // Extension function to convert pixels to dp
        private fun Int.pixelsToDp(displayMetrics: DisplayMetrics): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                this.toFloat(),
                displayMetrics
            ).toInt()
        }
    }
}

class J_AppInstalleDonTelephoneRepositoryImpl : J_AppInstalleDonTelephoneRepository {
    private val TAG = "J_AppInstalleDonTelephoneRepo"
    override var modelDatas: SnapshotStateList<J_AppInstalleDonTelephone> = mutableStateListOf()
    override val progressRepo: MutableStateFlow<Float> = MutableStateFlow(0f)

    private var listener: ValueEventListener? = null

    init {
        startDatabaseListener()
        verifyAndAddPhone(
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            J_AppInstalleDonTelephoneRepository.metricsWidthPixels
        )
    }

    private fun verifyAndAddPhone(phoneName: String, screenWidth: Int) {
        // Obtenir tous les téléphones et vérifier localement
        J_AppInstalleDonTelephoneRepository.caReference.get().addOnSuccessListener { snapshot ->
            var phoneExists = false
            var maxId = 0L

            // Parcourir tous les téléphones pour trouver celui avec le même nom
            snapshot.children.forEach { phoneSnapshot ->
                try {
                    val id = phoneSnapshot.child("id").getValue(Long::class.java) ?: 0
                    val nom = phoneSnapshot.child("infosDeBase/nom").getValue(String::class.java)
                        ?: phoneSnapshot.child("infosDeBase").child("nom")
                            .getValue(String::class.java)
                        ?: ""

                    // Mettre à jour l'ID maximum
                    if (id > maxId) {
                        maxId = id
                    }

                    // Vérifier si le téléphone existe déjà
                    if (nom == phoneName) {
                        phoneExists = true

                        // Créer l'objet téléphone à partir des données Firebase
                        val phone = J_AppInstalleDonTelephone().apply {
                            this.id = id
                            infosDeBase.nom = nom
                            infosDeBase.widthScreen = phoneSnapshot.child("infosDeBase/widthScreen")
                                .getValue(Int::class.java)
                                ?: phoneSnapshot.child("infosDeBase").child("widthScreen")
                                    .getValue(Int::class.java)
                                        ?: screenWidth
                            // Check if the device is a tablet based on screen width
                            infosDeBase.itsTablette = infosDeBase.widthScreen > 400
                            etatesMutable.itsReciverTelephone =
                                phoneSnapshot.child("etatesMutable/itsReciverTelephone")
                                    .getValue(Boolean::class.java)
                                    ?: phoneSnapshot.child("etatesMutable")
                                        .child("itsReciverTelephone").getValue(Boolean::class.java)
                                            ?: false
                        }

                        // Vérifier si le téléphone est déjà dans la liste locale
                        if (modelDatas.none { it.id == id }) {
                            modelDatas.add(phone)
                        }
                    }
                } catch (e: Exception) {
                    // Exception silently handled
                }
            }

            // Si le téléphone n'existe pas, l'ajouter
            if (!phoneExists) {
                val newId = maxId + 1

                val newPhone = J_AppInstalleDonTelephone().apply {
                    id = newId
                    infosDeBase.nom = phoneName
                    infosDeBase.widthScreen = screenWidth
                    // Check if the device is a tablet based on screen width
                    infosDeBase.itsTablette = screenWidth > 400
                    etatesMutable.itsReciverTelephone = false
                }

                // Ajouter à la liste locale
                if (modelDatas.none { it.id == newId }) {
                    modelDatas.add(newPhone)

                    // Ajouter à Firebase
                    J_AppInstalleDonTelephoneRepository.caReference
                        .child(newId.toString())
                        .setValue(newPhone)
                }
            }
        }.addOnFailureListener {
            // Exception silently handled
        }
    }

    private fun startDatabaseListener() {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val totalItems = snapshot.childrenCount.toInt()
                    var processedItems = 0

                    modelDatas.clear()
                    progressRepo.value = 0f

                    for (dataSnapshot in snapshot.children) {
                        val category = dataSnapshot.getValue(J_AppInstalleDonTelephone::class.java)
                        category?.let { cat ->
                            // Set tablet status based on screen width
                            cat.infosDeBase.itsTablette = cat.infosDeBase.widthScreen > 400
                            modelDatas.add(cat)
                        }

                        processedItems++
                        progressRepo.value = processedItems.toFloat() / totalItems.toFloat()
                    }

                    // Sort categories by position (classmentDonsParentList)
                    modelDatas.sortBy { it.etatesMutable.indexDonsParentList }

                    progressRepo.value = 1.0f
                } catch (e: Exception) {
                    progressRepo.value = 0f
                }
            }

            override fun onCancelled(error: DatabaseError) {
                progressRepo.value = 0f
            }
        }

        // Attach the listener to the Firebase reference
        listener?.let {
            J_AppInstalleDonTelephoneRepository.caReference.addValueEventListener(it)
        }
    }

    override suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<J_AppInstalleDonTelephone>, Flow<Float>> {
        val progressFlow = MutableStateFlow(0f)

        return suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                private var isResumed = false

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        // Prevent multiple resumptions
                        if (isResumed) return

                        val categories = mutableListOf<J_AppInstalleDonTelephone>()
                        val totalItems = snapshot.childrenCount.toInt()
                        var processedItems = 0

                        modelDatas.clear()
                        progressFlow.value = 0f
                        progressRepo.value = 0f

                        for (dataSnapshot in snapshot.children) {
                            val category =
                                dataSnapshot.getValue(J_AppInstalleDonTelephone::class.java)
                            category?.let { cat ->
                                // Set tablet status based on screen width
                                cat.infosDeBase.itsTablette = cat.infosDeBase.widthScreen > 400
                                categories.add(cat)
                                modelDatas.add(cat)
                            }

                            processedItems++
                            progressFlow.value = processedItems.toFloat() / totalItems.toFloat()
                            progressRepo.value = processedItems.toFloat() / totalItems.toFloat()
                        }

                        // Sort categories by position
                        categories.sortBy { it.etatesMutable.indexDonsParentList }
                        modelDatas.sortBy { it.etatesMutable.indexDonsParentList }

                        progressFlow.value = 1.0f
                        progressRepo.value = 1.0f

                        // Ensure resumption happens only once
                        if (!isResumed) {
                            isResumed = true
                            continuation.resume(Pair(categories, progressFlow))

                            // Remove the listener after successful data retrieval
                            J_AppInstalleDonTelephoneRepository.caReference.removeEventListener(this)
                        }
                    } catch (e: Exception) {
                        if (!isResumed) {
                            isResumed = true
                            continuation.resumeWithException(e)
                            progressRepo.value = 0f

                            // Remove the listener in case of error
                            J_AppInstalleDonTelephoneRepository.caReference.removeEventListener(this)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    if (!isResumed) {
                        isResumed = true
                        continuation.resumeWithException(Exception("Database error: ${error.message}"))
                        progressRepo.value = 0f

                        // Remove the listener in case of cancellation
                        J_AppInstalleDonTelephoneRepository.caReference.removeEventListener(this)
                    }
                }
            }

            // Attach the listener
            J_AppInstalleDonTelephoneRepository.caReference.addValueEventListener(listener)

            // Ensure listener is removed if coroutine is cancelled
            continuation.invokeOnCancellation {
                J_AppInstalleDonTelephoneRepository.caReference.removeEventListener(listener)
            }
        }
    }

    override suspend fun updateDatas(datas: SnapshotStateList<J_AppInstalleDonTelephone>) {
        // Update local modelDatas with the new data
        modelDatas.clear()
        modelDatas.addAll(datas)

        // Update Firebase with the new data
        datas.forEach { category ->
            J_AppInstalleDonTelephoneRepository.caReference.child(category.id.toString())
                .setValue(category)
        }
    }

    override fun updatePhones() {
        modelDatas.forEach { phone ->
            J_AppInstalleDonTelephoneRepository.caReference
                .child(phone.id.toString())
                .setValue(phone)
        }
    }
}
