package d_EntreBonsGro


import a_RoomDB.BaseDonne
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.ArticleDao
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import java.io.File
import kotlin.math.roundToInt

@Composable
fun DessinableImage(
    modifier: Modifier = Modifier,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    founisseurIdNowIs: Long?,
    soquetteBonNowIs: Int?,
    showDiviseurDesSections: Boolean,
    articlesRef: DatabaseReference,
    baseDonneRef: DatabaseReference,
    suggestionsList: List<String>,
    articleDao: ArticleDao,
    coroutineScope: CoroutineScope,
    showOutline: Boolean,
    showDialogeNbrIMGs: Boolean,
    onDissmiss: () -> Unit,
    heightOfImageAndRelatedDialogEditer: Boolean
) {
    val configuration = LocalConfiguration.current
    val isPortraitLandscap = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val filteredAndSortedArticles = articlesEntreBonsGrosTabele
        .filter { it.supplierIdBG == founisseurIdNowIs }
        .sortedBy { it.idArticleInSectionsOfImageBG }
    var nmbrImagesDuBon by remember { mutableStateOf(7) }
    var sectionsDonsChaqueImage by rememberSaveable { mutableStateOf(15) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val context = LocalContext.current
    val lazyListState = rememberLazyListState()

    var showOutlineDialog by remember { mutableStateOf(false) }
    var selectedArticle by remember { mutableStateOf<EntreBonsGrosTabele?>(null) }
    var lastLaunchTime by remember { mutableStateOf(0L) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }

    var heightAdjustment by remember { mutableStateOf(0) }
    var widthAdjustment by remember { mutableStateOf(0) }
    val baseHeight = if (isPortraitLandscap) 270 else 550
    val baseWidth = if (isPortraitLandscap) 350 else 700
    val heightOfImageAndRelated = (baseHeight + heightAdjustment).dp
    val widthOfImageAndRelated = (baseWidth + widthAdjustment).dp

    var leftColumnOffset by remember { mutableStateOf(0f) }
    var rightColumnOffset by remember { mutableStateOf(0f) }
    var imageOffset by remember { mutableStateOf(0f) }

    val reconnaisanceVocaleLencer = reconnaisanceVocaleLencer(
        selectedArticle,
        articlesRef,
        coroutineScope,
        baseDonneRef,
        articleDao,
        filteredSuggestions,
        suggestionsList,
        articlesArticlesAcheteModele,
        articlesBaseDonne,
        articlesEntreBonsGrosTabele,
        context
    )
    val speechRecognizerLauncher = reconnaisanceVocaleLencer.first
    filteredSuggestions = reconnaisanceVocaleLencer.second

    Box(modifier = modifier.fillMaxSize()) {
        Column {
            if (showDiviseurDesSections) {
                TreeCountControl(
                    sectionsDonsChaqueImage = sectionsDonsChaqueImage,
                    filteredAndSortedArticles = filteredAndSortedArticles,
                    founisseurIdNowIs = founisseurIdNowIs,
                    onCountChange = { newCount ->
                        sectionsDonsChaqueImage = newCount
                    }
                )
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f)
            ) {
                items(nmbrImagesDuBon) { imageIndex ->
                    val imagePath = "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${soquetteBonNowIs ?: 1}.${imageIndex + 1}).jpg"
                    val imageUri = Uri.parse(imagePath)
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context).data(imageUri).build()
                    )

                    Displayer(
                        imageIndex = imageIndex,
                        painter = painter,
                        sectionsDonsChaqueImage = sectionsDonsChaqueImage,
                        filteredAndSortedArticles = filteredAndSortedArticles,
                        heightOfImageAndRelated = heightOfImageAndRelated,
                        widthOfImageAndRelated = widthOfImageAndRelated,
                        onArticleClick = { article ->
                            selectedArticle = article
                            if (showOutline) {
                                showOutlineDialog = true
                            } else {
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastLaunchTime > 1000) {
                                    lastLaunchTime = currentTime
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour cet article...")
                                    }
                                    speechRecognizerLauncher.launch(intent)
                                }
                            }
                        },
                        articlesBaseDonne = articlesBaseDonne,
                        onImageSizeChanged = { if (imageIndex == 0) imageSize = it },
                        leftColumnOffset = leftColumnOffset,
                        rightColumnOffset = rightColumnOffset,
                        onLeftColumnDrag = { delta ->
                            leftColumnOffset += delta
                        },
                        onRightColumnDrag = { delta ->
                            rightColumnOffset += delta
                        },
                        imageOffset
                    )
                }
            }
        }

        // Height and width adjustment controls with offset adjustment
        if (heightOfImageAndRelatedDialogEditer) {
            HeightAndWidthAdjustmentControls(
                heightOfImageAndRelated = heightOfImageAndRelated,
                widthOfImageAndRelated = widthOfImageAndRelated,
                onHeightAdjustment = { adjustment -> heightAdjustment += adjustment },
                onWidthAdjustment = { adjustment -> widthAdjustment += adjustment },
                onOffsetAdjustment = { adjustment -> imageOffset += adjustment } // Update image offset
            )
        }
    }

    // Dialogs
    if (showDialogeNbrIMGs) {
        ImageCountDialog(
            onDismiss = onDissmiss,
            onSelectCount = { newCount ->
                nmbrImagesDuBon = newCount
            }
        )
    }

    if (showOutlineDialog) {
        OutlineDialog(
            selectedArticle = selectedArticle,
            articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
            articlesArticlesAcheteModele = articlesArticlesAcheteModele,
            articlesBaseDonne = articlesBaseDonne,
            suggestionsList = suggestionsList,
            articlesRef = articlesRef,
            coroutineScope = coroutineScope,
            onDismiss = { showOutlineDialog = false }
        )
    }
    DialogsController(
        showDiviseurDesSections = showDiviseurDesSections,
        sectionsDonsChaqueImage = sectionsDonsChaqueImage,
        filteredAndSortedArticles = filteredAndSortedArticles,
        founisseurIdNowIs = founisseurIdNowIs,
        showDialogeNbrIMGs = showDialogeNbrIMGs,
        onDissmiss = onDissmiss,
        selectedArticle = selectedArticle,
        articlesRef = articlesRef,
        articlesArticlesAcheteModele = articlesArticlesAcheteModele,
        articlesBaseDonne = articlesBaseDonne,
        articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
        coroutineScope = coroutineScope,
        showOutlineDialog = showOutlineDialog,
        suggestionsList = suggestionsList,
        onSectionCountChange = { newCount ->
            sectionsDonsChaqueImage = newCount
        },
        onImageCountChange = { newCount ->
            nmbrImagesDuBon = newCount
        },
        onOutlineDialogClose = {
            showOutlineDialog = false
        }
    )
}@Composable
fun ImageDisplayer(
    painter: AsyncImagePainter,
    heightOfImageAndRelated: Dp,
    imageOffset: Offset,
    onImageSizeChanged: (IntSize) -> Unit,
    sectionsDonsChaqueImage: Int,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(heightOfImageAndRelated)
            .clip(RectangleShape)
            .offset { IntOffset(imageOffset.x.toInt(), imageOffset.y.toInt()) }
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 3f)

            // Calculate maximum offset based on scale and constraints
            val maxX = (constraints.maxWidth * (scale - 1)) / 2
            val maxY = (constraints.maxHeight * (scale - 1)) / 2

            // Calculate the new offset, ensuring it doesn't go beyond the zoomed-in image bounds
            offset = Offset(
                x = (offset.x + offsetChange.x).coerceIn(
                    -maxX + imageOffset.x, // Consider imageOffset for maxX
                    maxX + imageOffset.x  // Consider imageOffset for minX
                ),
                y = (offset.y + offsetChange.y).coerceIn(-maxY, maxY)
            )
        }

        Image(
            painter = painter,
            contentDescription = "Image for supplier section",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state)
                .onSizeChanged(onImageSizeChanged)
                .drawWithContent {
                    drawContent()
                    val redColor = Color.Red.copy(alpha = 0.3f)
                    val blueColor = Color.Blue.copy(alpha = 0.3f)
                    for (i in 0 until sectionsDonsChaqueImage) {
                        val top = size.height * i.toFloat() / sectionsDonsChaqueImage
                        val bottom = size.height * (i + 1).toFloat() / sectionsDonsChaqueImage
                        val color = if (i % 2 == 0) redColor else blueColor
                        drawRect(
                            color = color,
                            topLeft = Offset(0f, top),
                            size = Size(size.width, bottom - top)
                        )
                    }
                }
        )
    }
}
@Composable
fun Displayer(
    imageIndex: Int,
    painter: AsyncImagePainter,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    heightOfImageAndRelated: Dp,
    widthOfImageAndRelated: Dp,
    onArticleClick: (EntreBonsGrosTabele) -> Unit,
    articlesBaseDonne: List<BaseDonne>,
    onImageSizeChanged: (IntSize) -> Unit,
    leftColumnOffset: Float,
    rightColumnOffset: Float,
    onLeftColumnDrag: (Float) -> Unit,
    onRightColumnDrag: (Float) -> Unit,
    imageOffset: Float
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightOfImageAndRelated)
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val columnWidthPx = with(density) { 100.dp.toPx() }
        val imageWidthPx = with(density) { widthOfImageAndRelated.toPx() }

        // Image Displayer
        Box(
            modifier = Modifier
                .width(widthOfImageAndRelated)
                .height(heightOfImageAndRelated)
                .align(Alignment.Center)
        ) {
            ImageDisplayer(
                painter = painter,
                heightOfImageAndRelated = heightOfImageAndRelated,
                imageOffset = Offset(imageOffset, 0f),  // Correctly pass Offset using x and y
                onImageSizeChanged = onImageSizeChanged,
                sectionsDonsChaqueImage = sectionsDonsChaqueImage,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Right floating column (QuantityPrice)
        Box(
            modifier = Modifier
                .offset { IntOffset(rightColumnOffset.roundToInt(), 0) }
                .width(100.dp)
                .fillMaxHeight()
                .align(Alignment.CenterStart)
        ) {
            QuantityPrice(
                imageIndex = imageIndex,
                sectionsDonsChaqueImage = sectionsDonsChaqueImage,
                filteredAndSortedArticles = filteredAndSortedArticles,
                heightOfImageAndRelated = heightOfImageAndRelated,
                onArticleClick = onArticleClick,
                articlesBaseDonne = articlesBaseDonne,
                onDrag = onRightColumnDrag
            )
        }

        // Left floating column (ArticleNames)
        Box(
            modifier = Modifier
                .offset { IntOffset(leftColumnOffset.roundToInt(), 0) }
                .width(100.dp)
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
        ) {
            NameColumn(
                imageIndex = imageIndex,
                sectionsDonsChaqueImage = sectionsDonsChaqueImage,
                filteredAndSortedArticles = filteredAndSortedArticles,
                heightOfImageAndRelated = heightOfImageAndRelated,
                onArticleClick = onArticleClick,
                articlesBaseDonne = articlesBaseDonne,
                onDrag = onLeftColumnDrag
            )
        }

        // Loading and error states
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is AsyncImagePainter.State.Error -> {
                Text(
                    text = "Error loading image",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {}
        }
    }
}
@Composable
fun QuantityPrice(
    modifier: Modifier = Modifier,
    imageIndex: Int,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    heightOfImageAndRelated: Dp,
    onArticleClick: (EntreBonsGrosTabele) -> Unit,
    articlesBaseDonne: List<BaseDonne>,
    onDrag: (Float) -> Unit
) {
    val density = LocalDensity.current
    val columnWidth by remember { mutableStateOf(100.dp) }

    Box(
        modifier = modifier
            .height(heightOfImageAndRelated)
            .width(columnWidth)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    onDrag(delta)
                }
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(sectionsDonsChaqueImage) { sectionIndex ->
                val articleIndex = imageIndex * sectionsDonsChaqueImage + sectionIndex
                val article = filteredAndSortedArticles.getOrNull(articleIndex)

                article?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heightOfImageAndRelated / sectionsDonsChaqueImage)
                            .clickable { onArticleClick(article) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        QuantityPrixCompos(article)
                    }
                }
            }
        }
    }
}

@Composable
fun NameColumn(
    modifier: Modifier = Modifier,
    imageIndex: Int,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    heightOfImageAndRelated: Dp,
    onArticleClick: (EntreBonsGrosTabele) -> Unit,
    articlesBaseDonne: List<BaseDonne>,
    onDrag: (Float) -> Unit
) {
    val density = LocalDensity.current
    val columnWidth by remember { mutableStateOf(100.dp) }

    Box(
        modifier = modifier
            .height(heightOfImageAndRelated)
            .width(columnWidth)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    onDrag(delta)
                }
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(sectionsDonsChaqueImage) { sectionIndex ->
                val articleIndex = imageIndex * sectionsDonsChaqueImage + sectionIndex
                val article = filteredAndSortedArticles.getOrNull(articleIndex)

                article?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heightOfImageAndRelated / sectionsDonsChaqueImage)
                            .clickable { onArticleClick(article) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        ArticleNamesCompos(articlesBaseDonne, article)
                    }
                }
            }
        }
    }
}
@Composable
private fun QuantityPrixCompos(article: EntreBonsGrosTabele) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val isZeroQuantityOrPrice =
                article.quantityAcheteBG.toDouble() == 0.0 || article.newPrixAchatBG == 0.0
            val cardColor = if (isZeroQuantityOrPrice) Color.Transparent else Color.Red
            val textColor = if (isZeroQuantityOrPrice) Color.Red else Color.White
            val borderColor = if (isZeroQuantityOrPrice) Color.Red else Color.White

            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                border = BorderStroke(1.dp, borderColor)
            ) {
                if (isZeroQuantityOrPrice) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Zero quantity or price",
                        tint = Color.Red,
                    )
                } else {
                    AutoResizedTextDI(
                        text = "${article.quantityAcheteBG} X ${article.newPrixAchatBG}",
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ArticleNamesCompos(
    articlesBaseDonne: List<BaseDonne>,
    article: EntreBonsGrosTabele
) {
    val relatedArticle = articlesBaseDonne.find { baseDonne -> baseDonne.idArticle.toLong() == article.idArticleBG }

    relatedArticle?.let { related ->
        var imageExist by remember { mutableStateOf(false) }

        Row(modifier = Modifier.fillMaxWidth()) {
            // Image Card (30% width)
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .aspectRatio(1f)
            ) {
                ImageArticles(
                    article = article,
                    onImageExist = { imageExist = true },
                    onImageNonexist = { imageExist = false }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Text Card (70% width)
            val isNewArticle = article.nomArticleBG.contains("New", ignoreCase = true)
            val cardColor = if (isNewArticle) Color.Yellow.copy(alpha = 0.3f) else Color.Unspecified
            val textColor = if (isNewArticle) Color.Red else Color.Black

            Card(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = cardColor)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizedTextDI(
                        text = "${if (!imageExist) article.nomArticleBG else ""} ${related.nomArab ?: ""}",
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ImageArticles(
    article: EntreBonsGrosTabele,
    onImageExist: () -> Unit,
    onImageNonexist: () -> Unit,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imagePathWithoutExt = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticleBG}_1"
            val imagePathWebp = "$imagePathWithoutExt.webp"
            val imagePathJpg = "$imagePathWithoutExt.jpg"
            val webpExists = File(imagePathWebp).exists()
            val jpgExists = File(imagePathJpg).exists()

            when {
                webpExists || jpgExists -> {
                    val imagePath = if (webpExists) imagePathWebp else imagePathJpg
                    AsyncImage(
                        model = imagePath,
                        contentDescription = "Article image",
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(90f),
                        contentScale = ContentScale.Fit,
                        onSuccess = { onImageExist() },
                        onError = { onImageNonexist() }
                    )
                }
                else -> {
                    onImageNonexist()
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image")
                    }
                }
            }
        }
    }
}



