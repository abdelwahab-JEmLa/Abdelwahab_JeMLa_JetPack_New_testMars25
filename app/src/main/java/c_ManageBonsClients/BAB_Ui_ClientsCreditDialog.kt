package c_ManageBonsClients

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ClientsCreditDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    clientsId: Long?,
    clientsName: String,
    clientsTotal: Double,
    coroutineScope: CoroutineScope,
    context: Context, // Add context parameter

) {
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<ClientsInvoiceOther>>(emptyList()) }
    var isPositive by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val clientsColor = remember(clientsName) { generateClientColor(clientsName) }

    var ancienCredit by remember { mutableDoubleStateOf(0.0) }
    var clientsPaymentActuelle by remember { mutableStateOf("") }
    val restCreditDeCetteBon by remember {
        mutableDoubleStateOf(
            clientsTotal - (clientsPaymentActuelle.toDoubleOrNull() ?: 0.0)
        )
    }
    val newBalenceOfCredits by remember {
        mutableDoubleStateOf(
            ancienCredit + restCreditDeCetteBon
        )
    }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            fetchRecentInvoices(clientsId, onFetchComplete = { invoices, credit ->
                recentInvoices = invoices
                ancienCredit = credit
                isLoading = false
            })
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Manage Client Credit: $clientsName", color = Color.White) },
            containerColor = clientsColor,
            text = {
                Column {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Current Credit + New Purchase Total: ${"%.2f".format(ancienCredit + clientsTotal)}", color = Color.White)
                        Text("Total of Current Invoice: ${"%.2f".format(clientsTotal)}", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("New Credit Balance: ${"%.2f".format(newBalenceOfCredits)}", color = Color.White)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = clientsPaymentActuelle,
                                onValueChange = { newClientsPaymentActuelle ->
                                    clientsPaymentActuelle = newClientsPaymentActuelle},
                                label = { Text("Payment Amount", color = Color.White) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (isPositive) Color.Green else Color.Red,
                                    unfocusedTextColor = if (isPositive) Color.Green.copy(alpha = 0.7f) else Color.Red.copy(alpha = 0.7f),
                                    focusedBorderColor = if (isPositive) Color.Green else Color.Red,
                                    unfocusedBorderColor = if (isPositive) Color.Green.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconToggleButton(
                                checked = isPositive,
                                onCheckedChange = { isPositive = it },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = if (isPositive) Color.Green else Color.Red,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isPositive) Icons.Default.Add else Icons.Default.Remove,
                                    contentDescription = if (isPositive) "Add" else "Subtract",
                                    tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Recent Invoices", style = MaterialTheme.typography.titleMedium, color = Color.White)
                        if (recentInvoices.isEmpty()) {
                            Text("No recent invoices found", color = Color.White)
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
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Date: ${invoice.date} (${getDayOfWeekClients(invoice.date)})")
                                                Text("Total: ${"%.2f".format(invoice.totaleDeCeBon)}")
                                                Text("Paid: ${"%.2f".format(invoice.payeCetteFoit)}")
                                                Text("Credit: ${"%.2f".format(invoice.creditFaitDonCeBon)}")
                                                Text("Previous Balance: ${"%.2f".format(invoice.ancienCredits)}")
                                            }
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        try {
                                                            deleteInvoice(clientsId, invoice.date)
                                                            fetchRecentInvoices(clientsId, onFetchComplete = { invoices, credit ->
                                                                recentInvoices = invoices
                                                                ancienCredit = credit
                                                            })
                                                        } catch (e: Exception) {
                                                            errorMessage = "Error deleting invoice: ${e.message}"
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Invoice")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        errorMessage?.let {
                            Text(it, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            clientsId?.let { id ->
                                val paymentAmount = clientsPaymentActuelle.toDoubleOrNull() ?: 0.0

                                imprimeLeTiquetDuCreditChangement(
                                    ancienCredit,
                                    clientsTotal,
                                    paymentAmount,
                                    clientsName,
                                    context,
                                    onDismiss
                                )
                                updateClientsCreditCCD(
                                    id.toInt(),
                                    clientsTotalDeCeBon = clientsTotal,
                                    clientsPaymentActuelle = paymentAmount,
                                    restCreditDeCetteBon = restCreditDeCetteBon,
                                    newBalenceOfCredits = newBalenceOfCredits
                                )

                                fetchRecentInvoices(clientsId, onFetchComplete = { invoices, credit ->
                                    recentInvoices = invoices
                                    ancienCredit = credit
                                })
                            }
                        }
                    },
                    enabled = !isLoading && clientsPaymentActuelle.isNotEmpty()
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White)
                }
            }
        )
    }
}

private fun imprimeLeTiquetDuCreditChangement(
    ancienCredit: Double,
    clientsTotal: Double,
    paymentAmount: Double,
    clientsName: String,
    context: Context,
    onDismiss: () -> Unit
) {
    val texteImprimable = StringBuilder().apply {
        val currentDateTime = LocalDateTime.now()
        val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(
            TextStyle.FULL, Locale.FRENCH
        )
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDateTime = currentDateTime.format(dateTimeFormatter)
        val newCredit = "%.2f".format(ancienCredit + clientsTotal - paymentAmount)
        append("<BR><BR>")
        append("<MEDIUM1><CENTER>Abdelwahab<BR>")
        append("<MEDIUM1><CENTER>JeMla.Com<BR>")
        append("<SMALL><CENTER>0553885037<BR>")
        append("<LEFT><NORMAL><MEDIUM1>=====================")
        append("<BR><SMALL><CENTER>$clientsName")
        append("<BR><MEDIUM1><CENTER>($dayOfWeek)${formattedDateTime}")
        append("<BR>")
        append("<BR><LEFT><NORMAL><MEDIUM1>--------------")
        append("<BR><MEDIUM1><LEFT>Versement : ")
        append("<BR><BIG><CENTER> -$paymentAmount Da")
        append("<BR><LEFT><NORMAL><MEDIUM1>--------------")
        append("<BR><MEDIUM1><LEFT>Totale Des Credits : ")
        append("<BR><BIG><CENTER>$newCredit Da")
        append("<BR><LEFT><NORMAL><MEDIUM1>=====================<BR>")
        append("<BR><BR><BR>>")
    }.toString()

    imprimerDonnees(context, texteImprimable, clientsTotal)
    onDismiss()
}

fun updateClientsCreditCCD(
    clientId: Int,
    clientsTotalDeCeBon: Double,
    clientsPaymentActuelle: Double,
    restCreditDeCetteBon: Double,
    newBalenceOfCredits: Double
) {
    val firestore = Firebase.firestore
    val currentDateTime = LocalDateTime.now()
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)


    val data = hashMapOf(
        "date" to formattedDateTime,
        "totaleDeCeBon" to clientsTotalDeCeBon,
        "payeCetteFoit" to clientsPaymentActuelle,
        "creditFaitDonCeBon" to restCreditDeCetteBon,
        "ancienCredits" to newBalenceOfCredits
    )

    try {
        val documentId = documentIdClientFireStoreClientCredit()
        firestore.collection("F_ClientsArticlesFireS")
            .document(clientId.toString())
            .collection("Totale et Credit Des Bons")
            .document(documentId)
            .set(data)

        firestore.collection("F_ClientsArticlesFireS")
            .document(clientId.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")
            .set(data)

        Log.d("Firestore", "Clients credit updated successfully")
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating clients credit: ", e)
    }
}

fun documentIdClientFireStoreClientCredit(
): String {
    val currentDateTime = LocalDateTime.now()
    val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)

    val documentId = "Bon($dayOfWeek)${formattedDateTime}"
    return documentId
}

suspend fun fetchRecentInvoices(clientsId: Long?, onFetchComplete: (List<ClientsInvoiceOther>, Double) -> Unit) {
    clientsId?.let { id ->
        val firestore = com.google.firebase.ktx.Firebase.firestore
        try {
            val latestDoc = firestore.collection("F_ClientsArticlesFireS")
                .document(id.toString())
                .collection("latest Totale et Credit Des Bons")
                .document("latest")
                .get()
                .await()

            val ancienCredit = latestDoc.getDouble("ancienCredits") ?: 0.0

            val invoicesQuery = firestore.collection("F_ClientsArticlesFireS")
                .document(id.toString())
                .collection("Totale et Credit Des Bons")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(3)

            val invoicesSnapshot = invoicesQuery.get().await()
            val recentInvoices = invoicesSnapshot.documents.mapNotNull { doc ->
                ClientsInvoiceOther(
                    date = doc.getString("date") ?: "",
                    totaleDeCeBon = doc.getDouble("totaleDeCeBon") ?: 0.0,
                    payeCetteFoit = doc.getDouble("payeCetteFoit") ?: 0.0,
                    creditFaitDonCeBon = doc.getDouble("creditFaitDonCeBon") ?: 0.0,
                    ancienCredits = doc.getDouble("ancienCredits") ?: 0.0
                )
            }
            onFetchComplete(recentInvoices, ancienCredit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching data: ", e)
            onFetchComplete(emptyList(), 0.0)
        }
    }
}

suspend fun deleteInvoice(clientsId: Long?, invoiceDate: String) {
    clientsId?.let { id ->
        val firestore = com.google.firebase.ktx.Firebase.firestore
        val invoiceRef = firestore.collection("F_ClientsArticlesFireS")
            .document(id.toString())
            .collection("Totale et Credit Des Bons")
            .whereEqualTo("date", invoiceDate)
            .limit(1)

        try {
            val querySnapshot = invoiceRef.get().await()
            if (!querySnapshot.isEmpty) {
                val documentToDelete = querySnapshot.documents[0]
                documentToDelete.reference.delete().await()
                updateLatestDocument(id, invoiceDate)
            } else {
                throw Exception("No matching invoice found for deletion")
            }
        } catch (e: Exception) {
            throw e
        }
    } ?: throw IllegalArgumentException("Invalid clients ID")
}

suspend fun updateLatestDocument(clientsId: Long, deletedInvoiceDate: String) {
    val firestore = com.google.firebase.ktx.Firebase.firestore
    val latestDocRef = firestore.collection("F_ClientsArticlesFireS")
        .document(clientsId.toString())
        .collection("latest Totale et Credit Des Bons")
        .document("latest")

    val invoicesQuery = firestore.collection("F_ClientsArticlesFireS")
        .document(clientsId.toString())
        .collection("Totale et Credit Des Bons")
        .orderBy("date", Query.Direction.DESCENDING)
        .limit(1)

    try {
        val querySnapshot = invoicesQuery.get().await()
        if (!querySnapshot.isEmpty) {
            val latestInvoice = querySnapshot.documents[0]
            latestDocRef.set(latestInvoice.data!!).await()
        } else {
            // If no invoices left, set default values or delete the latest document
            latestDocRef.delete().await()
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating latest document: ", e)
        throw e
    }
}
data class ClientsInvoiceOther(
    val date: String,
    val totaleDeCeBon: Double,
    val payeCetteFoit: Double,
    val creditFaitDonCeBon: Double,
    val ancienCredits: Double
)


fun getDayOfWeekClients(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(dateString, formatter)
    return dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
}