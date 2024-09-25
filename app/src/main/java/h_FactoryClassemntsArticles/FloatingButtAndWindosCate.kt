package h_FactoryClassemntsArticles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.window.Dialog
import com.example.abdelwahabjemlajetpack.DialogButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun FloatingActionButtons(
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    categories: List<CategoriesTabelle>,
    onCategorySelected: (CategoriesTabelle) -> Unit,
    viewModel: ClassementsArticlesViewModel,
    coroutineScope: CoroutineScope,
    onUpdateProgress: (Float) -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateComplete: () -> Unit,

    ) {
    var showCategorySelection by remember { mutableStateOf(false) }
    var showDialogeDataBaseEditer by remember { mutableStateOf(false) }

    Column {
        if (showFloatingButtons) {
            FloatingActionButton(
                onClick = { showCategorySelection = true },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Category, "Select Category")
            }
            FloatingActionButton(
                onClick = onToggleNavBar,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Default.Home, "Toggle Navigation Bar")
            }
            FloatingActionButton(
                onClick = onToggleFilter,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff,
                    "Toggle Filter"
                )
            }

            FloatingActionButton(
                onClick = { showDialogeDataBaseEditer = true },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    if (showDialogeDataBaseEditer) Icons.Default.Close else Icons.Default.PermMedia,
                    "Dialoge"
                )
            }

        }
        FloatingActionButton(onClick = onToggleFloatingButtons) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                if (showFloatingButtons) "Hide Buttons" else "Show Buttons"
            )
        }
    }

    if (showCategorySelection) {
        CategorySelectionWindow(
            categories = categories,
            viewModel = viewModel,
            onDismiss = { showCategorySelection = false },
            onCategorySelected = { category ->
                onCategorySelected(category)
                showCategorySelection = false
            }
        )
    }
    if (showDialogeDataBaseEditer) {
        DialogeDataBaseEditer(
            viewModel = viewModel,
            onDismiss = { showDialogeDataBaseEditer = false },
            coroutineScope=coroutineScope  ,
            onUpdateStart=onUpdateStart,
            onUpdateProgress=onUpdateProgress,
            onUpdateComplete=onUpdateComplete,

        )
    }

}

@Composable
fun DialogeDataBaseEditer(
    onDismiss: () -> Unit,
    viewModel: ClassementsArticlesViewModel,
    coroutineScope: CoroutineScope,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit ,
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showUpdateConfirmationDialog by remember { mutableStateOf(false) }

    if (showConfirmationDialog) {
        ConfirmationDialog(
            onDismiss = { showConfirmationDialog = false },
            onConfirm = {
                coroutineScope.launch {
                    viewModel.delete()
                }
                showConfirmationDialog = false
                onDismiss()
            }
        )
    }

    if (showUpdateConfirmationDialog) {
        ConfirmationDialog(
            onDismiss = { showUpdateConfirmationDialog = false },
            onConfirm = {
                coroutineScope.launch {
                    viewModel.updateCategorieTabelee()
                }
                showUpdateConfirmationDialog = false
                onDismiss()
            }
        )
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Firebase Data",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DialogButton(
                    text = "updateCategorieTabelee",
                    icon = Icons.Default.Upload,
                    onClick = {
                        showUpdateConfirmationDialog = true
                    },
                    tint2 = Color.Red
                )

                HorizontalDivider(color = Color.Red,thickness=5.dp, modifier = Modifier.padding(8.dp))

                // DialogButton
                DialogButton(
                    text = "giveNumAuSubCategorieArticle",
                    icon = Icons.Default.Mode,
                    onClick = {
                        coroutineScope.launch {
                            viewModel.giveNumAuSubCategorieArticle()
                        }
                    },
                    tint2 = Color.Red
                )

                DialogButton(
                    text = "Delete Ref Classment ",
                    icon = Icons.Default.Delete,
                    onClick = {
                        showConfirmationDialog = true
                    },
                    tint2 = Color.Blue
                )

                HorizontalDivider(color = Color.Blue,thickness=5.dp, modifier = Modifier.padding(8.dp))

                DialogButton(
                    text = "updateChangeInClassmentToe_DBJetPackExport",
                    icon = Icons.Default.Refresh,
                    onClick = {
                        coroutineScope.launch {
                            onUpdateStart()
                            onDismiss()
                            try {
                                viewModel.updateChangeInClassmentToe_DBJetPackExport { progress ->
                                    onUpdateProgress(progress)
                                }
                            } finally {
                                onUpdateComplete()
                            }
                        }
                    },
                    tint2 = Color.Black
                )
            }

        },
    )
}

@Composable
fun ConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(text = "Confirm Action")
        },
        text = {
            Text(text = "Are you sure you want to proceed?")
        },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun CategoryItem(
    category: CategoriesTabelle,
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
            AutoResizedTextFC(category.nomCategorieInCategoriesTabele, maxLines = 2)
        }
    }
}
@Composable
fun AutoResizedTextFC(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center,
    bodyLarge: Boolean = false,
    maxLines: Int = Int.MAX_VALUE
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize =
        if (bodyLarge) MaterialTheme.typography.bodyLarge.fontSize else MaterialTheme.typography.bodyMedium.fontSize

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.drawWithContent {
                if (shouldDraw) drawContent()
            },
            softWrap = true,
            style = resizedTextStyle,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    if (style.fontSize.isUnspecified) {
                        resizedTextStyle = resizedTextStyle.copy(fontSize = defaultFontSize)
                    }
                    resizedTextStyle =
                        resizedTextStyle.copy(fontSize = resizedTextStyle.fontSize * 0.95)
                } else {
                    shouldDraw = true
                }
            }
        )
    }
}

@Composable
fun CategorySelectionWindow(
    categories: List<CategoriesTabelle>,
    onDismiss: () -> Unit,
    viewModel: ClassementsArticlesViewModel,
    onCategorySelected: (CategoriesTabelle) -> Unit,

    ) {
    var multiSelectionMode by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf<List<CategoriesTabelle>>(emptyList()) }
    var movingCategory by remember { mutableStateOf<CategoriesTabelle?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
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
                    items(categories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = category in selectedCategories,
                            isMoving = category == movingCategory,
                            onClick = {
                                if (multiSelectionMode) {
                                    selectedCategories = if (category in selectedCategories) {
                                        selectedCategories - category
                                    } else {
                                        selectedCategories + category
                                    }
                                } else if (movingCategory != null) {
                                    viewModel.moveCategory(movingCategory!!, category)
                                    movingCategory = null
                                } else {
                                    onCategorySelected(category)
                                    onDismiss()
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
                    Button(
                        onClick = {
                            multiSelectionMode = !multiSelectionMode
                            if (!multiSelectionMode) {
                                selectedCategories = emptyList()
                            }
                            movingCategory = null
                        }
                    ) {
                        Text(if (multiSelectionMode) "Cancel" else "Select Multiple")
                    }
                    if (multiSelectionMode) {
                        Button(
                            onClick = {
                                viewModel.reorderCategories(selectedCategories)
                                multiSelectionMode = false
                                selectedCategories = emptyList()
                            },
                            enabled = selectedCategories.isNotEmpty()
                        ) {
                            Text("Reorder")
                        }
                    } else if (movingCategory != null) {
                        Button(
                            onClick = {
                                movingCategory = null
                            }
                        ) {
                            Text("Cancel Move")
                        }
                    }
                }
            }
        }
    }
}
