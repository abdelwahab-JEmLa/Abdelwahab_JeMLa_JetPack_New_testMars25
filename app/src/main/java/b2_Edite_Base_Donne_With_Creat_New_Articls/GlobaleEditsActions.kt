package b2_Edite_Base_Donne_With_Creat_New_Articls

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FloatingActionButtons(
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    viewModel: HeadOfViewModels,
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
                onChangeGridColumns = onChangeGridColumns,
                viewModel = viewModel,
                coroutineScope = coroutineScope
            )
        }
        FloatingActionButton(onClick = onToggleFloatingButtons) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Floating Buttons"
            )
        }
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
    onChangeGridColumns: (Int) -> Unit,
    viewModel: HeadOfViewModels,
    coroutineScope: CoroutineScope
) {
    var currentGridColumns by remember { mutableIntStateOf(2) }
    val maxGridColumns = 4
    var showContentDescription by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageTypeDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            showImageTypeDialog = true
        }
    }

    val buttons = listOf(
        Triple(Icons.Default.Add, "Add Article") {
            tempImageUri = createTempImageUri(context)
            tempImageUri?.let { cameraLauncher.launch(it) }
        },
        Triple(Icons.Default.Category, "Category", onCategorySelectionClick),
        Triple(Icons.Default.Home, "Home", onToggleNavBar),
        Triple(if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff, "Filter", onToggleFilter),
        Triple(Icons.Default.PermMedia, "Database Editor", onDialogDataBaseEditerClick),
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
                    onClick = onClick as () -> Unit,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(icon, contentDescription = contentDescription)
                }
            }
        }
    }

    if (showImageTypeDialog) {
        ImageTypeSelectionDialog(
            onDismiss = { showImageTypeDialog = false },
            onSelectType = { isParent, colorChoice ->
                tempImageUri?.let { uri ->
                    coroutineScope.launch {
                        viewModel.handleImageCapture(uri, isParent, colorChoice)
                    }
                }
                showImageTypeDialog = false
            }
        )
    }
}

@Composable
fun ImageTypeSelectionDialog(
    onDismiss: () -> Unit,
    onSelectType: (Boolean, String?) -> Unit
) {
    var showColorSelection by remember { mutableStateOf(false) }

    if (!showColorSelection) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Image Type") },
            text = { Text("Is this a parent article or a color variation?") },
            confirmButton = {
                Button(onClick = { onSelectType(true, null) }) {
                    Text("Parent")
                }
            },
            dismissButton = {
                Button(onClick = { showColorSelection = true }) {
                    Text("Color")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = { showColorSelection = false },
            title = { Text("Select Color") },
            text = { Text("Choose the color variation") },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { onSelectType(false, "Couleur_2") }) {
                        Text("Couleur 2")
                    }
                    Button(onClick = { onSelectType(false, "Couleur_3") }) {
                        Text("Couleur 3")
                    }
                    Button(onClick = { onSelectType(false, "Couleur_4") }) {
                        Text("Couleur 4")
                    }
                }
            },
            dismissButton = {
                Button(onClick = { showColorSelection = false }) {
                    Text("Back")
                }
            }
        )
    }
}

// Helper function to create a temporary image URI
fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", tempFile)
}
