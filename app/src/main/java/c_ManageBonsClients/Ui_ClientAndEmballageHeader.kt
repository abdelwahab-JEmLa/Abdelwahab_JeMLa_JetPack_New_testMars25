package c_ManageBonsClients

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ClientAndEmballageHeader(
    nomClient: String,
    typeEmballage: String,
    onPrintClick: (List<ArticlesAcheteModele>) -> Unit,
    onToggleActive: () -> Unit,
    isActive: Boolean,
    articles: List<ArticlesAcheteModele>,
    allArticles: List<ArticlesAcheteModele>,
    clientTotal: Double
) {
    val context = LocalContext.current

    var showPrintDialog by remember { mutableStateOf(false) }
    var showClientsBonUpdateDialog by remember { mutableStateOf(false) }
    var clientId by remember { mutableStateOf<Long?>(null) }
    var ancienCredits by remember { mutableStateOf(0.0) }
    val verifiedCount = allArticles.count { it.nomClient == nomClient && it.verifieState }
    val clientColor = remember(nomClient) { generateClientColor(nomClient) }
    val clientProfit = calculateClientProfit(allArticles, nomClient)
    val coroutineScope = rememberCoroutineScope()
    var clientsPaymentActuelle by remember { mutableStateOf("") }
    var restCreditDeCetteBon by remember { mutableDoubleStateOf(0.0) }
    var newBalenceOfCredits by remember { mutableDoubleStateOf(0.0) }

    // Safe parsing function with logging
    val safeParseDouble = { s: String ->
        try {
            s.takeIf { it.isNotEmpty() }?.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            Log.e("SafeParseDouble", "Error parsing string to double: $s", e)
            0.0
        }
    }

    fun fetchAncienCredits(clientId: Long?, onCreditsFetched: (Double) -> Unit) {
        if (clientId != null) {
            val firestore = Firebase.firestore
            firestore.collection("F_ClientsArticlesFireS")
                .document(clientId.toString())
                .collection("latest Totale et Credit Des Bons")
                .document("latest")
                .get()
                .addOnSuccessListener { document ->
                    val credits = document.getDouble("ancienCredits") ?: 0.0
                    onCreditsFetched(credits)
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching ancienCredits: ", e)
                    onCreditsFetched(0.0)
                }
        } else {
            onCreditsFetched(0.0)
        }
    }

    LaunchedEffect(nomClient) {
        val clientsTableRef = Firebase.database.getReference("G_Clients")
        clientsTableRef.orderByChild("nomClientsSu").equalTo(nomClient).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val clientData = snapshot.children.first().getValue(ClientsTabelle::class.java)
                        clientId = clientData?.idClientsSu
                        fetchAncienCredits(clientId) { credits ->
                            ancienCredits = credits
                        }
                    } else {
                        // Client doesn't exist, add new client
                        addNewClient(nomClient) { newClientId ->
                            clientId = newClientId
                            fetchAncienCredits(clientId) { credits ->
                                ancienCredits = credits
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching client ID: ${error.message}")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(clientColor)
            .padding(4.dp)
    ) {
        Text(
            text = "$nomClient - $typeEmballage",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showPrintDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "A.C:${String.format("%.2f", ancienCredits)}Da",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            IconButton(onClick = onToggleActive) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Check else Icons.Default.FilterList,
                    contentDescription = "Toggle Verification and Filter",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { createEmptyArticle(nomClient) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Empty Article",
                    tint = Color.Black
                )
            }
            IconButton(
                onClick = {
                    if (clientId != null) {
                        showClientsBonUpdateDialog = true
                    } else {
                        Log.e("ClientAndEmballageHeader", "Client ID is null for $nomClient")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Update Client Credit",
                    tint = Color.Black
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${String.format("%.2f", clientProfit)}Da",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total: ${String.format("%.2f", clientTotal)}Da",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showPrintDialog) {
        PrintConfirmationDialog(
            verifiedCount = verifiedCount,
            onConfirm = {
                val verifiedClientArticles = allArticles.filter { it.nomClient == nomClient && it.verifieState }
                onPrintClick(verifiedClientArticles)
                coroutineScope.launch {
                    if (clientId != null) {
                        updateClientsCreditFromHeader(
                            clientId!!.toInt(),
                            clientsTotalDeCeBon=clientTotal,
                            clientsPaymentActuelle =clientsPaymentActuelle.toDouble(),
                            restCreditDeCetteBon =restCreditDeCetteBon,
                            newBalenceOfCredits =newBalenceOfCredits,
                        )
                    }
                }
                showPrintDialog = false
            },
            onDismiss = { showPrintDialog = false }
        )
    }

    if (showClientsBonUpdateDialog && clientId != null) {
        ClientsCreditDialog(
            showDialog = showClientsBonUpdateDialog,
            onDismiss = { showClientsBonUpdateDialog = false },
            clientsId = clientId,
            clientsName = nomClient,
            clientsTotal = clientTotal,
            coroutineScope = coroutineScope,
            context = context,
            onValueChange = { input ->
                clientsPaymentActuelle = input
                val payment = safeParseDouble(input)
                restCreditDeCetteBon = (clientTotal - payment).coerceAtLeast(0.0)
                newBalenceOfCredits = (ancienCredits + restCreditDeCetteBon).coerceAtLeast(0.0)
            },
            clientsPaymentActuelle = clientsPaymentActuelle,
            restCreditDeCetteBon = restCreditDeCetteBon,
            newBalenceOfCredits = newBalenceOfCredits,
        )
    }
}

fun updateClientsCreditFromHeader(
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
            .addOnSuccessListener {
                Log.d("Firestore", "Document successfully written!")
                // Update the latest document
                firestore.collection("F_ClientsArticlesFireS")
                    .document(clientId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Latest document successfully updated!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating latest document", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error writing document", e)
            }
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating clients credit: ", e)
    }
}
@Composable
fun PrintConfirmationDialog(
    verifiedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirm Printing") },
        text = { Text("There are $verifiedCount verified articles. Do you want to proceed with printing?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}