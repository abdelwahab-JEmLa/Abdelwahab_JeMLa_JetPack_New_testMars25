package c_ManageBonsClients

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.AutoResizedText
import b_Edite_Base_Donne.capitalizeFirstLetter
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun C_ManageBonsClients () {
    var articles by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var selectedArticleId by remember { mutableStateOf<Long?>(null) } // Changed from String? to Long?
    var showDialog by remember { mutableStateOf(false) }
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
                }
            )
        }
    ) { paddingValues ->
        DisplayManageBonsClients(
            articles = articles,
            selectedArticleId = selectedArticleId,
            onArticleSelect = { selectedArticleId = it },
            coroutineScope = coroutineScope,
            listState = listState,
            paddingValues = paddingValues
        )
    }
}

@Composable
fun KeyboardAwareLayout(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val windowInsets = WindowInsets.ime

    val imeHeight by remember {
        derivedStateOf {
            windowInsets.getBottom(density)
        }
    }

    val imeVisible by remember {
        derivedStateOf {
            imeHeight > 0
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = with(density) { imeHeight.toDp() })
    ) {
        content()
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
    paddingValues: PaddingValues
) {
    var currentChangingField by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    var activeClients by remember { mutableStateOf(emptySet<String>()) }
    val context = LocalContext.current

    // Group articles by nomClient
    val groupedArticles = articles.groupBy { it.nomClient }

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
            groupedArticles.forEach { (nomClient, clientArticles) ->
                stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nomClient,
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    processClientData(context, nomClient)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = "Print",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(
                            onClick = {
                                activeClients = if (activeClients.contains(nomClient)) {
                                    activeClients - nomClient
                                } else {
                                    activeClients + nomClient
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (activeClients.contains(nomClient)) Icons.Default.Check else Icons.Default.FilterList,
                                contentDescription = "Toggle Verification and Filter",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                val filteredArticles = if (activeClients.contains(nomClient)) {
                    clientArticles.filter { !it.nonTrouveState }
                } else {
                    clientArticles
                }

                items(filteredArticles.chunked(2)) { pairOfArticles ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        pairOfArticles.find { it.idArticle == selectedArticleId }?.let { article ->
                            DisplayDetailleArticle(
                                article = article,
                                currentChangingField = currentChangingField,
                                onValueOutlineChange = {
                                    currentChangingField = it
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            pairOfArticles.forEach { article ->
                                ArticleBoardCard(
                                    article = article,
                                    onClickNonTrouveState = { clickedArticle ->
                                        updateNonTrouveState(clickedArticle)
                                    },
                                    onClickVerificated = { clickedArticle ->
                                        updateVerifieState(clickedArticle)
                                    },
                                    onArticleSelect = { selectedArticle ->
                                        focusManager.clearFocus()
                                        onArticleSelect(selectedArticle.idArticle)
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
                                        currentChangingField = ""
                                    },
                                    isVerificationMode = activeClients.contains(article.nomClient),
                                )
                            }
                        }
                    }
                }
            }
        }}
    }
}


@Composable
fun ArticleBoardCard(
    article: ArticlesAcheteModele,
    onClickNonTrouveState: (ArticlesAcheteModele) -> Unit,
    onArticleSelect: (ArticlesAcheteModele) -> Unit,
    isVerificationMode: Boolean,
    onClickVerificated: (ArticlesAcheteModele) -> Unit,
) {
    val cardColor = when {
        article.nonTrouveState -> Color.Red
        article.verifieState -> Color.Yellow
        else -> Color.White
    }
    val textColor = if (!article.nonTrouveState) Color.Black else Color.White
    var showPackagingDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(170.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.padding(2.dp)) {
            Column {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .height(230.dp)
                        .clickable { onArticleSelect(article) }
                ) {
                    if (article.quantityAcheteCouleur2 + article.quantityAcheteCouleur3 + article.quantityAcheteCouleur4 == 0) {
                        SingleColorImage(article)
                    } else {
                        MultiColorGrid(article)
                    }

                    PriceOverlay(article.monPrixVentBons)
                }
                Row {
                    var totalQuantityText by remember { mutableStateOf(article.totalQuantity.toString()) }

                    OutlinedTextField(
                        value = totalQuantityText,
                        onValueChange = { newValue ->
                            totalQuantityText = newValue
                            val newTotalQuantity = newValue.toIntOrNull() ?: 0
                            val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
                            val articleUpdate = articleFromFireBase.child("totalQuantity")
                            articleUpdate.setValue(newTotalQuantity)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = "Total") },
                        modifier = Modifier.weight(0.3f)
                    )
                    ArticleName(
                        name = article.nomArticleFinale,
                        color = textColor,
                        onNameClick = {
                            if (isVerificationMode) {
                                onClickVerificated(article)
                            } else {
                                onClickNonTrouveState(article)
                            }
                        },
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
            IconButton(
                onClick = { showPackagingDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.AllInbox,
                    contentDescription = "Packaging Type",
                    tint = Color.Blue
                )
            }
        }
    }

    if (showPackagingDialog) {
        ShowPackagingDialog(
            article = article,
            onDismiss = { showPackagingDialog = false }
        )
    }
}

@Composable
fun ShowPackagingDialog(
    article: ArticlesAcheteModele,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Packaging Type") },
        text = {
            Column {
                PackagingToggleButton("Carton", article.typeEmballage == "Carton") {
                    updateTypeEmballage(article, "Carton")
                    onDismiss()
                }
                PackagingToggleButton("Boit", article.typeEmballage == "Boit") {
                    updateTypeEmballage(article, "Boit")
                    onDismiss()
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

@Composable
fun PackagingToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Red else Color.Green
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}

fun updateTypeEmballage(article: ArticlesAcheteModele, newType: String) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child("typeEmballage").setValue(newType)
}

@Composable
private fun ArticleName(
    name: String,
    color: Color,
    onNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNameClick)
    ) {
        AutoResizedText(
            text = capitalizeFirstLetter(name),
            modifier = Modifier.padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
            color = color
        )
    }
}


@Composable
private fun SingleColorImage(article: ArticlesAcheteModele) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
        LoadImageFromPathBC(imagePath = imagePath)
    }
}

@Composable
private fun MultiColorGrid(article: ArticlesAcheteModele) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        listOf(
            article.quantityAcheteCouleur1,
            article.quantityAcheteCouleur2,
            article.quantityAcheteCouleur3,
            article.quantityAcheteCouleur4
        ).forEachIndexed { index, quantity ->
            item {
                if (quantity > 0) {
                    ImageWithColorName(
                        imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_${index + 1}",
                        colorQuantity = quantity.toString()
                    )
                }
            }
        }
    }
}


@Composable
private fun PriceOverlay(price: Double) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.7f))
                .padding(4.dp)
        ) {
            AutoResizedText(
                text = "Pv>$price",
                textAlign = TextAlign.Center,
                color = Color.Red,
            )
        }
    }
}




@Composable
fun ImageWithColorName(imagePath: String, colorQuantity: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadImageFromPathBC(imagePath = imagePath)
        colorQuantity?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )

        }
    }
}



