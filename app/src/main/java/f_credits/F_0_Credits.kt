package f_credits

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
import d_EntreBonsGro.updateSupplierCredit
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
fun FragmentCredits(viewModel: CreditsViewModel = viewModel()) {
    val suppliers by viewModel.supplierList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Credits", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Red),
                actions = {
                    IconButton(onClick = { showMenuDialog = true }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Supplier", tint = Color.White)
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
            items(suppliers) { supplier ->
                SupplierItem(supplier, viewModel)
            }
        }
    }

    if (showAddDialog) {
        AddSupplierDialog(
            onDismiss = { showAddDialog = false },
            onAddSupplier = { name ->
                viewModel.addSupplier(name)
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
                        viewModel.clearAllBonDuSupplierSu()
                        showMenuDialog = false
                    }) {
                        Text("Clear all bonDuSupplierSu")
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
fun SupplierItem(supplier: SupplierTabelle, viewModel: CreditsViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var showCreditDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val backgroundColor = remember(supplier.couleurSu) {
        try {
            Color(android.graphics.Color.parseColor(supplier.couleurSu))
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
            IconButton(onClick = { viewModel.deleteSupplier(supplier.idSupplierSu) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
            Column {
                Text(
                    text = "ID: ${supplier.idSupplierSu}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.drawTextWithOutline(Color.Black)
                )
                Text(
                    text = "Name: ${supplier.nomSupplierSu}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.drawTextWithOutline(Color.Black)
                )
                if (supplier.bonDuSupplierSu.isNotBlank()) {
                    Text(
                        text = "Bon N°: ${supplier.bonDuSupplierSu}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        modifier = Modifier.drawTextWithOutline(Color.Black)
                    )
                }
                Text(
                    text = "Credit Balance: ${supplier.currentCreditBalance}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    modifier = Modifier.drawTextWithOutline(Color.Black)
                )
            }
            Row {
                IconButton(onClick = { showCreditDialog = true }) {
                    Icon(Icons.Default.CreditCard, contentDescription = "Credit", tint = Color.White)
                }
                IconButton(onClick = { showDialog = true }) {
                    val icon = if (supplier.bonDuSupplierSu.isNotBlank()) {
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
                                        viewModel.updateSupplierBon(supplier.idSupplierSu, j.toString())
                                        showDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (supplier.bonDuSupplierSu == j.toString()) Color.Red else MaterialTheme.colorScheme.primary
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
        SupplierCreditDialog(
            showDialog = true,
            onDismiss = { showCreditDialog = false },
            supplierId = supplier.idSupplierSu,
            supplierName = supplier.nomSupplierSu,
            supplierTotal = 0.0, // You may want to pass the actual total here
            coroutineScope = rememberCoroutineScope(),
            supplierColor = backgroundColor // Pass the background color here
        )
    }

    if (showEditDialog) {
        EditSupplierDialog(
            supplier = supplier,
            onDismiss = { showEditDialog = false },
            onEditSupplier = { editedSupplier ->
                viewModel.updateSupplier(editedSupplier)
                showEditDialog = false
            }
        )
    }
}

@Composable
fun EditSupplierDialog(supplier: SupplierTabelle, onDismiss: () -> Unit, onEditSupplier: (SupplierTabelle) -> Unit) {
    var editedName by remember { mutableStateOf(supplier.nomSupplierSu) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Supplier") },
        text = {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                label = { Text("Supplier Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (editedName.isNotBlank()) {
                        onEditSupplier(supplier.copy(nomSupplierSu = editedName))
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
fun SupplierCreditDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    supplierId: Long?,
    supplierName: String,
    supplierTotal: Double,
    coroutineScope: CoroutineScope,
    supplierColor: Color
) {
    var supplierPayment by remember { mutableStateOf("") }
    var ancienCredit by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<SupplierInvoice>>(emptyList()) }
    var isPositive by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Reset supplierPayment when dialog is opened
    LaunchedEffect(showDialog) {
        if (showDialog) {
            supplierPayment = ""
            isPositive = true
            errorMessage = null
        }
    }

    suspend fun fetchRecentInvoices() {
        supplierId?.let { id ->
            isLoading = true
            errorMessage = null
            val firestore = Firebase.firestore
            try {
                val latestDoc = firestore.collection("F_SupplierArticlesFireS")
                    .document(id.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                ancienCredit = latestDoc.getDouble("ancienCredits") ?: 0.0

                val invoicesQuery = firestore.collection("F_SupplierArticlesFireS")
                    .document(id.toString())
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
                errorMessage = "Error fetching data: ${e.message}"
                ancienCredit = 0.0
                recentInvoices = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun updateLatestDocument(supplierId: Long, deletedInvoiceDate: String) {
        val firestore = Firebase.firestore
        val latestDocRef = firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")

        val invoicesRef = firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
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
        supplierId?.let { id ->
            val firestore = Firebase.firestore
            val invoiceRef = firestore.collection("F_SupplierArticlesFireS")
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
            errorMessage = "Invalid supplier ID"
            throw IllegalArgumentException("Invalid supplier ID")
        }
    }

    LaunchedEffect(showDialog, supplierId) {
        if (showDialog && supplierId != null) {
            fetchRecentInvoices()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Manage Supplier Credit: $supplierName", color = Color.White) },
            containerColor = supplierColor,
            text = {
                Column {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text("Current Credit + New Purchase Total: ${"%.2f".format(ancienCredit + supplierTotal)}", color = Color.White)
                        Text("Total of Current Invoice: ${"%.2f".format(supplierTotal)}", color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        val paymentAmount = supplierPayment.toDoubleOrNull() ?: 0.0
                        val adjustedPayment = if (isPositive) paymentAmount else -paymentAmount
                        val newCredit = ancienCredit + supplierTotal - adjustedPayment
                        Text("New Credit Balance: ${"%.2f".format(newCredit)}", color = Color.White)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = supplierPayment,
                                onValueChange = { supplierPayment = it },
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
                                                Text("Date: ${invoice.date} (${getDayOfWeek(invoice.date)})")
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
                            supplierId?.let { id ->
                                val paymentAmount = supplierPayment.toDoubleOrNull() ?: 0.0
                                val adjustedPayment = if (isPositive) paymentAmount else -paymentAmount
                                try {
                                    updateSupplierCredit(id.toInt(), supplierTotal, adjustedPayment, ancienCredit)
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

fun getDayOfWeek(dateString: String): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val dateTime = LocalDateTime.parse(dateString, formatter)
    return dateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
}


// Update SupplierTabelle to include currentCreditBalance
data class SupplierTabelle(
    val vidSu: Long = 0,
    var idSupplierSu: Long = 0,
    var nomSupplierSu: String = "",
    var bonDuSupplierSu: String = "",
    val couleurSu: String = "#FFFFFF", // Default color
    var currentCreditBalance: Double = 0.0 // New field for current credit balance
) {
    constructor() : this(0)
}

data class SupplierInvoice(
    val date: String,
    val totaleDeCeBon: Double,
    val payeCetteFoit: Double,
    val creditFaitDonCeBon: Double,
    val ancienCredits: Double
)
@Composable
fun AddSupplierDialog(onDismiss: () -> Unit, onAddSupplier: (String) -> Unit) {
    var supplierName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Supplier") },
        text = {
            OutlinedTextField(
                value = supplierName,
                onValueChange = { supplierName = it },
                label = { Text("Supplier Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (supplierName.isNotBlank()) {
                        onAddSupplier(supplierName)
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

fun Modifier.drawTextWithOutline(outlineColor: Color) = this.drawBehind {
    drawRect(outlineColor, style = Stroke(width = 3f))
}
class CreditsViewModel : ViewModel() {
    private val _supplierList = MutableStateFlow<List<SupplierTabelle>>(emptyList())
    val supplierList: StateFlow<List<SupplierTabelle>> = _supplierList.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val suppliersRef = database.getReference("F_Suppliers")

    init {
        loadSuppliers()
    }

    private fun loadSuppliers() {
        suppliersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = snapshot.children.mapNotNull { it.getValue(SupplierTabelle::class.java) }
                viewModelScope.launch {
                    newList.forEach { supplier ->
                        supplier.currentCreditBalance = getCurrentCreditBalance(supplier.idSupplierSu)
                    }
                    _supplierList.value = newList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading suppliers: ${error.message}")
            }
        })
    }

    private suspend fun getCurrentCreditBalance(supplierId: Long): Double {
        return withContext(Dispatchers.IO) {
            try {
                val firestore = Firebase.firestore
                val latestDoc = firestore.collection("F_SupplierArticlesFireS")
                    .document(supplierId.toString())
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

    fun updateSupplier(updatedSupplier: SupplierTabelle) {
        suppliersRef.child(updatedSupplier.idSupplierSu.toString()).setValue(updatedSupplier)
    }
    fun updateSupplierBon(supplierId: Long, bonNumber: String) {
        suppliersRef.child(supplierId.toString()).child("bonDuSupplierSu").setValue(bonNumber)
    }

    fun addSupplier(name: String) {
        val maxId = _supplierList.value.maxOfOrNull { it.idSupplierSu } ?: 0
        val newSupplier = SupplierTabelle(
            vidSu = System.currentTimeMillis(),
            idSupplierSu = maxId + 1,
            nomSupplierSu = name,
            bonDuSupplierSu = "",
            couleurSu = generateRandomTropicalColor()
        )
        suppliersRef.child(newSupplier.idSupplierSu.toString()).setValue(newSupplier)
    }


    private fun generateRandomTropicalColor(): String {
        val hue = Random.nextFloat() * 360
        val saturation = 0.7f + Random.nextFloat() * 0.3f  // 70-100% saturation
        val value = 0.5f + Random.nextFloat() * 0.3f  // 50-80% value (darker colors)
        return "#%06X".format(0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
    }
    fun deleteSupplier(supplierId: Long) {
        suppliersRef.child(supplierId.toString()).removeValue()
    }

    fun clearAllBonDuSupplierSu() {
        _supplierList.value.forEach { supplier ->
            suppliersRef.child(supplier.idSupplierSu.toString()).child("bonDuSupplierSu").setValue("")
        }
    }

}


