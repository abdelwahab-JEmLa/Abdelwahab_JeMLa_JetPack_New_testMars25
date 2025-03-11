// ViewModelExtension_App1_F5.kt
package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.ViewModel.Extension

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList

class ViewModelExtension_App1_F5(
    val viewModel: ViewModelInitApp,
    val produitsMainDataBase: MutableList<A_ProduitModel>,
) {
    var excludedProduits: MutableList<A_ProduitModel> =
        emptyList<A_ProduitModel>().toMutableStateList()

    var produitsVerifie: MutableList<A_ProduitModel> =
        emptyList<A_ProduitModel>().toMutableStateList()

    var prochenClickIncludeProduit by mutableStateOf<A_ProduitModel?>(null)
}
