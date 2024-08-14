package f_credits.f_2

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentCreditsClients(viewModel: CreditsClientsViewModel = viewModel()) {
    val clients by viewModel.clientsList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CreditsClients", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Red),
                actions = {
                    IconButton(onClick = { showMenuDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Client", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(clients) { clients ->
                ClientsItem(clients, viewModel)
            }
        }
    }

    if (showAddDialog) {
        AddClientsDialog(
            onDismiss = { showAddDialog = false },
            onAddClients = { name ->
                viewModel.addClients(name)
                showAddDialog = false
            }
        )
    }

    if (showMenuDialog) {
        AlertDialog(
            onDismissRequest = { showMenuDialog = false },
            title = { Text("Menu") },
            text = {
                Column {
                    Button(onClick = {
                        viewModel.clearAllBonDuClientsSu()
                        showMenuDialog = false
                    }) {
                        Text("Clear all bonDuClientsSu")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMenuDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ClientsItem(clients: ClientsTabelle, viewModel: CreditsClientsViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showCreditDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val backgroundColor = remember(clients.couleurSu) {
        try {
            Color(android.graphics.Color.parseColor(clients.couleurSu))
        } catch (e: IllegalArgumentException) {
            Color.Gray // Fallback color
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.deleteClients(clients.idClientsSu) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
            Column {
                Text(
                    text = "ID: ${clients.idClientsSu}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.drawTextWithOutlineClients(Color.Black)
                )
                Text(
                    text = "Name: ${clients.nomClientsSu}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.drawTextWithOutlineClients(Color.Black)
                )
                if (clients.bonDuClientsSu.isNotBlank()) {
                    Text(
                        text = "Bon N°: ${clients.bonDuClientsSu}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.drawTextWithOutlineClients(Color.Black)
                    )
                }
                Text(
                    text = "Credit Balance: ${clients.currentCreditBalance}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.drawTextWithOutlineClients(Color.Black)
                )
            }
            Row {
                IconButton(onClick = { showCreditDialog = true }) {
                    Icon(Icons.Default.CreditCard, contentDescription = "Credit", tint = Color.White)
                }
                IconButton(onClick = { showDialog = true }) {
                    val icon = if (clients.bonDuClientsSu.isNotBlank()) {
                        Icons.AutoMirrored.Filled.ReceiptLong
                    } else {
                        Icons.Default.Receipt
                    }
                    Icon(icon, contentDescription = "Invoice", tint = Color.White)
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Invoice Number") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (i in 1..15 step 3) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (j in i..minOf(i + 2, 15)) {
                                Button(
                                    onClick = {
                                        viewModel.updateClientsBon(clients.idClientsSu, j.toString())
                                        showDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (clients.bonDuClientsSu == j.toString()) Color.Red else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(j.toString())
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showCreditDialog) {
        ClientsCreditDialog(
            showDialog = true,
            onDismiss = { showCreditDialog = false },
            clientsId = clients.idClientsSu,
            clientsName = clients.nomClientsSu,
            clientsTotal = 0.0, // You may want to pass the actual total here
            coroutineScope = rememberCoroutineScope(),
            clientsColor = backgroundColor // Pass the background color here
        )
    }

    if (showEditDialog) {
        EditClientsDialog(
            clients = clients,
            onDismiss = { showEditDialog = false },
            onEditClients = { editedClients ->
                viewModel.updateClients(editedClients)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditClientsDialog(clients: ClientsTabelle, onDismiss: () -> Unit, onEditClients: (ClientsTabelle) -> Unit) {
    var editedName by remember { mutableStateOf(clients.nomClientsSu) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Clients") },
        text = {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Clients Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (editedName.isNotBlank()) {
                        onEditClients(clients.copy(nomClientsSu = editedName))
                    }
                }
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




@Composable
fun ClientsCreditDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    clientsId: Long?,
    clientsName: String,
    clientsTotal: Double,
    coroutineScope: CoroutineScope,
    clientsColor: Color
) {
    var clientsPayment by remember { mutableStateOf("") }
    var ancienCredit by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<ClientsInvoice>>(emptyList()) }
    var isPositive by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Reset clientsPayment when dialog is opened
    LaunchedEffect(showDialog) {
        if (showDialog) {
            clientsPayment = ""
            isPositive = true
            errorMessage = null
        }
    }

    suspend fun fetchRecentInvoices() {
        clientsId?.let { id ->
            isLoading = true
            errorMessage = null
            val firestore = Firebase.firestore
            try {
                val latestDoc = firestore.collection("F_ClientsArticlesFireS")
                    .document(id.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                ancienCredit = latestDoc.getDouble("ancienCredits") ?: 0.0

                val invoicesQuery = firestore.collection("F_ClientsArticlesFireS")
                    .document(id.toString())
                    .collection("Totale et Credit Des Bons")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)

                val invoicesSnapshot = invoicesQuery.get().await()
                recentInvoices = invoicesSnapshot.documents.mapNotNull { doc ->
                    ClientsInvoice(
                        date = doc.getString("date") ?: "",
                        totaleDeCeBon = doc.getDouble("totaleDeCeBon") ?: 0.0,
                        payeCetteFoit = doc.getDouble("payeCetteFoit") ?: 0.0,
                        creditFaitDonCeBon = doc.getDouble("creditFaitDonCeBon") ?: 0.0,
                        ancienCredits = doc.getDouble("ancienCredits") ?: 0.0
                    )
                }
            } catch (e: Exception) {
                errorMessage = "Error fetching data: ${e.message}"
                ancienCredit = 0.0
                recentInvoices = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun updateLatestDocument(clientsId: Long, deletedInvoiceDate: String) {
        val firestore = Firebase.firestore
        val latestDocRef = firestore.collection("F_ClientsArticlesFireS")
            .document(clientsId.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")

        val invoicesRef = firestore.collection("F_ClientsArticlesFireS")
            .document(clientsId.toString())
            .collection("Totale et Credit Des Bons")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)

        try {
            val latestInvoiceSnapshot = invoicesRef.get().await()
            if (!latestInvoiceSnapshot.isEmpty) {
                val latestInvoice = latestInvoiceSnapshot.documents[0]
                latestDocRef.set(latestInvoice.data!!).await()
            } else {
                latestDocRef.set(mapOf(
                    "ancienCredits" to 0.0,
                    "date" to "",
                    "totaleDeCeBon" to 0.0,
                    "payeCetteFoit" to 0.0,
                    "creditFaitDonCeBon" to 0.0
                )).await()
            }
        } catch (e: Exception) {
            errorMessage = "Error updating latest document: ${e.message}"
            throw e
        }
    }

    suspend fun deleteInvoice(invoiceDate: String) {
        clientsId?.let { id ->
            val firestore = Firebase.firestore
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
                    errorMessage = "No matching invoice found for deletion"
                }
            } catch (e: Exception) {
                errorMessage = "Error deleting invoice: ${e.message}"
                throw e
            }
        } ?: run {
            errorMessage = "Invalid clients ID"
            throw IllegalArgumentException("Invalid clients ID")
        }
    }

    LaunchedEffect(showDialog, clientsId) {
        if (showDialog && clientsId != null) {
            fetchRecentInvoices()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Manage Clients Credit: $clientsName", color = Color.White) },
            containerColor = clientsColor,
            text = {
                Column {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Current Credit + New Purchase Total: ${"%.2f".format(ancienCredit + clientsTotal)}", color = Color.White)
                        Text("Total of Current Invoice: ${"%.2f".format(clientsTotal)}", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        val paymentAmount = clientsPayment.toDoubleOrNull() ?: 0.0
                        val adjustedPayment = if (isPositive) paymentAmount else -paymentAmount
                        val newCredit = ancienCredit + clientsTotal - adjustedPayment
                        Text("New Credit Balance: ${"%.2f".format(newCredit)}", color = Color.White)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = clientsPayment,
                                onValueChange = { clientsPayment = it },
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
                                                            deleteInvoice(invoice.date)
                                                            fetchRecentInvoices()
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
                                val paymentAmount = clientsPayment.toDoubleOrNull() ?: 0.0
                                val adjustedPayment = if (isPositive) paymentAmount else -paymentAmount
                                try {
                                    updateClientsCredit(id.toInt(), clientsTotal, adjustedPayment, ancienCredit)
                                    fetchRecentInvoices()
                                    onDismiss()
                                } catch (e: Exception) {
                                    errorMessage = "Error updating credit: ${e.message}"
                                }
                            }
                        }
                    },
                    enabled = !isLoading
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

fun getDayOfWeekClients(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(dateString, formatter)
    return dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
}


// Update ClientsTabelle to include currentCreditBalance
data class ClientsTabelle(
    val vidSu: Long = 0,
    var idClientsSu: Long = 0,
    var nomClientsSu: String = "",
    var bonDuClientsSu: String = "",
    val couleurSu: String = "#FFFFFF", // Default color
    var currentCreditBalance: Double = 0.0 // New field for current credit balance
) {
    constructor() : this(0)
}

data class ClientsInvoice(
    val date: String,
    val totaleDeCeBon: Double,
    val payeCetteFoit: Double,
    val creditFaitDonCeBon: Double,
    val ancienCredits: Double
)
@Composable
fun AddClientsDialog(onDismiss: () -> Unit, onAddClients: (String) -> Unit) {
    var clientsName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Clients") },
        text = {
            OutlinedTextField(
                value = clientsName,
                onValueChange = { clientsName = it },
                label = { Text("Clients Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (clientsName.isNotBlank()) {
                        onAddClients(clientsName)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun Modifier.drawTextWithOutlineClients(outlineColor: Color) = this.drawBehind {
    drawRect(outlineColor, style = Stroke(width = 3f))
}
class CreditsClientsViewModel : ViewModel() {
    private val _clientsList = MutableStateFlow<List<ClientsTabelle>>(emptyList())
    val clientsList: StateFlow<List<ClientsTabelle>> = _clientsList.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val clientsRef = database.getReference("G_Clients")

    init {
        loadClients()
    }

    private fun loadClients() {
        clientsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = snapshot.children.mapNotNull { it.getValue(ClientsTabelle::class.java) }
                viewModelScope.launch {
                    newList.forEach { clients ->
                        clients.currentCreditBalance = getCurrentCreditBalance(clients.idClientsSu)
                    }
                    _clientsList.value = newList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading clients: ${error.message}")
            }
        })
    }

    private suspend fun getCurrentCreditBalance(clientsId: Long): Double {
        return withContext(Dispatchers.IO) {
            try {
                val firestore = Firebase.firestore
                val latestDoc = firestore.collection("F_ClientsArticlesFireS")
                    .document(clientsId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                latestDoc.getDouble("ancienCredits") ?: 0.0
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching current credit balance: ", e)
                0.0
            }
        }
    }

    fun updateClients(updatedClients: ClientsTabelle) {
        clientsRef.child(updatedClients.idClientsSu.toString()).setValue(updatedClients)
    }
    fun updateClientsBon(clientsId: Long, bonNumber: String) {
        clientsRef.child(clientsId.toString()).child("bonDuClientsSu").setValue(bonNumber)
    }

    fun addClients(name: String) {
        val maxId = _clientsList.value.maxOfOrNull { it.idClientsSu } ?: 0
        val newClients = ClientsTabelle(
            vidSu = System.currentTimeMillis(),
            idClientsSu = maxId + 1,
            nomClientsSu = name,
            bonDuClientsSu = "",
            couleurSu = generateRandomTropicalColor()
        )
        clientsRef.child(newClients.idClientsSu.toString()).setValue(newClients)
    }


    private fun generateRandomTropicalColor(): String {
        val hue = Random.nextFloat() * 360
        val saturation = 0.7f + Random.nextFloat() * 0.3f  // 70-100% saturation
        val value = 0.5f + Random.nextFloat() * 0.3f  // 50-80% value (darker colors)
        return "#%06X".format(0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }
    fun deleteClients(clientsId: Long) {
        clientsRef.child(clientsId.toString()).removeValue()
    }

    fun clearAllBonDuClientsSu() {
        _clientsList.value.forEach { clients ->
            clientsRef.child(clients.idClientsSu.toString()).child("bonDuClientsSu").setValue("")
        }
    }

}


