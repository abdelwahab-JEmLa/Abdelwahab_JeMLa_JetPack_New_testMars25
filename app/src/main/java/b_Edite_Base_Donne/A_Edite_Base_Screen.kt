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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
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
            columnToChange = "nmbrUnite",
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
            columnToChange = "nmbrCaron",
            abbreviation = "n.c",
            function = function,
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            modifier = Modifier
                .weight(1f)
                .height(63.dp)
        )
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
                    .height(100.dp)
            ) {
                AutoResizedText(
                    text = "${article.monPrixAchatUniter}/U",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }
            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                    .weight(0.70f)
                    .height(100.dp)
            ) {
                AutoResizedText(
                    text = "m.PA -> ${article.monPrixAchat}",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )

            }
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
            columnToChange = "monBenfice",
            abbreviation = "M.B",
            currentChangingField = currentChangingField,
            article = article,
            viewModel = editeBaseDonneViewModel,
            function = { currentChangingField = it },
            modifier = Modifier
        )

        Spacer(modifier = Modifier.width(5.dp))
        Row(
            modifier = Modifier
                .height(63.dp)
        )
        {
            Spacer(modifier = Modifier.width(5.dp))

            Box(
                modifier = Modifier
                    .padding(top = 7.dp)
                    .border(1.dp, Color.Gray, shape = MaterialTheme.shapes.extraSmall)
                    .height(100.dp)
                    .weight(0.30f)
            ) {
                AutoResizedText(
                    text = "${article.monPrixVentUniter}/U",
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.Center)
                        .height(40.dp)
                )
            }

            OutlineTextEditeBaseDonne(
                columnToChange = "monPrixVent",
                abbreviation = "M.P.V",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = editeBaseDonneViewModel,
                function = { currentChangingField = it },
                modifier = Modifier
                    .weight(0.70f)
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





