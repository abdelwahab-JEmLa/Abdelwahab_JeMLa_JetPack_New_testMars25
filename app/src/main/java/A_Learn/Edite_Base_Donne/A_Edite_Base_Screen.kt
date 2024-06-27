package com.example.mycomposeapp.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import com.example.mycomposeapp.ui.BaseDonne
import com.example.mycomposeapp.ui.MainAppViewModel
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun A_Edite_Base_Screen(
    modifier: Modifier = Modifier,
    mainAppViewModel: MainAppViewModel = viewModel()
) {
    Column(modifier = modifier.fillMaxSize()) {
        ArticlesScreenList(
            articlesList = mainAppViewModel.articlesBaseDonne
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreenList(articlesList: List<BaseDonne>) {
    var selectedArticle by remember { mutableStateOf<BaseDonne?>(null) }
    val listState = rememberLazyListState()

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
                            TestCard(article) { updatedArticle ->
                                selectedArticle = updatedArticle
                            }
                        }
                    }
                    selectedArticle?.let { article ->
                        if (pairOfArticles.contains(article)) {
                            DisplayClickedArticle(article)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestCard(article: BaseDonne, onClick: (BaseDonne) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick(article) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(230.dp)
            ) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_1"
                LoadImageFromPath(imagePath = imagePath)
            }
            Text(
                text = article.a_d_nomarticlefinale_c,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun DisplayClickedArticle(article: BaseDonne) {
    val alphaAnimation = remember { androidx.compose.animation.core.Animatable(0f) }
    val yAnimation = remember { androidx.compose.animation.core.Animatable(0f) }
    val scaleAnimation = remember { androidx.compose.animation.core.Animatable(3f) }
    LaunchedEffect(Unit) {
        launch {
            alphaAnimation.animateTo(1f, animationSpec = tween(1000))
        }
        launch {
            yAnimation.animateTo(35f, animationSpec = tween(1000))
        }
        launch {
            scaleAnimation.animateTo(1f, animationSpec = tween(800))
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .animateContentSize(animationSpec = tween(1500))
            .graphicsLayer(alpha = alphaAnimation.value, translationY = yAnimation.value),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(230.dp)
            ) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_1"
                LoadImageFromPath(imagePath = imagePath, modifier = Modifier.graphicsLayer(scaleX = scaleAnimation.value, scaleY = scaleAnimation.value))
            }
            Text(
                text = article.a_d_nomarticlefinale_c,
                modifier = Modifier.padding(8.dp)
            )
        }
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
            .fillMaxSize(),
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
