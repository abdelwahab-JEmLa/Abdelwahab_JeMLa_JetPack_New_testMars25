package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.Dialog.A_OptionsControlsButtons_F1
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "A_id1_GerantDefinirePosition"

@Composable
internal fun A_id1_GerantDefinirePosition(
    modifier: Modifier = Modifier,
    viewModel: ViewModelInitApp = viewModel(),
) {
    LaunchedEffect(viewModel.isLoading, viewModel.loadingProgress) {
        logLoadingState(viewModel.isLoading, viewModel.loadingProgress)
    }

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

    val visibleProducts = viewModel._modelAppsFather.produitsMainDataBase.filter { product ->
        product.bonCommendDeCetteCota
            ?.idGrossistChoisi == viewModel.frag1_A1_ExtVM.idAuFilter
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (databaseSize > 0) {
                    B_ListMainFragment(
                        visibleProducts = visibleProducts,
                        viewModel = viewModel,
                        paddingValues = paddingValues
                    )
                }

                if (viewModel._paramatersAppsViewModelModel.fabsVisibility) {
                    A_OptionsControlsButtons_F1(
                        appsHeadModel = viewModel._modelAppsFather,
                        viewModel = viewModel,
                    )

                    MainScreenFilterFAB(
                        viewModel = viewModel,
                    )
                }
            }

        }
    }
}

private fun logLoadingState(isLoading: Boolean, progress: Float) {
    Log.d(TAG, "Loading State: isLoading=$isLoading, progress=${progress * 100}%")
}
