package Z_MasterOfApps.Kotlin.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class H_GroupeCategories(
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
        var classmentDonsParentList: Long = 0,
    ) {
        var idPremierCategorieDeCetteGroupe by mutableLongStateOf(0L)
    }

    companion object {
        val caReference =GroupesCategoriesRepository.caReference
    }
}


interface GroupesCategoriesRepository {
    suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<H_GroupeCategories>, Flow<Float>>

    companion object {
        val caReference = ref_HeadOfModels.child("F_CataloguesCategories")
    }
}

class GroupesCategoriesRepositoryImpl : GroupesCategoriesRepository {

    override suspend fun onDataBaseChangeListnerAndLoad(): Pair<List<H_GroupeCategories>, Flow<Float>> {
        val progressFlow = MutableStateFlow(0f)

        val groupescategories = suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val datas = mutableListOf<H_GroupeCategories>()
                        val totalItems = snapshot.childrenCount.toInt()
                        var processedItems = 0

                        // Update initial progress
                        progressFlow.value = 0f

                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(H_GroupeCategories::class.java)
                            category?.let {
                                datas.add(it)
                            }

                            // Update progress
                            processedItems++
                            progressFlow.value = processedItems.toFloat() / totalItems.toFloat()
                        }

                        datas.sortBy { it.statuesMutable.classmentDonsParentList }

                        // Set progress to complete
                        progressFlow.value = 1.0f

                        // Resume the coroutine with the result
                        continuation.resume(datas)
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

        return Pair(groupescategories, progressFlow)
    }
}

