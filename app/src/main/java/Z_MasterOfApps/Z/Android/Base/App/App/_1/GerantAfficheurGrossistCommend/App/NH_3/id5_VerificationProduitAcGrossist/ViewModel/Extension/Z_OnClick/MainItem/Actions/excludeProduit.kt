package Z_MasterOfApps.Z.Android.Packages._1.GerantAfficheurGrossistCommend.App.NH_5.ID5_VerificationProduitAcGrossist.ViewModel.Extension.Z_OnClick.MainItem.Actions

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.ViewModel.Extension.ViewModelExtension_App1_F5

fun ViewModelExtension_App1_F5.excludeProduit(
    product: A_ProduitModel,
) {
    produitsVerifie.remove(product)
    excludedProduits.add(product)

}
