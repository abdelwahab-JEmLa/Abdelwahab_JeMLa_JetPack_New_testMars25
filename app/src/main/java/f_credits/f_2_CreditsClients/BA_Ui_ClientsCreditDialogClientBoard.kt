package f_credits.f_2_CreditsClients

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
import androidx.compose.material.icons.filled.CreditScore
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Paid
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
import c_ManageBonsClients.imprimerDonnees
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.generateClientColor
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import g_BoardStatistiques.BoardStatistiquesStatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun ClientsCreditDialogClientsBoard(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    clientsId: Long?,
    clientsName: String,
    clientsTotal: Double,
    coroutineScope: CoroutineScope,
    context: Context, // Add context parameter
    boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel,

    ) {
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<ClientsInvoiceOtherCB>>(emptyList()) }
    var itsPayment by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val clientsColor = remember(clientsName) { generateClientColor(clientsName) }

    var ancienCredit by remember { mutableDoubleStateOf(0.0) }
    var clientsPaymentActuelle by remember { mutableStateOf("") }

    var clientsPaymentActuelleDouble by remember { mutableStateOf(0.0) }
    var restCreditDeCetteBon by remember { mutableDoubleStateOf(0.0) }
    var newBalenceOfCredits by remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            fetchRecentInvoicesCB(clientsId, onFetchComplete = { invoices, credit ->
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
                                    clientsPaymentActuelle = newClientsPaymentActuelle
                                    clientsPaymentActuelleDouble = (newClientsPaymentActuelle.toDoubleOrNull() ?: 0.0)

                                    restCreditDeCetteBon = 0.0
                                    newBalenceOfCredits = ancienCredit + (if (itsPayment) clientsPaymentActuelleDouble else -clientsPaymentActuelleDouble)
                                },
                                label = { Text("Payment Amount", color = Color.White) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = if (itsPayment) Color.Green else Color.Red,
                                    unfocusedTextColor = if (itsPayment) Color.Green.copy(alpha = 0.7f) else Color.Red.copy(alpha = 0.7f),
                                    focusedBorderColor = if (itsPayment) Color.Green else Color.Red,
                                    unfocusedBorderColor = if (itsPayment) Color.Green.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconToggleButton(
                                checked = itsPayment,
                                onCheckedChange = { newItsPayment ->
                                    itsPayment = newItsPayment
                                    // Update newBalenceOfCredits based on the new itsPayment value
                                    newBalenceOfCredits = ancienCredit + (if (newItsPayment) clientsPaymentActuelleDouble else -clientsPaymentActuelleDouble)
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = if (itsPayment) Color.Green else Color.Red,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = if (itsPayment) Icons.Default.Paid else Icons.Default.CreditScore,
                                    contentDescription = if (itsPayment) "Payment" else "Credit",
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
                                                Text("Date: ${invoice.date} (${getDayOfWeekClientsCB(invoice.date)})")
                                                Text("Total Du Bon: ${"%.2f".format(invoice.totaleDeCeBon)}")
                                                Text("Versement: ${"%.2f".format(invoice.payeCetteFoit)}")
                                                val creditFaitDonCeBonIf0 = if (invoice.creditFaitDonCeBon == 0.0) "Its Payment"
                                                else "(Credit fait: ${"%.2f".format(invoice.creditFaitDonCeBon)})"
                                                Text(creditFaitDonCeBonIf0)
                                                Text("New Balance After: ${"%.2f".format(invoice.newBalence)}")
                                            }
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        try {
                                                            deleteInvoiceCB(clientsId, invoice.date)
                                                            fetchRecentInvoicesCB(
                                                                clientsId,
                                                                onFetchComplete = { invoices, credit ->
                                                                    recentInvoices = invoices
                                                                    ancienCredit = credit
                                                                })
                                                        } catch (e: Exception) {
                                                            errorMessage =
                                                                "Error deleting invoice: ${e.message}"
                                                        }
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Delete Invoice"
                                                )
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
                Row(
                    modifier = Modifier.padding(end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                clientsId?.let { id ->
                                    val paymentAmount =
                                        clientsPaymentActuelle.toDoubleOrNull() ?: ancienCredit

                                    imprimeLeTiquetDuCreditChangementCB(
                                        clientsTotalDeCeBon = clientsTotal,
                                        clientsPaymentActuelle = paymentAmount,
                                        newBalenceOfCredits = newBalenceOfCredits,
                                        context = context,
                                        onDismiss = onDismiss,
                                        clientsName = clientsName
                                    )

                                    updateClientsCreditCB(
                                        id.toInt(),
                                        clientsTotalDeCeBon = clientsTotal,
                                        clientsPaymentActuelle = paymentAmount,
                                        restCreditDeCetteBon = restCreditDeCetteBon,
                                        newBalenceOfCredits = newBalenceOfCredits ,
                                        boardStatistiquesStatViewModel
                                    )
                                    fetchRecentInvoicesCB(
                                        clientsId,
                                        onFetchComplete = { invoices, credit ->
                                            recentInvoices = invoices
                                            ancienCredit = credit
                                        })
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Save & Print", color = Color.White)
                    }

                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                clientsId?.let { id ->
                                    val paymentAmount =
                                        clientsPaymentActuelle.toDoubleOrNull() ?: ancienCredit

                                    updateClientsCreditCB(
                                        id.toInt(),
                                        clientsTotalDeCeBon = clientsTotal,
                                        clientsPaymentActuelle = paymentAmount,
                                        restCreditDeCetteBon = restCreditDeCetteBon,
                                        newBalenceOfCredits = newBalenceOfCredits,
                                        boardStatistiquesStatViewModel
                                    )

                                    fetchRecentInvoicesCB(
                                        clientsId,
                                        onFetchComplete = { invoices, credit ->
                                            recentInvoices = invoices
                                            ancienCredit = credit
                                        })

                                    onDismiss()
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Text("Save Only", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.White)
                }
            })
    }
}

private fun imprimeLeTiquetDuCreditChangementCB(
    clientsTotalDeCeBon: Double,
    clientsPaymentActuelle: Double,
    newBalenceOfCredits: Double,
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
        append("<BR><BIG><CENTER> $clientsPaymentActuelle Da")
        append("<BR><LEFT><NORMAL><MEDIUM1>--------------")
        append("<BR><MEDIUM1><LEFT>Totale Des Credits : ")
        append("<BR><BIG><CENTER>$newBalenceOfCredits Da")
        append("<BR><LEFT><NORMAL><MEDIUM1>=====================<BR>")
        append("<BR><BR><BR>>")
    }.toString()

    imprimerDonnees(context, texteImprimable, clientsTotalDeCeBon)
    onDismiss()
}


fun updateClientsCreditCB(
    clientId: Int,
    clientsTotalDeCeBon: Double,
    clientsPaymentActuelle: Double,
    restCreditDeCetteBon: Double,
    newBalenceOfCredits: Double,
    boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel
) {
    val firestore = Firebase.firestore
    val currentDateTime = LocalDateTime.now()
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)

    val data = hashMapOf(
        "date" to formattedDateTime,
        "totaleDeCeBon" to clientsTotalDeCeBon,
        "payeCetteFoit" to clientsPaymentActuelle.coerceAtLeast(0.0),
        "creditFaitDonCeBon" to restCreditDeCetteBon.coerceAtLeast(0.0),
        "ancienCredits" to newBalenceOfCredits
    )

    try {
        val documentId = documentIdClientFireStoreClientCreditCB()
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

        // Firebase Realtime Database update
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val g_StatistiquesRef = Firebase.database.getReference("G_Statistiques")

        // Update _statistics in BoardStatistiquesStatViewModel
        boardStatistiquesStatViewModel.updateTotaleCreditsClients(clientsPaymentActuelle)

        g_StatistiquesRef.child(currentDate).runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val currentValue = mutableData.getValue(Double::class.java) ?: 0.0
                mutableData.value = currentValue - clientsPaymentActuelle
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                if (committed) {
                    Log.d("Firebase", "G_Statistiques updated successfully")
                } else {
                    Log.e("Firebase", "Error updating G_Statistiques: ${error?.message}")
                }
            }
        })

        Log.d("Firestore", "Clients credit updated successfully")
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating clients credit: ", e)
    }
}

fun documentIdClientFireStoreClientCreditCB(
): String {
    val currentDateTime = LocalDateTime.now()
    val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)

    val documentId = "Bon($dayOfWeek)${formattedDateTime}"
    return documentId
}
suspend fun fetchRecentInvoicesCB(clientsId: Long?, onFetchComplete: (List<ClientsInvoiceOtherCB>, Double) -> Unit) {
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
                ClientsInvoiceOtherCB(
                    date = doc.getString("date") ?: "",
                    totaleDeCeBon = doc.getDouble("totaleDeCeBon") ?: 0.0,
                    payeCetteFoit = doc.getDouble("payeCetteFoit") ?: 0.0,
                    creditFaitDonCeBon = doc.getDouble("creditFaitDonCeBon") ?: 0.0,
                    newBalence = doc.getDouble("ancienCredits") ?: 0.0
                )
            }
            onFetchComplete(recentInvoices, ancienCredit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error fetching data: ", e)
            onFetchComplete(emptyList(), 0.0)
        }
    }
}

suspend fun deleteInvoiceCB(clientsId: Long?, invoiceDate: String) {
    clientsId?.let { id ->
        val firestore = Firebase.firestore
        val clientDocRef = firestore.collection("F_ClientsArticlesFireS").document(id.toString())
        val invoicesCollectionRef = clientDocRef.collection("Totale et Credit Des Bons")
        val latestDocRef = clientDocRef.collection("latest Totale et Credit Des Bons").document("latest")

        try {
            // Query for the document to delete and the one before it
            val querySnapshot = invoicesCollectionRef
                .orderBy("date")
                .endAt(invoiceDate)
                .limitToLast(2)
                .get()
                .await()

            if (querySnapshot.documents.size >= 1) {
                val documentToDelete = querySnapshot.documents.last()
                val previousDocument = if (querySnapshot.documents.size == 2) querySnapshot.documents.first() else null

                firestore.runTransaction { transaction ->
                    val ancienCredits = if (previousDocument != null) {
                        previousDocument.getDouble("ancienCredits")
                    } else {
                        // If there's no previous document, use 0.0 or another appropriate default value
                        0.0
                    }

                    // Update the latest document with the ancienCredits from the previous invoice
                    transaction.update(latestDocRef, "ancienCredits", ancienCredits)

                    // Delete the invoice document
                    transaction.delete(documentToDelete.reference)
                }.await()
            } else {
                throw Exception("No matching invoice found for deletion")
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error deleting invoice: ", e)
            throw e
        }
    } ?: throw IllegalArgumentException("Invalid clients ID")
}

data class ClientsInvoiceOtherCB(
    val date: String,
    val totaleDeCeBon: Double,
    val payeCetteFoit: Double,
    val creditFaitDonCeBon: Double,
    val newBalence: Double
)

fun getDayOfWeekClientsCB(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(dateString, formatter)
    return dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
}
