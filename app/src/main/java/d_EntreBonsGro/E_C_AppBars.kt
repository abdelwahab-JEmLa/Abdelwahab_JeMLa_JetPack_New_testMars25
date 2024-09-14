package d_EntreBonsGro

import a_RoomDB.BaseDonne
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete all data?") },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ActionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDeleteAllData: () -> Unit,
    editionPassedMode: Boolean,
    onEditionPassedModeChange: (Boolean) -> Unit,
    modeFilterChangesDB: Boolean,
    onModeFilterChangesDBChange: (Boolean) -> Unit,
    showMissingArticles: Boolean,
    onShowMissingArticlesChange: (Boolean) -> Unit,
    addedArticlesCount: Int,
    totalMissingArticles: Int,
    onDeleteReferencesWithSupplierId100: () -> Unit,
    founisseurNowIs: Int?,
    onImagePathChange: (String) -> Unit,
    suppliersList: List<SupplierTabelle>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    coroutineScope: CoroutineScope,
    articlesBaseDonne: List<BaseDonne>
) {
    var showImageSelectDialog by remember { mutableStateOf(false) }
    var showExportConfirmDialog by remember { mutableStateOf(false) }
    var showCreditDialog by remember { mutableStateOf(false) }
    var transferProgress by remember { mutableStateOf(0f) }
    var isTransferring by remember { mutableStateOf(false) }
    val filterArticlesGrosPourUpdate = articlesEntreBonsGrosTabele

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Actions") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onDeleteAllData()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete all data")
                        Spacer(Modifier.width(8.dp))
                        Text("Delete all data")
                    }
                    IconButton(onClick = { showCreditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Manage Credit"
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Edition Passed Mode")
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = editionPassedMode,
                            onCheckedChange = {
                                onEditionPassedModeChange(it)
                                onDismiss()
                            }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Changed Prices")
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = modeFilterChangesDB,
                            onCheckedChange = {
                                onModeFilterChangesDBChange(it)
                                onDismiss()
                            }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Missing Articles")
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = showMissingArticles,
                            onCheckedChange = {
                                onShowMissingArticlesChange(it)
                            }
                        )
                    }
                    if (showMissingArticles && totalMissingArticles > 0) {
                        Column {
                            Text("Adding missing articles: $addedArticlesCount / $totalMissingArticles")
                            LinearProgressIndicator(
                                progress = { addedArticlesCount.toFloat() / totalMissingArticles.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            )
                        }
                    } else if (addedArticlesCount > 0) {
                        Text("Added $addedArticlesCount missing articles", color = Color.Green)
                    }
                    TextButton(
                        onClick = {
                            onDeleteReferencesWithSupplierId100()
                            onDismiss()
                        }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete references with supplierIdBG = 100"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Delete references with supplierIdBG = 100")
                    }
                    TextButton(
                        onClick = {
                            showExportConfirmDialog = true
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Export to Firestore")
                        Spacer(Modifier.width(8.dp))
                        Text("Export to Firestore")
                    }

                    if (isTransferring) {
                        Column {
                            Text("Transferring data: ${(transferProgress * 100).toInt()}%")
                            LinearProgressIndicator(
                                progress = transferProgress,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            )
                        }
                    }

                    TextButton(
                        onClick = { showImageSelectDialog = true }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Select Image")
                        Spacer(Modifier.width(8.dp))
                        Text("Select Image")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }

    SupplierCreditDialog(
        showDialog = showCreditDialog,
        onDismiss = { showCreditDialog = false },
        supplierId = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }?.idSupplierSu,
        supplierName = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }?.nomSupplierSu
            ?: "Unknown Supplier",
        supplierTotal = articlesEntreBonsGrosTabele
            .filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
            .sumOf { it.subTotaleBG },
        coroutineScope = coroutineScope
    )


    if (showImageSelectDialog) {
        AlertDialog(
            onDismissRequest = { showImageSelectDialog = false },
            title = { Text("Select Image Number") },
            text = {
                Row {
                    (1..5).forEach { num ->
                        TextButton(
                            onClick = {
                                val newImagePath = if (num == 1) {
                                    "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}).jpg"
                                } else {
                                    "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}.$num).jpg"
                                }
                                onImagePathChange(newImagePath)
                                showImageSelectDialog = false
                            }
                        ) {
                            Text(if (num == 1) "Default" else num.toString())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSelectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val filterArticlesGrosPourUpdated = filterArticlesGrosPourUpdate(articlesEntreBonsGrosTabele)

    if (showExportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExportConfirmDialog = false },
            title = { Text("Confirm Export to Firestore") },
            text = { Text("Are you sure you want to export the data to Firestore?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            isTransferring = true
                            exportToFirestore()
                            trensfertBonSuppAuDataBaseArticles(
                                filterArticlesGrosPourUpdated,
                                articlesBaseDonne
                            ) { progress ->
                                transferProgress = progress
                            }
                            isTransferring = false
                        }
                        showExportConfirmDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
