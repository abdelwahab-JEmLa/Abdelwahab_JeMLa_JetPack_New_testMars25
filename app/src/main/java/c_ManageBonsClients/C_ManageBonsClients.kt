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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import b_Edite_Base_Donne.AutoResizedText
import b_Edite_Base_Donne.LoadImageFromPath
import b_Edite_Base_Donne.capitalizeFirstLetter
import com.example.abdelwahabjemlajetpack.ui.theme.DarkGreen
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                    Text(
                        text = nomClient,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(clientArticles.chunked(2)) { pairOfArticles ->
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
                                    onClickNonTrouveState = { },
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
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable { onArticleSelect(article) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.padding(2.dp)) {
            Column {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(230.dp)
                ) {
                    val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                    LoadImageFromPath(imagePath = imagePath)
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(0.dp)
                    ) {
                        AutoResizedText(
                            text = "Pv>${article.monPrixVentBons}",
                            textAlign = TextAlign.Center,
                            color = Color.Red,
                        )
                    }
                }
                AutoResizedText(
                    text = capitalizeFirstLetter(article.nomArticleFinale),
                    modifier = Modifier.padding(vertical = 0.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    IconButton(
                        onClick = { onClickNonTrouveState(article) }
                    ) {
                        Icon(
                            imageVector = if (article.nonTrouveState) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle Favorite",
                            tint = if (article.nonTrouveState) Color.Red else DarkGreen
                        )
                    }
                }
            }
        }
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
            OutlinesChangers(
                article = article,
                currentChangingField = currentChangingField,
                onValueOutlineChange = onValueOutlineChange
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
fun OutlinesChangers(
    article: ArticlesAcheteModele,
    onValueOutlineChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        OutlineTextEditeRegle(
            columnToChange = "monPrixVentBons",
            abbreviation = "mpv",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueOutlineChange(columnChanged)
                val monBenificeBC = newValue.toDoubleOrNull()?.minus(article.getColumnValue("prixAchat") as? Double ?: 0.0) ?: 0.0
                    updateFirebase("monBenificeBC", monBenificeBC.toString(), article.idArticle.toString())
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)
        )
        OutlineTextEditeRegle(
            columnToChange = "monBenificeBC",
            abbreviation = "mB",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueOutlineChange(columnChanged)
                val monPrixVentBons = newValue.toDoubleOrNull()?.plus(article.getColumnValue("prixAchat") as? Double ?: 0.0) ?: 0.0
                updateFirebase("monPrixVentBons", monPrixVentBons.toString(), article.idArticle.toString())
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)
        )

    }
}

fun updateFirebase(columnChanged: String, newValue: String, articleId: String) {
    val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(articleId)
    val articleUpdate = articleFromFireBase.child(columnChanged)
    articleUpdate.setValue(newValue.toDoubleOrNull() ?: 0.0)
}

fun ArticlesAcheteModele.getColumnValue(columnName: String): Any {
    return when (columnName) {
        "monPrixVentBons" -> monPrixVentBons
        "prixAchat" -> prixAchat
        "monBenificeBC" -> monBenificeBC
        else -> ""
    }
}

@Composable
fun OutlineTextEditeRegle(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: ArticlesAcheteModele,
    modifier: Modifier = Modifier,
    calculateOthersRelated: (String, String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf((article.getColumnValue(columnToChange) as? Double)?.toString() ?: "") }

    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    val labelValue = (article.getColumnValue(columnToChange) as? Double)?.toString() ?: ""

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 3.dp)
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                updateFirebase(columnToChange, newValue, article.idArticle.toString())
                calculateOthersRelated(columnToChange, newValue)
            },
            label = {
                AutoResizedText(
                    text = "$abbreviation$labelValue",
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
fun AutoResizedText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center,
    bodyLarge: Boolean = false
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize = if (bodyLarge) MaterialTheme.typography.bodyLarge.fontSize else MaterialTheme.typography.bodyMedium.fontSize

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.drawWithContent {
                if (shouldDraw) drawContent()
            },
            softWrap = false,
            style = resizedTextStyle,
            textAlign = textAlign,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    if (style.fontSize.isUnspecified) {
                        resizedTextStyle = resizedTextStyle.copy(fontSize = defaultFontSize)
                    }
                    resizedTextStyle = resizedTextStyle.copy(fontSize = resizedTextStyle.fontSize * 0.95)
                } else {
                    shouldDraw = true
                }
            }
        )
    }
}
fun capitalizeFirstLetter(text: String): String {
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

