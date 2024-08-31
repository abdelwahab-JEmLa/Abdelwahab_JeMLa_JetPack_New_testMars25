package d_EntreBonsGro


import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import b_Edite_Base_Donne.ArticleDao
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
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
    val baseHeight = if (isPortraitLandscap) 270 else 550
    val heightOfImageAndRelated = (baseHeight + heightAdjustment).dp

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
                        onImageSizeChanged = { if (imageIndex == 0) imageSize = it }
                    )
                }
            }
        }

        // Extracted and centered height adjustment controls
        if (heightOfImageAndRelatedDialogEditer) {
            HeightAdjustmentControls(
                heightOfImageAndRelated = heightOfImageAndRelated,
                onHeightAdjustment = { adjustment -> heightAdjustment += adjustment }
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
}

@Composable
fun HeightAdjustmentControls(
    heightOfImageAndRelated: Dp,
    onHeightAdjustment: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Height: ${heightOfImageAndRelated.value.toInt()}dp")
                Row {
                    IconButton(onClick = { onHeightAdjustment(-10) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }
                    IconButton(onClick = { onHeightAdjustment(10) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }
        }
    }
}
@Composable
fun Displayer(
    imageIndex: Int,
    painter: AsyncImagePainter,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    heightOfImageAndRelated: Dp,
    onArticleClick: (EntreBonsGrosTabele) -> Unit,
    articlesBaseDonne: List<BaseDonne>,
    onImageSizeChanged: (IntSize) -> Unit
) {
    var leftColumnOffset by remember { mutableStateOf(0f) }
    var rightColumnOffset by remember { mutableStateOf(0f) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightOfImageAndRelated)
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val columnWidthPx = with(density) { 100.dp.toPx() }

        // Image Displayer
        ImageDisplayer(
            painter,
            heightOfImageAndRelated,
            onImageSizeChanged,
            sectionsDonsChaqueImage,
            modifier = Modifier.fillMaxSize(),
        )

        // Floating columns
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left floating column
            Box(
                modifier = Modifier
                    .offset { IntOffset(leftColumnOffset.roundToInt(), 0) }
                    .width(100.dp)
                    .fillMaxHeight()
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            leftColumnOffset = (leftColumnOffset + delta).coerceIn(0f, maxWidthPx - columnWidthPx)
                        }
                    )
            ) {
                ArticleColumn(
                    imageIndex = imageIndex,
                    sectionsDonsChaqueImage = sectionsDonsChaqueImage,
                    filteredAndSortedArticles = filteredAndSortedArticles,
                    heightOfImageAndRelated = heightOfImageAndRelated,
                    onArticleClick = onArticleClick,
                    articlesBaseDonne = articlesBaseDonne,
                    columnType = ColumnType.QuantityPriceSubtotal
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Right floating column
            Box(
                modifier = Modifier
                    .offset { IntOffset(rightColumnOffset.roundToInt(), 0) }
                    .width(100.dp)
                    .fillMaxHeight()
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            rightColumnOffset = (rightColumnOffset + delta).coerceIn(-(maxWidthPx - columnWidthPx), 0f)
                        }
                    )
            ) {
                ArticleColumn(
                    imageIndex = imageIndex,
                    sectionsDonsChaqueImage = sectionsDonsChaqueImage,
                    filteredAndSortedArticles = filteredAndSortedArticles,
                    heightOfImageAndRelated = heightOfImageAndRelated,
                    onArticleClick = onArticleClick,
                    articlesBaseDonne = articlesBaseDonne,
                    columnType = ColumnType.ArticleNames
                )
            }
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
private fun ImageDisplayer(
    painter: AsyncImagePainter,
    heightOfImageAndRelated: Dp,
    onImageSizeChanged: (IntSize) -> Unit,
    sectionsDonsChaqueImage: Int,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(heightOfImageAndRelated)
            .clip(RectangleShape)
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 3f)
            val maxX = (constraints.maxWidth * (scale - 1)) / 2
            val maxY = (constraints.maxHeight * (scale - 1)) / 2
            offset = Offset(
                x = (offset.x + offsetChange.x).coerceIn(-maxX, maxX),
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
fun ArticleColumn(
    modifier: Modifier = Modifier,
    imageIndex: Int,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    heightOfImageAndRelated: Dp,
    onArticleClick: (EntreBonsGrosTabele) -> Unit,
    articlesBaseDonne: List<BaseDonne>,
    columnType: ColumnType
) {
    Box(
        modifier = modifier.height(heightOfImageAndRelated)
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
                        when (columnType) {
                            ColumnType.QuantityPriceSubtotal -> {
                                QuantityPriceSubtotalCompos(article)
                            }
                            ColumnType.ArticleNames -> {
                                ArticleNamesCompos(articlesBaseDonne, article)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityPriceSubtotalCompos(article: EntreBonsGrosTabele) {
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

@Composable
private fun reconnaisanceVocaleLencer(
    selectedArticle: EntreBonsGrosTabele?,
    articlesRef: DatabaseReference,
    coroutineScope: CoroutineScope,
    baseDonneRef: DatabaseReference,
    articleDao: ArticleDao,
    initialFilteredSuggestions: List<String>,
    suggestionsList: List<String>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    context: Context
): Pair<ManagedActivityResultLauncher<Intent, ActivityResult>, List<String>> {
    var filteredSuggestions by remember { mutableStateOf(initialFilteredSuggestions) }
    var showSuggestions by remember { mutableStateOf(false) }

    fun processVoiceInput(input: String) {
        if (input.firstOrNull()?.isDigit() == true || input.contains("+") || input.startsWith("-")) {
            selectedArticle?.let {
                updateQuantuPrixArticleDI(input, it, articlesRef, coroutineScope)
            }
        } else if (input.contains("تغيير")) {
            // Fixed: Extract the new Arabic name correctly
            val newArabName = input.substringAfter("تغيير").trim()
            if (newArabName.isNotEmpty()) {
                coroutineScope.launch {
                    selectedArticle?.let { article ->
                        baseDonneRef.child(article.idArticleBG.toString()).child("nomArab")
                            .setValue(newArabName)
                        articleDao.updateArticleArabName(article.idArticleBG, newArabName)
                    }
                }
            } else {
                // Handle the case when no new name is provided after "تغيير"
                Toast.makeText(context, "Veuillez fournir un nouveau nom après 'تغيير'", Toast.LENGTH_SHORT).show()
            }
        } else {
            val cleanInput = input.replace(".", "").toLowerCase()
            filteredSuggestions = suggestionsList.filter { it.replace(".", "").toLowerCase().contains(cleanInput) }

            when {
                filteredSuggestions.size == 1 -> {
                    updateArticleIdFromSuggestionDI(
                        suggestion = filteredSuggestions[0],
                        selectedArticle = selectedArticle?.vidBG,
                        articlesRef = articlesRef,
                        articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                        articlesBaseDonne = articlesBaseDonne,
                        onNameInputComplete = { /* Implement if needed */ },
                        editionPassedMode = false,
                        articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                        coroutineScope = coroutineScope
                    )
                }
                filteredSuggestions.isEmpty() -> {
                    val filteredSuggestions3Sentence = suggestionsList.filter {
                        it.replace(".", "").toLowerCase().contains(cleanInput.take(3))
                    }
                    showSuggestions = true
                    filteredSuggestions = filteredSuggestions3Sentence
                }
                else -> {
                    showSuggestions = true
                }
            }
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                processVoiceInput(it)
            }
        } else {
            Toast.makeText(
                context,
                "La reconnaissance vocale a échoué. Veuillez réessayer.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    if (showSuggestions) {
        SuggestionsDialog(
            filteredSuggestions = filteredSuggestions,
            onSuggestionSelected = { suggestion ->
                updateArticleIdFromSuggestionDI(
                    suggestion = suggestion,
                    selectedArticle = selectedArticle?.vidBG,
                    articlesRef = articlesRef,
                    articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                    articlesBaseDonne = articlesBaseDonne,
                    onNameInputComplete = { /* Implement if needed */ },
                    editionPassedMode = false,
                    articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                    coroutineScope = coroutineScope
                )
                showSuggestions = false
            },
            onDismiss = { showSuggestions = false }
        )
    }
    return Pair(speechRecognizerLauncher, filteredSuggestions)
}

enum class ColumnType {
    QuantityPriceSubtotal,
    ArticleNames
}



@Composable
fun ImageCountDialog(
    onDismiss: () -> Unit,
    onSelectCount: (Int) -> Unit
) {
    var selectedCount by remember { mutableStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Number of Images") },
        text = {
            Column {
                Text("Selected count: $selectedCount")
                Slider(
                    value = selectedCount.toFloat(),
                    onValueChange = { selectedCount = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSelectCount(selectedCount)
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DialogsController(
    showDiviseurDesSections: Boolean,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    founisseurIdNowIs: Long?,
    showDialogeNbrIMGs: Boolean,
    onDissmiss: () -> Unit,
    selectedArticle: EntreBonsGrosTabele?,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    coroutineScope: CoroutineScope,
    showOutlineDialog: Boolean,
    suggestionsList: List<String>,
    onSectionCountChange: (Int) -> Unit,
    onImageCountChange: (Int) -> Unit,
    onOutlineDialogClose: () -> Unit
) {
    if (showDiviseurDesSections) {
        TreeCountControl(
            sectionsDonsChaqueImage = sectionsDonsChaqueImage,
            filteredAndSortedArticles = filteredAndSortedArticles,
            founisseurIdNowIs = founisseurIdNowIs,
            onCountChange = onSectionCountChange
        )
    }

    if (showDialogeNbrIMGs) {
        ImageCountDialog(
            onDismiss = onDissmiss,
            onSelectCount = onImageCountChange
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
            onDismiss = onOutlineDialogClose
        )
    }
}
@Composable
fun SuggestionsDialog(
    filteredSuggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Suggestions") },
        text = {
            LazyColumn {
                items(filteredSuggestions) { suggestion ->
                    val randomColor = Color(
                        red = (0..255).random(),
                        green = (0..255).random(),
                        blue = (0..255).random()
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = randomColor
                        )
                    ) {
                        TextButton(
                            onClick = { onSuggestionSelected(suggestion) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Text(suggestion)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OutlineDialog(
    selectedArticle: EntreBonsGrosTabele?,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    suggestionsList: List<String>,
    articlesRef: DatabaseReference,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier l'article") },
        text = {
            selectedArticle?.let { article ->
                OutlineInputDI(
                    inputText = "",
                    articlesList = articlesEntreBonsGrosTabele,
                    articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                    articlesBaseDonne = articlesBaseDonne,
                    suggestionsList = suggestionsList,
                    articlesRef = articlesRef,
                    coroutineScope = coroutineScope,
                    selectedArticle = article.vidBG
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}


@Composable
fun AutoResizedTextDI(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center,
    bodyLarge: Boolean= false
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize = if (bodyLarge) MaterialTheme.typography.bodyLarge.fontSize else MaterialTheme.typography.bodyMedium.fontSize

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
fun OutlineInputDI(
    inputText: String,
    articlesList: List<EntreBonsGrosTabele>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    suggestionsList: List<String>,
    articlesRef: DatabaseReference,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    selectedArticle: Long
) {
    var showDropdown by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var textFieldFocused by remember { mutableStateOf(false) }
    var currentInputText by remember { mutableStateOf(inputText) }

    val selectedArticleData = articlesList.find { it.vidBG == selectedArticle }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = currentInputText,
                onValueChange = { newValue ->
                    currentInputText = newValue
                    if (newValue.length >= 3) {
                        val cleanInput = newValue.replace(".", "").toLowerCase()
                        filteredSuggestions = suggestionsList.asSequence().filter { suggestion ->
                            val cleanSuggestion = suggestion.replace(".", "").toLowerCase(Locale.ROOT)
                            if (isArabicDI(cleanInput)) {
                                cleanSuggestion.contains(cleanInput.take(3))
                            } else {
                                cleanSuggestion.contains(cleanInput)
                            }
                        }.take(10).toList()
                        showDropdown = filteredSuggestions.isNotEmpty() && textFieldFocused
                    } else {
                        filteredSuggestions = emptyList()
                        showDropdown = false
                    }
                },
                label = {
                    Text(
                        when {
                            currentInputText.isEmpty() && selectedArticleData != null -> {
                                val baseDonneArticle = articlesBaseDonne.find { it.idArticle.toLong() == selectedArticleData.idArticleBG }
                                val nomArabe = baseDonneArticle?.nomArab ?: ""
                                "Quantity: ${selectedArticleData.quantityAcheteBG} x ${selectedArticleData.newPrixAchatBG} (${selectedArticleData.nomArticleBG}) $nomArabe"
                            }
                            currentInputText.isEmpty() -> "Entrer quantité et prix"
                            else -> currentInputText
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        textFieldFocused = focusState.isFocused
                        showDropdown = filteredSuggestions.isNotEmpty() && textFieldFocused
                    },
                trailingIcon = {
                    if (currentInputText.isNotEmpty()) {
                        IconButton(onClick = { currentInputText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear input")
                        }
                    }
                }
            )

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            updateArticleIdFromSuggestionDI(
                                suggestion = suggestion,
                                selectedArticle = selectedArticle,
                                articlesRef = articlesRef,
                                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                                articlesBaseDonne = articlesBaseDonne,
                                onNameInputComplete = {
                                    currentInputText = ""
                                    showDropdown = false
                                },
                                editionPassedMode = false,
                                articlesEntreBonsGrosTabele = articlesList,
                                coroutineScope = coroutineScope
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (selectedArticleData != null) {
                    updateQuantuPrixArticleDI(currentInputText, selectedArticleData, articlesRef, coroutineScope)
                    currentInputText = ""
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Update")
        }
    }
}
// Helper function to check if a string contains Arabic characters
fun isArabicDI(text: String): Boolean {
    return text.any { it.code in 0x0600..0x06FF || it.code in 0x0750..0x077F || it.code in 0x08A0..0x08FF }
}

@Composable
private fun TreeCountControl(
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    founisseurIdNowIs: Long?,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("sections count: $sectionsDonsChaqueImage")
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            if (sectionsDonsChaqueImage > 1) {
                onCountChange(sectionsDonsChaqueImage - 1)
                if (filteredAndSortedArticles.isNotEmpty()) {
                    deleteTheNewArticleIZ(filteredAndSortedArticles.last().vidBG)
                }
            }
        }) {
            Text("-")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            onCountChange(sectionsDonsChaqueImage + 1)
            createNewArticle(filteredAndSortedArticles, founisseurIdNowIs, sectionsDonsChaqueImage + 1)
        }) {
            Text("+")
        }
    }
}
