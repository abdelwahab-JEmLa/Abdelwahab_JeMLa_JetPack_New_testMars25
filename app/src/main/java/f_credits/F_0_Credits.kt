package f_credits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            }
            IconButton(onClick = { showDialog = true }) {
                val icon = if (supplier.bonDuSupplierSu.isNotBlank()) {
                    Icons.Default.ReceiptLong
                } else {
                    Icons.Default.Receipt
                }
                Icon(icon, contentDescription = "Invoice", tint = Color.White)
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
}
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
                _supplierList.value = newList
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading suppliers: ${error.message}")
            }
        })
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
data class SupplierTabelle(
    val vidSu: Long = 0,
    var idSupplierSu: Long = 0,
    var nomSupplierSu: String = "",
    var bonDuSupplierSu: String = "",
    val couleurSu: String = "#FFFFFF" // Default color
) {
    constructor() : this(0)
}