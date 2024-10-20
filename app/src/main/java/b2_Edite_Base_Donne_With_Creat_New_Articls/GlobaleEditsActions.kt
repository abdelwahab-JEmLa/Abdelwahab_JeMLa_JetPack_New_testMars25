package b2_Edite_Base_Donne_With_Creat_New_Articls

import a_MainAppCompnents.CategoriesTabelleECB
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.wear.compose.material.Button
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FloatingActionButtons(
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    onToggleOutlineFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    viewModel: HeadOfViewModels,
    coroutineScope: CoroutineScope,
    onChangeGridColumns: (Int) -> Unit,
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    onToggleModeClickDispo: () -> Unit
) {
    var showCategorySelection by remember { mutableStateOf(false) }
    var showDialogeDataBaseEditer by remember { mutableStateOf(false) }
    var currentGridColumns by remember { mutableIntStateOf(2) }
    val maxGridColumns = 4
    var showContentDescription by remember { mutableStateOf(false) }
    var showonToggleModeClickDispo by remember { mutableStateOf(false) }

    Column {
        if (showFloatingButtons&&!showonToggleModeClickDispo) {
            Triple(if (showonToggleModeClickDispo) Icons.Default.Close else Icons.Default.Person, "onToggleModeClickDispo")
            { onToggleModeClickDispo ()
                showonToggleModeClickDispo=!showonToggleModeClickDispo
            }
        }

        if (showFloatingButtons) {
            val buttons = listOf(

                Triple(Icons.Default.EditCalendar, "outline Filter", onToggleOutlineFilter),
                Triple(Icons.Default.Category, "Category") { showCategorySelection = true },
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

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
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
                            onClick = onClick ,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(icon, contentDescription = contentDescription)
                        }
                    }
                }
            }
        }
        FloatingActionButton(onClick = onToggleFloatingButtons) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Floating Buttons"
            )
        }
    }

    AddCategoryDialog(
        showDialog = showCategorySelection,
        onDismiss = { showCategorySelection = false },
        onAddCategory = { newCategoryName ->
            coroutineScope.launch {
                val maxId = uiState.categoriesECB.maxByOrNull { it.idCategorieInCategoriesTabele }?.idCategorieInCategoriesTabele ?: 0
                val newCategory = CategoriesTabelleECB(
                    idCategorieInCategoriesTabele = maxId + 1,
                    idClassementCategorieInCategoriesTabele = 1.0,
                    nomCategorieInCategoriesTabele = newCategoryName
                )
                viewModel.addNewCategory(newCategory)
            }
        },
    )
}




@Composable
fun AddCategoryDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onAddCategory: (String) -> Unit,
) {
    var categoryName by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add New Category") },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            onAddCategory(categoryName)
                            onDismiss()
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
