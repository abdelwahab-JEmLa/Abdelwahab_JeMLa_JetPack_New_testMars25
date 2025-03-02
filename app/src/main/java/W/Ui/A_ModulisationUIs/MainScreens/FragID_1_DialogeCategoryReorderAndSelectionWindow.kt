package W.Ui.A_ModulisationUIs.MainScreens

import W.Ui.A_ModulisationUIs.A_MainLists.CategoryGridFragID_1
import Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.Ancien.BottomActions
import Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.Ancien.SearchField
import Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.Ancien.handleCategoryClick
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Archives.CategoriesTabelleECB
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun FragID_1_DialogeCategoryReorderAndSelectionWindow(
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    onCategorySelected: (CategoriesTabelleECB) -> Unit,
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
) {
    var multiSelectionMode by remember { mutableStateOf(false) }
    var renameOrFusionMode by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf<List<CategoriesTabelleECB>>(emptyList()) }
    var movingCategory by remember { mutableStateOf<CategoriesTabelleECB?>(null) }
    var heldCategory by remember { mutableStateOf<CategoriesTabelleECB?>(null) }
    var filterText by remember { mutableStateOf("") }
    var reorderMode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Search field
                SearchField(
                    filterText = filterText,
                    onFilterTextChange = { filterText = it }
                )

                // Filter categories based on search text
                val filteredCategories = remember(uiState.categoriesECB, filterText) {
                    if (filterText.isBlank()) {
                        uiState.categoriesECB
                    } else {
                        uiState.categoriesECB.filter {
                            it.nomCategorieInCategoriesTabele.contains(
                                filterText,
                                ignoreCase = true
                            )
                        }
                    }
                }

                // Category grid
                Box(modifier = Modifier.weight(1f)) {
                    CategoryGridFragID_1(
                        categories = filteredCategories,
                        selectedCategories = selectedCategories,
                        movingCategory = movingCategory,
                        heldCategory = heldCategory,
                        reorderMode = reorderMode,
                        onCategoryClick = { category ->
                            handleCategoryClick(
                                category = category,
                                filterText = filterText,
                                viewModel = viewModel,
                                renameOrFusionMode = renameOrFusionMode,
                                multiSelectionMode = multiSelectionMode,
                                reorderMode = reorderMode,
                                heldCategory = heldCategory,
                                selectedCategories = selectedCategories,
                                movingCategory = movingCategory,
                                onHeldCategoryChange = { heldCategory = it },
                                onSelectedCategoriesChange = { selectedCategories = it },
                                onRenameOrFusionModeChange = { renameOrFusionMode = it },
                                onMovingCategoryChange = { movingCategory = it },
                                onReorderModeChange = { reorderMode = it },
                                onCategorySelected = onCategorySelected,
                                onDismiss = onDismiss
                            )
                        }
                    )
                }

                BottomActions(
                    multiSelectionMode = multiSelectionMode,
                    renameOrFusionMode = renameOrFusionMode,
                    selectedCategories = selectedCategories,
                    movingCategory = movingCategory,
                    reorderMode = reorderMode,
                    onMultiSelectionModeChange = { newMode ->
                        multiSelectionMode = newMode
                        if (!newMode) {
                            selectedCategories = emptyList()
                            movingCategory = null
                            renameOrFusionMode = false
                            heldCategory = null
                            reorderMode = false
                        }
                    },
                    onRenameOrFusionModeChange = { newMode ->
                        renameOrFusionMode = newMode
                        if (!newMode) {
                            heldCategory = null
                            multiSelectionMode = false
                            selectedCategories = emptyList()
                            movingCategory = null
                            reorderMode = false
                        }
                    },
                    onReorderModeActivate = {
                        reorderMode = true
                    },
                    onCancelMove = {
                        movingCategory = null
                        heldCategory = null
                        renameOrFusionMode = false
                        reorderMode = false
                    }
                )
            }
        }
    }
}
