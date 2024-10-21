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
import androidx.compose.material.icons.filled.Refresh
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
    onToggleModeClickDispo: () -> Unit  ,
    onCategorySelected: (CategoriesTabelleECB) -> Unit

) {
    var showDialogeDataBaseEditer by remember { mutableStateOf(false) }
    var currentGridColumns by remember { mutableIntStateOf(2) }
    var showContentDescription by remember { mutableStateOf(false) }
    var showModeClickDispo by remember { mutableStateOf(false) }
    val maxGridColumns = 4
    var showCategorySelection by remember { mutableStateOf(false) }

    Column {
        if (showFloatingButtons) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                // ModeClickDispo button is always shown when showFloatingButtons is true
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
                                    text = "Mode Click Dispo",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    FloatingActionButton(
                        onClick = {
                            onToggleModeClickDispo()
                            showModeClickDispo = !showModeClickDispo
                        },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            if (showModeClickDispo) Icons.Default.Close else Icons.Default.Person,
                            contentDescription = "Mode Click Dispo"
                        )
                    }
                }

                // Other buttons are only shown when ModeClickDispo is not active
                if (!showModeClickDispo) {
                    val buttons = listOf(
                        Triple(Icons.Default.Refresh, "showDialogeDataBaseEditer") {
                            showDialogeDataBaseEditer = !showDialogeDataBaseEditer
                        },
                        Triple(Icons.Default.EditCalendar, "Outline Filter", onToggleOutlineFilter),
                        Triple(Icons.Default.Home, "Home", onToggleNavBar),
                        Triple(if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff, "Filter", onToggleFilter),
                        Triple(Icons.Default.PermMedia, "Database Editor") {
                            showDialogeDataBaseEditer = true
                        },
                        Triple(Icons.Default.GridView, "Change Grid") {
                            currentGridColumns = (currentGridColumns % maxGridColumns) + 1
                            onChangeGridColumns(currentGridColumns)
                        },
                        Triple(if (showContentDescription) Icons.Default.Close else Icons.Default.Details, "Toggle Description") {
                            showContentDescription = !showContentDescription
                        }
                    )

                    buttons.forEach { (icon, contentDescription, onClick) ->
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
                                modifier = Modifier.size(56.dp)
                            ) {
                                Icon(icon, contentDescription = contentDescription)
                            }
                        }
                    }
                }
            }
        }

        // Toggle button is always visible
        FloatingActionButton(onClick = onToggleFloatingButtons) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Floating Buttons"
            )
        }
    }
    if (showCategorySelection) {
        CategoryReorderAndSelectionWindow(
            uiState=uiState,
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
