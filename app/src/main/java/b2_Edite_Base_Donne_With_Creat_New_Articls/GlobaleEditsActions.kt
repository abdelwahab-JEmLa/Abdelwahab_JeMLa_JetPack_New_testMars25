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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

data class ButtonInfo(
    val icon: ImageVector,
    val description: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
fun FloatingActionButtons(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    onToggleOutlineFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    viewModel: HeadOfViewModels,
    onChangeGridColumns: (Int) -> Unit,
    onToggleModeClickDispo: () -> Unit,
    onCategorySelected: (CategoriesTabelleECB) -> Unit
) {
    var showDialogeDataBaseEditer by remember { mutableStateOf(false) }
    var currentGridColumns by remember { mutableIntStateOf(2) }
    var showContentDescription by remember { mutableStateOf(false) }
    var showModeClickDispo by remember { mutableStateOf(false) }
    var showCategorySelection by remember { mutableStateOf(false) }
    val maxGridColumns = 4

    Column {
        if (showFloatingButtons) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                FloatingButton(
                    ButtonInfo(
                        icon = if (showModeClickDispo) Icons.Default.Close else Icons.Default.Person,
                        description = "Mode Click Dispo",
                        color = Color(0xFFE91E63),
                        onClick = {
                            onToggleModeClickDispo()
                            showModeClickDispo = !showModeClickDispo
                        }
                    ),
                    showContentDescription
                )

                if (!showModeClickDispo) {
                    val buttons = listOf(
                        ButtonInfo(Icons.Default.CalendarViewMonth, "Category Selection", Color(0xFF9C27B0)) { showCategorySelection = true },
                        ButtonInfo(Icons.Default.EditCalendar, "Outline Filter", Color(0xFF2196F3), onToggleOutlineFilter),
                        ButtonInfo(Icons.Default.Home, "Home", Color(0xFF4CAF50), onToggleNavBar),
                        ButtonInfo(if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff, "Filter", Color(0xFFFFC107), onToggleFilter),
                        ButtonInfo(Icons.Default.PermMedia, "Database Editor", Color(0xFFFF5722)) { showDialogeDataBaseEditer = true },
                        ButtonInfo(Icons.Default.GridView, "Change Grid", Color(0xFF795548)) {
                            currentGridColumns = (currentGridColumns % maxGridColumns) + 1
                            onChangeGridColumns(currentGridColumns)
                        },
                        ButtonInfo(if (showContentDescription) Icons.Default.Close else Icons.Default.Details, "Toggle Description", Color(0xFF607D8B)) {
                            showContentDescription = !showContentDescription
                        }
                    )

                    buttons.forEach { buttonInfo ->
                        FloatingButton(buttonInfo, showContentDescription)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleFloatingButtons,
            containerColor = Color(0xFF3F51B5)
        ) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Floating Buttons"
            )
        }
    }

    if (showCategorySelection) {
        CategoryReorderAndSelectionWindow(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showCategorySelection = false },
            onCategorySelected = { category ->
                onCategorySelected(category)
                showCategorySelection = false
            }
        )
    }
}

@Composable
private fun FloatingButton(
    buttonInfo: ButtonInfo,
    showContentDescription: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        if (showContentDescription) {
            Card(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .heightIn(min = 30.dp)
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = buttonInfo.description,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = buttonInfo.onClick,
            modifier = Modifier.size(56.dp),
            containerColor = buttonInfo.color
        ) {
            Icon(buttonInfo.icon, contentDescription = buttonInfo.description)
        }
    }
}

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        )    {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                SearchField(
                    filterText = filterText,
                    onFilterTextChange = { filterText = it }
                )

                val filteredCategories = remember(uiState.categoriesECB, filterText) {
                    if (filterText.isBlank()) {
                        uiState.categoriesECB
                    } else {
                        uiState.categoriesECB.filter {
                            it.nomCategorieInCategoriesTabele.contains(filterText, ignoreCase = true)
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    CategoryGrid(
                        categories = filteredCategories,
                        selectedCategories = selectedCategories,
                        movingCategory = movingCategory,
                        heldCategory = heldCategory,
                        onCategoryClick = { category ->
                            handleCategoryClick(
                                category = category,
                                filterText = filterText,
                                viewModel = viewModel,
                                renameOrFusionMode = renameOrFusionMode,
                                multiSelectionMode = multiSelectionMode,
                                heldCategory = heldCategory,
                                selectedCategories = selectedCategories,
                                movingCategory = movingCategory,
                                onHeldCategoryChange = { heldCategory = it },
                                onSelectedCategoriesChange = { selectedCategories = it },
                                onRenameOrFusionModeChange = { renameOrFusionMode = it },
                                onMovingCategoryChange = { movingCategory = it },
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
                    onMultiSelectionModeChange = { newMode ->
                        multiSelectionMode = newMode
                        if (!newMode) {
                            selectedCategories = emptyList()
                            movingCategory = null
                            renameOrFusionMode = false
                            heldCategory = null
                        }
                    },
                    onRenameOrFusionModeChange = { newMode ->
                        renameOrFusionMode = newMode
                        if (!newMode) {
                            heldCategory = null
                            multiSelectionMode = false
                            selectedCategories = emptyList()
                            movingCategory = null
                        }
                    },
                    onReorderCategories = { fromCategory, toCategory ->
                        viewModel.handleCategoryMove(
                            holdedIdCate = fromCategory.idCategorieInCategoriesTabele,
                            clickedCategoryId = toCategory.idCategorieInCategoriesTabele
                        ) {
                            multiSelectionMode = false
                            selectedCategories = emptyList()
                        }
                    },
                    onCancelMove = {
                        movingCategory = null
                        heldCategory = null
                        renameOrFusionMode = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DialogHeader(
    title: String,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "Close")
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

@Composable
private fun CategoryGrid(
    categories: List<CategoriesTabelleECB>,
    selectedCategories: List<CategoriesTabelleECB>,
    movingCategory: CategoriesTabelleECB?,
    heldCategory: CategoriesTabelleECB?,
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
    selectionOrder: Int,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isHeld -> MaterialTheme.colorScheme.primaryContainer
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isMoving -> MaterialTheme.colorScheme.tertiaryContainer
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

@Composable
private fun BottomActions(
    multiSelectionMode: Boolean,
    renameOrFusionMode: Boolean,
    selectedCategories: List<CategoriesTabelleECB>,
    movingCategory: CategoriesTabelleECB?,
    onMultiSelectionModeChange: (Boolean) -> Unit,
    onRenameOrFusionModeChange: (Boolean) -> Unit,
    onReorderCategories: (CategoriesTabelleECB, CategoriesTabelleECB) -> Unit,
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
            Text(if (multiSelectionMode) "C" else "Mul")
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
            Text(if (renameOrFusionMode) "C" else "Merg")
        }

        if (selectedCategories.size >= 2) {
            Button(
                onClick = {
                    onReorderCategories(selectedCategories[0], selectedCategories[1])
                }
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reo")
            }
        }

        if (movingCategory != null) {
            Button(onClick = onCancelMove) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Mov")
            }
        }
    }
}

private fun handleCategoryClick(
    category: CategoriesTabelleECB,
    filterText: String,
    viewModel: HeadOfViewModels,
    renameOrFusionMode: Boolean,
    multiSelectionMode: Boolean,
    heldCategory: CategoriesTabelleECB?,
    selectedCategories: List<CategoriesTabelleECB>,
    movingCategory: CategoriesTabelleECB?,
    onHeldCategoryChange: (CategoriesTabelleECB?) -> Unit,
    onSelectedCategoriesChange: (List<CategoriesTabelleECB>) -> Unit,
    onRenameOrFusionModeChange: (Boolean) -> Unit,
    onMovingCategoryChange: (CategoriesTabelleECB?) -> Unit,
    onCategorySelected: (CategoriesTabelleECB) -> Unit,
    onDismiss: () -> Unit
) {
    when {
        category.nomCategorieInCategoriesTabele == "Add New Category" -> {
            if (filterText.isNotBlank()) {
                viewModel.addNewCategory(filterText)
            }
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
                    selectedCategories - category
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



