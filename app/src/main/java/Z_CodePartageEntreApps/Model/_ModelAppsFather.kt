package Z_CodePartageEntreApps.Model

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.Exclude
import com.google.firebase.database.database
import com.google.firebase.storage.storage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
open class _ModelAppsFather(
    initial_Produits_Main_DataBase: List<A_ProduitModel> = emptyList()
) {
    @get:Exclude
    var applicationEstInstalleDonTelephone: SnapshotStateList<E_AppsOptionsStates.ApplicationEstInstalleDonTelephone> =
        emptyList<E_AppsOptionsStates.ApplicationEstInstalleDonTelephone>().toMutableStateList()
    @get:Exclude
    var f_PrototypseDeProgramationInfos: SnapshotStateList<E_AppsOptionsStates.F_PrototypseDeProgramationInfos> =
        emptyList<E_AppsOptionsStates.F_PrototypseDeProgramationInfos>().toMutableStateList()

    @get:Exclude
    var h_GroupesCategories: SnapshotStateList<H_GroupeCategories> =
        emptyList<H_GroupeCategories>().toMutableStateList()

    @get:Exclude
    var i_CategoriesProduits: SnapshotStateList<I_CategoriesProduits> =
        emptyList<I_CategoriesProduits>().toMutableStateList()

    @get:Exclude
    var produitsMainDataBase: SnapshotStateList<A_ProduitModel> =
        initial_Produits_Main_DataBase.toMutableStateList()

    @get:Exclude
    var clientDataBase: SnapshotStateList<B_ClientsDataBase> =
        emptyList<B_ClientsDataBase>().toMutableStateList()

    @get:Exclude
    var grossistsDataBase: SnapshotStateList<C_GrossistsDataBase> =
        emptyList<C_GrossistsDataBase>().toMutableStateList()
    @get:Exclude
    var couleursProduitsInfos: SnapshotStateList<D_CouleursEtGoutesProduitsInfos> =
        emptyList<D_CouleursEtGoutesProduitsInfos>().toMutableStateList()

    val groupedProductsParGrossist: List<Map.Entry<C_GrossistsDataBase, List<A_ProduitModel>>>
        get() = grossistsDataBase.map { grossist ->
            val matchingProducts = produitsMainDataBase.filter { product ->
                product.bonCommendDeCetteCota?.idGrossistChoisi == grossist.id &&
                        product.bonsVentDeCetteCota.any { bonVent ->
                            bonVent.colours_Achete.any { color ->
                                color.quantity_Achete > 0
                            }
                        }
            }

            java.util.AbstractMap.SimpleEntry(grossist, matchingProducts)
        }.sortedBy { entry ->
            entry.key.statueDeBase.itIndexInParentList
        }


    val groupedProductsParClients: List<Map.Entry<B_ClientsDataBase, List<A_ProduitModel>>>
        get() = clientDataBase.map { client ->
            val matchingProducts = produitsMainDataBase.filter { product ->
                product.bonsVentDeCetteCota.any { bonVent ->
                    bonVent.clientIdChoisi == client.id
                }
            }

            java.util.AbstractMap.SimpleEntry(client, matchingProducts)
        }.sortedBy { entry ->
            entry.key.statueDeBase.positionDonClientsList
        }

    companion object {
        val firebaseDatabase = Firebase.database

        val ref_HeadOfModels = firebaseDatabase
            .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")

        val produitsFireBaseRef = firebaseDatabase
            .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")
            .child("produits")
        val imagesProduitsFireBaseStorageRef = Firebase.storage.reference
            .child("Images Articles Data Base")
            .child("produits")
        const val imagesProduitsLocalExternalStorageBasePath =
            "/storage/emulated/0/" +
                    "Abdelwahab_jeMla.com" +
                    "/IMGs" +
                    "/BaseDonne"

        fun update_AllProduits(
            updatedProducts: List<A_ProduitModel>,
            viewModelProduits: ViewModelInitApp
        ) {
            viewModelProduits.viewModelScope.launch {
                try {
                    // Update local state
                    viewModelProduits._modelAppsFather.produitsMainDataBase.clear()
                    viewModelProduits._modelAppsFather.produitsMainDataBase.addAll(updatedProducts)

                    // Then update Firebase in chunks to prevent overwhelming the connection
                    UpdateFireBase(updatedProducts)
                } catch (e: Exception) {
                    Log.e("Firebase", "Error updating products", e)
                    throw e
                }
            }
        }

        suspend fun UpdateFireBase(updatedProducts: List<A_ProduitModel>) {
            updatedProducts.forEach { product ->
                try {
                    produitsFireBaseRef.child(product.id.toString()).setValue(product)
                        .await()
                    Log.d("Firebase", "Successfully updated product ${product.id}")
                } catch (e: Exception) {
                    Log.e("Firebase", "Failed to update product ${product.id}", e)
                }
            }
        }

        fun updateProduit(product: A_ProduitModel, viewModelProduits: ViewModelInitApp) {
            viewModelProduits.viewModelScope.launch {
                try {
                    // Créer une nouvelle liste temporaire
                    val updatedList =
                        viewModelProduits._modelAppsFather.produitsMainDataBase.toMutableList()
                    val index = updatedList.indexOfFirst { it.id == product.id }
                    if (index != -1) {
                        updatedList[index] = product
                        // Remplacer toute la liste
                        viewModelProduits._modelAppsFather.produitsMainDataBase.clear()
                        viewModelProduits._modelAppsFather.produitsMainDataBase.addAll(updatedList)
                    }

                    produitsFireBaseRef.child(product.id.toString()).setValue(product).await()
                } catch (e: Exception) {
                    Log.e("ViewModelInitApp", "Failed to update product ${product.id}", e)
                }
            }
        }
    }

}
