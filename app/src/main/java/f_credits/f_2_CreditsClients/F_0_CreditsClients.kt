package f_credits.f_2_CreditsClients

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ClientsTabelle
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.random.Random
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentCreditsClients(
    viewModel: CreditsClientsViewModel = viewModel(),
    onToggleNavBar: () -> Unit
) {
    val clients by viewModel.clientsList.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenuDialog by remember { mutableStateOf(false) }
    var showFloatingButtons by remember { mutableStateOf(true) }

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
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (showFloatingButtons) {
                    FloatingActionButton(
                        onClick = { onToggleNavBar() },
                        containerColor = Color.Red
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Toggle Navigation Bar",
                            tint = Color.White
                        )
                    }
                    FloatingActionButton(
                        onClick = { viewModel.toggleFilter() },
                        containerColor = Color.Red
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Toggle Filter",
                            tint = Color.White
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { showFloatingButtons = !showFloatingButtons },
                    containerColor = Color.Red
                ) {
                    Icon(
                        imageVector = if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showFloatingButtons) "Hide Buttons" else "Show Buttons",
                        tint = Color.White
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
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
            IconButton(
                onClick = { viewModel.deleteClients(clients.idClientsSu) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
            Column(
                modifier = Modifier
                    .weight(4f)
                    .padding(horizontal = 8.dp)
            ) {
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
            Row(
                modifier = Modifier.weight(3f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
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
    val context = LocalContext.current

    if (showCreditDialog) {
        ClientsCreditDialogClientsBoard(
            showDialog = true,
            onDismiss = { showCreditDialog = false },
            clientsId = clients.idClientsSu,
            clientsName = clients.nomClientsSu,
            clientsTotal = 0.0,
            coroutineScope = rememberCoroutineScope(),
            context = context,
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
    private val _showOnlyWithCredit = MutableStateFlow(true) // Changed to true by default

    val clientsList: StateFlow<List<ClientsTabelle>> = combine(_clientsList, _showOnlyWithCredit) { clients, onlyWithCredit ->
        if (onlyWithCredit) {
            clients.filter { it.currentCreditBalance != 0.0 }
        } else {
            clients
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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

    fun toggleFilter() {
        _showOnlyWithCredit.value = !_showOnlyWithCredit.value
    }
}
