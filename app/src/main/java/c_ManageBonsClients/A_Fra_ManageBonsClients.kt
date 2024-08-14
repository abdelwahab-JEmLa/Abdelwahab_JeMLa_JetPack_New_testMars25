package c_ManageBonsClients

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
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
import kotlin.math.round

@Composable
fun FragmentManageBonsClients() {
    var articles by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var clientsData by remember { mutableStateOf<List<ClientsTabelle>>(emptyList()) }

    var selectedArticleId by remember { mutableStateOf<Long?>(null) }
    var showClientDialog by remember { mutableStateOf(false) }
    var selectedClientFilter by remember { mutableStateOf<String?>(null) }
    var totalProfit by remember { mutableStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val clientsTableRef = Firebase.database.getReference("G_Clients")
    val articlesAcheteModeleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted")

    LaunchedEffect(Unit) {
        // Fetch ClientsTabelle data
        clientsTableRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                clientsData = dataSnapshot.children.mapNotNull { it.getValue(ClientsTabelle::class.java) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
        articlesAcheteModeleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newArticles = dataSnapshot.children.mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }
                articles = newArticles
                selectedArticleId?.let { id ->
                    if (newArticles.none { it.idArticle == id }) {
                        selectedArticleId = null
                    }
                }
                totalProfit = calculateTotalProfit(newArticles)
                updateTotalProfitInFirestore(totalProfit)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    Column {
        // Custom app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Bénéfice Total: ${String.format("%.2f", totalProfit)}Da",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = { showClientDialog = true }) {
                Icon(
                    imageVector = Icons.Default.AllInbox,
                    contentDescription = "Select Client",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            DisplayManageBonsClients(
                articles = articles.filter { selectedClientFilter == null || it.nomClient == selectedClientFilter },
                selectedArticleId = selectedArticleId,
                onArticleSelect = { selectedArticleId = it },
                coroutineScope = coroutineScope,
                listState = listState,
                paddingValues = PaddingValues(0.dp),
            )
        }
    }

    if (showClientDialog) {
        val distinctClients = articles.map { it.nomClient }.distinct().sorted()
        val numberedClients = distinctClients.mapIndexed { index, client ->
            "${index + 1}. $client" to client
        }

        ClientSelectionDialog(
            numberedClients = numberedClients,
            onClientSelected = { selectedClientName ->
                selectedClientFilter = selectedClientName
                showClientDialog = false
            },
            onDismiss = { showClientDialog = false },
            onClearFilter = {
                selectedClientFilter = null
                showClientDialog = false
            },
            calculateClientProfit = { clientName -> calculateClientProfit(articles, clientName) },
            articles = articles  // Ajout de ce paramètre
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayManageBonsClients(
    articles: List<ArticlesAcheteModele>,
    selectedArticleId: Long?,
    onArticleSelect: (Long?) -> Unit,
    coroutineScope: CoroutineScope,
    listState: LazyListState,
    paddingValues: PaddingValues,
) {
    var currentChangingField by remember { mutableStateOf("") }
    var activeClients by remember { mutableStateOf(emptySet<String>()) }
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var isDetailDisplayed by remember { mutableStateOf(false) }

    val sortedArticles = articles.sortedWith(compareBy({ it.nomClient }, { it.typeEmballage }))
    val groupedArticles = sortedArticles.groupBy { it.typeEmballage to it.nomClient }
    val clientTotals = articles.groupBy { it.nomClient }.mapValues { (_, clientArticles) ->
        clientArticles.filter { !it.nonTrouveState }.sumOf { article ->
            val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
                article.monPrixVentBM else article.monPrixVentFireStoreBM
            val arrondi = round(monPrixVentDetermineBM * 10) / 10
            arrondi * article.totalQuantity
        }
    }

    BoxWithConstraints(
        modifier = Modifier.padding(paddingValues)
    ) {
        val height = maxHeight
        var selectedItemOffset by remember { mutableFloatStateOf(0f) }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            groupedArticles.forEach { (groupKey, clientArticles) ->
                val (typeEmballage, nomClient) = groupKey
                stickyHeader(key = "${nomClient}_${typeEmballage}") {
                    // Extract clientId from the first article of the client
                    val clientId = clientArticles.firstOrNull()?.idArticle
                    ClientAndEmballageHeader(
                        nomClient = nomClient,
                        typeEmballage = typeEmballage,
                        onPrintClick = { verifiedClientArticles ->
                            coroutineScope.launch {
                                processClientData(context, nomClient, verifiedClientArticles)
                            }
                        },
                        onToggleActive = {
                            activeClients = if (activeClients.contains(nomClient)) {
                                activeClients - nomClient
                            } else {
                                activeClients + nomClient
                            }
                        },
                        isActive = activeClients.contains(nomClient),
                        articles = clientArticles,
                        allArticles = articles,
                        clientTotal = clientTotals[nomClient] ?: 0.0,
                        clientId = clientId // Pass the extracted clientId here
                    )
                }

                val filteredArticles = if (activeClients.contains(nomClient)) {
                    clientArticles.filter { !it.nonTrouveState &&
                            (it.monPrixVentFireStoreBM * it.totalQuantity != 0.0 || it.monPrixVentBM * it.totalQuantity != 0.0)
                    }
                } else {
                    clientArticles
                }

                items(filteredArticles.chunked(2), key = { it.map { article -> article.vid } }) { pairOfArticles ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (isDetailDisplayed) {
                            pairOfArticles.find { it.idArticle == selectedArticleId }?.let { article ->
                                DisplayDetailleArticle(
                                    article = article,
                                    currentChangingField = currentChangingField,
                                    onValueOutlineChange = {
                                        currentChangingField = it
                                    },
                                    focusRequester = focusRequester,
                                )
                                LaunchedEffect(selectedArticleId) {
                                    focusRequester.requestFocus()
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            pairOfArticles.forEach { article ->
                                ArticleBoardCard(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    article = article,
                                    onClickNonTrouveState = { clickedArticle ->
                                        updateNonTrouveState(clickedArticle)
                                    },
                                    onClickVerificated = { clickedArticle ->
                                        updateVerifieState(clickedArticle)
                                    },
                                    onArticleSelect = { selectedArticle ->
                                        if (selectedArticleId == selectedArticle.idArticle && isDetailDisplayed) {
                                            onArticleSelect(null)
                                            isDetailDisplayed = false
                                        } else {
                                            onArticleSelect(selectedArticle.idArticle)
                                            isDetailDisplayed = true
                                            coroutineScope.launch {
                                                val layoutInfo = listState.layoutInfo
                                                val visibleItemsInfo = layoutInfo.visibleItemsInfo
                                                val selectedItemInfo = visibleItemsInfo.find { it.key == selectedArticle.idArticle }

                                                selectedItemInfo?.let {
                                                    selectedItemOffset = it.offset.toFloat()
                                                    val scrollOffset = selectedItemOffset - paddingValues.calculateTopPadding().value
                                                    listState.animateScrollBy(scrollOffset)
                                                }
                                            }
                                        }
                                        currentChangingField = ""
                                    },
                                    isVerificationMode = activeClients.contains(article.nomClient),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


data class ClientsInvoiceOther(
    val date: String,
    val totaleDeCeBon: Double,
    val payeCetteFoit: Double,
    val creditFaitDonCeBon: Double,
    val ancienCredits: Double
)

@Composable
fun ClientsCreditDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    clientsId: Long?,
    clientsName: String,
    clientsTotal: Double,
    coroutineScope: CoroutineScope
) {
    var clientsPayment by remember { mutableStateOf("") }
    var ancienCredit by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<ClientsInvoiceOther>>(emptyList()) }

    // Reset clientsPayment when dialog is opened
    LaunchedEffect(showDialog) {
        if (showDialog) {
            clientsPayment = ""
        }
    }

    LaunchedEffect(showDialog, clientsId) {
        if (showDialog && clientsId != null) {
            isLoading = true
            val firestore = com.google.firebase.ktx.Firebase.firestore
            try {
                val latestDoc = firestore.collection("F_ClientsArticlesFireS")
                    .document(clientsId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                ancienCredit = latestDoc.getDouble("ancienCredits") ?: 0.0

                // Fetch recent invoices, excluding the "latest" document
                val invoicesQuery = firestore.collection("F_ClientsArticlesFireS")
                    .document(clientsId.toString())
                    .collection("Totale et Credit Des Bons")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)

                val invoicesSnapshot = invoicesQuery.get().await()
                recentInvoices = invoicesSnapshot.documents.mapNotNull { doc ->
                    ClientsInvoiceOther(
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
            title = { Text("Manage Clients Credit: $clientsName") },
            text = {
                Column {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Current Credit + New Purchase Total: ${"%.2f".format(ancienCredit + clientsTotal)}")
                        Text("Total of Current Invoice: ${"%.2f".format(clientsTotal)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = clientsPayment,
                            onValueChange = { clientsPayment = it },
                            label = { Text("Payment Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val paymentAmount = if((clientsPayment.toDoubleOrNull() ?: 0.0) == 0.0) ancienCredit else clientsPayment.toDoubleOrNull() ?: 0.0
                        val newCredit = ancienCredit + clientsTotal - paymentAmount
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
                            clientsId?.let { id ->
                                val paymentAmount = clientsPayment.toDoubleOrNull() ?: 0.0
                                updateClientsCredit(id.toInt(), clientsTotal, paymentAmount,ancienCredit)
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

@Composable
fun ClientAndEmballageHeader(
    nomClient: String,
    typeEmballage: String,
    onPrintClick: (List<ArticlesAcheteModele>) -> Unit,
    onToggleActive: () -> Unit,
    isActive: Boolean,
    articles: List<ArticlesAcheteModele>,
    allArticles: List<ArticlesAcheteModele>,
    clientTotal: Double,
    clientId: Long?
) {
    var showPrintDialog by remember { mutableStateOf(false) }
    var showClientsBonUpdateDialog by remember { mutableStateOf(false) }
    var ancienCredits by remember { mutableStateOf(0.0) }
    val verifiedCount = allArticles.count { it.nomClient == nomClient && it.verifieState }
    val clientColor = remember(nomClient) { generateClientColor(nomClient) }
    val clientProfit = calculateClientProfit(allArticles, nomClient)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(clientId) {
        if (clientId != null) {
            val firestore = Firebase.firestore
            try {
                val latestDoc = firestore.collection("F_ClientsArticlesFireS")
                    .document(clientId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                ancienCredits = latestDoc.getDouble("ancienCredits") ?: 0.0
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching ancienCredits: ", e)
            }
        }
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
                    text = "Ancien Crédit: ${String.format("%.2f", ancienCredits)}Da",
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
                        updateClientsCredit(clientId.toInt(), clientTotal, 0.0, ancienCredits)
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
            coroutineScope = coroutineScope
        )
    }
}

suspend fun updateClientsCredit(
    clientId: Int,
    clientsTotal: Double,
    clientsPayment: Double,
    ancienCredit: Double
) {
    val firestore = Firebase.firestore
    val currentDateTime = LocalDateTime.now()
    val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)

    val restCreditDeCetteBon = clientsTotal - clientsPayment
    val newTotalCredit = ancienCredit + restCreditDeCetteBon

    val data = hashMapOf(
        "date" to formattedDateTime,
        "totaleDeCeBon" to clientsTotal,
        "payeCetteFoit" to clientsPayment,
        "creditFaitDonCeBon" to restCreditDeCetteBon,
        "ancienCredits" to newTotalCredit
    )

    try {
        val documentId = "Bon($dayOfWeek)${formattedDateTime}=${"%.2f".format(clientsTotal)}"
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
@Entity
data class ArticlesAcheteModele(
    @PrimaryKey(autoGenerate = true) val vid: Long = 0,
    val idArticle: Long = 0,
    val nomArticleFinale: String = "",
    val prixAchat: Double = 0.0,
    val nmbrunitBC: Double = 0.0,
    val clientPrixVentUnite: Double = 0.0,
    val nomClient: String = "",
    val dateDachate: String = "",
    val nomCouleur1: String = "",
    val quantityAcheteCouleur1: Int = 0,
    val nomCouleur2: String = "",
    val quantityAcheteCouleur2: Int = 0,
    val nomCouleur3: String = "",
    val quantityAcheteCouleur3: Int = 0,
    val nomCouleur4: String = "",
    val quantityAcheteCouleur4: Int = 0,
    val totalQuantity: Int = 0,
    val nonTrouveState: Boolean = false,
    val verifieState: Boolean = false,
    var changeCaronState: String = "",
    var monPrixAchatUniterBC: Double =  0.0,
    var benificeDivise: Double =  0.0,

    //Stats
    var typeEmballage: String = "",
    var choisirePrixDepuitFireStoreOuBaseBM: String = "",

    //FireBase PrixEditeur
    val monPrixVentBM: Double = 0.0,
    var monPrixVentUniterBM: Double =  0.0,

    var monBenificeBM: Double =  0.0,
    var monBenificeUniterBM: Double =  0.0,
    var totalProfitBM: Double =  0.0,


    var clientBenificeBM: Double =  0.0,

    //FireStore
    var monPrixVentFireStoreBM: Double =  0.0,
    var monPrixVentUniterFireStoreBM: Double =  0.0,

    var monBenificeFireStoreBM: Double =  0.0,
    var monBenificeUniterFireStoreBM: Double =  0.0,
    var totalProfitFireStoreBM: Double =  0.0,

    var clientBenificeFireStoreBM: Double =  0.0,

    ) {
    // Constructeur sans argument nécessaire pour Firebase
    constructor() : this(0)
    fun getColumnValue(columnName: String): Any = when (columnName) {
        "nomArticleFinale" -> nomArticleFinale

        "clientPrixVentUnite" -> clientPrixVentUnite
        "nmbrunitBC" -> nmbrunitBC
        "prixAchat" -> prixAchat
        "monPrixAchatUniterBC" -> monPrixAchatUniterBC

        "benificeDivise" -> benificeDivise
        "totalQuantity" -> totalQuantity


        //FireBase PrixEditeur
        "monPrixVentBM" -> monPrixVentBM
        "monPrixVentUniterBM" -> monPrixVentUniterBM

        "monBenificeBM" -> monBenificeBM
        "monBenificeUniterBM" -> monBenificeUniterBM
        "totalProfitBM" -> totalProfitBM

        "clientBenificeBM" -> clientBenificeBM

        //FireStore
        "monPrixVentFireStoreBM" -> monPrixVentFireStoreBM
        "monPrixVentUniterFireStoreBM" -> monPrixVentUniterFireStoreBM

        "monBenificeFireStoreBM" -> monBenificeFireStoreBM
        "monBenificeUniterFireStoreBM" -> monBenificeUniterFireStoreBM
        "totalProfitFireStoreBM" -> totalProfitFireStoreBM

        "clientBenificeFireStoreBM" -> clientBenificeFireStoreBM

        else -> ""
    }
}