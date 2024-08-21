package d_EntreBonsGro

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import f_credits.SupplierTabelle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


fun updateSupplierCredit(
    supplierId: Long,
    supplierTotal: Double,
    supplierPayment: Double,
    ancienCredit: Double
) {
    val firestore = Firebase.firestore
    val currentDateTime = LocalDateTime.now()
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)

    // Calculate restCreditDeCetteBon, ensuring it's not negative
    val restCreditDeCetteBon = maxOf(supplierTotal - supplierPayment, 0.0)


    // Calculate the new total credit
    val newTotalCredit = ancienCredit + supplierTotal - supplierPayment
    // Prepare the updated total data
    val data = hashMapOf(
        "date" to formattedDateTime,
        "totaleDeCeBon" to supplierTotal,
        "payeCetteFoit" to supplierPayment,
        "creditFaitDonCeBon" to restCreditDeCetteBon,
        "ancienCredits" to newTotalCredit
    )


    try {
        // Update the current bon document
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("Totale et Credit Des Bons")
            .document(documentIdFireStoreClientCredit())
            .set(data)

        // Update the latest document
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")
            .set(data)

        Log.d("Firestore", "Supplier credit updated successfully")
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating supplier credit: ", e)
    }
}

private fun documentIdFireStoreClientCredit(
): String {
    val currentDateTime = LocalDateTime.now()
    val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)

    val documentId = "Bon($dayOfWeek)${formattedDateTime}"
    return documentId
}


@Composable
fun SupplierSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSupplierSelected: (Int,Long) -> Unit,
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
                                        if (supplier != null) {
                                            onSupplierSelected(supplierNumber,supplier.idSupplierSu)
                                        }
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