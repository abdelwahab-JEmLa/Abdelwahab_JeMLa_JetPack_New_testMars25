package A_Learn.Edite_Base_Donne

import A_Learn.A_Main_Ui.MainAppViewModel
import a_RoomDB.BaseDonne
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import java.io.File
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType

@Composable
fun A_Edite_Base_Screen(
    modifier: Modifier = Modifier,
    mainAppViewModel: MainAppViewModel,
) {
    val articlesList by mainAppViewModel.articlesBaseDonne.collectAsStateWithLifecycle()
    Column(modifier = modifier.fillMaxSize()) {
        ArticlesScreenList(
            articlesList = articlesList,
            mainAppViewModel = mainAppViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreenList(articlesList: List<BaseDonne>, mainAppViewModel: MainAppViewModel) {
    var selectedArticle by remember { mutableStateOf<BaseDonne?>(null) }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LazyVerticalGrid Sample") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(paddingValues)
        ) {
            items(items = articlesList.chunked(2)) { pairOfArticles ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        pairOfArticles.forEach { article ->
                            ArticleBoardCard(article) { updatedArticle ->
                                selectedArticle = updatedArticle
                                focusManager.clearFocus() // Clear the focus from the text field
                            }
                        }
                    }
                    selectedArticle?.let { article ->
                        if (pairOfArticles.contains(article)) {
                            CardDetailleArticle(
                                article,
                                mainAppViewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleBoardCard(article: BaseDonne, onClick: (BaseDonne) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick(article) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Column {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(230.dp)
            ) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                LoadImageFromPath(imagePath = imagePath)
            }
            Text(
                text = article.nomArticleFinale,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun CardDetailleArticle(
    article: BaseDonne,
    mainAppViewModel: MainAppViewModel,
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

            TopRowQuantitys(article, mainAppViewModel)
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                DisplayColorsCards(article, Modifier.weight(0.38f))
                DisplayArticleInformations(article, mainAppViewModel, Modifier.weight(0.62f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.nomArticleFinale,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun TopRowQuantitys(
    article: BaseDonne,
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier
) {
    // Define the column names and their labels
    val nomColumesList = listOf(
        Pair(BaseDonne::clienPrixVentUnite, "c.p.U"),
        Pair(BaseDonne::nmbrCaron, "n.C"),
        Pair(BaseDonne::nmbrUnite, "n.U")
    )

    Row(
        modifier = modifier
            .padding(3.dp)
            .fillMaxWidth()
    ) {
        nomColumesList.forEach { (nomColume, label) ->
            Spacer(modifier = Modifier.width(3.dp))
            OutlinedTextFieldDynamique(
                article = article,
                nomColum = nomColume,
                mainAppViewModel = mainAppViewModel,
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp),
                abdergNomColum = label // Use the label directly
            )
        }
    }
}

@Composable
fun DisplayColorsCards(article: BaseDonne, modifier: Modifier = Modifier) {
    val couleursList = listOf(
        article.couleur1,
        article.couleur2,
        article.couleur3,
        article.couleur4,
    )

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(3.dp)
            .fillMaxWidth()
    ) {
        itemsIndexed(couleursList) { index, couleur ->
            if (!couleur.isNullOrEmpty()) {
                ColorsCard(article.idArticle.toString(), index, couleur)
            }
        }
    }
}

@Composable
fun ColorsCard(idArticle: String, index: Int, couleur: String) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(300.dp)
            .padding(end = 8.dp) // Added padding between cards
    ) {
        val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${idArticle}_${index + 1}"
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            ) {
                LoadImageFromPath(imagePath = imagePath)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = couleur)
        }
    }
}

@Composable
fun DisplayArticleInformations(
    article: BaseDonne,
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier
) {
    val (benificeEntreNous, benificeDiviseEn2, calculatePrixUniter) = remember(article) { CalculatedAndReturned(article) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(3.dp)
    ) {
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp) // Ensure consistent height for the row
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                    .weight(0.40f)
                    .height(100.dp)
            ) {
                AutoResizedText(
                    text = "pA.U -> $calculatePrixUniter",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            OutlinedTextFieldDynamique(
                article = article,
                nomColum = BaseDonne::monPrixVent,
                mainAppViewModel = mainAppViewModel,
                modifier = Modifier
                    .fillMaxHeight() // Ensure the TextField takes up the full height
                    .weight(0.70f)
                    .height(45.dp),
                abdergNomColum = "m.P.V" // Set the same height as the Box
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp) // Ensure consistent height for the row
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                    .height(100.dp)
                    .weight(1f)
            ) {
                AutoResizedText(
                    text = "b.EN -> $benificeEntreNous",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                    .height(100.dp)
                    .weight(1f)
            ) {
                AutoResizedText(
                    text = "b./2 -> $benificeDiviseEn2",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }
        }
    }
}


fun CalculatedAndReturned(article: BaseDonne): Triple<Double, Double, Double> {
    val calculatePrixUniter = article.monPrixVent / article.nmbrUnite
    val sortieDeClientTotale = article.nmbrUnite * article.clienPrixVentUnite
    val benificeEntreNous = sortieDeClientTotale - article.monPrixVent
    val benificeDiviseEn2 = benificeEntreNous / 2
    return Triple(benificeEntreNous, benificeDiviseEn2, calculatePrixUniter)
}

// Placeholder implementation for OutlinedTextFieldDynamique
@Composable
fun <T : Any> OutlinedTextFieldDynamique(
    article: BaseDonne,
    nomColum: KMutableProperty1<BaseDonne, T>,
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier.height(45.dp),
    textColore: Color = Color.Red, // Default color
    labelFontSize: Pair<TextUnit, TextUnit>? = null, // Making labelFontSize an optional parameter
    abdergNomColum: String? = nomColum.name
) {
    var valeurText by remember { mutableStateOf("") }
    var initialLabel by remember { mutableStateOf(nomColum.get(article).toString()) }

    // Reset valeurText and initialLabel whenever the article changes
    LaunchedEffect(article) {
        valeurText = ""
        initialLabel = nomColum.get(article).toString()
    }

    OutlinedTextField(
        value = valeurText,
        onValueChange = { newText ->
            valeurText = newText
            val newValue: T? = parseValue(newText, nomColum.returnType)
            if (newValue != null) {
                nomColum.set(article, newValue)
                mainAppViewModel.updateOrDelete(article)
            }
        },
        label = {
            if (labelFontSize != null) {
                Text(buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = labelFontSize.first)) {
                        append(nomColum.name)
                    }
                    append(" -> ")
                    withStyle(style = SpanStyle(fontSize = labelFontSize.second, fontWeight = FontWeight.Bold)) {
                        append(initialLabel)
                    }
                })
            } else {
                AutoResizedText(
                    text = "$abdergNomColum:$initialLabel",
                    color = textColore,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        textStyle = TextStyle(color = textColore, textAlign = TextAlign.Center), // Apply text color here
        modifier = modifier.fillMaxWidth()
    )
}

// AutoResizedText implementation
@Composable
fun AutoResizedText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center // Added textAlign parameter with default value
) {
    var resizedTextStyle by remember {
        mutableStateOf(style)
    }
    var shouldDraw by remember {
        mutableStateOf(false)
    }

    val defaultFontSize = MaterialTheme.typography.bodyMedium.fontSize

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center // Center horizontally and vertically
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier
                .drawWithContent {
                    if (shouldDraw) {
                        drawContent()
                    }
                },
            softWrap = false,
            style = resizedTextStyle,
            textAlign = textAlign, // Setting the textAlign property
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    if (style.fontSize.isUnspecified) {
                        resizedTextStyle = resizedTextStyle.copy(
                            fontSize = defaultFontSize
                        )
                    }
                    resizedTextStyle = resizedTextStyle.copy(
                        fontSize = resizedTextStyle.fontSize * 0.95
                    )
                } else {
                    shouldDraw = true
                }
            }
        )
    }
}

// Helper function to parse the input text to the appropriate type
fun <T : Any> parseValue(value: String, type: KType): T? {
    return try {
        when (type) {
            Int::class.createType() -> value.toInt() as? T
            Float::class.createType() -> value.toFloat() as? T
            Double::class.createType() -> value.toDouble() as? T
            Long::class.createType() -> value.toLong() as? T
            Boolean::class.createType() -> value.toBoolean() as? T
            String::class.createType() -> value as? T
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun LoadImageFromPath(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.neaveau
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
