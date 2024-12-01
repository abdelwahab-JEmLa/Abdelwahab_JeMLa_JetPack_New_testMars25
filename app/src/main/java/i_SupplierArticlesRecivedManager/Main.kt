package i_SupplierArticlesRecivedManager

import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.DataBaseArticles
import a_MainAppCompnents.GroupeurBonCommendToSupplierTabele
import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.TabelleSuppliersSA
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.abdelwahabjemlajetpack.R
import i2_FragmentMapArticleInSupplierStore.DisplayeImageById
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Fragment_SupplierArticlesRecivedManager(
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    onNewArticleAdded: (DataBaseArticles) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSupplierArticle by viewModel.currentSupplierArticle.collectAsState()
    var dialogeDisplayeDetailleChanger by remember { mutableStateOf<GroupeurBonCommendToSupplierTabele?>(null) }
    var showFloatingButtons by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableIntStateOf(2) }
    var voiceInputText by remember { mutableStateOf("") }

    // State declarations
    var toggleCtrlToFilterToMove by remember { mutableStateOf(false) }
    var idSupplierOfFloatingButtonClicked by remember { mutableStateOf<Long?>(null) }
    var itsReorderMode by remember { mutableStateOf(false) }
    var firstClickedSupplierForReorder by remember { mutableStateOf<Long?>(null) }
    var itsMoveFirstNonDefined by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            voiceInputText = spokenText
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = modifier.fillMaxSize()
        ) { padding ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                VoiceInputField(
                    value = voiceInputText,
                    onValueChange = { newText ->
                        voiceInputText = newText
                        val parts = newText.split("+").map { it.trim() }
                        when {
                            parts.size == 2 -> {
                                handleTwoPartInput(parts[0], parts[1], viewModel, uiState)
                            }
                            parts.size == 1 && parts[0].length >= 2 -> {
                                // Search by classmentSupplier or idSupplierSu when input is 2+ characters
                                val searchText = parts[0]
                                val matchingSupplier = uiState.tabelleSuppliersSA.find { supplier ->
                                    supplier.classmentSupplier.toString().startsWith(searchText) ||
                                            supplier.idSupplierSu.toString().startsWith(searchText)
                                }
                                matchingSupplier?.let { supplier ->
                                    idSupplierOfFloatingButtonClicked = supplier.idSupplierSu
                                }
                            }
                            itsMoveFirstNonDefined && parts.size == 1 -> {
                                val inputText = parts[0]
                                if (inputText.length >= 2) {
                                    val clasmentToSupp = inputText.toDoubleOrNull()
                                    // Find the first article from the "Non Defined" supplier (ID 10)
                                    val nonDefinedArticles = uiState.groupeurBonCommendToSupplierTabele.filter {
                                        it.idSupplierTSA.toLong() == 10L
                                    }
                                    val firstNonDefinedArticle = nonDefinedArticles.firstOrNull()

                                    // Find target supplier by classement
                                    val targetSupplier = uiState.tabelleSuppliersSA.find {
                                        it.classmentSupplier == clasmentToSupp
                                    }

                                    if (firstNonDefinedArticle != null && targetSupplier != null) {
                                        viewModel.moveArticlesToSupplier(
                                            articlesToMove = listOf(firstNonDefinedArticle),
                                            toSupp = targetSupplier.idSupplierSu
                                        )
                                    }
                                }
                            }
                        }
                    },
                    uiState = uiState
                )

                LazyColumn(
                    modifier = modifier.fillMaxSize()
                ) {
                    val filterSupp = if (idSupplierOfFloatingButtonClicked != null) {
                        uiState.tabelleSuppliersSA.filter {
                            it.idSupplierSu == idSupplierOfFloatingButtonClicked
                        }
                    } else {
                        uiState.tabelleSuppliersSA
                    }

                    filterSupp.forEach { supplier ->
                        val articlesSupplier = uiState.groupeurBonCommendToSupplierTabele.filter {
                            it.idSupplierTSA.toLong() == supplier.idSupplierSu &&
                                    (!toggleCtrlToFilterToMove || it.itsInFindedAskSupplierSA)
                        }

                        if (articlesSupplier.isNotEmpty() &&
                            supplier.nomSupplierSu != "Find" &&
                            supplier.nomSupplierSu != "Non Define"
                        ) {
                            stickyHeader(key = "header_${supplier.idSupplierSu}") {
                                SupplierHeaderSA(
                                    supplier = supplier,
                                    viewModel = viewModel,
                                )
                            }

                            items(
                                items = articlesSupplier,
                                key = { article ->
                                    // Create a unique key combining supplier ID and article ID
                                    "article_${supplier.idSupplierSu}_${article.vid}"
                                }
                            ) { article ->
                                CardArticlePlace(
                                    uiState=   uiState,
                                    article = article,
                                    onClickToShowWindowsInfoArt = { dialogeDisplayeDetailleChanger = it } ,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating buttons container
        Box(
            modifier = modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            var offset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offset += dragAmount
                        }
                    }
            ) {
                GlobaleControlsFloatingButtonsSA(
                    showFloatingButtons = showFloatingButtons,
                    onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                    onChangeGridColumns = { gridColumns = it },
                    onToggleToFilterToMove = { toggleCtrlToFilterToMove = !toggleCtrlToFilterToMove },
                    filterSuppHandledNow = toggleCtrlToFilterToMove,
                    onLaunchVoiceRecognition = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour cet article...")
                        }
                        speechRecognizerLauncher.launch(intent)
                    },
                    viewModel = viewModel,
                    uiState = uiState,
                    onNewArticleAdded = onNewArticleAdded  ,
                    onToggleMoveFirstNonDefined = { itsMoveFirstNonDefined = !itsMoveFirstNonDefined }
                )
            }
        }

        // Show supplier floating buttons
        SuppliersFloatingButtonsSA(
            allArticles = uiState.groupeurBonCommendToSupplierTabele,
            suppliers = uiState.tabelleSuppliersSA,
            supplierFlotBisHandled = idSupplierOfFloatingButtonClicked,
            onClickFlotButt = { clickedSupplierId ->
                if (itsReorderMode) {
                    if (firstClickedSupplierForReorder == null) {
                        firstClickedSupplierForReorder = clickedSupplierId
                    } else if (firstClickedSupplierForReorder != clickedSupplierId) {
                        viewModel.reorderSuppliers(firstClickedSupplierForReorder!!, clickedSupplierId)
                        firstClickedSupplierForReorder = null
                    } else {
                        firstClickedSupplierForReorder = null
                    }
                } else {
                    if (toggleCtrlToFilterToMove) {
                        val filterBytabelleSupplierArticlesRecived =
                            uiState.groupeurBonCommendToSupplierTabele.filter {
                                it.itsInFindedAskSupplierSA
                            }
                        viewModel.moveArticlesToSupplier(
                            articlesToMove = filterBytabelleSupplierArticlesRecived,
                            toSupp = clickedSupplierId
                        )
                        toggleCtrlToFilterToMove = false
                    } else {
                        idSupplierOfFloatingButtonClicked = when (idSupplierOfFloatingButtonClicked) {
                            clickedSupplierId -> null
                            else -> clickedSupplierId
                        }
                    }
                }
            },
            itsReorderMode = itsReorderMode,
            firstClickedSupplierForReorder = firstClickedSupplierForReorder,
            onUpdateVocalArabName = { supplierId, newName ->
                viewModel.updateSupplierVocalArabName(supplierId, newName)
            },
            onUpdateVocalFrencheName = { supplierId, newName ->
                viewModel.updateSupplierVocalFrencheName(supplierId, newName)
            },
            onToggleReorderMode = {
                itsReorderMode = !itsReorderMode
                if (!itsReorderMode) {
                    firstClickedSupplierForReorder = null
                }
            }
        )

        // Display article detail dialog
        val displayedArticle = currentSupplierArticle?.takeIf {
            it.a_c_idarticle_c.toLong() == dialogeDisplayeDetailleChanger?.a_c_idarticle_c
        } ?: dialogeDisplayeDetailleChanger

        displayedArticle?.let { article ->
            WindowArticleDetail(
                article = article,
                onDismissWithUpdatePlaceArticle = {
                    dialogeDisplayeDetailleChanger = null
                },
                onDismissWithUpdateOfnonDispo = { updatedArticle ->
                    dialogeDisplayeDetailleChanger = null
                    viewModel.updateArticleStatus(updatedArticle)
                },
                onDismiss = {
                    dialogeDisplayeDetailleChanger = null
                },
                viewModel = viewModel,
                modifier = modifier.padding(horizontal = 3.dp)
            )
        }
    }
}

@Composable
fun VoiceInputField(
    value: String,
    onValueChange: (String) -> Unit,
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Voice Input") },
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),                     
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = {
            Text(
                text = "Format: articleId + supplierName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    )
}

private fun handleTwoPartInput(
    articleIdStr: String,
    supplierName: String,
    viewModel: HeadOfViewModels,
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
) {
    val articleId = articleIdStr.toLongOrNull()
    if (articleId != null) {
        val article = uiState.groupeurBonCommendToSupplierTabele.find {
            it.vid == articleId
        }
        val supplier = uiState.tabelleSuppliersSA.find {
            it.nameInFrenche.equals(supplierName, ignoreCase = true)
        }

        if (article != null && supplier != null) {
            viewModel.moveArticlesToSupplier(
                articlesToMove = listOf(article),
                toSupp = supplier.idSupplierSu
            )
        }
    }
}

@Composable
fun CardArticlePlace(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    article: GroupeurBonCommendToSupplierTabele,
    modifier: Modifier = Modifier,
    onClickToShowWindowsInfoArt: (GroupeurBonCommendToSupplierTabele) -> Unit,
    reloadKey: Long = 0,
) {
    val corependentDataBase = uiState.articlesBaseDonneECB.find { it.idArticle.toLong() == article.vid }
    val corependentnamePlacePLaceInStore = uiState.mapArticleInSupplierStore.find {
        it.idPlace.toLong() == (corependentDataBase?.idPlaceStandartInStoreSupplier ?: 0)
    }?.namePlace

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable(onClick = { onClickToShowWindowsInfoArt(article) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = modifier.fillMaxSize()
        )  {
            // Background image
            DisplayeImageById(
                idArticle = article.a_c_idarticle_c,
                modifier = modifier
                    .fillMaxWidth()
                    .height(120.dp),
                reloadKey = reloadKey
            )

            // Semi-transparent overlay
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // Darker blinking yellow overlay for itsInFindedAskSupplierSA
            if (article.itsInFindedAskSupplierSA) {
                Box(
                    modifier = modifier
                        .fillMaxSize()
                        .alpha(alpha)
                        .background(Color(0xFFFFD700).copy(alpha = 0.7f))  // Darker yellow
                )
            }

            // Article details
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.nameArticle,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val text = "[${article.vid}] Q: ${article.totalquantity}"
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )

                    // Added Row for location info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = corependentnamePlacePLaceInStore ?: "Non Defini",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }

                    if (article.itsInFindedAskSupplierSA) {
                        Text(
                            text = "Ask",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFD700)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun WindowArticleDetail(
    article: GroupeurBonCommendToSupplierTabele,
    onDismissWithUpdatePlaceArticle: () -> Unit,
    onDismissWithUpdateOfnonDispo: (GroupeurBonCommendToSupplierTabele) -> Unit,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier
) {
    val reloadKey = remember(article) { System.currentTimeMillis() }

    val infiniteTransition = rememberInfiniteTransition(label = "yellowPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yellowPulseAlpha"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .clickable { onDismissWithUpdatePlaceArticle() },
            shape = MaterialTheme.shapes.large,
            color = if (article.itsInFindedAskSupplierSA) Color.Blue.copy(alpha = 0.3f)
            else Color.Red.copy(alpha = 0.3f)
        ) {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = modifier.fillMaxWidth()) {
                    val ifStat =
                        article.color2SoldQuantity + article.color3SoldQuantity + article.color4SoldQuantity == 0
                    Box(
                        modifier = modifier
                            .clickable { onDismissWithUpdatePlaceArticle() }
                            .height(if (ifStat) 250.dp else 500.dp)
                    ) {
                        if (ifStat) {
                            SingleColorImageSA(article, viewModel, reloadKey)
                        } else {
                            MultiColorGridSA(article, viewModel, reloadKey)
                        }
                    }

                    // Article name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onDismissWithUpdateOfnonDispo(article) },
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                article.itsInFindedAskSupplierSA -> Color.Yellow.copy(alpha = alpha)
                                else -> Color.Red.copy(alpha = 0.3f)
                            }
                        )
                    ) {
                        AutoResizedText(
                            text = article.nameArticle.capitalize(Locale.current),
                            fontSize = 25.sp,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }

                    // Client names
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        items(article.nameClientsNeedItGBC.split(")")) { clientName ->
                            val cleanedName = clientName.trim().replace("(", "").replace(")", "")
                            if (cleanedName.isNotBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    AutoResizedText(
                                        text = cleanedName.capitalize(Locale.current),
                                        fontSize = 20.sp,
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun AutoResizedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize
) {
    var currentFontSize by remember { mutableStateOf(fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.capitalize(Locale.current),
            color = color,
            fontSize = currentFontSize,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.drawWithContent { if (readyToDraw) drawContent() },
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.didOverflowHeight) {
                    currentFontSize *= 0.9f
                } else {
                    readyToDraw = true
                }
            }
        )
    }
}

@Composable
fun DisplayeImageSA(
    article: GroupeurBonCommendToSupplierTabele,
    viewModel: HeadOfViewModels,
    index: Int = 0,
    reloadKey: Any = Unit
) {
    val context = LocalContext.current
    val baseImagePath =
        "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_${index + 1}"


    val imageExist = remember(reloadKey) {
        listOf("jpg", "webp").firstNotNullOfOrNull { extension ->
            listOf(baseImagePath).firstOrNull { path ->
                File("$path.$extension").exists()
            }?.let { "$it.$extension" }
        }
    }

    val imageSource = imageExist ?: R.drawable.blanc

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageSource)
            .size(Size(1000, 1000))
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun SingleColorImageSA(
    article: GroupeurBonCommendToSupplierTabele,
    viewModel: HeadOfViewModels,
    reloadKey: Long
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imagePathWithoutExt = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_1"
            val imagePathWebp = "$imagePathWithoutExt.webp"
            val imagePathJpg = "$imagePathWithoutExt.jpg"
            val webpExists = File(imagePathWebp).exists()
            val jpgExists = File(imagePathJpg).exists()

            if (webpExists || jpgExists) {
                DisplayeImageSA(article=article,
                    viewModel=viewModel,
                    index=0,
                    reloadKey
                )            } else {
                // Display rotated article name for empty articles
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = article.nameArticle,
                        color = Color.Red,
                        modifier = Modifier
                            .rotate(45f)
                            .padding(4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!article.a_d_nomarticlefinale_c_1.contains("Sta", ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = article.a_d_nomarticlefinale_c_1,
                        color = Color.Red,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "${article.totalquantity}",
                    color = Color.Red,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MultiColorGridSA(article: GroupeurBonCommendToSupplierTabele, viewModel: HeadOfViewModels,
                     reloadKey: Any = Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        val colorData = listOf(
            article.color1SoldQuantity to article.a_d_nomarticlefinale_c_1,
            article.color2SoldQuantity to article.a_d_nomarticlefinale_c_2,
            article.color3SoldQuantity to article.a_d_nomarticlefinale_c_3,
            article.color4SoldQuantity to article.a_d_nomarticlefinale_c_4
        )

        items(colorData.size) { index ->
            val (quantity, colorName) = colorData[index]
            if (quantity > 0) {
                ColorItemCard(article, index, quantity, colorName, viewModel,reloadKey)
            }
        }
    }
}

@Composable
private fun ColorItemCard(
    article: GroupeurBonCommendToSupplierTabele,
    index: Int,
    quantity: Int,
    colorName: String?,
    viewModel: HeadOfViewModels,
    reloadKey: Any = Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxSize()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF40E0D0) // Bleu turquoise
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            val imagePathWithoutExt = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_${index + 1}"
            val imagePathWebp = "$imagePathWithoutExt.webp"
            val imagePathJpg = "$imagePathWithoutExt.jpg"
            val webpExists = File(imagePathWebp).exists()
            val jpgExists = File(imagePathJpg).exists()

            if (webpExists || jpgExists) {
                DisplayeImageSA(article=article,
                    viewModel=viewModel,
                    index=index,
                    reloadKey
                )
            } else {
                Text(
                    text = colorName ?: "",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(45f)
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
            }
            Text(
                text = quantity.toString(),
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )

        }
    }
}
@Composable
fun SuppliersFloatingButtonsSA(
    allArticles: List<GroupeurBonCommendToSupplierTabele>,
    suppliers: List<TabelleSuppliersSA>,
    onClickFlotButt: (Long) -> Unit,
    supplierFlotBisHandled: Long?,
    itsReorderMode: Boolean,
    firstClickedSupplierForReorder: Long?,
    onToggleReorderMode: () -> Unit,
    onUpdateVocalArabName: (Long, String) -> Unit,
    onUpdateVocalFrencheName: (Long, String) -> Unit,

    ) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isExpanded by remember { mutableStateOf(false) }
    var filterButtonsWhereArtNotEmpty by remember { mutableStateOf(false) }
    var showDescriptionFlotBS by remember { mutableStateOf(true) }
    var showNoms by remember { mutableStateOf(false) }
    var onToggleReorderModeCliked by remember { mutableStateOf(false) }

    val filteredSuppliers = remember(suppliers, allArticles, filterButtonsWhereArtNotEmpty) {
        if (filterButtonsWhereArtNotEmpty) {
            suppliers.filter { supplier ->
                allArticles.any { article ->
                    article.idSupplierTSA.toLong() == supplier.idSupplierSu && !article.itsInFindedAskSupplierSA
                }
            }
        } else {
            suppliers
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            supplierFlotBisHandled?.let { supplierId ->
                onUpdateVocalArabName(supplierId, spokenText)
            }
        }
    }
    val speechRecognizerLauncherFrenche = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            supplierFlotBisHandled?.let { supplierId ->
                onUpdateVocalFrencheName(supplierId, spokenText)
            }
        }
    }
    Box(
        modifier = Modifier
            .padding(8.dp)
            .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    dragOffset += Offset(dragAmount.x, dragAmount.y)
                }
            }
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .widthIn(max = 100.dp)
                    ) {
                        item {
                            FloatingActionButton(
                                onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour le nom vocal arabe du fournisseur...")
                                    }
                                    speechRecognizerLauncherFrenche.launch(intent)
                                }
                            ) {
                                Icon(
                                    Icons.Default.MicOff,
                                    contentDescription = "Update vocal Frenche name"
                                )
                            }
                        }
                        item {
                            FloatingActionButton(
                                onClick = {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour le nom vocal arabe du fournisseur...")
                                    }
                                    speechRecognizerLauncher.launch(intent)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Mic,
                                    contentDescription = "Update vocal Arab name"
                                )
                            }
                        }
                        item {
                            FloatingActionButton(
                                onClick = { filterButtonsWhereArtNotEmpty = !filterButtonsWhereArtNotEmpty }
                            ) {
                                Icon(
                                    if (filterButtonsWhereArtNotEmpty) Icons.Default.Close else Icons.Default.FilterAlt,
                                    contentDescription = if (filterButtonsWhereArtNotEmpty) "Clear filter" else "Filter suppliers"
                                )
                            }
                        }
                        item {
                            FloatingActionButton(
                                onClick = { showDescriptionFlotBS = !showDescriptionFlotBS }
                            ) {
                                Icon(
                                    if (showDescriptionFlotBS) Icons.Default.Close else Icons.Default.Dehaze,
                                    contentDescription = if (showDescriptionFlotBS) "Hide descriptions" else "Show descriptions"
                                )
                            }
                        }
                        item {
                            FloatingActionButton(
                                onClick = { showNoms = !showNoms }
                            ) {
                                Icon(
                                    if (showNoms) Icons.Default.Close else Icons.Default.Dehaze,
                                    contentDescription = if (showNoms) "Hide names" else "Show names"
                                )
                            }
                        }
                        item {
                            FloatingActionButton(
                                onClick = { onToggleReorderMode()
                                    onToggleReorderModeCliked = !onToggleReorderModeCliked
                                }
                            ) {
                                Icon(
                                    if (onToggleReorderModeCliked) Icons.Default.Close else Icons.Default.Autorenew,
                                    contentDescription =null
                                )
                            }
                        }

                    }
                }
                items(filteredSuppliers) { supplier ->
                    SupplierButton(
                        supplier = supplier,
                        showDescription = showDescriptionFlotBS,
                        showNoms = showNoms,
                        isSelected = supplierFlotBisHandled == supplier.idSupplierSu,
                        isFirstClickedForReorder = firstClickedSupplierForReorder == supplier.idSupplierSu,
                        isReorderMode = itsReorderMode,
                        onClick = { onClickFlotButt(supplier.idSupplierSu) },
                        allArticles = allArticles
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                contentDescription = if (isExpanded) "Collapse supplier list" else "Expand supplier list"
            )
        }
    }
}

@Composable
private fun SupplierButton(
    supplier: TabelleSuppliersSA,
    allArticles: List<GroupeurBonCommendToSupplierTabele>,
    showDescription: Boolean,
    isSelected: Boolean,
    isFirstClickedForReorder: Boolean,
    isReorderMode: Boolean,
    onClick: () -> Unit,
    showNoms: Boolean
) {
    val totalValue = remember(supplier, allArticles) {
        allArticles
            .filter { it.idSupplierTSA.toLong() == supplier.idSupplierSu }
            .sumOf { it.totalquantity * it.a_q_prixachat_c }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,      
        modifier = Modifier
            .padding(bottom = 16.dp)
            .widthIn(min = 50.dp, max = if (showNoms) 300.dp else 170.dp)
            .heightIn(max = if (showNoms) 100.dp else 40.dp)
    ) {
        if (showDescription) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "T: $${String.format("%.2f", totalValue)}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (showNoms) {
                        Text(
                            text = supplier.nomSupplierSu,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = supplier.nameInFrenche,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp),
            containerColor = when {
                isFirstClickedForReorder -> MaterialTheme.colorScheme.tertiary
                isReorderMode -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                isSelected -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.secondary
            }
        ) {
            Text(
                text ="${supplier.classmentSupplier} ${supplier.nameInFrenche.take(3)}",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SupplierHeaderSA(
    supplier: TabelleSuppliersSA,
    viewModel: HeadOfViewModels,
) {
    val backgroundColor = remember(supplier.couleurSu) {
        Color(android.graphics.Color.parseColor(supplier.couleurSu))
    }
    var isClicked by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isClicked) 1.1f else 1f, label = "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                isClicked = true
            }
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = supplier.nomSupplierSu,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
        }
    }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            kotlinx.coroutines.delay(200)
            isClicked = false
        }
    }
}

@Composable
fun OverlayContentSA(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White)
    }
}

@Composable
fun DisponibilityOverlaySA(state: String) {
    when (state) {
        "Ask WHer" -> OverlayContentSA(color = Color.Black, icon = Icons.Default.TextDecrease)
        "TO Find" -> OverlayContentSA(color = Color.Gray, icon = Icons.Default.Person)
    }
}


