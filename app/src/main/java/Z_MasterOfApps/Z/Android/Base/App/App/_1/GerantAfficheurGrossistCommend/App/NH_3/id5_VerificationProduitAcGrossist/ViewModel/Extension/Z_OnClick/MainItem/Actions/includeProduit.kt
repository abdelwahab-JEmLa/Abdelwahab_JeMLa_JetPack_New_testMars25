package Z_MasterOfApps.Z.Android.Packages._1.GerantAfficheurGrossistCommend.App.NH_5.ID5_VerificationProduitAcGrossist.ViewModel.Extension.Z_OnClick.MainItem.Actions

import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.ViewModel.Extension.ViewModelExtension_App1_F5
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.update_AllProduits
import Z_MasterOfApps.Kotlin.Model.A_ProduitModel

fun ViewModelExtension_App1_F5.includeProduit(clickeProduct: A_ProduitModel) {
    excludedProduits.remove(prochenClickIncludeProduit)
    val targetIndex = produitsVerifie.indexOf(clickeProduct)
    if (targetIndex != -1) {
        produitsVerifie.add(targetIndex + 1, prochenClickIncludeProduit!!)
    } else {
        produitsVerifie.add(prochenClickIncludeProduit!!)
    }
    prochenClickIncludeProduit = null

    // Fixed: Use forEachIndexed instead of trying to destructure
    produitsVerifie.forEachIndexed { index, produit ->
        produit.bonCommendDeCetteCota
            ?.mutableBasesStates
            ?.positionProduitDonGrossistChoisiPourAcheterCeProduit =
            index + 1
    }

    update_AllProduits(produitsVerifie,viewModel)
}
