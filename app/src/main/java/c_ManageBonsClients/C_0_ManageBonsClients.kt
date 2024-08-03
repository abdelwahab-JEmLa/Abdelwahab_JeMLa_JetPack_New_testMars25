package c_ManageBonsClients

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun C_ManageBonsClients() {
    var articles by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var selectedArticleId by remember { mutableStateOf<Long?>(null) }
    var showClientDialog by remember { mutableStateOf(false) }
    var selectedClientFilter by remember { mutableStateOf<String?>(null) }
    var totalProfit by remember { mutableStateOf(0.0) }
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
                totalProfit = calculateTotalProfit(newArticles)
                updateTotalProfitInFirestore(totalProfit)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("ManageBonsClients")
                        Text(
                            "Bénéfice Total: ${String.format("%.2f", totalProfit)}Da",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showClientDialog = true }) {
                        Icon(imageVector = Icons.Default.AllInbox, contentDescription = "Select Client")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
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
                },
                calculateClientProfit = { clientName -> calculateClientProfit(articles, clientName) },
                articles = articles  // Ajout de ce paramètre
            )
        }
    }
}

// Title: Calculate Total Profit
// Cette fonction calcule le bénéfice total pour tous les articles
fun calculateTotalProfit(articles: List<ArticlesAcheteModele>): Double {
    return articles.sumOf { article ->
        val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
            article.monPrixVentBM else article.monPrixVentFireStoreBM
        val prixVente = round(monPrixVentDetermineBM * 10) / 10
        val prixAchatC = if (article.prixAchat == 0.0) prixVente else article.prixAchat
        val profit = prixVente - prixAchatC
        profit * article.totalQuantity
    }
}

// Title: Calculate Client Profit
// Cette fonction calcule le bénéfice total pour un client donné
fun calculateClientProfit(articles: List<ArticlesAcheteModele>, clientName: String): Double {
    return articles.filter { it.nomClient == clientName && !it.nonTrouveState }
        .sumOf { article ->
            val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
                article.monPrixVentBM else article.monPrixVentFireStoreBM
            val prixVente = round(monPrixVentDetermineBM * 10) / 10
            val prixAchatC = if (article.prixAchat == 0.0) prixVente else article.prixAchat
            val profit = prixVente - prixAchatC
            profit * article.totalQuantity
        }
}

// Titre: Correction de la fonction ClientSelectionDialog pour inclure la liste des articles
@Composable
fun ClientSelectionDialog(
    numberedClients: List<Pair<String, String>>,
    onClientSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit,
    calculateClientProfit: (String) -> Double,
    articles: List<ArticlesAcheteModele>  // Ajout de ce paramètre
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = generateClientColor(clientName)
                        )
                    ) {
                        TextButton(
                            onClick = { onClientSelected(clientName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(numberedClient, color = Color.Black)
                                Text(
                                    "Bénéfice: ${String.format("%.2f", calculateClientProfit(clientName))}Da",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                // Titre: Calcul et affichage du total client
                                val clientTotal = calculateClientTotal(articles.filter { it.nomClient == clientName })
                                Text(
                                    "Total: ${String.format("%.2f", clientTotal)}Da",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
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
// Titre: Fonction pour calculer le total du client
fun calculateClientTotal(clientArticles: List<ArticlesAcheteModele>): Double {
    return clientArticles.filter { !it.nonTrouveState }.sumOf { article ->
        val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")
            article.monPrixVentBM else article.monPrixVentFireStoreBM
        round(monPrixVentDetermineBM * 10) / 10 * article.totalQuantity
    }
}
fun updateTotalProfitInFirestore(totalProfit: Double) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val db = Firebase.firestore
    db.collection("Benifice Du Jour").document(currentDate)
        .set(mapOf("benifice" to totalProfit))
        .addOnSuccessListener { println("Bénéfice mis à jour avec succès") }
        .addOnFailureListener { e -> println("Erreur lors de la mise à jour du bénéfice: $e") }
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
                            clientTotal = clientTotals[nomClient] ?: 0.0
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
    val verifiedCount = allArticles.count { it.nomClient == nomClient && it.verifieState }
    val clientColor = remember(nomClient) { generateClientColor(nomClient) }
    val clientProfit = calculateClientProfit(allArticles, nomClient)

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
            // New IconButton for creating an empty article
            IconButton(onClick = { createEmptyArticle(nomClient) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Empty Article",
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
                onPrintClick()
                showPrintDialog = false
            },
            onDismiss = { showPrintDialog = false }
        )
    }
}
fun createEmptyArticle(nomClient: String) {
    val database = FirebaseDatabase.getInstance()
    val articleRef = database.getReference("ArticlesAcheteModeleAdapted")

    articleRef.get().addOnSuccessListener { dataSnapshot ->
        val maxId = dataSnapshot.children.mapNotNull { it.child("idArticle").getValue(Long::class.java) }.maxOrNull() ?: 0
        val newId = maxId + 1

        val emptyArticle = ArticlesAcheteModele(
            idArticle = newId,
            nomArticleFinale = "New Empty Article",
            nomClient = nomClient,
            typeEmballage = "Boit", // Default value
            choisirePrixDepuitFireStoreOuBaseBM = "CardFireBase" // Default value
        )

        articleRef.child(newId.toString()).setValue(emptyArticle)
    }.addOnFailureListener { exception ->
        // Handle any errors here
        println("Error creating empty article: ${exception.message}")
    }
}

