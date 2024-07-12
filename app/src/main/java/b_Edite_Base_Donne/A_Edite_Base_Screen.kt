package b_Edite_Base_Donne

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun A_Edite_Base_Screen(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedArticle by remember { mutableStateOf<BaseDonneStatTabel?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize()) {
        ArticlesScreenList(
            editeBaseDonneViewModel,
            articlesBaseDonneStatTabel = editeBaseDonneViewModel.baseDonneStatTabel,
            selectedArticle = selectedArticle,
            onArticleSelect = { article ->
                editeBaseDonneViewModel.updateCalculated("0.0", "", article)
                selectedArticle = article
                val index = editeBaseDonneViewModel.baseDonneStatTabel.indexOf(article)
                coroutineScope.launch {
                    if (index >= 0) {
                        listState.scrollToItem(index / 2) // Divide by 2 because we are chunking by 2
                    }
                }
            },
            listState = listState
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
    listState: LazyListState
) {
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
            itemsIndexed(items = articlesBaseDonneStatTabel.chunked(2)) { index, pairOfArticles ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (selectedArticle != null && pairOfArticles.contains(selectedArticle)) {
                        DisplayDetailleArticle(
                            article = selectedArticle,
                            editeBaseDonneViewModel = editeBaseDonneViewModel
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        pairOfArticles.forEach { article ->
                            ArticleBoardCard(article, editeBaseDonneViewModel) { updatedArticle ->
                                onArticleSelect(updatedArticle)
                                focusManager.clearFocus()
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
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    onClick: (BaseDonneStatTabel) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable {
                onClick(article)
            },
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
                viewModel = editeBaseDonneViewModel,
                currentChangingField = currentChangingField,
                function = { currentChangingField = it }
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically

            ) {
                DisplayColorsCards(article,
                    Modifier.weight(0.38f),
                )
                DisplayArticleInformations(
                    editeBaseDonneViewModel = editeBaseDonneViewModel,
                    article = article,
                    modifier = Modifier.weight(0.62f),
                    function = { currentChangingField = it },
                    currentChangingField = currentChangingField,
                )
            }
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
fun capitalizeFirstLetter(text: String): String {
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
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
            .fillMaxWidth()
    ) {
        OutlineTextEditeBaseDonne(
            columnToChange = "clienPrixVentUnite",
            abbreviation = "c.pU",
            function = function,
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)
        )
        OutlineTextEditeBaseDonne(
            columnToChange = "nmbrCaron",
            abbreviation = "n.c",
            currentChangingField =currentChangingField ,
            article = article,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .height(67.dp),
            function = function
        )
        OutlineTextEditeBaseDonne(
            columnToChange = "nmbrUnite",
            abbreviation = "n.u",
            function = function,
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)
        )

    }
}
// Composable Function to Display Article Information
@Composable
fun DisplayArticleInformations(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    article: BaseDonneStatTabel,
    modifier: Modifier = Modifier,
    function: (String) -> Unit,
    currentChangingField: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
        ) {
            if (article.nmbrUnite > 1) {
                OutlineTextEditeBaseDonne(
                    columnToChange = "monPrixAchatUniter",
                    abbreviation = "U/",
                    currentChangingField = currentChangingField,
                    article = article,
                    viewModel = editeBaseDonneViewModel,
                    function = function,
                    modifier = Modifier.weight(0.40f).height(63.dp)
                )
            }

            OutlineTextEditeBaseDonne(
                columnToChange = "monPrixAchat",
                abbreviation = "m.pA>",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = editeBaseDonneViewModel,
                function = function,
                modifier = Modifier.weight(0.60f).height(63.dp)
            )
        }

        if (article.clienPrixVentUnite > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(top = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                        .height(100.dp)
                        .weight(1f)
                ) {
                    AutoResizedText(
                        text = "b.E2 -> ${article.benificeTotaleEn2}",
                        modifier = Modifier
                            .padding(4.dp)
                            .align(Alignment.Center)
                            .height(40.dp)
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
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
            }
            OutlineTextEditeBaseDonne(
                columnToChange = "benificeClient",
                abbreviation = "b.c",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = editeBaseDonneViewModel,
                function = function,
                modifier = Modifier
            )
        }

        OutlineTextEditeBaseDonne(
            columnToChange = "monBenfice",
            abbreviation = "M.B",
            currentChangingField = currentChangingField,
            article = article,
            viewModel = editeBaseDonneViewModel,
            function = function,
            modifier = Modifier
        )

        Row(
            modifier = Modifier
                .height(63.dp)
        ) {
            if (article.nmbrUnite > 1) {
                OutlineTextEditeBaseDonne(
                    columnToChange = "monPrixVentUniter",
                    abbreviation = "u/",
                    currentChangingField = currentChangingField,
                    article = article,
                    viewModel = editeBaseDonneViewModel,
                    function = function,
                    modifier = Modifier.weight(0.35f)
                )
            }

            OutlineTextEditeBaseDonne(
                columnToChange = "monPrixVent",
                abbreviation = "M.P.V",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = editeBaseDonneViewModel,
                function = function,
                modifier = Modifier.weight(0.65f)
            )
        }
        ArticleToggleButton(article = article, viewModel = editeBaseDonneViewModel)

    }
}
@Composable
fun ArticleToggleButton(
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel
) {
    // Utilisation d'un état mutable pour que l'UI réagisse aux changements
    var articleDataBaseDonneStat by remember {
        mutableStateOf(viewModel.dataBaseDonne.find { it.idArticle == article.idArticle })
    }

    // Si articleDataBaseDonneStat n'est pas null, afficher le bouton
    articleDataBaseDonneStat?.let { data ->
        UniteToggleButton(
            articleDataBaseDonneStat = data,
            onClick = {
                // Inverser l'état de affichageUniteState et mettre à jour l'état mutable
                val copyChange = data.copy(affichageUniteState = !data.affichageUniteState)
                articleDataBaseDonneStat = copyChange
                viewModel.updateDataBaseDonne(copyChange)
            }
        )
    }
}

@Composable
fun UniteToggleButton(
    articleDataBaseDonneStat: DataBaseDonne,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (articleDataBaseDonneStat.affichageUniteState) Color.Green else Color.Red
        ),
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = if (articleDataBaseDonneStat.affichageUniteState)
                "Cacher les Unités"
            else
                "Afficher les Unités"
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
                Card(
                    modifier = Modifier
                        .width(250.dp)
                        .height(300.dp)
                        .padding(end = 8.dp)
                ) {
                    val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_${index + 1}"
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
        }
    }
}

//---------------------------------------------------------------



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





