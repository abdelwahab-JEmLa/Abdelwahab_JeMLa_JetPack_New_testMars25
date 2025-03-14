package d_EntreBonsGro


import a_MainAppCompnents.ArticlesAcheteModele
import a_RoomDB.BaseDonne
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
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
import com.google.firebase.database.DatabaseReference
import f_credits.SupplierTabelle
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
    heightOfImageAndRelatedDialogEditer: Boolean,
    supplierList: List<SupplierTabelle>,
    modeVerificationAvantUpdateBD: Boolean,
    voiceFrancais: Boolean,
    suggestionsListFromAutreNom: List<String>,
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
    val baseHeight = if (isPortraitLandscap) 270 else 420
    val baseWidth = if (isPortraitLandscap) 350 else 700
    val heightOfImageAndRelated = (baseHeight + heightAdjustment).dp
    val widthOfImageAndRelated = (baseWidth + widthAdjustment).dp

    var leftColumnOffset by remember { mutableStateOf(0f) }
    var rightColumnOffset by remember { mutableStateOf(0f) }
    var imageOffset by remember { mutableStateOf(0f) }

    var itsImageClick by remember { mutableStateOf(false) }

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
        articlesEntreBonsGrosTabele.filter { it.supplierIdBG==founisseurIdNowIs },
        context ,itsImageClick  ,founisseurIdNowIs ,
        suggestionsListFromAutreNom=suggestionsListFromAutreNom,
        voiceFrancais=voiceFrancais
    )
    val speechRecognizerLauncher = reconnaisanceVocaleLencer.first
    filteredSuggestions = reconnaisanceVocaleLencer.second


    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (modeVerificationAvantUpdateBD){
            Windos_AvantExpo(
                articlesEntreBonsGro = filterArticlesGrosPourUpdate(articlesEntreBonsGrosTabele),
                articlesRef = articlesRef,
                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                coroutineScope = coroutineScope,
            )
        }else{

        Column {

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
                        itsImageClick=  false
                        selectedArticle = article
                        if (showOutline) {
                            showOutlineDialog = true
                        } else {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastLaunchTime > 1000) {
                                lastLaunchTime = currentTime
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (voiceFrancais) "fr-FR" else "ar-DZ")
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
                    // Example onImageClick logic that launches the speech recognizer
                    onImageClick = {
                        itsImageClick=  true
                        if (showOutline) {
                            showOutlineDialog = true
                        } else {
                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastLaunchTime > 1000) {
                                lastLaunchTime = currentTime
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (voiceFrancais) "fr-FR" else "ar-DZ")
                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour cet article...")
                                }
                                speechRecognizerLauncher.launch(intent)
                            }
                        }
                    },
                    imageOffset = imageOffset, voiceFrancais =voiceFrancais ,
                    suggestionsListFromAutreNom=suggestionsListFromAutreNom,
                         )

                     }
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
            onDismiss = { showOutlineDialog = false },
            suggestionsListFromAutreNom = suggestionsListFromAutreNom, voiceFrancais = voiceFrancais
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
        }, supplierList = supplierList  ,
        suggestionsListFromAutreNom=suggestionsListFromAutreNom,  voiceFrancais = voiceFrancais,

        )
}

@Composable
fun filterArticlesGrosPourUpdate(articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>) =
    articlesEntreBonsGrosTabele.filter { article ->
        (article.ancienPrixBG - article.newPrixAchatBG) != 0.0
                && article.quantityAcheteBG != 0
                && article.nomArticleBG != ""
                && !article.nomArticleBG.contains("New", ignoreCase = true)
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
    onImageClick: () -> Unit,
    articlesBaseDonne: List<BaseDonne>,
    onImageSizeChanged: (IntSize) -> Unit,
    leftColumnOffset: Float,
    rightColumnOffset: Float,
    onLeftColumnDrag: (Float) -> Unit,
    onRightColumnDrag: (Float) -> Unit,
    imageOffset: Float, voiceFrancais: Boolean, suggestionsListFromAutreNom: List<String>
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
                .clickable { onImageClick() }

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

        // Right floating column (ArticleNames)
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
                onDrag = onLeftColumnDrag,
                voiceFrancais=voiceFrancais
            )
        }

        //  Left floating column (QuantityPrice)
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
    val columnWidth = 100.dp

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
                    val isZeroQuantityOrPrice =
                        article.quantityAcheteBG.toDouble() == 0.0 || article.newPrixAchatBG == 0.0

                    val cardColor = if (isZeroQuantityOrPrice) Color.Transparent else Color.Transparent
                    val textColor = if (isZeroQuantityOrPrice) Color.White else Color.White

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heightOfImageAndRelated / sectionsDonsChaqueImage)
                            .clickable { onArticleClick(article) }
                            .background(cardColor)
                            .then(
                                if (!isZeroQuantityOrPrice) {
                                    Modifier.drawWithContent {
                                        drawContent()
                                        // Blue dashed line at the top
                                        drawDashedLine(
                                            start = Offset(0f, 0f),
                                            end = Offset(size.width, 0f),
                                            color = Color.Blue,
                                            strokeWidth = 2f
                                        )
                                        // Red dotted line at the bottom
                                        drawDottedLine(
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, size.height),
                                            color = Color.Red,
                                            strokeWidth = 2f
                                        )
                                    }
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                       val textOf =if (!isZeroQuantityOrPrice) {"${article.quantityAcheteBG} X ${article.newPrixAchatBG}"
                        } else {article.idArticleInSectionsOfImageBG}
                            AutoResizedTextDI(
                                text = textOf,
                                color = textColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
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
    onDrag: (Float) -> Unit,
    voiceFrancais: Boolean,
) {
    val columnWidth = 100.dp

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
                    val isZeroQuantityOrPrice =
                        article.quantityAcheteBG.toDouble() == 0.0 || article.newPrixAchatBG == 0.0

                    if (!isZeroQuantityOrPrice) {
                        val relatedArticle = articlesBaseDonne.find { baseDonne -> baseDonne.idArticle.toLong() == article.idArticleBG }
                        var imageExist by remember { mutableStateOf(false) }

                        val isNewArticle = article.nomArticleBG.contains("New", ignoreCase = true)
                        val cardColor = if (isNewArticle) Color.Yellow.copy(alpha = 0.3f) else Color.Transparent
                        val textColor = if (isNewArticle) Color.White else Color.White

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heightOfImageAndRelated / sectionsDonsChaqueImage)
                                .clickable { onArticleClick(article) }
                                .border(BorderStroke(1.dp, Color.Gray))
                                .background(cardColor)
                                .drawWithContent {
                                    drawContent()
                                    // Blue dashed line at the top
                                    drawDashedLine(
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        color = Color.Blue,
                                        strokeWidth = 2f
                                    )
                                    // Red dotted line at the bottom
                                    drawDottedLine(
                                        start = Offset(0f, size.height),
                                        end = Offset(size.width, size.height),
                                        color = Color.Red,
                                        strokeWidth = 2f
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                                val standart =  "${if (!imageExist||article.idArticleBG>2000) article.nomArticleBG else ""} ${if (article.idArticleBG<2000) relatedArticle?.nomArab ?: "" else ""}"
                                val voiceFrancaisText= if (article.idArticleBG>2000) article.nomArticleBG else  relatedArticle?.autreNomDarticle ?: ""
                                val itsvoiceFrancais= if (voiceFrancais) voiceFrancaisText else   standart
                                // Text Card (70% width)
                                Box(
                                    modifier = Modifier
                                        .weight(0.7f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AutoResizedTextDI(
                                        text = itsvoiceFrancais,
                                        color = textColor,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        // If isZeroQuantityOrPrice is true, we don't display anything
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heightOfImageAndRelated / sectionsDonsChaqueImage)
                        )
                    }
                }
            }
        }
    }
}
private fun DrawScope.drawDashedLine(start: Offset, end: Offset, color: Color, strokeWidth: Float) {
    val pathLength = (end - start).getDistance()
    val dashLength = 15f
    val gapLength = 10f
    val intervals = pathLength / (dashLength + gapLength)

    for (i in 0 until intervals.toInt()) {
        val startX = start.x + i * (dashLength + gapLength)
        val startY = start.y
        val endX = (startX + dashLength).coerceAtMost(end.x)
        val endY = startY

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawDottedLine(start: Offset, end: Offset, color: Color, strokeWidth: Float) {
    val pathLength = (end - start).getDistance()
    val dotLength = 5f
    val gapLength = 5f
    val intervals = pathLength / (dotLength + gapLength)

    for (i in 0 until intervals.toInt()) {
        val startX = start.x + i * (dotLength + gapLength)
        val startY = start.y
        val endX = (startX + dotLength).coerceAtMost(end.x)
        val endY = startY

        drawLine(
            color = color,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = strokeWidth
        )
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
                }
            }
        }
    }
}




