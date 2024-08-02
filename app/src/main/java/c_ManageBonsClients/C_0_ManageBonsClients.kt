package c_ManageBonsClients

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun C_ManageBonsClients() {
    var articles by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var selectedArticleId by remember { mutableStateOf<Long?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showClientDialog by remember { mutableStateOf(false) }
    var selectedClientFilter by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val articlesAcheteModeleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted")

    LaunchedEffect(Unit) {
        articlesAcheteModeleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newArticles = dataSnapshot.children.mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }
                articles = newArticles
                selectedArticleId?.let { id ->
                    if (newArticles.none { it.idArticle == id }) {
                        selectedArticleId = null
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ManageBonsClients") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Filter")
                    }
                    IconButton(onClick = { showClientDialog = true }) {
                        Icon(imageVector = Icons.Default.AllInbox, contentDescription = "Select Client")
                    }
                }
            )
        }
    ) { paddingValues ->
        KeyboardAwareLayout {
            DisplayManageBonsClients(
                articles = articles.filter { selectedClientFilter == null || it.nomClient == selectedClientFilter },
                selectedArticleId = selectedArticleId,
                onArticleSelect = { selectedArticleId = it },
                coroutineScope = coroutineScope,
                listState = listState,
                paddingValues = paddingValues,
            )
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
                }
            )
        }
    }
}

@Composable
fun ClientSelectionDialog(
    numberedClients: List<Pair<String, String>>,
    onClientSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Client") },
        text = {
            LazyColumn {
                item {
                    TextButton(
                        onClick = onClearFilter,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Filter")
                    }
                }
                items(numberedClients.size) { index ->
                    val (numberedClient, clientName) = numberedClients[index]
                    TextButton(
                        onClick = { onClientSelected(clientName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(numberedClient)
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

    // Sort articles by typeEmballage and then by nomClient
    val sortedArticles = articles.sortedWith(compareBy({ it.nomClient }, { it.typeEmballage }))

    // Group sorted articles by typeEmballage and nomClient
    val groupedArticles = sortedArticles.groupBy { it.typeEmballage to it.nomClient }
    // Calculate total for each client (only for non-missing articles)
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
        KeyboardAwareLayout {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                groupedArticles.forEach { (groupKey, clientArticles) ->
                    val (typeEmballage, nomClient) = groupKey
                    stickyHeader {
                        ClientAndEmballageHeader(
                            nomClient = nomClient,
                            typeEmballage = typeEmballage,
                            onPrintClick = {
                                coroutineScope.launch {
                                    processClientData(context, nomClient)
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
                            clientTotal = clientTotals[nomClient] ?: 0.0 // Pass the calculated total for non-missing articles
                        )
                    }

                    val filteredArticles = if (activeClients.contains(nomClient)) {
                        clientArticles.filter { !it.nonTrouveState &&
                                (it.monPrixVentFireStoreBM * it.totalQuantity != 0.0 || it.monPrixVentBM * it.totalQuantity != 0.0)
                        }
                    } else {
                        clientArticles
                    }

                    items(filteredArticles.chunked(2)) { pairOfArticles ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                pairOfArticles.forEach { article ->
                                    ArticleBoardCard(
                                        //TODO ajoute un petit espace entre chaque card d article

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
                        }
                    }
                }
            }
        }
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

fun generateClientColor(clientName: String): Color {
    val hash = abs(clientName.hashCode())
    val hue = (hash % 360).toFloat()
    return Color.hsl(hue, 0.4f, 0.6f)
}



@Composable
fun ClientAndEmballageHeader(
    nomClient: String,
    typeEmballage: String,
    onPrintClick: () -> Unit,
    onToggleActive: () -> Unit,
    isActive: Boolean,
    articles: List<ArticlesAcheteModele>,
    allArticles: List<ArticlesAcheteModele>,
    clientTotal: Double
) {
    var showPrintDialog by remember { mutableStateOf(false) }

    // Count verified articles for the entire client
    val verifiedCount = allArticles.count { it.nomClient == nomClient && it.verifieState }

    // Generate a consistent color for this client
    val clientColor = remember(nomClient) { generateClientColor(nomClient) }

    // Calculate total profit for the client
    val clientProfit = allArticles
        .filter { it.nomClient == nomClient && !it.nonTrouveState }
        .sumOf { article ->
            val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
                article.monPrixVentBM else article.monPrixVentFireStoreBM
            val prixVente = round(monPrixVentDetermineBM * 10) / 10
            val prixAchatC = if (article.prixAchat ==0.0 ) prixVente else article.prixAchat
            val profit = prixVente - prixAchatC
            profit * article.totalQuantity
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(clientColor)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$nomClient: $typeEmballage",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { showPrintDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = "Print",
                    tint = Color.Black
                )
            }
            IconButton(onClick = onToggleActive) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Check else Icons.Default.FilterList,
                    contentDescription = "Toggle Verification and Filter",
                    tint = Color.Black
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Display the total profit for this client
            Text(
                text = "${String.format("%.2f", clientProfit)}Da",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
            // Display the total for this client (non-missing articles)
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
                onPrintClick()
                showPrintDialog = false
            },
            onDismiss = { showPrintDialog = false }
        )
    }
}