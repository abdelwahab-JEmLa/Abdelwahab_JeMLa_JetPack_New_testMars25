package i_SupplierArticlesRecivedManager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Details
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.GridView
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

@Composable
fun FloatingActionButtonsSA(
    showFloatingButtons: Boolean,
    onToggleFloatingButtons: () -> Unit,
    onToggleSuppDescriptions: () -> Unit,
    onToggleToFilterToMove: () -> Unit,
    onChangeGridColumns: (Int) -> Unit,
    filterSuppHandledNow: Boolean
) {
    var currentGridColumns by remember { mutableIntStateOf(2) }
    val maxGridColumns = 6
    var showContentDescription by remember { mutableStateOf(false) }

    Column {
        if (showFloatingButtons) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                val buttons = listOf(
                    Triple(if (filterSuppHandledNow) Icons.Default.FileUpload else Icons.Default.FilterAlt, "Filter To Move") {
                        onToggleToFilterToMove()
                    },
                    Triple( Icons.Default.Details, "Toggle Suppliers Description") {
                        onToggleSuppDescriptions()
                    },
                    Triple(if (showContentDescription) Icons.Default.Close else Icons.Default.Details, "Toggle Description") {
                        showContentDescription = !showContentDescription
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
        FloatingActionButton(onClick = onToggleFloatingButtons) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Toggle Floating Buttons"
            )
        }
    }
}

