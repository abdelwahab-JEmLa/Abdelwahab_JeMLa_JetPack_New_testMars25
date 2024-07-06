package b_Edite_Base_Donne

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File


@Composable
fun A_Edite_Base_Screen(
    articleDao: ArticleDao,
    modifier: Modifier = Modifier,
) {
    var articlesList by remember { mutableStateOf<List<BaseDonne>>(emptyList()) }
    var selectedArticle by remember { mutableStateOf<BaseDonne?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val refFirebase = Firebase.database.getReference("d_db_jetPack")

    LaunchedEffect(true) {
        articlesList = articleDao.getAllArticlesOrder()
    }

    Column(modifier = modifier.fillMaxSize()) {
        ArticlesScreenList(
            articlesList = articlesList,
            selectedArticle = selectedArticle,
            onArticleSelect = { selectedArticle = it },
            function = { articleUpdated ->
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        refFirebase.setValue(articleUpdated).await()
                        articleDao.insert(articleUpdated)
                    } catch (e: Exception) {
                        // Handle the error here
                    }
                }
            },
            articleDao
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreenList(
    articlesList: List<BaseDonne>,
    selectedArticle: BaseDonne?,
    onArticleSelect: (BaseDonne) -> Unit,
    function: (BaseDonne) -> Unit,
    articleDao: ArticleDao,
) {
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles List") }
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
                                onArticleSelect(updatedArticle)
                                focusManager.clearFocus() // Clear the focus from the text field
                            }
                        }
                    }
                    selectedArticle?.let { article ->
                        if (pairOfArticles.contains(article)) {
                            DisplayeDetailleArticle(
                                article = article,
                                function = function,
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
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
fun DisplayeDetailleArticle(
    article: BaseDonne,
    function: (BaseDonne) -> Unit,
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
            TopRowQuantitys(article)
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                DisplayColorsCards(article, Modifier.weight(0.38f))
                DisplayArticleInformations(article,function, Modifier.weight(0.62f))

            }
            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.nomArticleFinale,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
@Composable
fun DisplayArticleInformations(
    article: BaseDonne,
    function: (BaseDonne) -> Unit,
    modifier: Modifier = Modifier

) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(3.dp)
    ) {
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                    .weight(0.40f)
                    .height(100.dp)
            ) {
                AutoResizedText(
                    text = "pA.U -> ${article.monPrixAchatUniter}",
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
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.70f)
                    .height(45.dp),
                abdergNomColum = "m.P.V"
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                    .height(100.dp)
                    .weight(1f)
            ) {
                AutoResizedText(
                    text = "b.EN -> ${article.benficeTotaleEntreMoiEtClien}",
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
                    text = "b./2 -> ${article.benificeTotaleEn2}",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }
        }
        Dis_InformationsNewPractice(
            article = article,
            onValueChange = function,
        )
        OutlineTextEditeBaseDonne(
            article = article,
            onValueChange = function,
            valueDBToChange = "monPrixVent",
            abbreviations = "p.v",
        )
    }
}

@Composable
fun OutlineTextEditeBaseDonne(
    article: BaseDonne,
    onValueChange: (BaseDonne) -> Unit,
    modifier: Modifier = Modifier,
    valueDBToChange: String,
    abbreviations: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(3.dp)
    ) {
        var articleState by remember { mutableStateOf(article) }
        var currentChangingField by remember { mutableStateOf("") }
        val textValue = if (currentChangingField == valueDBToChange) articleState.getColumnValue(valueDBToChange)?.toString() ?: "" else ""
        val labelValue = articleState.getColumnValue(valueDBToChange)?.toString() ?: ""

        OutlinedTextField(
            value = removeTrailingZero(textValue),
            onValueChange = {
                val updatedArticle = calculateNewValues(valueDBToChange, removeTrailingZero(it), articleState)
                articleState = updatedArticle
                currentChangingField = valueDBToChange
                onValueChange(articleState)
            },
            label = {
                Text(
                    text = "$abbreviations: $labelValue",
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center, fontSize = 14.sp),
            modifier = modifier.fillMaxWidth().height(65.dp)
        )
    }
}

fun removeTrailingZero(value: String): String {
    return when {
        value == "0.0" -> ""
        value.endsWith(".0") -> value.removeSuffix(".0")
        else -> value
    }
}
fun calculateNewValues(
    columnName: String,
    newValue: String?,
    article: BaseDonne,
): BaseDonne {
    val value = newValue?.toDoubleOrNull() ?: 0.0
    val newArticle = article.copy()

    when (columnName) {
        "monPrixVent" -> newArticle.monPrixVent = value
        "monBenefice" -> newArticle.monBenfice = value
        "prixDeVentTotaleChezClient" -> newArticle.prixDeVentTotaleChezClient = value
        "monPrixAchatUniter" -> newArticle.monPrixAchatUniter = value
    }

    newArticle.apply {
        if (columnName != "monPrixVent") {
            monPrixVent = monBenfice + article.monPrixAchat
        }
        if (columnName != "prixDeVentTotaleChezClient") {
            prixDeVentTotaleChezClient = article.clienPrixVentUnite * article.nmbrUnite
        }
        if (columnName != "monBenefice") {
            monBenfice = monPrixVent - article.monPrixAchat
        }
        if (columnName != "monPrixAchatUniter") {
            monPrixAchatUniter = monPrixVent / article.nmbrUnite
        }
    }

    return newArticle
}

fun BaseDonne.getColumnValue(columnName: String): Double? {
    return when (columnName) {
        "monPrixVent" -> monPrixVent
        "monBenefice" -> monBenfice
        "prixDeVentTotaleChezClient" -> prixDeVentTotaleChezClient
        "monPrixAchatUniter" -> monPrixAchatUniter
        else -> null
    }
}
//---------------------------------------------------------------------------
@Composable
fun OutlinedTextFieldModifier(
    textValue: String,
    labelValue: String,
    onValueChange: (String) -> Unit,
    abbreviation: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Red,
) {
    OutlinedTextField(
        value = removeTrailingZero(textValue),
        onValueChange = {
            onValueChange(removeTrailingZero(it))
        },
        label = {
            Text(
                text = "$abbreviation: $labelValue",
                color = textColor,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        textStyle = TextStyle(color = textColor, textAlign = TextAlign.Center, fontSize = 14.sp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun Dis_InformationsNewPractice(
    article: BaseDonne,
    modifier: Modifier = Modifier,
    onValueChange: (BaseDonne) -> Unit,
) {
    var articleState by remember { mutableStateOf(article) }
    var currentChangingField by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(3.dp)
    ) {
        val fields = listOf("monPrixVent", "monBenefice")
        val abbreviations = listOf("p.v", "m.b")

        fields.forEachIndexed { index, field ->
            OutlinedTextFieldModifier(
                textValue = if (currentChangingField == field) articleState.getColumnValue(field).toString() else "",
                onValueChange = {
                    articleState = calculateNewValues(field, it, article)
                    currentChangingField = field
                    onValueChange(articleState)
                },
                abbreviation = abbreviations[index],
                labelValue = articleState.getColumnValue(field).toString(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

////////////////////////////////////////////////////////////////////

@Composable
fun TopRowQuantitys(
    article: BaseDonne,
    modifier: Modifier = Modifier
) {
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
                modifier = Modifier
                    .weight(1f)
                    .height(63.dp),
                abdergNomColum = label
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
            .padding(end = 8.dp)
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
fun AutoResizedText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize = MaterialTheme.typography.bodyMedium.fontSize

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
