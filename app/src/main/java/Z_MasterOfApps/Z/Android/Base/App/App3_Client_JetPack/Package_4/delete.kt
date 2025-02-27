package Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Package_4

import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.updateProduit
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Models.ArticlesBasesStatsTable

fun deleteColore(
    viewModelInitApp: ViewModelInitApp,
    colorIndex: Int,
    article: ArticlesBasesStatsTable,
    clientBuyerNow: B_ClientsDataBase
) {
    val colorId = when (colorIndex) {
        0 -> article.idcolor1
        1 -> article.idcolor2
        2 -> article.idcolor3
        3 -> article.idcolor4
        else -> 0L
    }

    viewModelInitApp._modelAppsFather.produitsMainDataBase
        .find { it.id.toInt() == article.idArticle }?.let { product ->
            // Find and remove the specific sale for the current client
            product.bonsVentDeCetteCota
                .filter { it.clientIdChoisi == clientBuyerNow.id }
                .forEach { bonVente ->
                    bonVente.colours_Achete.removeIf { it.couleurId == colorId }
                }

            // Remove empty bonVente entries (those with no colors)
            product.bonsVentDeCetteCota.removeIf { bonVente ->
                bonVente.colours_Achete.isEmpty() ||
                        bonVente.colours_Achete.all { it.quantity_Achete == 0 }
            }

            // Calculate total quantities across all sales
            val totalQuantities = product.bonsVentDeCetteCota
                .flatMap { it.colours_Achete }
                .groupBy { it.couleurId }
                .mapValues { (_, colorList) ->
                    colorList.sumOf { it.quantity_Achete }
                }

            // If there are no sales or all quantities are 0, clear bonCommendDeCetteCota
            if (totalQuantities.isEmpty() || totalQuantities.values.all { it == 0 }) {
                // Save current bon commande to history if it exists
                product.bonCommendDeCetteCota?.let { currentBonCommande ->
                    if (!product.historiqueBonsCommend.any { it.mutableBasesStates?.dateInString == currentBonCommande.mutableBasesStates?.dateInString }) {
                        product.historiqueBonsCommend.add(currentBonCommande)
                    }
                }

                // Clear current bon commande
                product.bonCommendDeCetteCota = null

                // Clear all bonsVentDeCetteCota
                product.bonsVentDeCetteCota.clear()
            } else {
                // Update bonCommendDeCetteCota quantities to match the new totals
                product.bonCommendDeCetteCota?.let { bonCommande ->
                    bonCommande.coloursEtGoutsCommendee.forEach { color ->
                        color.quantityAchete = totalQuantities[color.id] ?: 0
                    }

                    // Remove colors with 0 quantity
                    bonCommande.coloursEtGoutsCommendee.removeIf { it.quantityAchete == 0 }
                }
            }

            // Update the product in Firebase
            updateProduit(product, viewModelInitApp)
        }
}
