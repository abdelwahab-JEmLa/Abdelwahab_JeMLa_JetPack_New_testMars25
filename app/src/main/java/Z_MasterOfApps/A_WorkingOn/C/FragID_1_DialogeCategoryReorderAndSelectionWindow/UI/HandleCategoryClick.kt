package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.UI

import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.I_CategoriesProduits
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.ViewModel_A4FragID1

fun handleCategoryClick_F1(
    category: I_CategoriesProduits,
    filterText: String,
    viewModel: ViewModel_A4FragID1,
    renameOrFusionMode: Boolean,
    multiSelectionMode: Boolean,
    reorderMode: Boolean,
    heldCategory: I_CategoriesProduits?,
    selectedCategories: List<I_CategoriesProduits>,
    movingCategory: I_CategoriesProduits?,
    onHeldCategoryChange: (I_CategoriesProduits?) -> Unit,
    onSelectedCategoriesChange: (List<I_CategoriesProduits>) -> Unit,
    onRenameOrFusionModeChange: (Boolean) -> Unit,
    onMovingCategoryChange: (I_CategoriesProduits?) -> Unit,
    onReorderModeChange: (Boolean) -> Unit,
    onCategorySelected: (I_CategoriesProduits) -> Unit,
    onDismiss: () -> Unit
) {
   when {
       category.infosDeBase.nom == "Add New Category" -> {
           if (filterText.isNotBlank()) {
               viewModel.addNewCategory(filterText)
           }
       }
       reorderMode -> {
           // Déplace toutes les catégories sélectionnées après la catégorie cliquée
           selectedCategories.forEach { selectedCategory ->
               viewModel.handleCategoryMove(
                   holdedIdCate = selectedCategory.statuesMutable.indexDonsParentList,
                   clickedCategoryId = category.statuesMutable.indexDonsParentList
               ) {}
           }
           // Réinitialise le mode de réorganisation
           onReorderModeChange(false)
           onSelectedCategoriesChange(emptyList())
       }
       renameOrFusionMode -> {
           if (heldCategory == null) {
               onHeldCategoryChange(category)
           } else if (heldCategory != category) {
               viewModel.moveArticlesBetweenCategories(
                   fromCategoryId = heldCategory.statuesMutable.indexDonsParentList,
                   toCategoryId = category.statuesMutable.indexDonsParentList
               )
               onHeldCategoryChange(null)
               onRenameOrFusionModeChange(false)
           }
       }
       multiSelectionMode -> {
           onSelectedCategoriesChange(
               if (category in selectedCategories) {
                   selectedCategories.filterNot { it == category }
               } else {
                   selectedCategories + category
               }
           )
       }
       movingCategory != null -> {
           viewModel.handleCategoryMove(
               holdedIdCate = movingCategory.statuesMutable.indexDonsParentList,
               clickedCategoryId = category.statuesMutable.indexDonsParentList
           ) {
               onMovingCategoryChange(null)
           }
       }
       else -> {
           onCategorySelected(category)
           onDismiss()
       }
   }
}
