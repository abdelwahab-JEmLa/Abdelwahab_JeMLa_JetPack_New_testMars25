package B_Edite_Base_Donne

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
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.withNullability

private val refFirebase = Firebase.database.getReference("d_db_jetPack")

@Composable
fun A_Edite_Base_Screen(
    articleDao: ArticleDao,
    modifier: Modifier = Modifier,
) {
    var articlesList by remember { mutableStateOf<List<BaseDonne>>(emptyList()) }
    var selectedArticle by remember { mutableStateOf<BaseDonne?>(null) }
    val coroutineScope = rememberCoroutineScope()

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
            }
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
                            CardDetailleArticle(
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
fun CardDetailleArticle(
    article: BaseDonne,
    function: (BaseDonne) -> Unit,
) {
    var articleState by remember { mutableStateOf(article.copy()) }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .wrapContentSize()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopRowQuantitys(articleState)
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                DisplayColorsCards(articleState, Modifier.weight(0.38f))
                DisplayArticleInformations(articleState, Modifier.weight(0.62f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            DisplayArticleInformations3(
                article = articleState,
                onValueChange = { updatedArticle ->
                    articleState = updatedArticle
                    function(updatedArticle)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = articleState.nomArticleFinale,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
@Composable
fun DisplayArticleInformations3(
    article: BaseDonne,
    modifier: Modifier = Modifier,
    onValueChange: (BaseDonne) -> Unit,
) {
    var allColumnsEmptyExceptInputNow by remember { mutableStateOf(BaseDonne()) }
    var labelValue by remember { mutableStateOf(BaseDonne()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(3.dp)
    ) {
        val columns = listOf("monPrixVent", "monBenfice")
        columns.forEach { column ->
            OutlinedTextFieldModifier(
                textValue = if (allColumnsEmptyExceptInputNow.isEmptyColumn(column)) "" else article.getColumnValue(column).toString(),
                labelValue = article.getColumnValue(column).toString(),
                onValueChange = {
                    val (newArticle, newAllColumnsEmptyExceptInputNow) = calculateNewValues(column, it, article, allColumnsEmptyExceptInputNow)
                    allColumnsEmptyExceptInputNow = newAllColumnsEmptyExceptInputNow
                    onValueChange(newArticle)
                    labelValue = newArticle
                },
                abregation = "m.b",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OutlinedTextFieldModifier(
    textValue: String,
    labelValue: String,
    onValueChange: (String) -> Unit,
    abregation: String,
    modifier: Modifier = Modifier.height(63.dp),
    textColor: Color = Color.Red,
) {
    OutlinedTextField(
        value = textValue,
        onValueChange = {
            onValueChange(it)
        },
        label = {
            Text(
                text = "$abregation: $labelValue",
                color = textColor,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        textStyle = TextStyle(color = textColor, textAlign = TextAlign.Center, fontSize = 14.sp),
        modifier = modifier.fillMaxWidth()
    )
}

fun calculateNewValues(
    columnName: String,
    newValue: String?,
    article: BaseDonne,
    allColumnsEmptyExceptInputNow: BaseDonne
): Pair<BaseDonne, BaseDonne> {
    val newArticle = article.copy()
    val updatedAllColumnsEmptyExceptInputNow = allColumnsEmptyExceptInputNow.copy()

    val value = newValue?.toDoubleOrNull() ?: 0.0

    when (columnName) {
        "monBenfice" -> {
            newArticle.monBenfice = value
        }
        "monPrixVent" -> {
            newArticle.monPrixVent = value
        }
        "prixDeVentTotaleChezClient" -> newArticle.prixDeVentTotaleChezClient = value
        "monPrixAchatUniter" -> newArticle.monPrixAchatUniter = value
    }

    if (columnName != "monPrixVent") {
        newArticle.monPrixVent = newArticle.monBenfice + article.monPrixAchat
        updatedAllColumnsEmptyExceptInputNow.monPrixVent = 0.0
    } else {
        updatedAllColumnsEmptyExceptInputNow.monPrixVent = value
    }

    if (columnName != "prixDeVentTotaleChezClient") {
        newArticle.prixDeVentTotaleChezClient = article.clienPrixVentUnite * article.nmbrUnite
    }

    if (columnName != "monBenfice") {
        newArticle.monBenfice = newArticle.monPrixVent - article.monPrixAchat
        updatedAllColumnsEmptyExceptInputNow.monBenfice = 0.0
    } else {
        updatedAllColumnsEmptyExceptInputNow.monBenfice = value
    }

    if (columnName != "monPrixAchatUniter") {
        newArticle.monPrixAchatUniter = article.monPrixVent / article.nmbrUnite
    }

    return Pair(newArticle, updatedAllColumnsEmptyExceptInputNow,)
}

// Extension functions to support the changes
fun BaseDonne.isEmptyColumn(columnName: String): Boolean {
    return when (columnName) {
        "monPrixVent" -> this.monPrixVent == 0.0
        "monBenfice" -> this.monBenfice == 0.0
        else -> false
    }
}

fun BaseDonne.getColumnValue(columnName: String): Double {
    return when (columnName) {
        "monPrixVent" -> this.monPrixVent
        "monBenfice" -> this.monBenfice
        else -> 0.0
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
fun DisplayArticleInformations(
    article: BaseDonne,
    modifier: Modifier = Modifier
) {
    var valeurText by remember { mutableStateOf("") }
    var articleState by remember { mutableStateOf(article.copy()) }

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

    }
}
fun calculateurPArRelationsEntreColumes(article: BaseDonne) {
    article.monPrixAchatUniter = article.monPrixVent / article.nmbrUnite
    article.prixDeVentTotaleChezClient = article.nmbrUnite * article.clienPrixVentUnite
    article.benficeTotaleEntreMoiEtClien = article.prixDeVentTotaleChezClient - article.monPrixAchat
    article.benificeTotaleEn2 = article.benficeTotaleEntreMoiEtClien / 2
    article.monBenfice = article.monPrixVent - article.monPrixAchat
    article.monPrixVent = article.monBenfice + article.monPrixAchat

}
@Composable
fun <T : Any> OutlinedTextFieldDynamique(
    article: BaseDonne,
    nomColum: KMutableProperty1<BaseDonne, T>,
    modifier: Modifier = Modifier.height(63.dp),
    textColore: Color = Color.Red,
    abdergNomColum: String? = nomColum.name
) {
    var valeurText by remember { mutableStateOf(nomColum.get(article).toString()) }

    LaunchedEffect(article) {
        valeurText = nomColum.get(article).toString()
    }

    OutlinedTextField(
        value = valeurText,
        onValueChange = { newText ->
            valeurText = newText
            val newValue: T? = parseValue(newText, nomColum.returnType)
            if (newValue != null) {
                nomColum.set(article, newValue)
                calculateurPArRelationsEntreColumes(article, )
            }
        },
        label = {
            AutoResizedText(
                text = "$abdergNomColum: ${nomColum.get(article)}",
                color = textColore,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        textStyle = TextStyle(color = textColore, textAlign = TextAlign.Center),
        modifier = modifier.fillMaxWidth()
    )
}

fun <T : Any> parseValue(value: String?, type: KType): T? {
    return try {
        when (type.withNullability(false)) {
            Int::class.createType() -> value?.toInt()
            Float::class.createType() -> value?.toFloat()
            Double::class.createType() -> value?.toDouble()
            Long::class.createType() -> value?.toLong()
            Boolean::class.createType() -> value?.toBoolean()
            String::class.createType() -> value
            else -> null
        } as? T
    } catch (e: Exception) {
        null
    }
}
fun <T : Any> calculateurParRelationsEntreColonnes2(
    newValue: String?,
    article: BaseDonne,
    nomColonne: KMutableProperty1<BaseDonne, T>,
    type: (String) -> T?,
) {
    val newValueTyped = newValue?.let(type)
    if (newValueTyped != null) {
        nomColonne.set(article, newValueTyped)
    }

    val monPrixAchat = article.monPrixAchat.toDouble()
    when (nomColonne) {
        BaseDonne::monPrixVent -> {
            val newBenfice = (newValueTyped as? Number)?.toDouble()?.minus(monPrixAchat)
            if (newBenfice != null) {
                article.monBenfice = newBenfice
            }
        }
        BaseDonne::monBenfice -> {
            val newPrixVent = (newValueTyped as? Number)?.toDouble()?.plus(monPrixAchat)
            if (newPrixVent != null) {
                article.monPrixVent = newPrixVent
            }
        }
    }

}
@Composable
fun DisplayArticleInformations2(
    article: BaseDonne,
    modifier: Modifier = Modifier,
) {
    // Using state to hold the values that will be shown in the OutlinedTextFields
    var valeurTextmonBenfice by remember { mutableStateOf(article.monBenfice.toString()) }
    var valeurTextmonPrixVent by remember { mutableStateOf(article.monPrixVent.toString()) }
    var valeurNmbrUnite by remember { mutableStateOf(article.nmbrUnite.toString()) }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        OutlinedTextField(
            value = valeurTextmonBenfice,
            onValueChange = { newText ->
                valeurTextmonBenfice = newText
                calculateurParRelationsEntreColonnes2(newText, article, BaseDonne::monBenfice, { it.toDoubleOrNull() }, )
            },
            label = { Text("m.B${article.monBenfice}") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = valeurNmbrUnite,
            onValueChange = { newText ->
                valeurNmbrUnite = newText
                calculateurParRelationsEntreColonnes2(newText, article, BaseDonne::nmbrUnite, { it.toIntOrNull() }, )
            },
            label = { Text("n.u${article.nmbrUnite}") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = valeurTextmonPrixVent,
            onValueChange = { newText ->
                valeurTextmonPrixVent = newText
                calculateurParRelationsEntreColonnes2(newText, article, BaseDonne::monPrixVent, { it.toDoubleOrNull() }, )
            },
            label = { Text("mpv${article.monPrixVent}") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, textAlign = TextAlign.Center)
        )
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
