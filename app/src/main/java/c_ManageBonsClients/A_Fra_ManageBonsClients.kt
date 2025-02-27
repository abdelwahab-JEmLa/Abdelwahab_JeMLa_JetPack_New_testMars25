package c_ManageBonsClients

import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import a_MainAppCompnents.ArticlesAcheteModele
import a_MainAppCompnents.HeadOfViewModels
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import g_BoardStatistiques.BoardStatistiquesStatViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.round
import kotlin.random.Random

@Composable
fun FragmentManageBonsClients(boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel,
                              headOfViewModels: HeadOfViewModels,
) {
    var articles by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var clientsData by remember { mutableStateOf<List<B_ClientsDataBase>>(emptyList()) }

    var selectedArticleId by remember { mutableStateOf<Long?>(null) }
    var showClientDialog by remember { mutableStateOf(false) }
    var selectedClientFilter by remember { mutableStateOf<String?>(null) }
    var totalProfit by remember { mutableDoubleStateOf(0.0) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val clientsTableRef = Firebase.database.getReference("G_Clients")
    val articlesAcheteModeleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted")
    var lastFocusedColumn by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // Fetch B_ClientsDataBase data
        clientsTableRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                clientsData = dataSnapshot.children.mapNotNull { it.getValue(B_ClientsDataBase::class.java) }
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
                onArticleSelect = { selectedArticleId = it
                    lastFocusedColumn=""
                                  },
                coroutineScope = coroutineScope,
                listState = listState,
                paddingValues = PaddingValues(0.dp),
                boardStatistiquesStatViewModel = boardStatistiquesStatViewModel, onFocuseChange = {lastFocusedColumn=""},lastFocusedColumn,
                headOfViewModels = headOfViewModels,
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
    boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel,
    onFocuseChange: () -> Unit,
    lastFocusedColumn: String,
    headOfViewModels: HeadOfViewModels,
) {
    var currentChangingField by remember { mutableStateOf("") }
    var activeClients by remember { mutableStateOf(emptySet<String>()) }
    val focusRequester = remember { FocusRequester() }
    var isDetailDisplayed by remember { mutableStateOf(false) }

    val sortedArticles = articles.sortedWith(compareBy({ it.nomClient }, { it.idArticlePlaceInCamionette }))
    val groupedArticles = sortedArticles.groupBy { it.idArticlePlaceInCamionette to it.nomClient }
    val clientTotals = articles.groupBy { it.nomClient }.mapValues { (_, clientArticles) ->
        clientArticles.filter {
            it.verifieState
        }.sumOf { article ->
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
                    ClientAndEmballageHeader(
                        nomClient = nomClient,
                        typeEmballage = typeEmballage,
                        onToggleActive = {
                            activeClients = if (activeClients.contains(nomClient)) {
                                activeClients - nomClient
                            } else {
                                activeClients + nomClient
                            }
                        },
                        isActive = activeClients.contains(nomClient),
                        allArticles = articles,
                        clientTotal = clientTotals[nomClient] ?: 0.0,
                        boardStatistiquesStatViewModel = boardStatistiquesStatViewModel,
                        headOfViewModels = headOfViewModels
                    )
                }

                val filteredArticles = if (activeClients.contains(nomClient)) {
                    clientArticles.filter { !it.nonTrouveState }
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
                                    onFocuseChange = onFocuseChange,
                                    lastFocusedColumn = lastFocusedColumn,
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
                                    headOfViewModels = headOfViewModels,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add this effect to close the detail view when scrolling past it
        LaunchedEffect(listState) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { firstVisibleItem ->
                    if (isDetailDisplayed && firstVisibleItem > 0) {
                        val visibleItems = listState.layoutInfo.visibleItemsInfo
                        val selectedItemVisible = visibleItems.any { it.key == selectedArticleId }
                        if (!selectedItemVisible) {
                            isDetailDisplayed = false
                            onArticleSelect(null)
                        }
                    }
                }
        }
    }
}

fun addNewClient(name: String, onComplete: (Long) -> Unit) {
    val clientsTableRef = Firebase.database.getReference("G_Clients")
    clientsTableRef.orderByChild("idClientsSu").limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val maxId = if (snapshot.exists()) {
                snapshot.children.first().getValue(B_ClientsDataBase::class.java)?.id ?: 0
            } else {
                0
            }
            val newClients = B_ClientsDataBase(
                id = maxId + 1,
                nom = name,
            ) .apply {
                statueDeBase.couleur = generateRandomTropicalColor()
            }
            clientsTableRef.child((maxId + 1).toString()).setValue(newClients)
                .addOnSuccessListener {
                    onComplete(maxId + 1)
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error adding new client: ${e.message}")
                }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error fetching max client ID: ${error.message}")
        }
    })
}

private fun generateRandomTropicalColor(): String {
    val hue = Random.nextFloat() * 360
    val saturation = 0.7f + Random.nextFloat() * 0.3f  // 70-100% saturation
    val value = 0.5f + Random.nextFloat() * 0.3f  // 50-80% value (darker colors)
    return "#%06X".format(0xFFFFFF and android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value)))
}

