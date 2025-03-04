package Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface CategoriesRepository {
    suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<I_CategoriesProduits>, Flow<Float>>
    suspend fun getCategoriesById(id: String): I_CategoriesProduits?

    companion object {
        val ancienBaseDonneRef = firebaseDatabase.getReference("H_CategorieTabele")
        val caReference = ref_HeadOfModels.child("I_CategoriesProduits")
    }
}

// Then in CategoriesRepositoryImpl.kt
class CategoriesRepositoryImpl : CategoriesRepository {

    override suspend fun getCategoriesById(id: String): I_CategoriesProduits? {
        return null
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

                        // Update initial progress
                        progressFlow.value = 0f

                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(I_CategoriesProduits::class.java)
                            category?.let {
                                categories.add(it)
                            }

                            // Update progress
                            processedItems++
                            progressFlow.value = processedItems.toFloat() / totalItems.toFloat()
                        }

                        // Sort categories by position (classmentDonsParentList)
                        categories.sortBy { it.statuesMutable.classmentDonsParentList }

                        // Set progress to complete
                        progressFlow.value = 1.0f

                        // Resume the coroutine with the result
                        continuation.resume(categories)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(Exception("Database error: ${error.message}"))
                }
            }

            // Attach the listener to the reference
            CategoriesRepository.caReference.addValueEventListener(listener)

            // Ensure we remove the listener when the coroutine is cancelled
            continuation.invokeOnCancellation {
                CategoriesRepository.caReference.removeEventListener(listener)
            }
        }

        return Pair(categories, progressFlow)
    }
}
