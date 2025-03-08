package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B2.Windows

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import android.content.res.Resources
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.androidx.compose.koinViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//-->
//TODO(1): cree prevwie
@Composable
fun MainScreen_Windows4(
    modifier: Modifier = Modifier,
    viewModel: ViewModelW4 = koinViewModel(),
) {

}

@Composable
fun MainList_Windows4(
    modifier: Modifier = Modifier,
    viewModel: ViewModelW4 = koinViewModel(),
) {
    //-->
    //TODO(1): ici c lazy colum contien   modelDatas list
}

@Composable
fun MainItem_Windows4(
    modifier: Modifier = Modifier,
    viewModel: ViewModelW4 = koinViewModel(),
) {   //-->
//TODO(1): ICI un belle elevated card 
    //-->
    //TODO(1): ajout un icon buton au click
}

class ViewModelW4(
    val j_AppInstalleDonTelephone: J_AppInstalleDonTelephone,
) : ViewModel() {



}

class J_AppInstalleDonTelephone(
    var id: Long = 0,
) {

    var infosDeBase by mutableStateOf(InfosDeBase())
    @IgnoreExtraProperties
    class InfosDeBase{
        var nom by mutableStateOf("Non Defini")
        var widthScreen by mutableIntStateOf(0)
    }

    var etatesMutable by mutableStateOf(EtatesMutable())
     @IgnoreExtraProperties
    class EtatesMutable {
         var itsReciverTelephone by mutableStateOf(false)
         var indexDonsParentList by mutableLongStateOf(0)
    }

    companion object {
        val caReference = J_AppInstalleDonTelephoneRepository.caReference
    }
}


interface J_AppInstalleDonTelephoneRepository {
    var modelDatas: SnapshotStateList<J_AppInstalleDonTelephone>
    val progressRepo: MutableStateFlow<Float>  // Initialize progressRepo
        get() = MutableStateFlow(0f)

    suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<J_AppInstalleDonTelephone>, Flow<Float>>
    suspend fun getCategoriesById(id: String): J_AppInstalleDonTelephone?
    suspend fun updateDatas(datas: SnapshotStateList<J_AppInstalleDonTelephone>)


    companion object {
        val metricsWidthPixels = Resources.getSystem().displayMetrics.widthPixels
        val caReference = ref_HeadOfModels.child("J_AppInstalleDonTelephone")
    }
}

class J_AppInstalleDonTelephoneRepositoryImpl : J_AppInstalleDonTelephoneRepository {
    override var modelDatas: SnapshotStateList<J_AppInstalleDonTelephone> = mutableStateListOf()
    override val progressRepo: MutableStateFlow<Float> = MutableStateFlow(0f) // Added progressRepo

    private var listener: ValueEventListener? = null

    init {
        // Verify and add the phone
        verifyAndAddPhone("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
            J_AppInstalleDonTelephoneRepository.metricsWidthPixels
        )
        startDatabaseListener()
    }

    private fun verifyAndAddPhone(phoneName: String, screenWidth: Int) {
        // Get reference to the phones in Firebase
        J_AppInstalleDonTelephoneRepository.caReference.get().addOnSuccessListener { snapshot ->
            var phoneExists = false
            var maxId = 0L

            // Check if phone exists and find max ID
            snapshot.children.forEach { snap ->
                try {
                    val phone = J_AppInstalleDonTelephone().apply {
                        id = snap.child("id").getValue(Long::class.java) ?: 0
                            infosDeBase.nom = snap.child("nom").getValue(String::class.java) ?: ""
                        infosDeBase.widthScreen = snap.child("widthScreen").getValue(Int::class.java) ?: 0
                            etatesMutable.itsReciverTelephone = snap.child("itsReciverTelephone").getValue(Boolean::class.java) ?: false
                    }

                    // Update max ID
                    if (phone.id > maxId) {
                        maxId = phone.id
                    }

                    // Check if phone exists
                    if (phone.infosDeBase.nom == phoneName) {
                        phoneExists = true
                        // Update local state
                        modelDatas.add(phone)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // If phone doesn't exist, add it
            if (!phoneExists) {
                val newId = maxId + 1

                val newPhone = J_AppInstalleDonTelephone().apply {
                    id = newId
                    infosDeBase.nom = phoneName
                    infosDeBase.widthScreen = screenWidth
                        etatesMutable.itsReciverTelephone = false
                }

                // Add to local state
                modelDatas.add(newPhone)

                // Add to Firebase
                J_AppInstalleDonTelephone.caReference
                    .child(newId.toString())
                    .setValue(newPhone)
            }
        }.addOnFailureListener { exception ->
            // Handle any errors
            exception.printStackTrace()
        }
    }

    private fun startDatabaseListener() {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val totalItems = snapshot.childrenCount.toInt()
                    var processedItems = 0

                    modelDatas.clear()
                    progressRepo.value = 0f // Reset progress

                    for (dataSnapshot in snapshot.children) {
                        val category = dataSnapshot.getValue(J_AppInstalleDonTelephone::class.java)
                        category?.let { cat ->
                            modelDatas.add(cat)
                        }

                        processedItems++
                        progressRepo.value = processedItems.toFloat() / totalItems.toFloat()
                    }

                    // Sort categories by position (classmentDonsParentList)
                    modelDatas.sortBy { it.etatesMutable.indexDonsParentList }

                    progressRepo.value = 1.0f // Complete progress
                } catch (e: Exception) {
                    Log.e("CategoriesRepositoryImpl", "Error loading data: ${e.message}")
                    progressRepo.value = 0f // Reset progress on error
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("CategoriesRepositoryImpl", "Database error: ${error.message}")
                progressRepo.value = 0f // Reset progress on cancellation
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

    override suspend fun getCategoriesById(id: String): J_AppInstalleDonTelephone? {
        return modelDatas.find { it.id.toString() == id }
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
}
