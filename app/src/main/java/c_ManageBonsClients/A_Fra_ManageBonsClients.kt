package com.example.abdelwahabjemlajetpack.c_ManageBonsClients

import android.content.Context
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.room.Entity
import androidx.room.PrimaryKey
import c_ManageBonsClients.ArticleBoardCard
import c_ManageBonsClients.ClientAndEmballageHeader
import c_ManageBonsClients.ClientSelectionDialog
import c_ManageBonsClients.DisplayDetailleArticle
import c_ManageBonsClients.updateNonTrouveState
import c_ManageBonsClients.updateTotalProfitInFirestore
import c_ManageBonsClients.updateVerifieState
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
import kotlin.random.Random

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
    val focusRequester = remember { FocusRequester() }
    var isDetailDisplayed by remember { mutableStateOf(false) }

    val sortedArticles = articles.sortedWith(compareBy({ it.nomClient }, { it.typeEmballage }))
    val groupedArticles = sortedArticles.groupBy { it.typeEmballage to it.nomClient }
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



fun addNewClient(name: String, onComplete: (Long) -> Unit) {
    val clientsTableRef = Firebase.database.getReference("G_Clients")
    clientsTableRef.orderByChild("idClientsSu").limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val maxId = if (snapshot.exists()) {
                snapshot.children.first().getValue(ClientsTabelle::class.java)?.idClientsSu ?: 0
            } else {
                0
            }
            val newClients = ClientsTabelle(
                vidSu = System.currentTimeMillis(),
                idClientsSu = maxId + 1,
                nomClientsSu = name,
                bonDuClientsSu = "",
                couleurSu = generateRandomTropicalColor()
            )
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