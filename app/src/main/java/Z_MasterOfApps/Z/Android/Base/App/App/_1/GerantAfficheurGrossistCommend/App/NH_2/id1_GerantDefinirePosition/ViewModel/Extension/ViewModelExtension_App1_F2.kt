package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.ViewModel.Extension

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Frag2_A1_ExtVM(
    val viewModel: ViewModelInitApp,
    val produitsMainDataBase: MutableList<A_ProduitModel>,
) {
    val produitsAChoisireLeurClient = viewModel
        ._paramatersAppsViewModelModel.produitsAChoisireLeurClient

    var idAuFilter by mutableStateOf<Long?>(0)

    fun addToproduitsAChoisireLeurClient(last: A_ProduitModel) {
        produitsAChoisireLeurClient.add(last)
    }

}
