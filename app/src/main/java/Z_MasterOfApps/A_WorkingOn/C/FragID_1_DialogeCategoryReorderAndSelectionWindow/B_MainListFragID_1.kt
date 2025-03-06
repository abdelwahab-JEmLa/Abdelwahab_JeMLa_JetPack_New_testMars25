package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow

import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.I_CategoriesProduits
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.ViewModel_A4FragID1
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
    val addNewCategoryItem = remember {
        I_CategoriesProduits(
            id = (categories.maxOfOrNull { it.id } ?: 0) + 1,
        ).apply {
            infosDeBase.nom = "Add New Category"
            statuesMutable.indexDonsParentList = 0
        }
    }

    // Pre-compute product mappings by category ID
    val productsByCategory = remember(viewModel.a_ProduitModelRepository.modelDatas) {
        viewModel.a_ProduitModelRepository.modelDatas.groupBy { it.parentCategoryId }
    }

    LazyColumn(
        contentPadding = PaddingValues(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(
            items = listOf(addNewCategoryItem) + categories,
            key = { it.id } // Use stable ID as key
        ) { category ->
            val categoryClickHandler = remember(category) {
                { onCategoryClick(category) }
            }

            CategoriesStikyHeaderF1(
                category = category,
                isSelected = category in selectedCategories,
                isMoving = category == movingCategory,
                isHeld = category == heldCategory,
                isReorderTarget = reorderMode && category !in selectedCategories,
                selectionOrder = selectedCategories.indexOf(category) + 1,
                onClick = categoryClickHandler
            )

            // Access pre-computed products for this category
            val categoryProducts = productsByCategory[category.id] ?: emptyList()

            // LazyRow of products in this category
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = categoryProducts,
                    key = { _, product -> product.id }
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
