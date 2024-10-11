package i_SupplierArticlesRecivedManager

// Assuming these are your custom classes/components
import a_MainAppCompnents.BaseDonneECBTabelle
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import a_MainAppCompnents.TabelleSuppliersSA
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import b2_Edite_Base_Donne_With_Creat_New_Articls.AutoResizedTextECB
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.abdelwahabjemlajetpack.R
import java.io.File
import kotlin.math.roundToInt

@Composable
fun Fragment_SupplierArticlesRecivedManager(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
    modifier: Modifier = Modifier,
    onNewArticleAdded: (BaseDonneECBTabelle) -> Unit
) {
    val allModels by viewModel.uiState.collectAsState()
    val currentSupplierArticle by viewModel.currentSupplierArticle.collectAsState()
    var dialogeDisplayeDetailleChanger by remember { mutableStateOf<TabelleSupplierArticlesRecived?>(null) }

    var showFloatingButtons by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(2) }

    val gridState = rememberLazyGridState()

    var toggleCtrlToFilterToMove by remember { mutableStateOf(false) }
    var idSupplierOfFloatingButtonClicked by remember { mutableStateOf<Long?>(null) }
    var itsReorderMode by remember { mutableStateOf(false) }
    var lastAskArticleChanged by remember { mutableStateOf<Long?>(null) }
    var windosMapArticleInSupplierStore by remember { mutableStateOf(false) }
    var firstClickedSupplierForReorder by remember { mutableStateOf<Long?>(null) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            processVoiceInput(spokenText, viewModel, allModels)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { padding ->

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val filterSupp = if (idSupplierOfFloatingButtonClicked != null) {
                    allModels.tabelleSuppliersSA.filter { it.idSupplierSu == idSupplierOfFloatingButtonClicked }
                } else {
                    allModels.tabelleSuppliersSA
                }

                filterSupp.forEach { supplier ->
                    val articlesSupplier = allModels.tabelleSupplierArticlesRecived.filter {
                        it.idSupplierTSA.toLong() == supplier.idSupplierSu &&
                                (!toggleCtrlToFilterToMove || it.itsInFindedAskSupplierSA)
                    }

                    if (articlesSupplier.isNotEmpty() && supplier.nomSupplierSu != "Find" && supplier.nomSupplierSu != "Non Define") {
                        item(span = { GridItemSpan(gridColumns) }) {
                            SupplierHeaderSA(
                                supplier = supplier,
                                viewModel = viewModel,
                            )
                        }
                        items(articlesSupplier) { article ->
                            ArticleItemSA(
                                article = article,
                                viewModel = viewModel,
                                onArticleClick = { clickedArticle ->
                                    val vidClicked = clickedArticle.aa_vid.toLong()

                                    if (lastAskArticleChanged != vidClicked) {
                                        viewModel.changeAskSupplier(clickedArticle)
                                        lastAskArticleChanged = vidClicked
                                    } else {
                                        dialogeDisplayeDetailleChanger = clickedArticle
                                        lastAskArticleChanged = null
                                    }
                                },
                                modifier=modifier,
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .zIndex(1f)
            ) {
                GlobaleControlsFloatingButtonsSA(
                    showFloatingButtons = showFloatingButtons,
                    onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                    onChangeGridColumns = { gridColumns = it },
                    onToggleToFilterToMove = { toggleCtrlToFilterToMove = !toggleCtrlToFilterToMove },
                    filterSuppHandledNow = toggleCtrlToFilterToMove,
                    onDisplyeWindosMapArticleInSupplierStore = { windosMapArticleInSupplierStore = !windosMapArticleInSupplierStore },
                    onLaunchVoiceRecognition = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                            putExtra(
                                RecognizerIntent.EXTRA_PROMPT,
                                "Parlez maintenant pour mettre à jour cet article..."
                            )
                        }
                        speechRecognizerLauncher.launch(intent)
                    }  ,
                    viewModel=viewModel  ,
                    uiState=allModels ,
                    onNewArticleAdded=onNewArticleAdded
                )
            }
        }

        SuppliersFloatingButtonsSA(
            allArticles = allModels.tabelleSupplierArticlesRecived,
            suppliers = allModels.tabelleSuppliersSA,
            supplierFlotBisHandled = idSupplierOfFloatingButtonClicked,
            onClickFlotButt = { clickedSupplierId ->
                if (itsReorderMode) {
                    if (firstClickedSupplierForReorder == null) {
                        firstClickedSupplierForReorder = clickedSupplierId
                    } else if (firstClickedSupplierForReorder != clickedSupplierId) {
                        viewModel.reorderSuppliers(firstClickedSupplierForReorder!!, clickedSupplierId)
                        firstClickedSupplierForReorder = null
                    } else {
                        // Cliquer deux fois sur le même fournisseur annule la réorganisation
                        firstClickedSupplierForReorder = null
                    }
                } else {
                    if (toggleCtrlToFilterToMove) {
                        val filterBytabelleSupplierArticlesRecived =
                            allModels.tabelleSupplierArticlesRecived.filter {
                                it.itsInFindedAskSupplierSA
                            }
                        viewModel.moveArticleNonFindToSupplier(
                            articlesToMove = filterBytabelleSupplierArticlesRecived,
                            toSupp = clickedSupplierId
                        )
                        toggleCtrlToFilterToMove = false
                    } else {
                        idSupplierOfFloatingButtonClicked =
                            when (idSupplierOfFloatingButtonClicked) {
                                clickedSupplierId -> null  // Deselect if the same supplier is clicked again
                                else -> clickedSupplierId  // Select the new supplier
                            }
                    }
                }
            },
            itsReorderMode = itsReorderMode,
            firstClickedSupplierForReorder = firstClickedSupplierForReorder ,
            onUpdateVocalArabName = { supplierId, newName ->
                viewModel.updateSupplierVocalArabName(supplierId, newName)
            },
            onToggleReorderMode = {
                itsReorderMode = !itsReorderMode
                if (!itsReorderMode) {
                    firstClickedSupplierForReorder = null
                }
            },
        )
    }

    // Use the current edited article if it matches the given article, otherwise use the original article
    val displayedArticle = currentSupplierArticle?.takeIf { it.a_c_idarticle_c.toLong() == dialogeDisplayeDetailleChanger?.a_c_idarticle_c }
        ?: dialogeDisplayeDetailleChanger

    displayedArticle?.let { article ->
        WindowArticleDetail(
            article = article,
            onDismissWithUpdate = {
                dialogeDisplayeDetailleChanger = null
            },
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
    }

    if (windosMapArticleInSupplierStore)
        WindowsMapArticleInSupplierStore(
            uiState=  allModels,
        onDismiss = {
            windosMapArticleInSupplierStore = false
        },
        viewModel = viewModel,
        modifier = Modifier.padding(horizontal = 3.dp),
        idSupplierOfFloatingButtonClicked=idSupplierOfFloatingButtonClicked,
        )
}

private fun processVoiceInput(spokenText: String, viewModel: HeadOfViewModels, uiState: CreatAndEditeInBaseDonnRepositeryModels) {
    val parts = spokenText.split("+")
    if (parts.size == 2) {
        val articleId = parts[0].trim().toLongOrNull()
        val supplierName = parts[1].trim()

        if (articleId != null) {
            val article = uiState.tabelleSupplierArticlesRecived.find { it.aa_vid == articleId}
            val supplier = uiState.tabelleSuppliersSA.find { it.nomVocaleArabeDuSupplier == supplierName }

            if (article != null && supplier != null) {
                viewModel.moveArticleNonFindToSupplier(
                    articlesToMove = listOf(article),
                    toSupp = supplier.idSupplierSu
                )
            }
        }
    }
}

@Composable
fun ArticleItemSA(
    article: TabelleSupplierArticlesRecived,
    viewModel: HeadOfViewModels,
    onArticleClick: (TabelleSupplierArticlesRecived) -> Unit,
    modifier: Modifier
) {
    val cardColor = if (article.itsInFindedAskSupplierSA) {
        Color.Yellow.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val reloadKey = remember(article) { System.currentTimeMillis() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(8.dp)) {
                SquareLayout(
                    modifier = modifier
                        .fillMaxWidth()
                        .clickable { onArticleClick(article) }
                ) {
                    if (article.quantityachete_c_2 + article.quantityachete_c_3 + article.quantityachete_c_4 == 0) {
                        SingleColorImageSA(article, viewModel, reloadKey)
                    } else {
                        MultiColorGridSA(article, viewModel, reloadKey)
                    }
                }
                DisponibilityOverlaySA(article.itsInFindedAskSupplierSA.toString())
                AutoResizedTextSA(text = article.a_d_nomarticlefinale_c)
            }

            // aa_vid display at top-end
            Text(
                text = "ID: ${article.aa_vid}",
                color = Color.Blue,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(4.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SquareLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val size = minOf(constraints.maxWidth, constraints.maxHeight)
        val placeables = measurables.map { measurable ->
            measurable.measure(Constraints.fixed(size, size))
        }
        layout(size, size) {
            placeables.forEach { placeable ->
                placeable.place(0, 0)
            }
        }
    }
}

//WindowArticleDetail
@Composable
fun WindowArticleDetail(
    article: TabelleSupplierArticlesRecived,
    onDismissWithUpdate: () -> Unit,
    viewModel: HeadOfViewModels,
    modifier: Modifier
) {
    val reloadKey = remember(article) { System.currentTimeMillis() }

    Dialog(
        onDismissRequest = onDismissWithUpdate,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = modifier.fillMaxWidth()) {
                    val ifStat =
                        article.quantityachete_c_2 + article.quantityachete_c_3 + article.quantityachete_c_4 == 0
                    Box(
                        modifier = modifier
                            .clickable { onDismissWithUpdate() }
                            .height(if (ifStat) 250.dp else 500.dp)
                        ){

                        if (ifStat) {
                            SingleColorImageSA(article, viewModel,reloadKey)
                        } else {
                            MultiColorGridSA(article, viewModel,reloadKey)
                        }
                    }
                    // Article name
                    AutoResizedTextECB(
                        text = article.a_d_nomarticlefinale_c.capitalize(Locale.current),
                        fontSize = 25.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = modifier.fillMaxWidth()
                    )
                    AutoResizedTextECB(
                        text = article.nomclient.capitalize(Locale.current),
                        fontSize = 25.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            onDismissWithUpdate()
                        },
                        modifier = Modifier
                    ) {
                        Text("Close And Update Place")
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayeImageSA(
    article: TabelleSupplierArticlesRecived,
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
    article: TabelleSupplierArticlesRecived,
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
                        text = article.a_d_nomarticlefinale_c,
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
fun MultiColorGridSA(article: TabelleSupplierArticlesRecived, viewModel: HeadOfViewModels,
                     reloadKey: Any = Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        val colorData = listOf(
            article.quantityachete_c_1 to article.a_d_nomarticlefinale_c_1,
            article.quantityachete_c_2 to article.a_d_nomarticlefinale_c_2,
            article.quantityachete_c_3 to article.a_d_nomarticlefinale_c_3,
            article.quantityachete_c_4 to article.a_d_nomarticlefinale_c_4
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
    article: TabelleSupplierArticlesRecived,
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
    allArticles: List<TabelleSupplierArticlesRecived>,
    suppliers: List<TabelleSuppliersSA>,
    onClickFlotButt: (Long) -> Unit,
    supplierFlotBisHandled: Long?,
    itsReorderMode: Boolean,
    firstClickedSupplierForReorder: Long?,
    onToggleReorderMode: () -> Unit,
    onUpdateVocalArabName: (Long, String) -> Unit
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isExpanded by remember { mutableStateOf(true) }
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
                        modifier = Modifier.padding(bottom = 8.dp)   .widthIn(max=100.dp)
                    ) {
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
    allArticles: List<TabelleSupplierArticlesRecived>,
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
        modifier = Modifier.padding(bottom = 16.dp)
            .widthIn(min = 50.dp, max = if (showNoms) 300.dp else 170.dp)
            .heightIn( max = if (showNoms) 100.dp else 40.dp)
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
                            text = supplier.nomVocaleArabeDuSupplier,
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
                text = supplier.nomVocaleArabeDuSupplier.take(3),
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


