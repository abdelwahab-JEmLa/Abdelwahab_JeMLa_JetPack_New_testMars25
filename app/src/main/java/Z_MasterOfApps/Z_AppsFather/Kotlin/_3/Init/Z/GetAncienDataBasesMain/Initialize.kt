package Z_MasterOfApps.Z_AppsFather.Kotlin._3.Init.Z.GetAncienDataBasesMain

import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.ProduitsAncienDataBaseMain
import com.example.Z_AppsFather.Kotlin._1.Model.Parent.GrossistInfosModel
import com.example.Z_AppsFather.Kotlin._1.Model.Parent.Maps.Companion.batchUpdateCompan
import com.example.Z_AppsFather.Kotlin._1.Model.Parent.TypePosition

suspend fun startImplementationViewModel(nombreEntries: Int = 100, onInitProgress: (Int) -> Unit) {
    if (nombreEntries <= 0) return

    // Basic grossist data
    val grossists = listOf(
        GrossistInfosModel(1, "Grossist Alpha"),
        GrossistInfosModel(2, "Grossist Beta"),
        GrossistInfosModel(3, "Grossist Gamma")
    )

    // Get and process products
    val products = GetAncienDataBasesMain().produitsDatabase
        .filter { it.idArticle != 0L }
        .shuffled()
        .take(nombreEntries)

    // Create Firebase data structure
    val firebaseData = grossists.mapIndexed { index, grossist ->
        val startIdx = index * (nombreEntries / grossists.size)
        val endIdx = minOf(startIdx + (nombreEntries / grossists.size), products.size)
        val grossistProducts = products.subList(startIdx, endIdx)

        val (positioned, nonPositioned) = grossistProducts.partition { it.idArticle % 2 == 0L }

        mapOf(
            "grossistInfo" to mapOf("id" to grossist.id, "nom" to grossist.nom),
            "products" to mapOf(
                TypePosition.POSITIONE.name to positioned.map { product ->
                    buildProductMap(product)
                },
                TypePosition.NON_POSITIONE.name to nonPositioned.map { product ->
                    buildProductMap(product)
                }
            )
        )
    }

    batchUpdateCompan(firebaseData)
    onInitProgress(100)
}

private fun buildProductMap(product: ProduitsAncienDataBaseMain) = mapOf(
    "articleInfo" to mapOf(
        "id" to product.idArticle,
        "nom" to product.nomArticleFinale,
        "besoinToBeUpdated" to false
    ),
    "colors" to listOf(
        product.idcolor1 to product.couleur1,
        product.idcolor2 to product.couleur2,
        product.idcolor3 to product.couleur3,
        product.idcolor4 to product.couleur4
    ).filter { (id, name) -> id != 0L && !name.isNullOrBlank() }
        .map { (id, name) ->
            mapOf(
                "colorInfo" to mapOf(
                    "id" to id,
                    "nom" to name,
                    "imogi" to name?.let { getEmoji(it) }
                ),
                "quantity" to (10..50).random()
            )
        }
)

private fun getEmoji(colorName: String) = when {
    colorName.contains("chocolat", true) -> "🍫"
    colorName.contains("fraise", true) -> "🍓"
    colorName.contains("banane", true) -> "🍌"
    colorName.contains("lait", true) -> "🥛"
    colorName.contains("ceris", true) -> "🍒"
    colorName.contains("caramel", true) -> "🥞"
    colorName.contains("fruité", true) -> "🍡"
    colorName.contains("noix", true) -> "🥥"
    colorName.contains("nougat", true) -> "🎇"
    colorName.contains("oreo", true) -> "🍪"
    colorName.contains("reglize", true) -> "🍙"
    colorName.contains("standard", true) -> "🎁"
    colorName.contains("multi", true) -> "🎨"
    else -> "📦"
}
