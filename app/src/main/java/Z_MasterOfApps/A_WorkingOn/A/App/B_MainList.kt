package Z_MasterOfApps.A_WorkingOn.A.App

import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.UiState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun B_MainList(
    state: UiState,
    coordinator: Coordinator,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.categories) { categorie ->
            C_MainItem(
                categorie = categorie,
                coordinator =coordinator,
            )
        }
    }
}
