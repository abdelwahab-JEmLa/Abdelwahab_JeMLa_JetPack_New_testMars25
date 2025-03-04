package Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
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
        var classmentDonsParentList: Long = 0,
    )
}

// Repository interfaces
interface CategoriesRepository {
    suspend fun onDataBaseChangeListnerAndLoad(): List<I_CategoriesProduits>
    suspend fun getCategoriesById(id: String): I_CategoriesProduits?
}

// Then in CategoriesRepositoryImpl.kt
class CategoriesRepositoryImpl : CategoriesRepository {

    private val caReference = ref_HeadOfModels
        .child("I_CategoriesProduits")

    override suspend fun getCategoriesById(id: String): I_CategoriesProduits? {
        return null
    }

    override suspend fun onDataBaseChangeListnerAndLoad(): List<I_CategoriesProduits> {
        return suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val categories = mutableListOf<I_CategoriesProduits>()

                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(I_CategoriesProduits::class.java)
                            category?.let {
                                categories.add(it)
                            }
                        }

                        // Sort categories by position (classmentDonsParentList)
                        categories.sortBy { it.statuesMutable.classmentDonsParentList }

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
            val ref = caReference.addValueEventListener(listener)

            // Ensure we remove the listener when the coroutine is cancelled
            continuation.invokeOnCancellation {
                caReference.removeEventListener(listener)
            }
        }
    }
}
