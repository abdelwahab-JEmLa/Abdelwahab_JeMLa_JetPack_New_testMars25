package b2_Edite_Base_Donne_With_Creat_New_Articls

import a_MainAppCompnents.CategoriesTabelleECB
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun CategoryReorderAndSelectionWindow(
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
                            it.nomCategorieInCategoriesTabele.contains(filterText, ignoreCase = true)
                        }
                    }
                }

                // Category grid
                Box(modifier = Modifier.weight(1f)) {
                    CategoryGrid(
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

@Composable
private fun BottomActions(
    multiSelectionMode: Boolean,
    renameOrFusionMode: Boolean,
    selectedCategories: List<CategoriesTabelleECB>,
    movingCategory: CategoriesTabelleECB?,
    reorderMode: Boolean,
    onMultiSelectionModeChange: (Boolean) -> Unit,
    onRenameOrFusionModeChange: (Boolean) -> Unit,
    onReorderModeActivate: () -> Unit,
    onCancelMove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = { onMultiSelectionModeChange(!multiSelectionMode) },
            enabled = !renameOrFusionMode && movingCategory == null
        ) {
            Icon(
                imageVector = if (multiSelectionMode) Icons.Default.Clear else Icons.Default.CheckBox,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (multiSelectionMode) "Cancel" else "Select")
        }

        OutlinedButton(
            onClick = { onRenameOrFusionModeChange(!renameOrFusionMode) },
            enabled = !multiSelectionMode && movingCategory == null
        ) {
            Icon(
                imageVector = if (renameOrFusionMode) Icons.Default.Clear else Icons.Default.Merge,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (renameOrFusionMode) "Cancel" else "Merge")
        }

        if (selectedCategories.size >= 2 && !reorderMode) {
            Button(
                onClick = onReorderModeActivate
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reo")
            }
        }

        if (reorderMode) {
            Button(onClick = onCancelMove) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cancel")
            }
        }
    }
}
@Composable
private fun SearchField(
    filterText: String,
    onFilterTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = filterText,
        onValueChange = onFilterTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        placeholder = { Text("Filter or new category") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true
    )
}

private fun handleCategoryClick(
    category: CategoriesTabelleECB,
    filterText: String,
    viewModel: HeadOfViewModels,
    renameOrFusionMode: Boolean,
    multiSelectionMode: Boolean,
    reorderMode: Boolean,
    heldCategory: CategoriesTabelleECB?,
    selectedCategories: List<CategoriesTabelleECB>,
    movingCategory: CategoriesTabelleECB?,
    onHeldCategoryChange: (CategoriesTabelleECB?) -> Unit,
    onSelectedCategoriesChange: (List<CategoriesTabelleECB>) -> Unit,
    onRenameOrFusionModeChange: (Boolean) -> Unit,
    onMovingCategoryChange: (CategoriesTabelleECB?) -> Unit,
    onReorderModeChange: (Boolean) -> Unit,
    onCategorySelected: (CategoriesTabelleECB) -> Unit,
    onDismiss: () -> Unit
) {
    when {
        category.nomCategorieInCategoriesTabele == "Add New Category" -> {
            if (filterText.isNotBlank()) {
                viewModel.addNewCategory(filterText)
            }
        }
        reorderMode -> {
            // Déplace toutes les catégories sélectionnées après la catégorie cliquée
            selectedCategories.forEach { selectedCategory ->
                viewModel.handleCategoryMove(
                    holdedIdCate = selectedCategory.idCategorieInCategoriesTabele,
                    clickedCategoryId = category.idCategorieInCategoriesTabele
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
                    fromCategoryId = heldCategory.idCategorieInCategoriesTabele,
                    toCategoryId = category.idCategorieInCategoriesTabele
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
                holdedIdCate = movingCategory.idCategorieInCategoriesTabele,
                clickedCategoryId = category.idCategorieInCategoriesTabele
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

@Composable
private fun CategoryGrid(
    categories: List<CategoriesTabelleECB>,
    selectedCategories: List<CategoriesTabelleECB>,
    movingCategory: CategoriesTabelleECB?,
    heldCategory: CategoriesTabelleECB?,
    reorderMode: Boolean,
    onCategoryClick: (CategoriesTabelleECB) -> Unit,
    modifier: Modifier = Modifier
) {
    val addNewCategory = CategoriesTabelleECB(
        idCategorieInCategoriesTabele = (categories.maxOfOrNull { it.idCategorieInCategoriesTabele } ?: 0) + 1,
        nomCategorieInCategoriesTabele = "Add New Category",
        idClassementCategorieInCategoriesTabele = 0
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
    ) {
        items(listOf(addNewCategory) + categories) { category ->
            CategoryItem(
                category = category,
                isSelected = category in selectedCategories,
                isMoving = category == movingCategory,
                isHeld = category == heldCategory,
                isReorderTarget = reorderMode && category !in selectedCategories,
                selectionOrder = selectedCategories.indexOf(category) + 1,
                onClick = { onCategoryClick(category) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoriesTabelleECB,
    isSelected: Boolean,
    isMoving: Boolean,
    isHeld: Boolean,
    isReorderTarget: Boolean,
    selectionOrder: Int,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isHeld -> MaterialTheme.colorScheme.primaryContainer
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isMoving -> MaterialTheme.colorScheme.tertiaryContainer
        isReorderTarget -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        category.nomCategorieInCategoriesTabele == "Add New Category" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .border(
                BorderStroke(
                    width = if (isSelected || isHeld || isMoving) 2.dp else 1.dp,
                    color = when {
                        isHeld -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.secondary
                        isMoving -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    }
                ),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (selectionOrder > 0) {
                Text(
                    text = selectionOrder.toString(),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.small
                        )
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                text = category.nomCategorieInCategoriesTabele,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )

            if (category.nomCategorieInCategoriesTabele == "Add New Category") {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
