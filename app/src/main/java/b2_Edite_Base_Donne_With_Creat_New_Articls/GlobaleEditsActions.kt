package b2_Edite_Base_Donne_With_Creat_New_Articls

import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import h_FactoryClassemntsArticles.AutoResizedTextClas

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
                    icon = if (showModeClickDispo) Icons.Default.Close else Icons.Default.Person,
                    contentDescription = "Mode Click Dispo",
                    showContentDescription = showContentDescription,
                    onClick = {
                        onToggleModeClickDispo()
                        showModeClickDispo = !showModeClickDispo
                    },
                    color = Color(0xFFE91E63) // Pink
                )

                if (!showModeClickDispo) {
                    val buttons = listOf(
                        Quadruple(Icons.Default.CalendarViewMonth, "Category Selection", Color(0xFF9C27B0)) { showCategorySelection = true }, // Purple
                        Quadruple(Icons.Default.EditCalendar, "Outline Filter", Color(0xFF2196F3), onToggleOutlineFilter), // Blue
                        Quadruple(Icons.Default.Home, "Home", Color(0xFF4CAF50), onToggleNavBar), // Green
                        Quadruple(if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff, "Filter", Color(0xFFFFC107), onToggleFilter), // Amber
                        Quadruple(Icons.Default.PermMedia, "Database Editor", Color(0xFFFF5722)) { showDialogeDataBaseEditer = true }, // Deep Orange
                        Quadruple(Icons.Default.GridView, "Change Grid", Color(0xFF795548)) { // Brown
                            currentGridColumns = (currentGridColumns % maxGridColumns) + 1
                            onChangeGridColumns(currentGridColumns)
                        },
                        Quadruple(if (showContentDescription) Icons.Default.Close else Icons.Default.Details, "Toggle Description", Color(0xFF607D8B)) { // Blue Grey
                            showContentDescription = !showContentDescription
                        }
                    )

                    buttons.forEach { (icon, contentDescription, color, onClick) ->
                        FloatingButton(
                            icon = icon,
                            contentDescription = contentDescription,
                            showContentDescription = showContentDescription,
                            onClick = onClick,
                            color = color
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onToggleFloatingButtons,
            containerColor = Color(0xFF3F51B5) // Indigo
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
    icon: ImageVector,
    contentDescription: String,
    showContentDescription: Boolean,
    onClick: () -> Unit,
    color: Color
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
                        text = contentDescription,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            containerColor = color
        ) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun CategoryReorderAndSelectionWindow(
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    onCategorySelected: (CategoriesTabelleECB) -> Unit,
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
) {
    var multiSelectionMode by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf<List<CategoriesTabelleECB>>(emptyList()) }
    var movingCategory by remember { mutableStateOf<CategoriesTabelleECB?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Select Category",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.categoriesECB) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = category in selectedCategories,
                            isMoving = category == movingCategory,
                            onClick = {
                                when {
                                    multiSelectionMode -> selectedCategories =
                                        selectedCategories.toMutableList().apply {
                                            if (contains(category)) remove(category) else add(category)
                                        }
                                    movingCategory != null -> {
                                        viewModel.moveCategory(movingCategory!!.idCategorieInCategoriesTabele, category.idCategorieInCategoriesTabele)
                                        movingCategory = null
                                    }
                                    else -> {
                                        onCategorySelected(category)
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {
                        multiSelectionMode = !multiSelectionMode
                        if (!multiSelectionMode) selectedCategories = emptyList()
                        movingCategory = null
                    }) {
                        Text(if (multiSelectionMode) "Cancel" else "Select Multiple")
                    }
                    if (multiSelectionMode) {
                        Button(
                            onClick = {
                                if (selectedCategories.size >= 2) {
                                    val fromCategory = selectedCategories.first()
                                    val toCategory = selectedCategories.last()
                                    viewModel.reorderCategories(
                                        fromCategoryId = fromCategory.idCategorieInCategoriesTabele,
                                        toCategoryId = toCategory.idCategorieInCategoriesTabele
                                    )
                                }
                                multiSelectionMode = false
                                selectedCategories = emptyList()
                            },
                            enabled = selectedCategories.size >= 2
                        ) {
                            Text("Reorder")
                        }
                    } else if (movingCategory != null) {
                        Button(onClick = { movingCategory = null }) {
                            Text("Cancel Move")
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun CategoryItem(
    category: CategoriesTabelleECB,
    isSelected: Boolean,
    isMoving: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isMoving -> MaterialTheme.colorScheme.secondary
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.primary
            }
        )
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AutoResizedTextClas(category.nomCategorieInCategoriesTabele, )
        }
    }
}
