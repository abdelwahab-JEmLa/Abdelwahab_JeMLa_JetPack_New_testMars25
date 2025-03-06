package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow

import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.I_CategoriesProduits
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    modifier: Modifier = Modifier
) {
    val addNewCategoryItem = I_CategoriesProduits(
        id = (categories.maxOfOrNull { it.id } ?: 0) + 1,
    ).apply {
        infosDeBase.nom = "Add New Category"
        statuesMutable.indexDonsParentList = 0
    }

    LazyColumn(
        contentPadding = PaddingValues(2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(listOf(addNewCategoryItem) + categories) { category ->
            C_MainItem_A4FragID_1(
                category = category,
                isSelected = category in selectedCategories,
                isMoving = category == movingCategory,
                isHeld = category == heldCategory,
                isReorderTarget = reorderMode && category !in selectedCategories,
                selectionOrder = selectedCategories.indexOf(category) + 1,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

