package Z_MasterOfApps.Z.Android.Packages._3.C_Serveur.App.Client_JetPack.Package_4

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

            // Update the product in Firebase
            updateProduit(product, viewModelInitApp)
        }
}
