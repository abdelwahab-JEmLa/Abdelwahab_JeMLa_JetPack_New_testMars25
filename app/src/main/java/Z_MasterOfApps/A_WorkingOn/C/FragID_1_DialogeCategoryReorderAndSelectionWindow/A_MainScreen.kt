package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow

import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.UI.BottonsActions
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.UI.SearchField_A4F1
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.UI.handleCategoryClick_F1
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.I_CategoriesProduits
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.ViewModel_A4FragID1
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
import org.koin.androidx.compose.koinViewModel

@Composable
fun A_MainScreen_SectionID4_FragmentID1(
    viewModel: ViewModel_A4FragID1 = koinViewModel(),
    onDismiss: () -> Unit,
) {
    val i_Categories = viewModel.i_CategoriesProduits

    var multiSelectionMode by remember { mutableStateOf(false) }
    var renameOrFusionMode by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf<List<I_CategoriesProduits>>(emptyList()) }
    var movingCategory by remember { mutableStateOf<I_CategoriesProduits?>(null) }
    var heldCategory by remember { mutableStateOf<I_CategoriesProduits?>(null) }
    var filterText by remember { mutableStateOf("") }
    var reorderMode by remember { mutableStateOf(false) }

    val onMultiSelectionModeChange: (Boolean) -> Unit = { newMode ->
        multiSelectionMode = newMode
        if (!newMode) {
            selectedCategories = emptyList()
            movingCategory = null
            renameOrFusionMode = false
            heldCategory = null
            reorderMode = false
        }
    }

    val onRenameOrFusionModeChange: (Boolean) -> Unit = { newMode ->
        renameOrFusionMode = newMode
        if (!newMode) {
            heldCategory = null
            multiSelectionMode = false
            selectedCategories = emptyList()
            movingCategory = null
            reorderMode = false
        }
    }

    val onReorderModeActivate: () -> Unit = {
        reorderMode = true
    }

    val onCancelMove: () -> Unit = {
        movingCategory = null
        heldCategory = null
        renameOrFusionMode = false
        reorderMode = false
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                SearchField_A4F1(
                    filterText = filterText,
                    onFilterTextChange = { filterText = it }
                )

                val filteredCategories = remember(i_Categories, filterText) {
                    if (filterText.isBlank()) {
                        i_Categories
                    } else {
                        i_Categories.filter {
                            it.infosDeBase.nom.contains(
                                filterText,
                                ignoreCase = true
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    B_MainList_A4FragID_1(
                        viewModel=viewModel,
                        categories = filteredCategories,
                        selectedCategories = selectedCategories,
                        movingCategory = movingCategory,
                        heldCategory = heldCategory,
                        reorderMode = reorderMode,
                        onCategoryClick = { category ->
                            handleCategoryClick_F1(
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
                                onCategorySelected = {},
                                onDismiss = {}
                            )
                        }
                    )
                }

                BottonsActions(
                    multiSelectionMode = multiSelectionMode,
                    renameOrFusionMode = renameOrFusionMode,
                    selectedCategories = selectedCategories,
                    movingCategory = movingCategory,
                    reorderMode = reorderMode,
                    onMultiSelectionModeChange = onMultiSelectionModeChange,
                    onRenameOrFusionModeChange = onRenameOrFusionModeChange,
                    onReorderModeActivate = onReorderModeActivate,
                    onCancelMove = onCancelMove
                )
            }
        }
    }
}
