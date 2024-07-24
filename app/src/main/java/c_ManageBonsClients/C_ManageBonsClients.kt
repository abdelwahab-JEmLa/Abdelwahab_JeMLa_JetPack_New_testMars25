package c_ManageBonsClients

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import b_Edite_Base_Donne.AutoResizedText
import b_Edite_Base_Donne.capitalizeFirstLetter
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun C_ManageBonsClients() {
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
    var isFiltered by remember { mutableStateOf(false) }

    // Group articles by nomClient
    val groupedArticles = articles.groupBy { it.nomClient }

    BoxWithConstraints(
        modifier = Modifier.padding(paddingValues)
    ) {
        val height = maxHeight
        var selectedItemOffset by remember { mutableStateOf(0f) }

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
                            style = MaterialTheme.typography.titleMedium
                        )
                        IconButton(
                            onClick = { isFiltered = !isFiltered }
                        ) {
                            Icon(
                                imageVector = if (isFiltered) Icons.Default.Check else Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                val filteredArticles = if (isFiltered) {
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
                                    }
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
fun ArticleBoardCard(
    article: ArticlesAcheteModele,
    onClickNonTrouveState: (ArticlesAcheteModele) -> Unit,
    onArticleSelect: (ArticlesAcheteModele) -> Unit
) {
    val cardColor = if (!article.nonTrouveState) Color.Red else Color.White
    val textColor = if (!article.nonTrouveState) Color.White else Color.Red

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
                ArticleName(
                    name = article.nomArticleFinale,
                    color = textColor,
                    onNameClick = {
                        onClickNonTrouveState(article)
                        updateVerifieState(article)
                    }
                )
            }
        }
    }
}

// Update Firebase functions
fun updateNonTrouveState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child("nonTrouveState").setValue(!article.nonTrouveState)
}

fun updateVerifieState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child("verifieState").setValue(!article.verifieState)
}
@Composable
private fun ArticleName(name: String, color: Color, onNameClick: () -> Unit) {
    Box(
        modifier = Modifier
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
        if (!article.nomCouleur1.contains("stan", ignoreCase = true)) {
            ColorNameOverlay(colorName = article.nomCouleur1)
        }
    }
}

@Composable
private fun MultiColorGrid(article: ArticlesAcheteModele) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        listOf(
            article.quantityAcheteCouleur1 to article.nomCouleur1,
            article.quantityAcheteCouleur2 to article.nomCouleur2,
            article.quantityAcheteCouleur3 to article.nomCouleur3,
            article.quantityAcheteCouleur4 to article.nomCouleur4
        ).forEachIndexed { index, (quantity, colorName) ->
            item {
                if (quantity > 0) {
                    ImageWithColorName(
                        imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_${index + 1}",
                        colorName = if (!colorName.contains("stan", ignoreCase = true)) colorName else null
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
private fun ArticleName(name: String) {
    AutoResizedText(
        text = capitalizeFirstLetter(name),
        modifier = Modifier.padding(vertical = 4.dp),
        textAlign = TextAlign.Center,
        color = Color.Red
    )
}



@Composable
fun ImageWithColorName(imagePath: String, colorName: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadImageFromPathBC(imagePath = imagePath)
        colorName?.let { ColorNameOverlay(colorName = it) }
    }
}


@Composable
fun ColorNameOverlay(colorName: String) {
    Text(
        text = colorName,
        color = Color.Red,
        modifier = Modifier
            .rotate(45f)
            .background(Color.White.copy(alpha = 0.7f))
            .padding(4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun LoadImageFromPathBC(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.blanc
    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }

    val painter = rememberAsyncImagePainter(imageExist ?: defaultDrawable)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        )
    }
}


@Composable
fun DisplayDetailleArticle(
    article: ArticlesAcheteModele,
    currentChangingField: String,
    onValueOutlineChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .wrapContentSize()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            InformationsChanger(
                article = article,
                currentChangingField = currentChangingField,
                onValueChange = onValueOutlineChange
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = capitalizeFirstLetter(article.nomArticleFinale),
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun InformationsChanger(
    article: ArticlesAcheteModele,
    onValueChange: (String, ) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            ColumnBenifices(article, onValueChange, currentChangingField,modifier = Modifier.weight(1f))
            ColumnPVetPa(article, onValueChange, currentChangingField,modifier = Modifier.weight(1f))
        }
        RowAutresInfo(article, onValueChange, currentChangingField,)
    }
}
@Composable
private fun RowAutresInfo(
    article: ArticlesAcheteModele,
    onValueChange: (String,) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
    Row {
        OutlineTextEditeRegle(
            columnToChange = "clientPrixVentUnite",
            abbreviation = "cVU",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueChange(columnChanged)
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)

        )
        OutlineTextEditeRegle(
            columnToChange = "nmbrunitBC",
            abbreviation = "nu",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueChange(columnChanged)
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)

        )

    }

}



@Composable
private fun ColumnBenifices(
    article: ArticlesAcheteModele,
    onValueChange: (String,) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
        Column (modifier = modifier.fillMaxWidth()){
            Row {
                OutlineTextEditeRegle(
                    columnToChange = "benificeDivise",
                    abbreviation = "b/2",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(67.dp)
                )

                OutlineTextEditeRegle(
                    columnToChange = "benificeClient",
                    abbreviation = "bC",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged, )
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(67.dp)
                )
            }
            Row {
                OutlineTextEditeRegle(
                    columnToChange = "monBenificeUniterBC",
                    abbreviation = "/U",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged, )
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(67.dp)
                )

                OutlineTextEditeRegle(
                    columnToChange = "monBenificeBC",
                    abbreviation = "mB",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged, )
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(67.dp)
                )
            }
        }

}

@Composable
private fun ColumnPVetPa(
    article: ArticlesAcheteModele,
    onValueChange: (String, ) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier

) {
    Column (modifier = modifier.fillMaxWidth()){
            Row {
                OutlineTextEditeRegle(
                    columnToChange = "monPrixAchatUniterBC",
                    abbreviation = "/U",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged, )
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(67.dp)
                )

                OutlineTextEditeRegle(
                    columnToChange = "prixAchat",
                    abbreviation = "mpA",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged, )
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.71f)
                        .height(67.dp)
                )
            }
            Row {
                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentUniterBC",
                    abbreviation = "/U",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged, )
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .height(67.dp)
                )

                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentBons",
                    abbreviation = "mpV",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged, )
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(67.dp)
                )
            }
    }

}

fun updateRelatedFields(ar: ArticlesAcheteModele, columnChanged: String, newValue: String) {
    val newValueDouble = newValue.toDoubleOrNull() ?: return
    when (columnChanged) {
        "benificeClient" -> {
            up("benificeDivise", ((newValueDouble / ar.nmbrunitBC) - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monBenificeUniterBC", (((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBC", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("monPrixVentUniterBC", ((ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentBons", (ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble).toString(), ar.idArticle)
        }
        "monBenificeUniterBC" -> {
            up("monBenificeBC", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterBC", (newValueDouble + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentBons", (newValueDouble * ar.nmbrunitBC + ar.prixAchat).toString(), ar.idArticle)
        }
        "monBenificeBC" -> {
            up("monBenificeUniterBC", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterBC", ((newValueDouble / ar.nmbrunitBC) + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentBons", (newValueDouble + ar.prixAchat).toString(), ar.idArticle)
            up("benificeClient", ((ar.clientPrixVentUnite * ar.nmbrunitBC)-(newValueDouble + ar.prixAchat)).toString(), ar.idArticle)
        }
        "monPrixAchatUniterBC" -> {
            up("prixAchat", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentBons", (newValueDouble * ar.nmbrunitBC + ar.monBenificeBC).toString(), ar.idArticle)
        }
        "prixAchat" -> {
            up("monPrixVentBons", (newValueDouble + ar.monBenificeBC).toString(), ar.idArticle)
        }
        "monPrixVentUniterBC" -> {
            up("monPrixVentBons", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBC", (newValueDouble * ar.nmbrunitBC - ar.prixAchat).toString(), ar.idArticle)
        }
        "monPrixVentBons" -> {
            up("monPrixVentUniterBC", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBC", (newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("benificeClient", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble).toString(), ar.idArticle)
        }
    }
}

//updateFireBase
fun up(columnChanged: String, newValue: String, articleId: Long) {
    val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(articleId.toString())
    val articleUpdate = articleFromFireBase.child(columnChanged)
    articleUpdate.setValue(newValue.toDoubleOrNull() ?: 0.0)
}

@Composable
fun OutlineTextEditeRegle(
    columnToChange: String,
    abbreviation: String,
    labelCalculated: String = "",
    currentChangingField: String,
    article: ArticlesAcheteModele,
    modifier: Modifier = Modifier,
    calculateOthersRelated: (String, String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf((article.getColumnValue(columnToChange) as? Double)?.toString() ?: "") }

    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    // Déterminer la valeur de l'étiquette
    val labelValue = labelCalculated.ifEmpty { (article.getColumnValue(columnToChange) as? Double)?.toString() ?: "" }
    val roundedValue = try {
        val doubleValue = labelValue.toDouble()
        if (doubleValue % 1 == 0.0) {
            doubleValue.toInt().toString()
        } else {
            String.format("%.1f", doubleValue)
        }
    } catch (e: NumberFormatException) {
        labelValue // Retourner la valeur initiale en cas d'exception
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 3.dp)
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                calculateOthersRelated(columnToChange, newValue)
            },
            label = {
                AutoResizedTextBC(
                    text = "$abbreviation$roundedValue",
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            textStyle = TextStyle(
                color = Color.Blue,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(65.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            )
        )
    }
}

@Composable
fun AutoResizedTextBC(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center,
    bodyLarge: Boolean = false
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    val defaultFontSize = if (bodyLarge) MaterialTheme.typography.bodyLarge.fontSize else MaterialTheme.typography.bodyMedium.fontSize
    val minFontSize = 8.sp // Set a minimum font size to prevent text from becoming too small

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.drawWithContent {
                if (readyToDraw) drawContent()
            },
            softWrap = false,
            style = resizedTextStyle,
            textAlign = textAlign,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    if (resizedTextStyle.fontSize > minFontSize) {
                        resizedTextStyle = resizedTextStyle.copy(
                            fontSize = (resizedTextStyle.fontSize.value * 0.95f).sp
                        )
                    } else {
                        readyToDraw = true // Stop resizing if we've reached the minimum font size
                    }
                } else {
                    readyToDraw = true // Text fits, ready to draw
                }
            }
        )
    }
}