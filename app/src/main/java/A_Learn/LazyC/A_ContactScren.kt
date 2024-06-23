package A_Learn.LazyC

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateRect
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abdelwahabjemlajetpack.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LazyGridApp() {
    var articlesList by rememberSaveable { mutableStateOf(generateArticles()) }
    var selectedArticle by remember { mutableStateOf<Article?>(null) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(listState.firstVisibleItemIndex) {
        coroutineScope.launch {
            articlesList = articlesList.map { it.copy(clicked = false) }
        }
    }

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
                            AnimatedVisibility(
                                visible = !article.clicked,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                            ) {
                                TestCard(article) { updatedArticle ->
                                    articlesList = articlesList.map {
                                        if (it.idArticle == updatedArticle.idArticle) updatedArticle else it.copy(clicked = false)
                                    }
                                    selectedArticle = updatedArticle
                                }
                            }
                        }
                    }
                    val clickedArticle = pairOfArticles.firstOrNull { it.clicked }
                    if (clickedArticle != null) {
                        DisplayClickedArticle(clickedArticle)
                    }
                }
            }
        }
    }
}

@Composable
fun TestCard(article: Article, onClick: (Article) -> Unit) {
    val alphaAnimation = remember { androidx.compose.animation.core.Animatable(0f) }
    val yAnimation = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            alphaAnimation.animateTo(1f, animationSpec = tween(1500))
        }
        launch {
            yAnimation.animateTo(50f, animationSpec = tween(1500))
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(150.dp)
            .clickable { onClick(article.copy(clicked = !article.clicked)) }
            .animateContentSize(animationSpec = tween(1500))
            .graphicsLayer(alpha = alphaAnimation.value, translationY = yAnimation.value),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = article.label,
                fontSize = 16.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = "Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun DisplayClickedArticle(article: Article) {
    val transition = updateTransition(article.clicked, label = "transition")

    val rect by transition.animateRect(label = "rect") { state ->
        if (state) Rect(0f, 0f, 300f, 300f) else Rect(0f, 0f, 150f, 150f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .graphicsLayer(
                scaleX = rect.width / 300f,
                scaleY = rect.height / 300f
            )
    ) {
        Column {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = "Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            Text(
                text = article.label,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

data class Article(
    val idArticle: Int,
    val label: String,
    var clicked: Boolean = false
)

fun generateArticles(): List<Article> {
    return List(20) { Article(idArticle = it, label = "Article $it") }
}
