package i_SupplierArticlesRecivedManager

import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.MapArticleInSupplierStore
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import a_MainAppCompnents.TabelleSuppliersSA
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentSupplierArticle by viewModel.currentSupplierArticle.collectAsState()
    var dialogeDisplayeDetailleChanger by remember { mutableStateOf<TabelleSupplierArticlesRecived?>(null) }

    var showFloatingButtons by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(2) }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    var toggleCtrlToFilterToMove by remember { mutableStateOf(false) }
    var idSupplierOfFloatingButtonClicked by remember { mutableStateOf<Long?>(null) }
    var itsReorderMode by remember { mutableStateOf(false) }
    var holdedIdSupplierForMove by remember { mutableStateOf<Long?>(null) }
    var lastAskArticleChanged by remember { mutableStateOf<Long?>(null) }
    var windosMapArticleInSupplierStore by remember { mutableStateOf(false) }
    var firstClickedSupplierForReorder by remember { mutableStateOf<Long?>(null) }

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
                    uiState.tabelleSuppliersSA.filter { it.idSupplierSu == idSupplierOfFloatingButtonClicked }
                } else {
                    uiState.tabelleSuppliersSA
                }

                filterSupp.forEach { supplier ->
                    val articlesSupplier = uiState.tabelleSupplierArticlesRecived.filter {
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
            GlobaleControlsFloatingsButtonsSA(
                showFloatingButtons = showFloatingButtons,
                onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                onChangeGridColumns = { gridColumns = it },
                onToggleToFilterToMove = { toggleCtrlToFilterToMove = !toggleCtrlToFilterToMove },
                filterSuppHandledNow = toggleCtrlToFilterToMove,
                onToggleReorderMode = {
                    itsReorderMode = !itsReorderMode
                    if (!itsReorderMode) {
                        firstClickedSupplierForReorder = null
                    }
                },
                onDisplyeWindosMapArticleInSupplierStore = { windosMapArticleInSupplierStore = !windosMapArticleInSupplierStore }
            )
        }

        SuppliersFloatingButtonsSA(
            allArticles = uiState.tabelleSupplierArticlesRecived,
            suppliers = uiState.tabelleSuppliersSA,
            supplierFlotBisHandled = idSupplierOfFloatingButtonClicked,
            onClickFlotButt = { clickedSupplierId ->
                if (itsReorderMode) {
                    if (firstClickedSupplierForReorder == null) {
                        firstClickedSupplierForReorder = clickedSupplierId
                    } else if (firstClickedSupplierForReorder != clickedSupplierId) {
                        viewModel.reorderSuppliers(firstClickedSupplierForReorder!!, clickedSupplierId)
                        firstClickedSupplierForReorder = null
                        itsReorderMode = false
                    } else {
                        // Cliquer deux fois sur le même fournisseur annule la réorganisation
                        firstClickedSupplierForReorder = null
                    }
                } else {
                    // ... (le code pour le mode non-réorganisation reste inchangé)
                }
            },
            itsReorderMode = itsReorderMode,
            firstClickedSupplierForReorder = firstClickedSupplierForReorder
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
            uiState=  uiState,
        onDismiss = {
            windosMapArticleInSupplierStore = false
        },
        viewModel = viewModel,
        modifier = Modifier.padding(horizontal = 3.dp),
        idSupplierOfFloatingButtonClicked=idSupplierOfFloatingButtonClicked,
        )
}
//Title:WindowsMapArticleInSupplierStore
@Composable
fun WindowsMapArticleInSupplierStore(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    idSupplierOfFloatingButtonClicked: Long?,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showNonPlacedArticles by remember { mutableStateOf<MapArticleInSupplierStore?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val Places = uiState.mapArticleInSupplierStore .filter {
                            it.idSupplierOfStore ==   idSupplierOfFloatingButtonClicked
                        }

                            items(Places) { placeItem ->
                                PlacesItem(
                                    uiState = uiState,
                                    placeItem=placeItem,
                                    modifier = Modifier.fillMaxWidth(),
                                    viewModel = viewModel,
                                    onDismiss = { showNonPlacedArticles = null }   ,
                                    onClickToDisplayNonPlaced = {showNonPlacedArticles=it}
                                )
                            }
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Place")
                }
                FloatingActionButton(
                    onClick = { showNonPlacedArticles = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.List, contentDescription = "Show Non-Placed Articles")
                }
            }
        }
    }
    showNonPlacedArticles?.let { place ->
        WindowsOfNonPlacedArticles(
            uiState = uiState,
            onDismiss = { showNonPlacedArticles = null },
            modifier = Modifier,
            gridColumns = 2,
            place = place,
            viewModel = viewModel
        )
    }
    if (showAddDialog) {
        AddPlaceDialog(
            onDismiss = { showAddDialog = false },
            onAddPlace = { name ->
                viewModel.addNewPlace(name, idSupplierOfFloatingButtonClicked)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PlacesItem(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    modifier: Modifier = Modifier,
    viewModel: HeadOfViewModels,
    onDismiss: () -> Unit,
    placeItem: MapArticleInSupplierStore,
    onClickToDisplayNonPlaced: (MapArticleInSupplierStore) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClickToDisplayNonPlaced(placeItem) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Card {
            Row {
                Text(
                    text = placeItem.namePlace,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (placeItem.inRightOfPlace) "R" else "L",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            val articlesForThisPlace = articleFilter(uiState, placeItem)

            items(articlesForThisPlace) { article ->
                ArticleItemOfPlace(
                    article = article,
                    viewModel = viewModel,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

private fun articleFilter(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    placeItem: MapArticleInSupplierStore
) = uiState.tabelleSupplierArticlesRecived.filter { article ->
    uiState.placesOfArticelsInEacheSupplierSrore.any { place ->
        place.idCombinedIdArticleIdSupplier == "${article.a_c_idarticle_c}_${article.idSupplierTSA}" &&
                place.idPlace == placeItem.idPlace
    }
}


@Composable
fun ArticleItemOfPlace(
    article: TabelleSupplierArticlesRecived,
    viewModel: HeadOfViewModels,
    onDismiss: () -> Unit
) {
    var showArticleDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { showArticleDetails = true },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = article.a_d_nomarticlefinale_c, style = MaterialTheme.typography.bodyLarge)
            Text(text = "ID: ${article.a_c_idarticle_c}", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (showArticleDetails) {
        WindowArticleDetail(
            article = article,
            onDismissWithUpdate = {showArticleDetails=false
                onDismiss()},
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
    }
}

@Composable
fun WindowsOfNonPlacedArticles(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    onDismiss: () -> Unit,
    modifier: Modifier,
    gridColumns: Int,
    viewModel: HeadOfViewModels,
    place: MapArticleInSupplierStore,
) {
    val gridState = rememberLazyGridState()
    var searchText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search Articles") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            state = gridState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val articlesSupplier = if (searchText.isEmpty()) {
                                uiState.tabelleSupplierArticlesRecived.filter { article ->
                                    article.idSupplierTSA.toLong() == place.idSupplierOfStore &&
                                            !uiState.placesOfArticelsInEacheSupplierSrore.any { placedArticle ->
                                                placedArticle.idCombinedIdArticleIdSupplier == "${article.a_c_idarticle_c}_${article.idSupplierTSA}"
                                            }
                                }
                            } else {
                                uiState.tabelleSupplierArticlesRecived.filter { article ->
                                    article.a_d_nomarticlefinale_c.contains(searchText, ignoreCase = true)
                                }
                            }

                            items(articlesSupplier) { article ->
                                ArticleItem(
                                    article = article,
                                    onDismissWithUpdate = { clickedArticle ->
                                        val idCombinedIdArticleIdSupplier = "${clickedArticle.a_c_idarticle_c}_${place.idSupplierOfStore}"
                                        viewModel.addOrUpdatePlacesOfArticelsInEacheSupplierSrore(
                                            idCombinedIdArticleIdSupplier = idCombinedIdArticleIdSupplier,
                                            placeId = place.idPlace,
                                            idArticle = clickedArticle.a_c_idarticle_c,
                                            idSupp = place.idSupplierOfStore
                                        )
                                        viewModel.moveArticleNonFindToSupplier(
                                            listOf(clickedArticle),  // Wrap the single article in a list
                                            place.idSupplierOfStore
                                        )
                                        onDismiss()
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ArticleItem(
    article: TabelleSupplierArticlesRecived,
    onDismissWithUpdate: (TabelleSupplierArticlesRecived) -> Unit,
    viewModel: HeadOfViewModels,
    ) {
    var showNonPlacedAricles by remember { mutableStateOf<TabelleSupplierArticlesRecived?>(null)  }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { showNonPlacedAricles = article },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = article.a_d_nomarticlefinale_c, style = MaterialTheme.typography.bodyLarge)
            Text(text = "ID: ${article.a_c_idarticle_c}", style = MaterialTheme.typography.bodySmall)
        }
    }
    showNonPlacedAricles?.let { articleDisplaye ->
        WindowArticleDetail(
            article = articleDisplaye,
            onDismissWithUpdate = {
                onDismissWithUpdate(articleDisplaye)
                showNonPlacedAricles=null
            },
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
    }
}


@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onAddPlace: (String) -> Unit
) {
    var newPlaceName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Place") },
        text = {
            Column {
                OutlinedTextField(
                    value = newPlaceName,
                    onValueChange = { newPlaceName = it },
                    label = { Text("Place Name") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAddPlace(newPlaceName) }) {
                Text("Add")
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
            Column(modifier = Modifier.padding(8.dp)) {
                // Image content
                Box(
                    modifier = modifier
                        .height(250.dp)    //TODO que le height = widhth et calcule car quand je change le grid                      columns = GridCells.Fixed(gridColumns),
                        //le widhth change et notmalement ca chhange
                        .clickable { onArticleClick(article) },

                    ){
                    if (article.quantityachete_c_2 + article.quantityachete_c_3 + article.quantityachete_c_4 == 0) {
                        SingleColorImageSA(article, viewModel,reloadKey)
                    } else {
                        MultiColorGridSA(article, viewModel,reloadKey)
                    }
                }
                DisponibilityOverlaySA(article.itsInFindedAskSupplierSA.toString())
                AutoResizedTextSA(text = article.a_d_nomarticlefinale_c)
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
    viewModel: HeadOfViewModels
    ,
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
    firstClickedSupplierForReorder: Long?
) {
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isExpanded by remember { mutableStateOf(false) }
    var filterButtonsWhereArtNotEmpty by remember { mutableStateOf(false) }
    var showDescriptionFlotBS by remember { mutableStateOf(false) }

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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { filterButtonsWhereArtNotEmpty = !filterButtonsWhereArtNotEmpty }
                        ) {
                            Icon(
                                if (filterButtonsWhereArtNotEmpty) Icons.Default.Close else Icons.Default.FilterAlt,
                                contentDescription = if (filterButtonsWhereArtNotEmpty) "Clear filter" else "Filter suppliers"
                            )
                        }
                        FloatingActionButton(
                            onClick = { showDescriptionFlotBS = !showDescriptionFlotBS }
                        ) {
                            Icon(
                                if (showDescriptionFlotBS) Icons.Default.Close else Icons.Default.Dehaze,
                                contentDescription = if (showDescriptionFlotBS) "Hide descriptions" else "Show descriptions"
                            )
                        }
                    }
                }
                items(filteredSuppliers) { supplier ->
                    SupplierButton(
                        supplier = supplier,
                        showDescription = showDescriptionFlotBS,
                        isSelected = supplierFlotBisHandled == supplier.idSupplierSu,
                        isFirstClickedForReorder = firstClickedSupplierForReorder == supplier.idSupplierSu,
                        isReorderMode = itsReorderMode,
                        onClick = { onClickFlotButt(supplier.idSupplierSu) }
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
    showDescription: Boolean,
    isSelected: Boolean,
    isFirstClickedForReorder: Boolean,
    isReorderMode: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        if (showDescription) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .heightIn(min = 30.dp)
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        text = supplier.nomSupplierSu,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                text = supplier.nomSupplierSu.take(3),
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


