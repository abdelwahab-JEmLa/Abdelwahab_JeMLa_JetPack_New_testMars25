package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_5.id3_AfficheurDesProduitsPourLeColecteur

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.koin.androidx.compose.koinViewModel

private const val TAG = "A_id1_GerantDefinirePosition"

@Composable
fun A_id3_AfficheurDesProduitsPourLeColecteur(
    modifier: Modifier = Modifier,
    ViewModel: ViewModelInitApp = koinViewModel(),
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (ViewModel._modelAppsFather.produitsMainDataBase.size > 0) {
                MainList_F3(
                    viewModel = ViewModel,
                    paddingValues = paddingValues
                )
            }
        }
        if (ViewModel
                ._paramatersAppsViewModelModel
                .fabsVisibility
        ) {
            MainScreenFilterFAB_F3(
                viewModel = ViewModel,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewA_id3_AfficheurDesProduitsPourLeColecteur() {
    // Create a mock ViewModel for preview purposes
    val mockViewModel = ViewModelInitApp().apply {
        // Initialize necessary properties for preview
        // For example:
        // _modelAppsFather.produitsMainDataBase = listOf(mockProduct1, mockProduct2)
        // _paramatersAppsViewModelModel.fabsVisibility = true
    }

    A_id3_AfficheurDesProduitsPourLeColecteur(
        ViewModel = mockViewModel
    )
}
