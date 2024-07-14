package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun A_Edite_Base_Screen(
    editeBaseDonneViewModel: EditeBaseDonneViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val articles by editeBaseDonneViewModel.baseDonneStatTabel.collectAsState()
    val articlesDataBaseDonne = editeBaseDonneViewModel.dataBaseDonne
    val isFilterApplied by editeBaseDonneViewModel.isFilterApplied.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentChangingField by remember { mutableStateOf("") }
    var selectedArticle by remember { mutableStateOf<BaseDonneStatTabel?>(null) }
    var articleDataBaseDonne by remember { mutableStateOf<BaseDonne?>(null) }

    // Explication : Appliquez le filtre par Prix = 0.0
    Ab_FilterManager(showDialog, isFilterApplied, editeBaseDonneViewModel) { showDialog = false }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Articles List") },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Filter")
                    }
                }
            )
        },
        content = { paddingValues ->
            ArticlesScreenList(
                editeBaseDonneViewModel = editeBaseDonneViewModel,
                articlesDataBaseDonne = articleDataBaseDonne,
                articlesBaseDonneStatTabel = articles,
                selectedArticle = selectedArticle,
                // Dans onArticleSelect
                onArticleSelect = { article ->
                    val index = articles.indexOf(article)
                    selectedArticle = article
                    coroutineScope.launch {
                        if (index >= 0) {
                            listState.scrollToItem(index / 2)
                        }
                    }
                    currentChangingField = ""
                    articleDataBaseDonne = articlesDataBaseDonne.find { it.idArticle == article.idArticle }
                },
                listState = listState,
                currentChangingField = currentChangingField,
                paddingValues = paddingValues,
                function =  { currentChangingField = it },
                function1 = { articlesDataBaseDonne ->
                    if (articlesDataBaseDonne != null) {
                        articleDataBaseDonne = articlesDataBaseDonne.copy(affichageUniteState = !selectedArticle?.affichageUniteState!!)
                    }
                }
            )
        }
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ArticlesScreenList(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articlesDataBaseDonne: BaseDonne?,
    articlesBaseDonneStatTabel: List<BaseDonneStatTabel>,
    selectedArticle: BaseDonneStatTabel?,
    onArticleSelect: (BaseDonneStatTabel) -> Unit,
    listState: LazyListState,
    currentChangingField: String, // Add this parameter to receive padding values
    paddingValues: PaddingValues,
    function: (String) -> Unit,
    function1: (BaseDonne?) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(paddingValues) // Apply padding values here
    ) {
        itemsIndexed(items = articlesBaseDonneStatTabel.chunked(2)) { _, pairOfArticles ->
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedArticle != null && pairOfArticles.contains(selectedArticle)) {
                    DisplayDetailleArticle(
                        article = selectedArticle,
                        articlesDataBaseDonne= articlesDataBaseDonne,
                        editeBaseDonneViewModel = editeBaseDonneViewModel,
                        currentChangingField = currentChangingField,
                        function = function,
                        function1 = function1,
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
    articlesDataBaseDonne: BaseDonne?,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    currentChangingField: String,
    function: (String) -> Unit,
    function1: (BaseDonne?) -> Unit,
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
            TopRowQuantitys(
                article,
                articlesDataBaseDonne= articlesDataBaseDonne,
                viewModel = editeBaseDonneViewModel,
                currentChangingField = currentChangingField,
                function = function
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DisplayColorsCards(article, Modifier.weight(0.38f))
                DisplayArticleInformations(
                    editeBaseDonneViewModel = editeBaseDonneViewModel,
                    article = article,
                    articlesDataBaseDonne= articlesDataBaseDonne,
                    modifier = Modifier.weight(0.62f),
                    function = function,
                    currentChangingField = currentChangingField,
                    function1 =function1,
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
    articlesDataBaseDonne: BaseDonne?,
    viewModel: EditeBaseDonneViewModel,
    modifier: Modifier = Modifier,
    function: (String) -> Unit,
    currentChangingField: String,
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
            currentChangingField = currentChangingField,
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
    articlesDataBaseDonne: BaseDonne?,
    modifier: Modifier = Modifier,
    function: (String) -> Unit,
    currentChangingField: String,
    function1: (BaseDonne?) -> Unit,
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
                    modifier = Modifier
                        .weight(0.40f)
                        .height(63.dp)
                )
            }

            OutlineTextEditeBaseDonne(
                columnToChange = "monPrixAchat",
                abbreviation = "m.pA>",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = editeBaseDonneViewModel,
                function = function,
                modifier = Modifier
                    .weight(0.60f)
                    .height(63.dp)
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
        Row(
            modifier = Modifier
                .height(63.dp)
        ) {
            if (article.nmbrUnite > 1) {
                OutlineTextEditeBaseDonne(
                    columnToChange = "monBeneficeUniter",
                    abbreviation = "u/",
                    currentChangingField = currentChangingField,
                    article = article,
                    viewModel = editeBaseDonneViewModel,
                    function = function,
                    modifier = Modifier.weight(0.35f)
                )
            }
            OutlineTextEditeBaseDonne(
                columnToChange = "monBenfice",
                abbreviation = "M.B",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = editeBaseDonneViewModel,
                function = function,
                modifier = Modifier.weight(0.65f)
            )
        }
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
        // Utilisation d'un état mutable pour que l'UI réagisse aux changements

        ArticleToggleButton(
            article = articlesDataBaseDonne,
            viewModel = editeBaseDonneViewModel,
            function1 = function1
        )

    }
}
@Composable
fun ArticleToggleButton(
    article: BaseDonne?,
    viewModel: EditeBaseDonneViewModel,
    function1: (BaseDonne?) -> Unit,
) {
        UniteToggleButton(
            articleDataBaseDonneStat = article,
            onClick = {function1(article)
                viewModel.updateDataBaseDonne(article)
            }
        )
}

@Composable
fun UniteToggleButton(
    articleDataBaseDonneStat: BaseDonne?,
    onClick: () -> Unit
) {
    if (articleDataBaseDonneStat != null) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (articleDataBaseDonneStat.affichageUniteState) Color.Green else Color.Red
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            if (articleDataBaseDonneStat != null) {
                Text(
                    text = if (articleDataBaseDonneStat.affichageUniteState)
                        "Cacher les Unités"
                    else
                        "Afficher les Unités"
                )
            }
        }
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





