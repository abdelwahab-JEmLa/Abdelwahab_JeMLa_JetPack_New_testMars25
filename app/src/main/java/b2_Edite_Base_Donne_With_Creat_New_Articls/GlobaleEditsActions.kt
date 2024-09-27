package b2_Edite_Base_Donne_With_Creat_New_Articls

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.abdelwahabjemlajetpack.DialogButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun FloatingActionButtons(
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    viewModel: ClassementsArticlesViewModel,
    coroutineScope: CoroutineScope,
    onUpdateProgress: (Float) -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateComplete: () -> Unit,
    onChangeGridColumns: (Int) -> Unit
) {
    var showCategorySelection by remember { mutableStateOf(false) }
    var showDialogeDataBaseEditer by remember { mutableStateOf(false) }

    Column {
        if (showFloatingButtons) {
            FloatingActionButtonGroup(
                onCategorySelectionClick = { showCategorySelection = true },
                onToggleNavBar = onToggleNavBar,
                onToggleFilter = onToggleFilter,
                showOnlyWithFilter = showOnlyWithFilter,
                onDialogDataBaseEditerClick = { showDialogeDataBaseEditer = true },
                showDialogeDataBaseEditer = showDialogeDataBaseEditer,
                onChangeGridColumns = onChangeGridColumns
            )
        }
        FloatingActionButton(onClick = onToggleFloatingButtons) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null
            )
        }
    }


    if (showDialogeDataBaseEditer) {
        DialogeDataBaseEditer(
            viewModel = viewModel,
            onDismiss = { showDialogeDataBaseEditer = false },
            coroutineScope = coroutineScope,
            onUpdateStart = onUpdateStart,
            onUpdateProgress = onUpdateProgress,
            onUpdateComplete = onUpdateComplete,
        )
    }
}

@Composable
fun FloatingActionButtonGroup(
    onCategorySelectionClick: () -> Unit,
    onToggleNavBar: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    onDialogDataBaseEditerClick: () -> Unit,
    showDialogeDataBaseEditer: Boolean,
    onChangeGridColumns: (Int) -> Unit
) {
    FloatingActionButton(
        onClick = onCategorySelectionClick,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(Icons.Default.Category, null)
    }
    FloatingActionButton(
        onClick = onToggleNavBar,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(Icons.Default.Home, null)
    }
    FloatingActionButton(
        onClick = onToggleFilter,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff,
            null
        )
    }
    FloatingActionButton(
        onClick = onDialogDataBaseEditerClick,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            if (showDialogeDataBaseEditer) Icons.Default.Close else Icons.Default.PermMedia,
            null
        )
    }
    FloatingActionButton(
        onClick = { onChangeGridColumns(if (Random.nextBoolean()) 3 else 4 ) },   //TODO ajoute else 5 et 2
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(Icons.Default.GridView, null)
    }
}

@Composable
fun DialogeDataBaseEditer(
    onDismiss: () -> Unit,
    viewModel: ClassementsArticlesViewModel,
    coroutineScope: CoroutineScope,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit,
) {
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showUpdateConfirmationDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Firebase Data",
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
                    "updateCategorieTabelee",
                    Icons.Default.Upload,
                    { showUpdateConfirmationDialog = true },
                    Color.Red
                )
                DialogButton(
                    "giveNumAuSubCategorieArticle",
                    Icons.Default.Mode,
                    { coroutineScope.launch { viewModel.giveNumAuSubCategorieArticle() } },
                    Color.Red
                )
                HorizontalDivider(
                    color = Color.Red,
                    thickness = 5.dp,
                    modifier = Modifier.padding(8.dp)
                )
                DialogButton(
                    "Delete Ref Classment",
                    Icons.Default.Delete,
                    { showConfirmationDialog = true },
                    Color.Blue
                )
                HorizontalDivider(
                    color = Color.Blue,
                    thickness = 5.dp,
                    modifier = Modifier.padding(8.dp)
                )
                DialogButton("updateChangeInClassmentToeDBJetPackExport", Icons.Default.Refresh, {
                    coroutineScope.launch {
                        onUpdateStart()
                        onDismiss()
                        try {
                            viewModel.updateChangeInClassmentToeDBJetPackExport(onUpdateProgress)
                        } finally {
                            onUpdateComplete()
                        }
                    }
                }, Color.Black)
            }
        },
    )
    if (showConfirmationDialog) {
        ConfirmationDialog(
            onDismiss = { showConfirmationDialog = false },
            onConfirm = {
                coroutineScope.launch { viewModel.delete() }
                showConfirmationDialog = false
                onDismiss()
            }
        )
    }
    if (showUpdateConfirmationDialog) {
        ConfirmationDialog(
            onDismiss = { showUpdateConfirmationDialog = false },
            onConfirm = {
                coroutineScope.launch { viewModel.updateCategorieTabelee() }
                showUpdateConfirmationDialog = false
                onDismiss()
            }
        )
    }
}

@Composable
fun ConfirmationDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Action") },
        text = { Text("Are you sure you want to proceed?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Cancel")
            }
        }
    )
}



