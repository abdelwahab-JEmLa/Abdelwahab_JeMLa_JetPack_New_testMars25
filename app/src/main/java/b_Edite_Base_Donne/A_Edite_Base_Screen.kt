package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.abdelwahabjemlajetpack.R
import com.example.abdelwahabjemlajetpack.ui.theme.DarkGreen
import com.example.abdelwahabjemlajetpack.ui.theme.Pink80
import kotlinx.coroutines.launch
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun A_Edite_Base_Screen(
    editeBaseDonneViewModel: EditeBaseDonneViewModel = viewModel(),
    articleDao: ArticleDao,
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
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    // Explication : Appliquez le filtre par Prix = 0.0
    Ab_FilterManager(showDialog, isFilterApplied, editeBaseDonneViewModel) { showDialog = false }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Articles List",
                )
                IconButton(onClick = { showDialog = true }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Filter")
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isSearchVisible = !isSearchVisible }
            ) {
                Icon(
                    imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchVisible) "Close Search" else "Open Search"
                )
            }
        },
        content = { paddingValues ->
            Column {
                if (isSearchVisible) {//TODO pk ca ne s affiche pas au bas de app ar row
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            editeBaseDonneViewModel.updateSearchQuery(it)
                        },
                        label = { Text("Search Articles") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
                ArticlesScreenList(
                    editeBaseDonneViewModel = editeBaseDonneViewModel,
                    articlesDataBaseDonne = articlesDataBaseDonne,
                    articlesBaseDonneStatTabel = articles,
                    selectedArticle = selectedArticle,
                    onArticleSelect = { article ->
                        val index = articles.indexOf(article)
                        focusManager.clearFocus()
                        selectedArticle = article
                        coroutineScope.launch {
                            if (index >= 0) {
                                listState.scrollToItem(index / 2)
                            }
                        }
                        currentChangingField = ""
                        articleDataBaseDonne =
                            articlesDataBaseDonne.find { it.idArticle == article.idArticle }
                    },
                    listState = listState,
                    currentChangingField = currentChangingField,
                    paddingValues = paddingValues,
                    function = { currentChangingField = it },
                    function1 = { articlesDataBaseDonne ->
                        if (articlesDataBaseDonne != null) {
                            articleDataBaseDonne = articlesDataBaseDonne.copy(affichageUniteState = !articlesDataBaseDonne.affichageUniteState)
                            editeBaseDonneViewModel.updateDataBaseDonne(articleDataBaseDonne)
                        }
                    },
                    onClickImageDimentionChangeur = { baseDonne ->
                        val updatedArticle = baseDonne.copy(funChangeImagsDimention = !baseDonne.funChangeImagsDimention)
                        editeBaseDonneViewModel.updateDataBaseDonne(updatedArticle)
                    }
                )
            }
        }
    )
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ArticlesScreenList(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articlesDataBaseDonne: List<BaseDonne>,
    articlesBaseDonneStatTabel: List<BaseDonneStatTabel>,
    selectedArticle: BaseDonneStatTabel?,
    onArticleSelect: (BaseDonneStatTabel) -> Unit,
    listState: LazyListState,
    currentChangingField: String,
    paddingValues: PaddingValues,
    function: (String) -> Unit,
    function1: (BaseDonne?) -> Unit,
    onClickImageDimentionChangeur: (BaseDonne) -> Unit,
) {

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(paddingValues)
    ) {
        itemsIndexed(items = articlesBaseDonneStatTabel.chunked(2)) { _, pairOfArticles ->
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedArticle != null && pairOfArticles.contains(selectedArticle)) {
                    val relatedBaseDonne = articlesDataBaseDonne.find { it.idArticle == selectedArticle.idArticle }
                    DisplayDetailleArticle(
                        article = selectedArticle,
                        articlesDataBaseDonne = relatedBaseDonne,
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
                        val relatedBaseDonne = articlesDataBaseDonne.find { it.idArticle == article.idArticle }
                        ArticleBoardCard(
                            article = article,
                            articlesDataBaseDonne = relatedBaseDonne,
                            onClickImageDimentionChangeur = onClickImageDimentionChangeur,
                            onArticleSelect = onArticleSelect
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArticleBoardCard(
    article: BaseDonneStatTabel,
    articlesDataBaseDonne: BaseDonne?,
    onClickImageDimentionChangeur: (BaseDonne) -> Unit,
    onArticleSelect: (BaseDonneStatTabel) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable {
                onArticleSelect(article)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.padding(2.dp)
        ) {
            Column {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(230.dp)
                ) {
                    val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                    LoadImageFromPath(imagePath = imagePath)

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(0.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .background(Color.White.copy(alpha = 0.7f))
                                .padding(0.dp)
                        ) {
                            val articlemonBenfice = article.monPrixVent - article.monPrixAchat
                            val monBeneficeUniter = articlemonBenfice / article.nmbrUnite
                            AutoResizedText(
                                text = "Be>${if (articlesDataBaseDonne?.affichageUniteState == false) String.format("%.1f", articlemonBenfice) else String.format("%.1f", monBeneficeUniter)}",
                                textAlign = TextAlign.Center,
                                color = DarkGreen,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .background(Color.White.copy(alpha = 0.7f))
                                .padding(0.dp)
                        ) {
                            AutoResizedText(
                                text = "Pv>${if (articlesDataBaseDonne?.affichageUniteState == false) String.format("%.1f", article.monPrixVent) else String.format("%.1f", article.monPrixVentUniter)}",
                                textAlign = TextAlign.Center,
                                color = Color.Red,
                            )
                        }
                    }
                }
                AutoResizedText(
                    text = capitalizeFirstLetter(article.nomArticleFinale),
                    modifier = Modifier.padding(vertical = 0.dp),
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
                AutoResizedText(
                    text = capitalizeFirstLetter(article.nomCategorie),
                    modifier = Modifier.padding(vertical = 0.dp),
                    textAlign = TextAlign.Center,
                    color = Pink80
                )
            }
            if (articlesDataBaseDonne != null) {
                IconButton(
                    onClick = { onClickImageDimentionChangeur(articlesDataBaseDonne) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = if (articlesDataBaseDonne.funChangeImagsDimention) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (articlesDataBaseDonne.funChangeImagsDimention) Color.Red else DarkGreen
                    )
                }
            }
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

        // Nouvelle ligne pour les boutons de calcul
        Row(
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    val newPrice = article.monPrixAchat / article.nmbrUnite
                    function("monPrixAchat")
                    editeBaseDonneViewModel.updateCalculated(newPrice.toString(), "monPrixAchat", article)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("/")
            }
            Button(
                onClick = {
                    val newPrice2 = article.monPrixAchat * article.nmbrUnite
                    function("monPrixAchat")
                    editeBaseDonneViewModel.updateCalculated(newPrice2.toString(), "monPrixAchat", article)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("*")
            }
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
            onClick = {function1(article) }
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
                containerColor = if (articleDataBaseDonneStat.affichageUniteState) DarkGreen else Color.Red
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


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoadImageFromPath(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.blanc

    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = imageExist ?: defaultDrawable,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
        ) {
            it
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(1000) // Set a larger size
                .thumbnail(0.25f) // Start with 25% quality
                .fitCenter() // Ensure the image fits within the bounds
                .transition(DrawableTransitionOptions.withCrossFade()) // Smooth transition as quality improves
        }
    }
}






