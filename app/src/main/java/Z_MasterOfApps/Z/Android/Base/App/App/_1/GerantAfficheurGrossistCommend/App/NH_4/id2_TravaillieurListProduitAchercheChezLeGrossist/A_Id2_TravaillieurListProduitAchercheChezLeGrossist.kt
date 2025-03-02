package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.E.Dialogs.A_OptionsControlsButtons_F2
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun A_Id2_TravaillieurListProduitAchercheChezLeGrossist(
    modifier: Modifier = Modifier,
    viewModel: ViewModelInitApp = viewModel(),
) {
    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                progress = {
                    viewModel.loadingProgress
                },
                modifier = Modifier.align(Alignment.Center),
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            )
        }
        return
    }

    val databaseSize = viewModel._modelAppsFather.produitsMainDataBase.size

    val visibleProducts = viewModel._modelAppsFather.produitsMainDataBase
        .filter { product ->
            product.bonCommendDeCetteCota?.let { bonCommend ->
                bonCommend.idGrossistChoisi == viewModel.frag2_A1_ExtVM.auFilter &&
                        bonCommend.mutableBasesStates?.cPositionCheyCeGrossit == true
            } ?: false
        }
        .sortedBy { product ->
            product.bonCommendDeCetteCota
                ?.mutableBasesStates
                ?.positionProduitDonGrossistChoisiPourAcheterCeProduit
                ?: Int.MAX_VALUE
        }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (databaseSize > 0) {
                MainList_F2(
                    initVisibleProducts = visibleProducts,
                    viewModelInitApp = viewModel,
                    paddingValues = paddingValues
                )
            }
        }
        if (viewModel
                ._paramatersAppsViewModelModel
                .fabsVisibility
        ) {
            A_OptionsControlsButtons_F2(
                viewModel = viewModel,
            )
            MainScreenFilterFAB_F2(
                viewModel = viewModel,
            )
        }
    }
}
