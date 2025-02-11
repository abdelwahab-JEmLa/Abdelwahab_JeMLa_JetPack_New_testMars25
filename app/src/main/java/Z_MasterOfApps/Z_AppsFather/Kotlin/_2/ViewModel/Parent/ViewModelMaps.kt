// ViewModelMaps.kt
package Z_MasterOfApps.Z_AppsFather.Kotlin._2.ViewModel.Parent

import Z_MasterOfApps.Z_AppsFather.Kotlin._2.ViewModel.ParamatersAppsViewModel
import Z_MasterOfApps.Z_AppsFather.Kotlin._3.Init.Z.GetAncienDataBasesMain.startImplementationViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.ArticleInfosModel
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.ColourEtGoutInfosModel
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.GrossistInfosModel
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.Maps
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.TypePosition
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import java.util.AbstractMap

class ViewModelMaps : ViewModel() {
    var _maps by mutableStateOf(Maps())
    val maps: Maps get() = _maps
    private val paramatersAppsViewModel = ParamatersAppsViewModel()


    var isLoading by mutableStateOf(false)
    var loadingProgress by mutableFloatStateOf(0f)

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                isLoading = true
                loadingProgress = 0f

                startImplementationViewModel(50) {
                    loadingProgress = it.toFloat()
                }
                loadingProgress = 0.5f

                Firebase.database
                    .getReference("0_UiState_3_Host_Package_3_Prototype11Dec/Maps/mapGroToMapPositionToProduits")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val newList = snapshot.children.mapNotNull { grossistSnapshot ->
                            parseGrossistData(grossistSnapshot.value as? Map<*, *> ?: return@mapNotNull null)
                        }.toMutableStateList()

                        _maps = Maps().apply {
                            mapGroToMapPositionToProduits = newList
                        }

                        loadingProgress = 1f
                        isLoading = false
                    }
            } catch (e: Exception) {
                println("Erreur de chargement: ${e.message}")
                isLoading = false
            }
        }
    }

    private fun parseGrossistData(data: Map<*, *>): Map.Entry<GrossistInfosModel, Map<TypePosition, MutableList<Map.Entry<ArticleInfosModel, MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>>>> {
        val grossistInfo = (data["grossistInfo"] as? Map<*, *>)?.let { info ->
            GrossistInfosModel(
                (info["id"] as? Number)?.toLong() ?: 0L,
                info["nom"] as? String ?: ""
            )
        } ?: GrossistInfosModel()

        val positionMap = mutableMapOf<TypePosition, MutableList<Map.Entry<ArticleInfosModel, MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>>>()

        (data["products"] as? Map<*, *>)?.forEach { (positionKey, products) ->
            val position = TypePosition.valueOf(positionKey.toString())
            positionMap[position] = parseProducts(products)
        }

        return AbstractMap.SimpleEntry(grossistInfo, positionMap)
    }

    private fun parseProducts(products: Any?): MutableList<Map.Entry<ArticleInfosModel, MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>> {
        return (products as? List<*>)?.mapNotNull { productData ->
            (productData as? Map<*, *>)?.let { pData ->
                val articleInfo = parseArticleInfo(pData["articleInfo"] as? Map<*, *>)
                val colors = parseColors(pData["colors"] as? List<*>)
                AbstractMap.SimpleEntry(articleInfo, colors)
            }
        }?.toMutableList() ?: mutableListOf()
    }

    private fun parseArticleInfo(data: Map<*, *>?): ArticleInfosModel {
        return ArticleInfosModel(
            id = (data?.get("id") as? Number)?.toLong() ?: 0L,
            nom = data?.get("nom") as? String ?: "",
            besoinToBeUpdated = data?.get("besoinToBeUpdated") as? Boolean ?: false,
            sonImageBesoinActualisation = data?.get("sonImageBesoinActualisation") as? Boolean ?: false,
            imageGlidReloadTigger = (data?.get("imageGlidReloadTigger") as? Number)?.toInt() ?: 0
        )
    }

    private fun parseColors(colors: List<*>?): MutableList<Map.Entry<ColourEtGoutInfosModel, Double>> {
        return colors?.mapNotNull { colorData ->
            (colorData as? Map<*, *>)?.let { cData ->
                val colorInfo = (cData["colorInfo"] as? Map<*, *>)?.let { cInfo ->
                    ColourEtGoutInfosModel(
                        (cInfo["id"] as? Number)?.toLong() ?: 0L,
                        cInfo["nom"] as? String ?: "",
                        cInfo["imogi"] as? String ?: ""
                    )
                } ?: return@mapNotNull null
                AbstractMap.SimpleEntry(colorInfo, (cData["quantity"] as? Number)?.toDouble() ?: 0.0)
            }
        }?.toMutableList() ?: mutableListOf()
    }
}

