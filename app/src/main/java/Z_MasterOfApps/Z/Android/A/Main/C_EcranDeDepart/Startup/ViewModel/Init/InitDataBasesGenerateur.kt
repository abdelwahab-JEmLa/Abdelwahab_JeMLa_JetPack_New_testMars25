package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.ViewModel.Init

import Z_CodePartageEntreApps.Model.A_ProduitModelRepository
import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.ref_HeadOfModels
import Z_CodePartageEntreApps.Model.I_CategoriesRepository
import Z_MasterOfApps.Z.Android.Base.App.Sections.ProtoMars.App.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.ViewModel_A4FragID1
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
    private val fragmentViewModel: ViewModel_A4FragID1,
    private val categoriesRepository: I_CategoriesRepository
) {
    @SuppressLint("SimpleDateFormat")
    suspend fun verifierAndBakupModels() = suspendCancellableCoroutine { continuation ->
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        val refBakup = ref_HeadOfModels.child("Z_BakupksModel").child(currentDate)

        if (false) {
            ref_HeadOfModels.child("Z_BakupksModel").removeValue()
        }

        refBakup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val backupData = a_ProduitModelRepository.modelDatas.toList()
                val backupCategories = categoriesRepository.modelDatas.toList()

                // Total number of all backup tasks
                val totalTasks = backupData.size + backupCategories.size
                var completedTasks = 0

                // Backup Products
                val productsRef = refBakup.child("A_ProduitModel")
                backupData.forEach { product ->
                    productsRef.child(product.id.toString()).setValue(product)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(
                                    "InitDataBasesGenerateur",
                                    "Backup created for product ID: ${product.id}"
                                )
                            } else {
                                Log.e(
                                    "InitDataBasesGenerateur",
                                    "Backup failed for product ID: ${product.id}",
                                    task.exception
                                )
                            }
                            completedTasks++
                            if (completedTasks == totalTasks) {
                                continuation.resume(Unit)
                            }
                        }
                }

                // Backup Categories
                val categoriesRef = refBakup.child("I_CategoriesProduits")
                backupCategories.forEach { category ->
                    categoriesRef.child(category.id.toString()).setValue(category)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(
                                    "InitDataBasesGenerateur",
                                    "Backup created for category ID: ${category.id}"
                                )
                            } else {
                                Log.e(
                                    "InitDataBasesGenerateur",
                                    "Backup failed for category ID: ${category.id}",
                                    task.exception
                                )
                            }
                            completedTasks++
                            if (completedTasks == totalTasks) {
                                continuation.resume(Unit)
                            }
                        }
                }

                // If no items to backup, resume continuation
                if (totalTasks == 0) {
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
            val declencherDeTout = false

            val importedProducts =
                a_ProduitModelRepository.modelDatas.filter {
                    it.nom == "Imported Product"
                            || it.parentCategoryId == 0L
                }
            if (importedProducts.isEmpty()) {
                continuation.resume(Unit)
                return@launch
            }

            val ancienFireBaseRefDatas =
                suspendCancellableCoroutine<List<DataBaseArticles>> { cont ->
                    A_ProduitModelRepository.ancienFireBaseRef.addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val datas = mutableListOf<DataBaseArticles>()
                            for (dataSnapshot in snapshot.children) {
                                val databaseArticle =
                                    dataSnapshot.getValue(DataBaseArticles::class.java)
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
                val matchingArticle =
                    ancienFireBaseRefDatas.find { it.idArticle.toLong() == product.id }
                matchingArticle?.let { ancienDataBase ->
                    product.nom = ancienDataBase.nomArticleFinale
                    product.parentCategoryId = categoriesRepository.modelDatas.find { cate ->
                        cate.infosDeBase.nom == ancienDataBase.nomCategorie
                    }?.id ?: 0
                    product.etatesMutable.diponibilityEtate = ancienDataBase.diponibilityState == ""
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
