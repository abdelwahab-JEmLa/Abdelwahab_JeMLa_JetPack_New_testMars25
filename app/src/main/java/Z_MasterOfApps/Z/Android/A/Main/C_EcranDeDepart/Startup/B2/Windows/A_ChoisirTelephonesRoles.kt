package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B2.Windows

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import android.content.res.Resources
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmergencyRecording
import androidx.compose.material.icons.filled.MapsHomeWork
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

@Composable
fun MainScreen_Windows4(
    modifier: Modifier = Modifier,
    viewModel: ViewModelW4 = koinViewModel(),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Role Phone",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MainList_Windows4(viewModel = viewModel)
        }
    }
}

@Composable
fun MainList_Windows4(
    modifier: Modifier = Modifier,
    viewModel: ViewModelW4 = koinViewModel(),
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(viewModel.j_AppInstalleDonTelephone.modelDatas) { phoneData ->
            MainItem_Windows4(
                phoneData = phoneData,
                onReceiverToggle = {
                    viewModel.setAsReceiverPhone(phoneData)
                }
            )
        }
    }
}

@Composable
fun MainItem_Windows4(
    modifier: Modifier = Modifier,
    phoneData: J_AppInstalleDonTelephone,
    onReceiverToggle: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = phoneData.infosDeBase.nom,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Screen width: ${phoneData.infosDeBase.widthScreen}px",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onReceiverToggle) {
                Icon(
                    imageVector = if (phoneData.etatesMutable.itsReciverTelephone)
                        Icons.Default.EmergencyRecording else Icons.Default.MapsHomeWork,
                    contentDescription = "Toggle receiver status",
                    tint = if (phoneData.etatesMutable.itsReciverTelephone)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreen_Windows4Preview() {
    // Utiliser le repository réel connecté à Firebase
    val firebaseRepository = J_AppInstalleDonTelephoneRepositoryImpl()
    val viewModel = ViewModelW4(firebaseRepository)

    MaterialTheme {
        MainScreen_Windows4(viewModel = viewModel)
    }
}

class ViewModelW4(
    val j_AppInstalleDonTelephone: J_AppInstalleDonTelephoneRepository,
) : ViewModel() {
    fun setAsReceiverPhone(phone: J_AppInstalleDonTelephone) {
        val updatedPhone = j_AppInstalleDonTelephone.modelDatas.find { it.id == phone.id }

        updatedPhone?.etatesMutable?.itsReciverTelephone =
            !updatedPhone?.etatesMutable?.itsReciverTelephone!!

        j_AppInstalleDonTelephone.updatePhones()
    }
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
    suspend fun updateDatas(datas: SnapshotStateList<J_AppInstalleDonTelephone>)
    fun updatePhones()

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
        // Obtenir tous les téléphones et vérifier localement
        J_AppInstalleDonTelephoneRepository.caReference.get().addOnSuccessListener { snapshot ->
            var phoneExists = false
            var maxId = 0L

            // Parcourir tous les téléphones pour trouver celui avec le même nom
            snapshot.children.forEach { phoneSnapshot ->
                try {
                    val id = phoneSnapshot.child("id").getValue(Long::class.java) ?: 0
                    val nom = phoneSnapshot.child("infosDeBase/nom").getValue(String::class.java)
                        ?: phoneSnapshot.child("infosDeBase").child("nom").getValue(String::class.java)
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
                            infosDeBase.widthScreen = phoneSnapshot.child("infosDeBase/widthScreen").getValue(Int::class.java)
                                ?: phoneSnapshot.child("infosDeBase").child("widthScreen").getValue(Int::class.java)
                                        ?: screenWidth
                            etatesMutable.itsReciverTelephone = phoneSnapshot.child("etatesMutable/itsReciverTelephone").getValue(Boolean::class.java)
                                ?: phoneSnapshot.child("etatesMutable").child("itsReciverTelephone").getValue(Boolean::class.java)
                                        ?: false
                        }

                        // Vérifier si le téléphone est déjà dans la liste locale
                        if (modelDatas.none { it.id == id }) {
                            modelDatas.add(phone)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Si le téléphone n'existe pas, l'ajouter
            if (!phoneExists) {
                val newId = maxId + 1

                val newPhone = J_AppInstalleDonTelephone().apply {
                    id = newId
                    infosDeBase.nom = phoneName
                    infosDeBase.widthScreen = screenWidth
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
        }.addOnFailureListener { exception ->
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
