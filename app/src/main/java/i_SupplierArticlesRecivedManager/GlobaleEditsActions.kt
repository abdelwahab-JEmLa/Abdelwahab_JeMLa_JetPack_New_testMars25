package i_SupplierArticlesRecivedManager

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
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PermMedia
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope

@Composable
fun FloatingActionButtons(
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    viewModel: HeadOfViewModels,
    coroutineScope: CoroutineScope,
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

    val buttons = listOf(

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
}


