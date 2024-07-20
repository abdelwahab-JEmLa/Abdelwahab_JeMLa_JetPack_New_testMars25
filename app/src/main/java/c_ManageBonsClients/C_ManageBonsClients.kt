package c_ManageBonsClients
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Favorite
//import androidx.compose.material.icons.filled.FavoriteBorder
//import androidx.compose.material.icons.filled.Refresh
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.drawWithContent
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.TextStyle
//import androidx.compose.ui.text.input.KeyboardType
//import androidx.compose.ui.text.input.VisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.isUnspecified
//import androidx.compose.ui.unit.sp
//import b_Edite_Base_Donne.*
//import coil.compose.rememberAsyncImagePainter
//import com.example.abdelwahabjemlajetpack.R
//import com.example.abdelwahabjemlajetpack.ui.theme.DarkGreen
//import com.google.firebase.database.DataSnapshot
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.ValueEventListener
//import com.google.firebase.database.ktx.database
//import com.google.firebase.ktx.Firebase
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.launch
//import java.io.File
//import androidx.compose.ui.focus.FocusManager
//import androidx.compose.ui.platform.LocalFocusManager
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun C_ManageBonsClients(
//    articlesAcheteModeleDao: ArticlesAcheteModeleDao,
//) {
//    var articles by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
//    var showDialog by remember { mutableStateOf(false) }
//    val coroutineScope = rememberCoroutineScope()
//    val focusManager = LocalFocusManager.current
//    val listState = rememberLazyListState()
//
//    // Initialize Firebase reference
//    val articlesAcheteModeleRef = Firebase.database.getReference("finale1")
//
//    // Load data from Firebase
//    LaunchedEffect(Unit) {
//        articlesAcheteModeleRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val newArticles = mutableListOf<ArticlesAcheteModele>()
//                for (articleSnapshot in dataSnapshot.children) {
//                    val article = articleSnapshot.getValue(ArticlesAcheteModele::class.java)
//                    if (article != null) {
//                        val mappedArticle = ArticlesAcheteModele(
//                            vid = article.vid,
//                            idArticle = article.idArticle,
//                            nomArticleFinale = article.nomArticleFinale,
//                            prix_1 = article.prix_1,
//                            prixAchat = article.prixAchat,
//                            nmbrunite = article.nmbrunite,
//                            clientPrixVentUnite = article.clientPrixVentUnite,
//                            nomClient = article.nomClient,
//                            dateDachate = article.dateDachate,
//                            nomCouleur1 = article.nomCouleur1,
//                            quantityAcheteCouleur1 = article.quantityAcheteCouleur1,
//                            nomCouleur2 = article.nomCouleur2,
//                            quantityAcheteCouleur2 = article.quantityAcheteCouleur2,
//                            nomCouleur3 = article.nomCouleur3,
//                            quantityAcheteCouleur3 = article.quantityAcheteCouleur3,
//                            nomCouleur4 = article.nomCouleur4,
//                            quantityAcheteCouleur41 = article.quantityAcheteCouleur41,
//                            totalQuantity = article.totalQuantity,
//                            nonTrouveState = article.nonTrouveState,
//                            verifieState = article.verifieState,
//                            changeCaronState = article.changeCaronState
//                        )
//                        newArticles.add(mappedArticle)
//                    }
//                }
//                articles = newArticles
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Handle possible errors.
//            }
//        })
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("ManageBonsClients") },
//                actions = {
//                    IconButton(onClick = { showDialog = true }) {
//                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Filter")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        DisplayManageBonsClients(
//            articles = articles,
//            showDialog = showDialog,
//            coroutineScope = coroutineScope,
//            listState = listState,
//            paddingValues = paddingValues
//        )
//    }
//}
//
//@Composable
//fun DisplayManageBonsClients(
//    articles: List<ArticlesAcheteModele>,
//    showDialog: Boolean,
//    coroutineScope: CoroutineScope,
//    listState: LazyListState,
//    paddingValues: PaddingValues
//) {
//
//    var currentChangingField by remember { mutableStateOf("") }
//    var selectedArticle by remember { mutableStateOf<ArticlesAcheteModele?>(null) }
//    val focusManager = LocalFocusManager.current
//
//    LazyColumn(
//        state = listState,
//        modifier = Modifier.padding(paddingValues)
//    ) {
//        itemsIndexed(items = articles.chunked(2)) { _, pairOfArticles ->
//            Column(modifier = Modifier.fillMaxWidth()) {
//                if (selectedArticle != null && pairOfArticles.contains(selectedArticle)) {
//                    DisplayDetailleArticle(
//                        article = selectedArticle!!,
//                        currentChangingField = currentChangingField,
//                        onValueOutlineChange = { newValue, columnToChange, articleToUpdate ->
//                            currentChangingField = columnToChange
//                                val updatedArticle = Firebase.database
//                                    .getReference("finale1")
//                                    .child(articleToUpdate.idArticle.toString())
//                                    .(it ref  = columnToChange)
//                                    .(value = newValue)
//                                Firebase.database.getReference("finale1").child(articleToUpdate.idArticle.toString()).setValue(updatedArticle)
//                            }
//                    )
//                }
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly
//                ) {
//                    pairOfArticles.forEach { article ->
//                        ArticleBoardCard(
//                            article = article,
//                            onClickNonTrouveState = {  },
//                            onArticleSelect = { article ->
//                                val index = articles.indexOf(article)
//                                focusManager.clearFocus()
//                                selectedArticle = article
//                                coroutineScope.launch {
//                                    if (index >= 0) {
//                                        listState.scrollToItem(index / 2)
//                                    }
//                                }
//                                currentChangingField = ""
//                            }
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//@Composable
//fun ArticleBoardCard(
//    article: ArticlesAcheteModele,
//    onClickNonTrouveState: (ArticlesAcheteModele) -> Unit,
//    onArticleSelect: (ArticlesAcheteModele) -> Unit
//) {
//    Card(
//        shape = RoundedCornerShape(8.dp),
//        modifier = Modifier
//            .width(170.dp)
//            .clickable { onArticleSelect(article) },
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Box(
//            modifier = Modifier.padding(2.dp)
//        ) {
//            Column {
//                Box(
//                    contentAlignment = Alignment.Center,
//                    modifier = Modifier.height(230.dp)
//                ) {
//                    val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
//                    LoadImageFromPath(imagePath = imagePath)
//                    Box(
//                        modifier = Modifier
//                            .weight(0.7f)
//                            .background(Color.White.copy(alpha = 0.7f))
//                            .padding(0.dp)
//                    ) {
//                        AutoResizedText(
//                            text = "Pv>${article.prix_1}",
//                            textAlign = TextAlign.Center,
//                            color = Color.Red,
//                        )
//                    }
//                }
//                AutoResizedText(
//                    text = capitalizeFirstLetter(article.nomArticleFinale),
//                    modifier = Modifier.padding(vertical = 0.dp),
//                    textAlign = TextAlign.Center,
//                    color = Color.Red
//                )
//                IconButton(
//                    onClick = { onClickNonTrouveState(article) },
//                    modifier = Modifier.align(Alignment.BottomEnd)
//                ) {
//                    Icon(
//                        imageVector = if (article.nonTrouveState) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
//                        contentDescription = "Toggle Favorite",
//                        tint = if (article.nonTrouveState) Color.Red else DarkGreen
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun DisplayDetailleArticle(
//    article: ArticlesAcheteModele,
//    currentChangingField: String,
//    onValueOutlineChange: (String, String, ArticlesAcheteModele) -> Unit
//) {
//    Card(
//        shape = RoundedCornerShape(8.dp),
//        modifier = Modifier
//            .wrapContentSize()
//            .padding(4.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = Color.White)
//    ) {
//        Column(modifier = Modifier.fillMaxWidth()) {
//            OutlinesChangers(
//                article = article,
//                currentChangingField = currentChangingField,
//                onValueOutlineChange = onValueOutlineChange
//            )
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(7.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(
//                    text = capitalizeFirstLetter(article.nomArticleFinale),
//                    fontSize = 25.sp,
//                    textAlign = TextAlign.Center,
//                    color = Color.Red
//                )
//            }
//        }
//    }
//}
//
//@Composable
//fun OutlinesChangers(
//    article: ArticlesAcheteModele,
//    onValueOutlineChange: (String, String, ArticlesAcheteModele) -> Unit,
//    currentChangingField: String,
//    modifier: Modifier = Modifier
//) {
//    Row(
//        modifier = modifier.fillMaxWidth()
//    ) {
//
//        OutlineTextEditeRegle(
//            columnToChange = "prix_1",
//            abbreviation = "mpv",
//            onValueChange = onValueOutlineChange,
//            currentChangingField = currentChangingField,
//            article = article,
//            modifier = Modifier
//                .weight(1f)
//                .height(67.dp)
//        )
//    }
//}
//
//@Composable
//fun OutlineTextEditeRegle(
//    columnToChange: String,
//    abbreviation: String,
//    currentChangingField: String,
//    article: ArticlesAcheteModele,
//    modifier: Modifier = Modifier,
//    onValueChange: (String, String, ArticlesAcheteModele) -> Unit
//) {
//    var textFieldValue by remember { mutableStateOf(article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: "") }
//
//    // Déterminer la valeur du champ texte
//    val textValue = if (currentChangingField == columnToChange) {
//        textFieldValue
//    } else ""
//
//    // Déterminer la valeur de l'étiquette
//    val labelValue = article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
//
//    // Get the keyboard controller
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = modifier.padding(horizontal = 3.dp)
//    ) {
//        OutlinedTextField(
//            value = textValue,
//            onValueChange = {
//                textFieldValue = it
//                onValueChange(it, columnToChange, article)
//            },
//            label = {
//                AutoResizedText(
//                    text = "$abbreviation$labelValue",
//                    color = Color.Red,
//                    modifier = Modifier.fillMaxWidth(),
//                )
//            },
//            textStyle = TextStyle(
//                color = Color.Blue,
//                textAlign = TextAlign.Center,
//                fontSize = 14.sp
//            ),
//            modifier = modifier
//                .fillMaxWidth()
//                .height(65.dp),
//            visualTransformation = VisualTransformation.None,
//            keyboardOptions = KeyboardOptions.Default.copy(
//                keyboardType = KeyboardType.Number,
//                imeAction = ImeAction.Done
//            ),
//            keyboardActions = KeyboardActions(
//                onDone = {
//                    keyboardController?.hide()
//                }
//            )
//        )
//    }
//}
//
//fun capitalizeFirstLetter(text: String): String {
//    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
//}
//
//@Composable
//fun AutoResizedText(
//    text: String,
//    style: TextStyle = MaterialTheme.typography.bodyMedium,
//    modifier: Modifier = Modifier,
//    color: Color = style.color,
//    textAlign: TextAlign = TextAlign.Center,
//    bodyLarge: Boolean = false
//) {
//    var resizedTextStyle by remember { mutableStateOf(style) }
//    var shouldDraw by remember { mutableStateOf(false) }
//
//    val defaultFontSize = if (bodyLarge) MaterialTheme.typography.bodyLarge.fontSize else MaterialTheme.typography.bodyMedium.fontSize
//
//    Box(
//        modifier = modifier.fillMaxWidth(),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = text,
//            color = color,
//            modifier = Modifier.drawWithContent {
//                if (shouldDraw) drawContent()
//            },
//            softWrap = false,
//            style = resizedTextStyle,
//            textAlign = textAlign,
//            onTextLayout = { result ->
//                if (result.didOverflowWidth) {
//                    if (style.fontSize.isUnspecified) {
//                        resizedTextStyle = resizedTextStyle.copy(fontSize = defaultFontSize)
//                    }
//                    resizedTextStyle = resizedTextStyle.copy(fontSize = resizedTextStyle.fontSize * 0.95)
//                } else {
//                    shouldDraw = true
//                }
//            }
//        )
//    }
//}
//
//@Composable
//fun LoadImageFromPath(imagePath: String, modifier: Modifier = Modifier) {
//    val defaultDrawable = R.drawable.neaveau
//    val imageExist: String? = when {
//        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
//        File("$imagePath.webp").exists() -> "$imagePath.webp"
//        else -> null
//    }
//
//    val painter = rememberAsyncImagePainter(imageExist ?: defaultDrawable)
//
//    Box(
//        modifier = modifier
//            .fillMaxWidth()
//            .aspectRatio(1f)
//            .wrapContentSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Image(
//            painter = painter,
//            contentDescription = null,
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.wrapContentSize(Alignment.Center)
//        )
//    }
//}
