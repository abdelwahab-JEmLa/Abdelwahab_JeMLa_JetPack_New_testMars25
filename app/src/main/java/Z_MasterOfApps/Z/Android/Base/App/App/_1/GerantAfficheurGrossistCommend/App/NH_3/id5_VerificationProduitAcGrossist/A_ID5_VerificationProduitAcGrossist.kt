package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.ViewModel.Extension.ViewModelExtension_App1_F5
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.E.Dialogs.A_OptionsControlsButtons_F2
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun A_ID5_VerificationProduitAcGrossist(
    modifier: Modifier = Modifier,
    viewModel: ViewModelInitApp = viewModel(),
) {

    val extensionVM =
        ViewModelExtension_App1_F5(
                viewModel,
                viewModel.produitsMainDataBase
            )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {

                if (extensionVM.produitsVerifie.size > 0) {
                    C_MainList_F5(
                        extensionVM = extensionVM,
                        viewModel = viewModel,
                        paddingValues = paddingValues
                    )
                }

                if (viewModel._paramatersAppsViewModelModel.fabsVisibility) {
                    B_MainScreenFilterFAB_F5(
                        extensionVM = extensionVM,
                        viewModelProduits = viewModel,
                    )

                }
            }
        }
    }
}

