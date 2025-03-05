package Z_MasterOfApps.A_WorkingOn.A.App

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.UiState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun A_MainScreen(
    state: UiState,
    coordinator: Coordinator
) {
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            B_MainList(state, coordinator)
        }
    }
}

