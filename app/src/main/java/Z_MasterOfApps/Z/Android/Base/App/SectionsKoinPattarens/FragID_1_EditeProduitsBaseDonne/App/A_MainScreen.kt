package Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App

import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Learn.ErrorContent
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.UiState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun A_MainScreen(
    state: UiState,
    onProductClick: (String) -> Unit,
    onRetry: () -> Unit
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.error != null -> {
                    ErrorContent(
                        message = state.error,
                        onRetry = onRetry,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                state.products.isEmpty() -> {
                    Text(
                        text = "Aucun produit disponible",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    B_MainList(state, onProductClick)
                }
            }
        }
    }
}

