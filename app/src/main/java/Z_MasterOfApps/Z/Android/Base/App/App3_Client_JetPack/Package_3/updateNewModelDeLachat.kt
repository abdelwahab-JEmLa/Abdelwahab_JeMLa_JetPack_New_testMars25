package Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Package_3

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.A_ProduitModel.ClientBonVentModel
import Z_MasterOfApps.Kotlin.Model.A_ProduitModel.GrossistBonCommandes
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Models.ColorsArticlesTabelle
import Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Models.SoldArticlesTabelle
import Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.LogUtils.LogUtils
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun updateNewModelDeLachat(
    quantity: Int,
    currentSale: SoldArticlesTabelle?,
    currentClient: B_ClientsDataBase?,
    colorDetails: ColorsArticlesTabelle,
    viewModelInitApp: ViewModelInitApp
) {
    try {
        if (currentSale == null || currentClient == null) {
            LogUtils.logError(LogUtils.Tags.QUANTITY_BUTTON, "Missing required data")
            return
        }

        // Find product or create new one if it doesn't exist
        var productIndex = viewModelInitApp._modelAppsFather.produitsMainDataBase
            .indexOfFirst { it.id == currentSale.idArticle }

        val product = if (productIndex != -1) {
            viewModelInitApp._modelAppsFather.produitsMainDataBase[productIndex]
        } else {
            // Create new product with the sale's article name
            createNewProduct(
                viewModelInitApp = viewModelInitApp,
                nameArticle = currentSale.nameArticle
            ).also {
                // Update productIndex for later use
                productIndex = viewModelInitApp._modelAppsFather.produitsMainDataBase.size - 1
            }
        }

        val colorPurchase = ClientBonVentModel.ColorAchatModel(
            vidPosition = System.currentTimeMillis(),
            couleurId = colorDetails.idColore,
            nom = colorDetails.nameColore,
            quantity_Achete = quantity,
            imogi = colorDetails.iconColore
        )

        val existingSaleIndex = product.bonsVentDeCetteCota
            .indexOfFirst { it.clientIdChoisi == currentClient.id }

        if (existingSaleIndex != -1) {
            val existingSale = product.bonsVentDeCetteCota[existingSaleIndex]
            val colorIndex = existingSale.colours_Achete
                .indexOfFirst { it.couleurId == colorDetails.idColore }

            if (colorIndex != -1) {
                existingSale.colours_Achete[colorIndex] = colorPurchase
            } else {
                existingSale.colours_Achete.add(colorPurchase)
            }
        } else {
            val newSale = ClientBonVentModel(
                vid = System.currentTimeMillis(),
                clientIdChoisi = currentClient.id
            ).apply {

                colours_Achete.add(colorPurchase)
            }
            product.bonsVentDeCetteCota.add(newSale)
        }

        // Format current date as yyyy-MM-dd
        val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        // Get grossist information with fallback to default
        val lastIdGrossitChoisi = product.historiqueBonsCommend.lastOrNull()?.idGrossistChoisi
            ?: 1

        val aggregatedColors = product.bonsVentDeCetteCota
            .flatMap { it.colours_Achete }
            .groupBy { it.couleurId }
            .mapNotNull { (couleurId, colorList) ->
                colorList.firstOrNull()?.let { firstColor ->
                    val totalQuantity = colorList.sumOf { it.quantity_Achete }
                    if (totalQuantity > 0) {
                        GrossistBonCommandes.ColoursGoutsCommendee(
                            id = couleurId,
                            nom = firstColor.nom,
                            emogi = firstColor.imogi
                        ).apply {
                            quantityAchete = totalQuantity
                        }
                    } else null
                }
            }

        val newBonCommande = GrossistBonCommandes(
            vid = System.currentTimeMillis(),
            idGrossistChoisi = lastIdGrossitChoisi,
            init_coloursEtGoutsCommendee = aggregatedColors
        ).apply {
            mutableBasesStates?.dateInString = currentDate
        }

        // Update current bon commande
        product.bonCommendDeCetteCota = newBonCommande

        // Add to history if date doesn't exist
        if (!product.historiqueBonsCommend.any {
                it.mutableBasesStates
                    ?.dateInString == currentDate
            }) {
            product.historiqueBonsCommend.add(newBonCommande)
        }

        viewModelInitApp.viewModelScope.launch {
            viewModelInitApp._modelAppsFather.produitsMainDataBase[productIndex] = product
            _ModelAppsFather.updateProduit(product, viewModelInitApp)
        }

    } catch (e: Exception) {
        LogUtils.logError(LogUtils.Tags.QUANTITY_BUTTON, "Error updating sale", e)
    }
}

fun createNewProduct(
    viewModelInitApp: ViewModelInitApp,
    nameArticle: String? = null
): A_ProduitModel {
    val maxId = viewModelInitApp._modelAppsFather.produitsMainDataBase
        .maxOfOrNull { it.id } ?: 0

    return A_ProduitModel(
        id = maxId + 1,
        itsTempProduit = true,
    ).apply {
        nom = nameArticle ?: "New Product ${maxId + 1}"
        coloursEtGouts.add(
            A_ProduitModel.ColourEtGout_Model(
                sonImageNeExistPas = true
            )
        )
    }.also {
        viewModelInitApp._modelAppsFather.produitsMainDataBase.add(it)
    }
}
