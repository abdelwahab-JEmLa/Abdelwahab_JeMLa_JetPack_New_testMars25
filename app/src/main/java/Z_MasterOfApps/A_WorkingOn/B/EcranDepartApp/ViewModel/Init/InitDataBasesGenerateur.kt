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

class InitDataBasesGenerateur(
    private val a_ProduitModelRepository: A_ProduitModelRepository,
    private val fragmentViewModel: FragmentViewModel
) {
    init {
        verifierAndBakupModels()
        checkAndUpdateImportedProduct()
    }


    @SuppressLint("SimpleDateFormat")
    private fun verifierAndBakupModels() {
        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date())
        val refBakup = ref_HeadOfModels.child("Z_BakupksModel").child(currentDate)

        refBakup.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    // Backup does not exist, create one
                    val backupData = a_ProduitModelRepository.modelDatas.toList()

                    // Iterate over each product and set it in the backup with its ID as the key
                    backupData.forEach { product ->
                        refBakup.child(product.id.toString()).setValue(product).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("InitDataBasesGenerateur", "Backup created successfully for product ID: ${product.id}")
                            } else {
                                Log.e("InitDataBasesGenerateur", "Failed to create backup for product ID: ${product.id}: ${task.exception?.message}")
                            }
                        }
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InitDataBasesGenerateur", "Database error: ${error.message}")
            }
        })
    }

    private fun checkAndUpdateImportedProduct() {
        fragmentViewModel.viewModelScope.launch {
            val importedProducts = a_ProduitModelRepository.modelDatas.filter { it.nom == "Imported Product" }

            importedProducts.forEach { product ->
                A_ProduitModelRepository.ancienFireBaseRef
                    .orderByChild("idArticle")
                    .equalTo(product.id.toString()).addListenerForSingleValueEvent(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                for (dataSnapshot in snapshot.children) {
                                    val databaseArticle = dataSnapshot.getValue(DataBaseArticles::class.java)
                                    databaseArticle?.let {
                                        product.nom = it.nomArticleFinale
                                    }
                                }
                            }
                            // Update the model datas after modifying the product names
                                a_ProduitModelRepository.updateModelDatas(a_ProduitModelRepository.modelDatas)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle the error
                            Log.e("InitDataBasesGenerateur", "Database error: ${error.message}")
                        }
                    })
            }
        }
    }
}
