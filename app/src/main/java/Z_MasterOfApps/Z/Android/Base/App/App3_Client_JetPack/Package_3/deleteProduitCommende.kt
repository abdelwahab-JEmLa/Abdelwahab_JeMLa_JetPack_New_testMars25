package Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Package_3

import Z_CodePartageEntreApps.Model.B_ClientsDataBase
import Z_CodePartageEntreApps.Model._ModelAppsFather
import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.produitsFireBaseRef
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Models.SoldArticlesTabelle

fun deleteProduitCommende(
    viewModelInitApp: ViewModelInitApp,
    currentSale: SoldArticlesTabelle,
    currentClient: B_ClientsDataBase?
) {
    viewModelInitApp._modelAppsFather.produitsMainDataBase
        .removeIf { it.id == currentSale.idArticle && it.itsTempProduit }
        .also {
            produitsFireBaseRef
                .child(currentSale.idArticle.toString())
                .removeValue()
        }
    // Find the product and update it
    viewModelInitApp._modelAppsFather.produitsMainDataBase
        .find { it.id == currentSale.idArticle }?.let { product ->

            product.bonsVentDeCetteCota
                .removeIf { bonsVent ->
                    bonsVent.clientIdChoisi == currentClient?.id
                }.also {
                    produitsFireBaseRef
                        .child(currentSale.idArticle.toString())
                        .child("bonsVentDeCetteCota")
                        .removeValue()
                }

            _ModelAppsFather.updateProduit(product, viewModelInitApp)
        }
}
