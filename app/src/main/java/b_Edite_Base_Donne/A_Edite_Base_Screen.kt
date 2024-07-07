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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import java.io.File

@Composable
fun A_Edite_Base_Screen(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedArticle by remember { mutableStateOf<BaseDonneStatTabel?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        ArticlesScreenList(
            editeBaseDonneViewModel,
            articlesBaseDonneStatTabel = editeBaseDonneViewModel.baseDonneStatTabel,
            selectedArticle = selectedArticle,
            onArticleSelect = { selectedArticle = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreenList(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articlesBaseDonneStatTabel: List<BaseDonneStatTabel>,
    selectedArticle: BaseDonneStatTabel?,
    onArticleSelect: (BaseDonneStatTabel) -> Unit,
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
            items(items = articlesBaseDonneStatTabel.chunked(2)) { pairOfArticles ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        pairOfArticles.forEach { article ->
                            ArticleBoardCard(article) { updatedArticle ->
                                onArticleSelect(updatedArticle)
                                focusManager.clearFocus()
                            }
                        }
                    }
                    selectedArticle?.let { article ->
                        if (pairOfArticles.contains(article)) {
                            DisplayDetailleArticle(
                                article = article,
                                editeBaseDonneViewModel = editeBaseDonneViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleBoardCard(article: BaseDonneStatTabel, onClick: (BaseDonneStatTabel) -> Unit) {
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
fun DisplayDetailleArticle(
    article: BaseDonneStatTabel,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
) {
    var currentChangingField by remember { mutableStateOf("") }
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .wrapContentSize()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopRowQuantitys(
                article,
                viewModel =editeBaseDonneViewModel,
                currentChangingField = currentChangingField,
                function = { currentChangingField = it }
            )
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                DisplayColorsCards(article, Modifier.weight(0.38f))
                DisplayArticleInformations(editeBaseDonneViewModel, article, Modifier.weight(0.62f))
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
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    modifier: Modifier = Modifier,
    function: (String) -> Unit,
    currentChangingField: String
) {


    Row(
        modifier = modifier
            .padding(3.dp)
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.width(3.dp))
        OutlineTextEditeBaseDonne(
            columnToChangeInString = "nmbrUnite",
            abbreviation = "n.u",
            currentChangingField =currentChangingField ,
            article = article,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .height(63.dp),
            function = function
        )
        Spacer(modifier = Modifier.width(3.dp))
        OutlineTextEditeBaseDonne(
            columnToChangeInString = "nmbrCaron",
            abbreviation = "n.c",
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .height(63.dp),
            function = function

        )
    }
}


@Composable
fun DisplayColorsCards(
    article: BaseDonneStatTabel,
    modifier: Modifier = Modifier
) {
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
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    article: BaseDonneStatTabel,
    modifier: Modifier = Modifier
) {
    var currentChangingField by remember { mutableStateOf("") }

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
            OutlineTextEditeBaseDonne(
                columnToChangeInString = "nmbrUniteIndicator",
                abbreviation = "",
                currentChangingField = currentChangingField,
                article = article,
                viewModel =editeBaseDonneViewModel,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.70f)
                    .height(45.dp),
                function = { currentChangingField = it }

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
                    text = "m.PF -> ${article.monPrixVent}",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(5.dp))
        OutlineTextEditeBaseDonne(
            columnToChangeInString = "monPrixVentIndicator",
            abbreviation = "",
            currentChangingField = currentChangingField,
            article = article,
            viewModel =editeBaseDonneViewModel,
            function = { currentChangingField = it }

        )
    }
}
//---------------------------------------------------------------

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



//----------------------------------------------------------------------


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





fun calculateNewValuesBaseDonneStatTabel(
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

