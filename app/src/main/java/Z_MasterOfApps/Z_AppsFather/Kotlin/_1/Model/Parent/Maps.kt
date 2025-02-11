package Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent

import Z_MasterOfApps.Z_AppsFather.Kotlin._2.ViewModel.Parent.ViewModelMaps
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.AbstractMap

class Maps {
    var mapGroToMapPositionToProduits = mutableStateListOf<
            Map.Entry<GrossistInfosModel,
                    Map<TypePosition,
                            MutableList<Map.Entry<ArticleInfosModel,
                                    MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>>>>>()

    var positionedArticles = mutableStateListOf<Map.Entry<ArticleInfosModel, MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>>()
    var nonPositionedArticles = mutableStateListOf<Map.Entry<ArticleInfosModel, MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>>()

    companion object {

            private val firebaseRef = Firebase.database
                .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")
                .child("Maps")
                .child("mapGroToMapPositionToProduits")

            // Add the new function for batch updates
            suspend fun batchUpdateCompan(data: List<Map<String, Any>>) {
                try {
                    val updates = data.mapIndexed { index, grossistData ->
                        "/$index" to grossistData
                    }.toMap()

                    firebaseRef.updateChildren(updates).await()
                } catch (e: Exception) {
                    throw e
                }
            }

        fun updateMapData(grossistIndex: Int, viewModel_Head: ViewModelMaps, itsDeplacement: Boolean = false) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val maps = viewModel_Head._maps
                    val mapData = maps.mapGroToMapPositionToProduits

                    if (grossistIndex !in mapData.indices) return@launch

                    val grossistEntry = mapData[grossistIndex]
                    val firebaseUpdates = mapData.map { entry ->
                        val products = if (entry == grossistEntry) {
                            val positioned = if (itsDeplacement) entry.value[TypePosition.POSITIONE] else maps.positionedArticles
                            val nonPositioned = if (itsDeplacement) entry.value[TypePosition.NON_POSITIONE] else maps.nonPositionedArticles

                            mapOf(
                                TypePosition.POSITIONE.name to (positioned?.map { formatArticle(it) } ?: emptyList()),
                                TypePosition.NON_POSITIONE.name to (nonPositioned?.map { formatArticle(it) } ?: emptyList())
                            )
                        } else {
                            entry.value.map { (pos, articles) ->
                                pos.name to articles.map { formatArticle(it) }
                            }.toMap()
                        }

                        mapOf(
                            "grossistInfo" to mapOf(
                                "id" to entry.key.id,
                                "nom" to entry.key.nom
                            ),
                            "products" to products
                        )
                    }

                    // Update Firebase
                    firebaseRef.updateChildren(
                        firebaseUpdates.mapIndexed { index, data -> "/$index" to data }.toMap()
                    ).await()

                    // Update local state
                    val updatedPositions = mutableMapOf<TypePosition, MutableList<Map.Entry<ArticleInfosModel, MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>>>().apply {
                        put(TypePosition.POSITIONE, if (itsDeplacement) grossistEntry.value[TypePosition.POSITIONE] ?: mutableListOf() else maps.positionedArticles.toMutableList())
                        put(TypePosition.NON_POSITIONE, if (itsDeplacement) grossistEntry.value[TypePosition.NON_POSITIONE] ?: mutableListOf() else maps.nonPositionedArticles.toMutableList())
                    }

                    mapData[grossistIndex] = AbstractMap.SimpleEntry(grossistEntry.key, updatedPositions)
                } catch (e: Exception) {
                    throw e
                }
            }
        }

        private fun formatArticle(article: Map.Entry<ArticleInfosModel, MutableList<Map.Entry<ColourEtGoutInfosModel, Double>>>) = mapOf(
            "articleInfo" to mapOf(
                "id" to article.key.id,
                "nom" to article.key.nom,
                "besoinToBeUpdated" to article.key.besoinToBeUpdated,
                "sonImageBesoinActualisation" to article.key.sonImageBesoinActualisation,
                "imageGlidReloadTigger" to article.key.imageGlidReloadTigger
            ),
            "colors" to article.value.map { color ->
                mapOf(
                    "colorInfo" to mapOf(
                        "id" to color.key.id,
                        "nom" to color.key.nom,
                        "imogi" to color.key.imogi
                    ),
                    "quantity" to color.value
                )
            }
        )
    }
}

enum class TypePosition { POSITIONE, NON_POSITIONE }

data class ArticleInfosModel(
    var id: Long = 0,
    var nom: String = "",
    var besoinToBeUpdated: Boolean = false,
    var sonImageBesoinActualisation: Boolean = false,
    var imageGlidReloadTigger: Int = 0
)

data class GrossistInfosModel(
    var id: Long = 0,
    var nom: String = ""
)

data class ColourEtGoutInfosModel(
    var id: Long = 0,
    var nom: String = "",
    var imogi: String = ""
)
