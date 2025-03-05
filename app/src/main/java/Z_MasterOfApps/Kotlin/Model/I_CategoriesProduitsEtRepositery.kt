package Z_MasterOfApps.Kotlin.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
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

class I_CategoriesProduits(
    var id: Long = 0,
    var infosDeBase: InfosDeBase = InfosDeBase(),
    var statuesMutable: StatuesMutable = StatuesMutable(),
) {
    @IgnoreExtraProperties
    class InfosDeBase(
        var nom: String = "Non Defini",
    )

    @IgnoreExtraProperties
    class StatuesMutable(
        var indexDonsParentList: Long = 0,
    )
}

interface CategoriesRepository {
    var modelDatas: SnapshotStateList<I_CategoriesProduits>
    val progressRepo: MutableStateFlow<Float>  // Initialize progressRepo
        get() = MutableStateFlow(0f)

    suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<I_CategoriesProduits>, Flow<Float>>
    suspend fun getCategoriesById(id: String): I_CategoriesProduits?

    companion object {
        val ancienBaseDonneRef = firebaseDatabase.getReference("H_CategorieTabele")
        val caReference = ref_HeadOfModels.child("I_CategoriesProduits")
    }
}

class CategoriesRepositoryImpl : CategoriesRepository {
    override var modelDatas: SnapshotStateList<I_CategoriesProduits> = mutableStateListOf()
    override val progressRepo: MutableStateFlow<Float> = MutableStateFlow(0f) // Added progressRepo

    private var listener: ValueEventListener? = null

    init {
        // Initialize the listener when the repository is created
        startDatabaseListener()
    }

    private fun startDatabaseListener() {
        listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val categories = mutableListOf<I_CategoriesProduits>()
                    val totalItems = snapshot.childrenCount.toInt()
                    var processedItems = 0

                    modelDatas.clear()
                    progressRepo.value = 0f // Reset progress

                    for (dataSnapshot in snapshot.children) {
                        val category = dataSnapshot.getValue(I_CategoriesProduits::class.java)
                        category?.let { cat ->
                            categories.add(cat)
                            modelDatas.add(cat)
                        }

                        processedItems++
                        progressRepo.value = processedItems.toFloat() / totalItems.toFloat()
                    }

                    // Sort categories by position (classmentDonsParentList)
                    categories.sortBy { it.statuesMutable.indexDonsParentList }
                    modelDatas.sortBy { it.statuesMutable.indexDonsParentList }

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
            CategoriesRepository.caReference.addValueEventListener(it)
        }
    }

    override suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<I_CategoriesProduits>, Flow<Float>> {
        val progressFlow = MutableStateFlow(0f)

        val categories = suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val categories = mutableListOf<I_CategoriesProduits>()
                        val totalItems = snapshot.childrenCount.toInt()
                        var processedItems = 0

                        modelDatas.clear()
                        progressFlow.value = 0f
                        progressRepo.value = 0f // Also update progressRepo

                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(I_CategoriesProduits::class.java)
                            category?.let { cat ->
                                categories.add(cat)
                                modelDatas.add(cat)
                            }

                            processedItems++
                            progressFlow.value = processedItems.toFloat() / totalItems.toFloat()
                            progressRepo.value = processedItems.toFloat() / totalItems.toFloat()
                        }

                        // Sort categories by position (classmentDonsParentList)
                        categories.sortBy { it.statuesMutable.indexDonsParentList }
                        modelDatas.sortBy { it.statuesMutable.indexDonsParentList }

                        progressFlow.value = 1.0f
                        progressRepo.value = 1.0f // Complete progress

                        continuation.resume(categories)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                        progressRepo.value = 0f // Reset progress on error
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception("Database error: ${error.message}"))
                    progressRepo.value = 0f // Reset progress on cancellation
                }
            }

            CategoriesRepository.caReference.addValueEventListener(listener)
            continuation.invokeOnCancellation {
                CategoriesRepository.caReference.removeEventListener(listener)
            }
        }

        return Pair(categories, progressFlow)
    }

    override suspend fun getCategoriesById(id: String): I_CategoriesProduits? {
        return modelDatas.find { it.id.toString() == id }
    }

    fun cleanup() {
        // Remove the listener when the repository is no longer needed
        listener?.let {
            CategoriesRepository.caReference.removeEventListener(it)
        }
    }
}
