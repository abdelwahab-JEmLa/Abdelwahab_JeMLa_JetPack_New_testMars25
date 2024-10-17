package i_SupplierArticlesRecivedManager

import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.MapArticleInSupplierStore
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.abdelwahabjemlajetpack.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FragmentMapArticleInSupplierStore(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    navController: NavController,
    modifier: Modifier = Modifier,
    idSupplierOfFloatingButtonClicked: Long?,
    onIdSupplierChanged: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showNonPlacedArticles by remember { mutableStateOf<MapArticleInSupplierStore?>(null) }
    var fabOffset by remember { mutableStateOf(Offset.Zero) }
    var showFab by remember { mutableStateOf(false) }
    var itsFilterInFindedAskSupplierSA by remember { mutableStateOf(false) }

    Scaffold(
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column {
                val supplier = uiState.tabelleSuppliersSA.find { it.idSupplierSu == idSupplierOfFloatingButtonClicked }
                supplier?.let {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "Supplier: ${it.nomSupplierSu}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showFab = !showFab }
                ) {
                    val places = uiState.mapArticleInSupplierStore.filter { it.idSupplierOfStore == idSupplierOfFloatingButtonClicked }

                    places.forEach { placeItem ->
                        stickyHeader {
                            PlaceHeader(placeItem)
                        }

                        val articlesForThisPlace = uiState.tabelleSupplierArticlesRecived.filter { article ->
                            uiState.placesOfArticelsInEacheSupplierSrore.any { place ->
                                place.idCombinedIdArticleIdSupplier == "${article.a_c_idarticle_c}_${article.idSupplierTSA}" &&
                                        place.idPlace == placeItem.idPlace
                            }
                        }

                        items(articlesForThisPlace) { article ->
                            ArticleItemOfPlace(
                                article = article,
                                viewModel = viewModel,
                                onDismissWithUpdate = { /* Handle update */ },
                                onDismiss = { /* Handle dismiss */ }
                            )
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
            if (showFab) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    MoveArticlesFAB(
                        uiState = uiState,
                        viewModel = viewModel,
                        idSupplierOfFloatingButtonClicked = idSupplierOfFloatingButtonClicked,
                        onIdSupplierChanged = onIdSupplierChanged
                    )

                    FilterFAB(
                        itsFilterInFindedAskSupplierSA = itsFilterInFindedAskSupplierSA,
                        onFilterChanged = { itsFilterInFindedAskSupplierSA = it }
                    )

                    AddPlaceFAB(
                        fabOffset = fabOffset,
                        onFabOffsetChanged = { fabOffset = it },
                        onAddDialogRequested = { showAddDialog = true }
                    )
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
fun PlaceHeader(placeItem: MapArticleInSupplierStore) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        )
    ) {
        Text(
            text = "${placeItem.namePlace} ${if (placeItem.inRightOfPlace) "R" else "L"}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun ArticleItemOfPlace(
    article: TabelleSupplierArticlesRecived,
    viewModel: HeadOfViewModels,
    onDismissWithUpdate: (TabelleSupplierArticlesRecived) -> Unit ,
    onDismiss: () -> Unit
) {
    var showArticleDetails by remember { mutableStateOf<TabelleSupplierArticlesRecived?>(null) }
    val reloadKey = remember(article) { System.currentTimeMillis() }

    CardArticlePlace(
        article = article,
        onClickToShowWindowsInfoArt = { showArticleDetails = it } ,
        reloadKey=reloadKey
    )

    showArticleDetails?.let { articleDisplate ->
        WindowArticleDetail(
            article = articleDisplate,
            onDismissWithUpdatePlaceArticle = {
                showArticleDetails = null
                onDismissWithUpdate(article)
            },
            onDismiss = { onDismiss()
                showArticleDetails = null
            },
            onDismissWithUpdateOfnonDispo ={
                showArticleDetails = null
                viewModel.changeAskSupplier(it)
            },
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
    }
}


@Composable
private fun CardArticlePlace(
    article: TabelleSupplierArticlesRecived,
    onClickToShowWindowsInfoArt: (TabelleSupplierArticlesRecived) -> Unit,
    reloadKey: Long
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,  // Start from a more visible state
        targetValue = 0.7f,   // Go to a more opaque state
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clickable(onClick = { onClickToShowWindowsInfoArt(article) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background image
            DisplayeImageById(
                idArticle = article.a_c_idarticle_c,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                reloadKey = reloadKey
            )

            // Semi-transparent overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            // Darker blinking yellow overlay for itsInFindedAskSupplierSA
            if (article.itsInFindedAskSupplierSA) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(alpha)
                        .background(Color(0xFFFFD700).copy(alpha = 0.7f))  // Darker yellow
                )
            }

            // Article details
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = article.a_d_nomarticlefinale_c,
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
                    Text(
                        text = "Qty: ${article.totalquantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    if (article.itsInFindedAskSupplierSA) {
                        Text(
                            text = "Ask",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFD700)  // Matching yellow color
                        )
                    }
                }
            }
        }
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
    var showNonPlacedArticles by remember { mutableStateOf(true) }

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

                    val articlesSupplier = if (searchText.isEmpty()) {
                        uiState.tabelleSupplierArticlesRecived.filter { article ->
                            article.idSupplierTSA.toLong() == place.idSupplierOfStore
                        }
                    } else {
                        uiState.tabelleSupplierArticlesRecived.filter { article ->
                            article.a_d_nomarticlefinale_c.contains(searchText, ignoreCase = true)
                        }
                    }

                    val (nonPlacedArticles, placedArticles) = articlesSupplier.partition { article ->
                        !uiState.placesOfArticelsInEacheSupplierSrore.any { placedArticle ->
                            placedArticle.idCombinedIdArticleIdSupplier == "${article.a_c_idarticle_c}_${article.idSupplierTSA}"
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(16.dp)
                            .clickable { showNonPlacedArticles = !showNonPlacedArticles },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column {
                            Text(
                                if (showNonPlacedArticles) "Articles With Non Defined Place" else "Placed Articles",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridColumns),
                                state = gridState,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(if (showNonPlacedArticles) nonPlacedArticles else placedArticles) { article ->
                                    ArticleItemOfPlace(
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
                                                listOf(clickedArticle),
                                                place.idSupplierOfStore
                                            )
                                            onDismiss()
                                        },
                                        viewModel = viewModel,
                                        onDismiss = onDismiss
                                    )
                                }
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
private fun MoveArticlesFAB(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    idSupplierOfFloatingButtonClicked: Long?,
    onIdSupplierChanged: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    var isActionCompleted by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val buttonColor by animateColorAsState(
        targetValue = when {
            isActionCompleted -> Color.Yellow
            isPressed -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.secondary
        },
        label = "buttonColor"
    )

    val currentSupplier = uiState.tabelleSuppliersSA.find { it.idSupplierSu == idSupplierOfFloatingButtonClicked }
    val currentClassment = currentSupplier?.classmentSupplier
    val nextClassment = currentClassment?.minus(1.0)
    val nextSupplier = uiState.tabelleSuppliersSA.find { it.classmentSupplier == nextClassment }
    Box(
        modifier = Modifier
            .padding(end = 16.dp)
            .size(56.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        isActionCompleted = false
                        progress = 0f
                        val pressStartTime = System.currentTimeMillis()

                        scope.launch {
                            try {
                                tryAwaitRelease()
                            } finally {
                                isPressed = false
                                val pressDuration = System.currentTimeMillis() - pressStartTime
                                if (pressDuration >= 1000) {
                                    performAction(
                                        uiState,
                                        viewModel,
                                        idSupplierOfFloatingButtonClicked,
                                        onIdSupplierChanged
                                    )
                                    isActionCompleted = true
                                } else {
                                    progress = 0f
                                }
                            }
                        }

                        // Progress animation
                        scope.launch {
                            repeat(100) {
                                delay(10)
                                if (isPressed) {
                                    progress = (it + 1) / 100f
                                } else {
                                    return@launch
                                }
                            }
                        }
                    }
                )
            }
    ) {
        // Button background
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = buttonColor
        ) {}

        // Progress indicator
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            strokeWidth = 4.dp,
            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
        )

        // Button text
        Text(
            nextSupplier?.nomVocaleArabeDuSupplier?.take(3) ?: "???",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private  fun performAction(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    idSupplierOfFloatingButtonClicked: Long?,
    onIdSupplierChanged: (Long) -> Unit
) {
    val filterBytabelleSupplierArticlesRecived =
        uiState.tabelleSupplierArticlesRecived.filter {
            it.itsInFindedAskSupplierSA
        }

    val currentSupplier = uiState.tabelleSuppliersSA.find { it.idSupplierSu == idSupplierOfFloatingButtonClicked }
    val currentClassment = currentSupplier?.classmentSupplier

    if (currentClassment != null) {
        val nextClassment = currentClassment - 1.0
        val nextSupplier = uiState.tabelleSuppliersSA.find { it.classmentSupplier == nextClassment }

        if (nextSupplier != null) {
            viewModel.moveArticleNonFindToSupplier(
                articlesToMove = filterBytabelleSupplierArticlesRecived,
                toSupp = nextSupplier.idSupplierSu
            )
            onIdSupplierChanged(nextSupplier.idSupplierSu)
        }
    }
}

@Composable
private fun FilterFAB(
    itsFilterInFindedAskSupplierSA: Boolean,
    onFilterChanged: (Boolean) -> Unit
) {
    FloatingActionButton(
        onClick = { onFilterChanged(!itsFilterInFindedAskSupplierSA) },
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Icon(
            if (itsFilterInFindedAskSupplierSA) Icons.Filled.FilterList else Icons.Filled.FilterListOff,
            contentDescription = "Toggle Filter"
        )
    }
}

@Composable
private fun AddPlaceFAB(
    fabOffset: Offset,
    onFabOffsetChanged: (Offset) -> Unit,
    onAddDialogRequested: () -> Unit
) {
    FloatingActionButton(
        onClick = onAddDialogRequested,
        modifier = Modifier
            .offset { IntOffset(fabOffset.x.toInt(), fabOffset.y.toInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onFabOffsetChanged(fabOffset + dragAmount)
                }
            }
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Add Place")
    }
}
@Composable
fun DisplayeImageById(
    idArticle: Long,
    index: Int = 0,
    reloadKey: Any = Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val baseImagePath =
        "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${idArticle}_${index + 1}"

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
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center
    )
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
                    onValueChange = { input ->
                        newPlaceName = input.replaceFirstChar { it.uppercase() }
                    },
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

