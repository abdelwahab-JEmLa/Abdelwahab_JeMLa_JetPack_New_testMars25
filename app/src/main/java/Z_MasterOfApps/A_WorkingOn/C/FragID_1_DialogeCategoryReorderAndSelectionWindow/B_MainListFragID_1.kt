package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow

import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.I_CategoriesProduits
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.ViewModel_A4FragID1
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun B_MainList_A4FragID_1(
    categories: List<I_CategoriesProduits>,
    selectedCategories: List<I_CategoriesProduits>,
    movingCategory: I_CategoriesProduits?,
    heldCategory: I_CategoriesProduits?,
    reorderMode: Boolean,
    onCategoryClick: (I_CategoriesProduits) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ViewModel_A4FragID1
) {
    val addNewCategoryItem = I_CategoriesProduits(
        id = (categories.maxOfOrNull { it.id } ?: 0) + 1,
    ).apply {
        infosDeBase.nom = "Add New Category"
        statuesMutable.indexDonsParentList = 0
    }

    val allCategories = listOf(addNewCategoryItem) + categories

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // For each category, add a header that spans the full width
        allCategories.forEach { category ->
            // Add the sticky header with span count of 4 (full width)
            item(span = { GridItemSpan(4) }) {
                CategoriesStikyHeaderF1(
                    category = category,
                    isSelected = category in selectedCategories,
                    isMoving = category == movingCategory,
                    isHeld = category == heldCategory,
                    isReorderTarget = reorderMode && category !in selectedCategories,
                    selectionOrder = selectedCategories.indexOf(category) + 1,
                    onClick = { onCategoryClick(category) }
                )
            }

            // Add products for this category in a grid
            items(
                viewModel.a_ProduitModelRepository.modelDatas
                    .filter { it.parentCategoryId == category.id }
            ) { produit ->
                C_MainItemF1(
                    mainItem = produit,
                    onClickOnMain = {},
                    // Removed the position parameter to avoid numbering in grid layout
                )
            }
        }
    }
}
