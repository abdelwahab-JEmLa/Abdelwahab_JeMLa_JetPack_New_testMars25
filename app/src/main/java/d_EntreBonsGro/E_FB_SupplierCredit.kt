package d_EntreBonsGro

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


fun updateSupplierCredit(
    supplierId: Int,
    supplierTotal: Double,
    supplierPayment: Double,
    ancienCredit: Double
) {
    val firestore = Firebase.firestore
    val currentDateTime = LocalDateTime.now()
    val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
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
        val documentId = "Bon($dayOfWeek)${formattedDateTime}=${"%.2f".format(supplierTotal)}"
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("Totale et Credit Des Bons")
            .document(documentId)
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



@Composable
fun SupplierCreditDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    supplierId: Long?,
    supplierName: String,
    supplierTotal: Double,
    coroutineScope: CoroutineScope
) {
    var supplierPayment by remember { mutableStateOf("") }
    var ancienCredit by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<SupplierInvoice>>(emptyList()) }

    // Reset supplierPayment when dialog is opened
    LaunchedEffect(showDialog) {
        if (showDialog) {
            supplierPayment = ""
        }
    }

    LaunchedEffect(showDialog, supplierId) {
        if (showDialog && supplierId != null) {
            isLoading = true
            val firestore = Firebase.firestore
            try {
                val latestDoc = firestore.collection("F_SupplierArticlesFireS")
                    .document(supplierId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                ancienCredit = latestDoc.getDouble("ancienCredits") ?: 0.0

                // Fetch recent invoices, excluding the "latest" document
                val invoicesQuery = firestore.collection("F_SupplierArticlesFireS")
                    .document(supplierId.toString())
                    .collection("Totale et Credit Des Bons")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)

                val invoicesSnapshot = invoicesQuery.get().await()
                recentInvoices = invoicesSnapshot.documents.mapNotNull { doc ->
                    SupplierInvoice(
                        date = doc.getString("date") ?: "",
                        totaleDeCeBon = doc.getDouble("totaleDeCeBon") ?: 0.0,
                        payeCetteFoit = doc.getDouble("payeCetteFoit") ?: 0.0,
                        creditFaitDonCeBon = doc.getDouble("creditFaitDonCeBon") ?: 0.0,
                        ancienCredits = doc.getDouble("ancienCredits") ?: 0.0
                    )
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching data: ", e)
                ancienCredit = 0.0
                recentInvoices = emptyList()
            }
            isLoading = false
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Manage Supplier Credit: $supplierName") },
            text = {
                Column {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Current Credit + New Purchase Total: ${"%.2f".format(ancienCredit + supplierTotal)}")
                        Text("Total of Current Invoice: ${"%.2f".format(supplierTotal)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = supplierPayment,
                            onValueChange = { supplierPayment = it },
                            label = { Text("Payment Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val paymentAmount = if((supplierPayment.toDoubleOrNull() ?: 0.0) == 0.0) ancienCredit else supplierPayment.toDoubleOrNull() ?: 0.0
                        val newCredit = ancienCredit + supplierTotal - paymentAmount
                        Text("New Credit Balance: ${"%.2f".format(newCredit)}")

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Recent Invoices", style = MaterialTheme.typography.titleMedium)
                        if (recentInvoices.isEmpty()) {
                            Text("No recent invoices found")
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(200.dp)
                            ) {
                                items(recentInvoices) { invoice ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Date: ${invoice.date}")
                                            Text("Total: ${"%.2f".format(invoice.totaleDeCeBon)}")
                                            Text("Paid: ${"%.2f".format(invoice.payeCetteFoit)}")
                                            Text("Credit: ${"%.2f".format(invoice.creditFaitDonCeBon)}")
                                            Text("Previous Balance: ${"%.2f".format(invoice.ancienCredits)}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            supplierId?.let { id ->
                                val paymentAmount = supplierPayment.toDoubleOrNull() ?: 0.0
                                updateSupplierCredit(id.toInt(), supplierTotal, paymentAmount,ancienCredit)
                            }
                        }
                        onDismiss()
                    },
                    enabled = !isLoading
                ) {
                    Text("Save")
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
