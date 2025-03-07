package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.FragmentViewModel
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.UiState
import Z_MasterOfApps.Z.Android.Base.App.Sections.ProtoMars.App.FragID_1_DialogeCategoryReorderAndSelectionWindow.C_MainItemF1
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
            CategoriesStikyHeader(
                viewModel = viewModel,
                state = state,
                categorie = categorie,
                coordinator = coordinator,
            )

            // LazyRow of products in this category
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Filter products by current category and create items
                itemsIndexed(
                    viewModel.a_ProduitModelRepository.modelDatas
                        .filter { it.parentCategoryId == categorie.id }
                ) { index, produit ->
                    C_MainItemF1(
                        mainItem = produit,
                        position = index + 1,
                        onClickOnMain = {}
                    )
                }
            }
        }
    }
}
