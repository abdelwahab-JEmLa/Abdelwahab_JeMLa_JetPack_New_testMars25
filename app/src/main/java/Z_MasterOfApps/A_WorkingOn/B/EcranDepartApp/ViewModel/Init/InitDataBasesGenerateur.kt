package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Init

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.FragmentViewModel
import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepository
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.ref_HeadOfModels
import a_MainAppCompnents.DataBaseArticles
import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class InitDataBasesGenerateur(
    private val a_ProduitModelRepository: A_ProduitModelRepository,
    private val fragmentViewModel: FragmentViewModel
) {
    @SuppressLint("SimpleDateFormat")
    suspend fun verifierAndBakupModels() = suspendCancellableCoroutine { continuation ->
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        val refBakup = ref_HeadOfModels.child("Z_BakupksModel").child(currentDate)

        refBakup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val backupData = a_ProduitModelRepository.modelDatas.toList()
                    if (backupData.isEmpty()) {
                        continuation.resume(Unit)
                        return
                    }

                    val totalTasks = backupData.size
                    var completedTasks = 0

                    backupData.forEach { product ->
                        refBakup.child(product.id.toString()).setValue(product)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("InitDataBasesGenerateur", "Backup created for product ID: ${product.id}")
                                } else {
                                    Log.e("InitDataBasesGenerateur", "Backup failed for product ID: ${product.id}", task.exception)
                                }
                                completedTasks++
                                if (completedTasks == totalTasks) {
                                    continuation.resume(Unit)
                                }
                            }
                    }
                } else {
                    continuation.resume(Unit)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(Exception("Backup failed: ${error.message}"))
            }
        })
    }

    suspend fun checkAndUpdateImportedProduct() = suspendCancellableCoroutine { continuation ->
        fragmentViewModel.viewModelScope.launch {
            val importedProducts = a_ProduitModelRepository.modelDatas.filter { it.nom == "Imported Product" }
            if (importedProducts.isEmpty()) {
                continuation.resume(Unit)
                return@launch
            }

            // Retrieve the ancienFireBaseRefDatas
            val ancienFireBaseRefDatas = suspendCancellableCoroutine<List<DataBaseArticles>> { cont ->
                A_ProduitModelRepository.ancienFireBaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val datas = mutableListOf<DataBaseArticles>()
                        for (dataSnapshot in snapshot.children) {
                            val databaseArticle = dataSnapshot.getValue(DataBaseArticles::class.java)
                            databaseArticle?.let {
                                datas.add(it)
                            }
                        }
                        cont.resume(datas)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        cont.resumeWithException(Exception("Failed to fetch ancienFireBaseRefDatas: ${error.message}"))
                    }
                })
            }

            val totalProducts = importedProducts.size
            var processedProducts = 0

            importedProducts.forEach { product ->
                val matchingArticle = ancienFireBaseRefDatas.find { it.idArticle.toLong() == product.id }
                matchingArticle?.let {
                    product.nom = it.nomArticleFinale
                }

                processedProducts++
                if (processedProducts == totalProducts) {
                    a_ProduitModelRepository.updateModelDatas(a_ProduitModelRepository.modelDatas)
                    continuation.resume(Unit)
                }
            }
        }
    }
}
