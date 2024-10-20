package b2_Edite_Base_Donne_With_Creat_New_Articls

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
    onToggleOutlineFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    viewModel: HeadOfViewModels,
    coroutineScope: CoroutineScope,
    onChangeGridColumns: (Int) -> Unit,
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    onToggleModeClickDispo: () -> Unit
) {
    var showDialogeDataBaseEditer by remember { mutableStateOf(false) }
    var currentGridColumns by remember { mutableIntStateOf(2) }
    var showContentDescription by remember { mutableStateOf(false) }
    var showModeClickDispo by remember { mutableStateOf(false) }
    val maxGridColumns = 4

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
}}
