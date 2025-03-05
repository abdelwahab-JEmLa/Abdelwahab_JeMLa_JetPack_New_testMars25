package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.FragmentViewModel
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.UiState
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
    viewModel: FragmentViewModel,
) {

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(viewModel.i_CategoriesRepository.modelDatas) { categorie ->
            C_MainItem(
                viewModel=viewModel,
                state=state,
                categorie = categorie,
                coordinator =coordinator,
            )
        }
    }
}
