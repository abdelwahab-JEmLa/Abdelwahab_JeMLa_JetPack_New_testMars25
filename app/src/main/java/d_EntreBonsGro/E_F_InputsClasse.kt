package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import b_Edite_Base_Donne.ArticleDao
import c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
@Composable
fun ActionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDeleteAllData: () -> Unit,
    editionPassedMode: Boolean,
    onEditionPassedModeChange: (Boolean) -> Unit,
    modeFilterChangesDB: Boolean,
    onModeFilterChangesDBChange: (Boolean) -> Unit,
    onExportToFirestore: () -> Unit,
    showMissingArticles: Boolean,
    onShowMissingArticlesChange: (Boolean) -> Unit,
    addedArticlesCount: Int,
    totalMissingArticles: Int,
    onDeleteReferencesWithSupplierId100: () -> Unit,
    founisseurNowIs: Int?,
    onImagePathChange: (String) -> Unit
) {
    var showImageSelectDialog by remember { mutableStateOf(false) }

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
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                        Icon(Icons.Default.Delete, contentDescription = "Delete references with supplierIdBG = 100")
                        Spacer(Modifier.width(8.dp))
                        Text("Delete references with supplierIdBG = 100")
                    }
                    TextButton(
                        onClick = {
                            onExportToFirestore()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Export to Firestore")
                        Spacer(Modifier.width(8.dp))
                        Text("Export to Firestore")
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

    if (showImageSelectDialog) {
        AlertDialog(
            onDismissRequest = { showImageSelectDialog = false },
            title = { Text("Select Image Number") },
            text = {
                Row {
                    (2..5).forEach { num ->
                        TextButton(
                            onClick = {
                                val newImagePath = "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}.$num).jpg"
                                onImagePathChange(newImagePath)
                                showImageSelectDialog = false
                            }
                        ) {
                            Text(num.toString())
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
}

@Composable
fun SupplierSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSupplierSelected: (Int) -> Unit,
    suppliersList: List<SupplierTabelle>,
    suppliersRef: DatabaseReference
) {
    if (showDialog) {
        var showBonUpdateDialog by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Supplier") },
            text = {
                LazyColumn {
                    items(16) { i ->
                        val supplierNumber = if (i == 15) 100 else i + 1
                        val supplier = suppliersList.find { it.bonDuSupplierSu == supplierNumber.toString() }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        onSupplierSelected(supplierNumber)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (supplier != null && supplier.bonDuSupplierSu.isNotEmpty()) {
                                        Text("$supplierNumber->.(${supplier.idSupplierSu}) ${supplier.nomSupplierSu}")
                                    } else {
                                        Text("$supplierNumber->.")
                                    }
                                }
                                IconButton(
                                    onClick = { showBonUpdateDialog = true }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Update Bon Number")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )

        SupplierBonUpdateDialog(
            showDialog = showBonUpdateDialog,
            onDismiss = { showBonUpdateDialog = false },
            onBonNumberSelected = { supplierId, newBonNumber ->
                updateSupplierBon(suppliersRef, supplierId, newBonNumber.toString())
            },
            suppliersList = suppliersList
        )
    }
}
data class SupplierInvoice(
    val date: String,
    val totaleDeCeBon: Double,
    val payeCetteFoit: Double,
    val creditFaitDonCeBon: Double,
    val ancienCredits: Double
)
@Composable
fun SupplierBonUpdateDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onBonNumberSelected: (Int, Int) -> Unit,
    suppliersList: List<SupplierTabelle>
) {
    if (showDialog) {
        var selectedSupplier by remember { mutableStateOf<SupplierTabelle?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Update Supplier Bon Number") },
            text = {
                Column {
                    Text("Select Supplier:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    ) {
                        items(suppliersList) { supplier ->
                            TextButton(
                                onClick = { selectedSupplier = supplier },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("${supplier.idSupplierSu} - ${supplier.nomSupplierSu}")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedSupplier != null) {
                        Text("Current Bon Number: ${selectedSupplier?.bonDuSupplierSu}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Select New Bon Number:", style = MaterialTheme.typography.titleMedium)
                        LazyColumn(
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                        ) {
                            items(15) { i ->
                                TextButton(
                                    onClick = {
                                        onBonNumberSelected(selectedSupplier!!.idSupplierSu.toInt(), i + 1)
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("${i + 1}")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}



fun updateSpecificArticle(input: String, article: EntreBonsGrosTabele, articlesRef: DatabaseReference, coroutineScope: CoroutineScope): Boolean {
    val regex = """(\d+)\s*[x+]\s*(\d+(\.\d+)?)""".toRegex()
    val matchResult = regex.find(input)

    val (quantity, price) = matchResult?.destructured?.let {
        Pair(it.component1().toIntOrNull(), it.component2().toDoubleOrNull())
    } ?: Pair(null, null)

    if (quantity != null && price != null) {
        val updatedArticle = article.copy(
            quantityAcheteBG = quantity,
            newPrixAchatBG = price,
            subTotaleBG = price * quantity
        )
        articlesRef.child(article.vidBG.toString()).setValue(updatedArticle)



        return true
    }
    return false
}

fun Double.roundToTwoDecimals() = (this * 100).roundToInt() / 100.0