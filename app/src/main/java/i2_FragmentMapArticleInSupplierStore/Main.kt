package i2_FragmentMapArticleInSupplierStore

import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.MapArticleInSupplierStore
import a_MainAppCompnents.PlacesOfArticelsInCamionette
import a_MainAppCompnents.GroupeurBonCommendToSupplierTabele
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NotListedLocation
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import i_SupplierArticlesRecivedManager.WindowArticleDetail

@Composable
fun FragmentMapArticleInSupplierStore(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    idSupplierOfFloatingButtonClicked: Long?,
    onIdSupplierChanged: (Long) -> Unit
) {
    var showNonPlacedArticles by remember { mutableStateOf<MapArticleInSupplierStore?>(null) }
    var showFab by remember { mutableStateOf(true) }
    val (articlesFilterByIdSupp, onFilterDispoActivate) = remember(
        uiState.groupeurBonCommendToSupplierTabele,
        idSupplierOfFloatingButtonClicked,
        uiState.showOnlyWithFilter
    ) {
        val filteredArticles = uiState.groupeurBonCommendToSupplierTabele
            .filter { article ->
                article.idSupplierTSA.toLong() == idSupplierOfFloatingButtonClicked &&
                        (!uiState.showOnlyWithFilter || article.itsInFindedAskSupplierSA)
            }
        Pair(filteredArticles) {
            viewModel.toggleFilter()
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            Column {
                    CardProgressBarWithDisplaySuppAndNext(
                        uiState = uiState,
                        idSupplier = idSupplierOfFloatingButtonClicked,
                        viewModel = viewModel,
                        onIdSupplierChanged = onIdSupplierChanged,
                        modifier = modifier
                    )

                ArticlesList(articlesFilterByIdSupp, uiState, viewModel, modifier) { showFab = !showFab }
            }
            if (showFab) {
                FabGroup(
                    uiState = uiState,
                    viewModel = viewModel,
                    idSupplierOfFloatingButtonClicked = idSupplierOfFloatingButtonClicked,
                    onFilterDispoActivate = onFilterDispoActivate
                )
            }
        }
    }

    showNonPlacedArticles?.let { place ->
        WindowsOfNonPlacedArticles(
            uiState = uiState,
            onDismiss = { showNonPlacedArticles = null },
            modifier = modifier,
            gridColumns = 2,
            place = place,
            viewModel = viewModel
        )
    }
}


@Composable
fun CardProgressBarWithDisplaySuppAndNext(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    idSupplier: Long?,
    viewModel: HeadOfViewModels,
    onIdSupplierChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAnimating by remember { mutableStateOf(false) }

    val supplier = uiState.tabelleSuppliersSA.find { it.idSupplierSu == idSupplier }
    val nextSupplier = uiState.tabelleSuppliersSA.find { it.classmentSupplier == supplier?.classmentSupplier?.minus(1.0) }

    val animationDuration = 5000 //  seconds

    val progress by animateFloatAsState(
        targetValue = if (isAnimating) 0f else 1f,
        animationSpec = tween(animationDuration, easing = LinearEasing),
        finishedListener = {
            if (it == 0f) {
                moveNonFindefArticles(uiState, viewModel, idSupplier, onIdSupplierChanged)
                isAnimating = false
            }
        },
        label = "Progress Animation"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isAnimating) Color.Red else MaterialTheme.colorScheme.primaryContainer,
        animationSpec = tween(animationDuration),
        label = "Background Color Animation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(6.dp)
            .height(40.dp)
            .clickable { isAnimating = !isAnimating },
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Text
            if (supplier != null) {
                AnimatedContent(
                    targetState = if (isAnimating) nextSupplier?.nomSupplierSu ?: "???" else supplier.nomSupplierSu,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn(animationSpec = tween(animationDuration / 2)))
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut(animationSpec = tween(animationDuration / 2)))
                    },
                    label = "Supplier Name Animation"
                ) { targetText ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = targetText,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun PlaceHeader(placeItem: PlacesOfArticelsInCamionette, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        )
    ) {
        Text(
            text = placeItem.namePlace,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticlesList(
    groupeurBonCommendToSupplierTabele: List<GroupeurBonCommendToSupplierTabele>,
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    modifier: Modifier,
    onClickToggleFab: () -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClickToggleFab)
    ) {
        val corespendetArticleInDataBase = uiState.articlesBaseDonneECB
        val placesOfArticelsInCamionette = uiState.placesOfArticelsInCamionette
        val groupedArticles = groupeurBonCommendToSupplierTabele
            .groupBy { article ->
                val correspondingDbArticle = corespendetArticleInDataBase.find { dbArticle ->
                    dbArticle.idArticle.toLong() == article.a_c_idarticle_c
                }
                placesOfArticelsInCamionette.find { placeInCam ->
                    placeInCam.idPlace == correspondingDbArticle?.idArticlePlaceInCamionette
                } ?: PlacesOfArticelsInCamionette(
                    idPlace = placesOfArticelsInCamionette.maxOf { it.idPlace } + 1,
                    namePlace = "Unplaced",
                    classement = placesOfArticelsInCamionette.maxOf { it.classement } + 1
                )
            }
            .toList()
            .sortedBy { (place, _) -> place.classement }

        groupedArticles.forEach { (place, articles) ->
            stickyHeader {
                PlaceHeader(placeItem = place)
            }

            items(articles) { article ->
                ArticleItemOfPlace(
                    article = article,
                    viewModel = viewModel,
                    onDismissWithUpdate = { /* Handle update */ },
                    onDismiss = { /* Handle dismiss */ }
                )
                HorizontalDivider(
                    modifier = modifier.fillMaxWidth(),
                    thickness = 4.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}


@Composable
fun ArticleItemOfPlace(
    article: GroupeurBonCommendToSupplierTabele,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    onDismissWithUpdate: (GroupeurBonCommendToSupplierTabele) -> Unit,
    onDismiss: () -> Unit
) {
    var showArticleDetails by remember { mutableStateOf<GroupeurBonCommendToSupplierTabele?>(null) }
    val reloadKey = remember(article) { System.currentTimeMillis() }

    CardArticlePlace(
        article = article,
        onClickToShowWindowsInfoArt = { showArticleDetails = it },
        onUpdateArticleStatus = { updatedArticle ->
            viewModel.updateArticleStatus(updatedArticle)
        },
        reloadKey = reloadKey,
        modifier = modifier
    )

    showArticleDetails?.let { articleDisplay ->
        WindowArticleDetail(
            article = articleDisplay,
            onDismissWithUpdatePlaceArticle = {
                showArticleDetails = null
                val updatedArticle =  article.copy(
                        itsInFindedAskSupplierSA = false,
                        disponibylityStatInSupplierStore = "Finded"
                    )
                viewModel.updateArticleStatus(updatedArticle)
                onDismiss()
            },
            onDismiss = {
                onDismiss()
                showArticleDetails = null
            },
            onDismissWithUpdateOfnonDispo = {
                showArticleDetails = null
                viewModel.updateArticleStatus(it)
            },
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
    }
}

@Composable
fun CardArticlePlace(
    article: GroupeurBonCommendToSupplierTabele,
    modifier: Modifier = Modifier,
    onClickToShowWindowsInfoArt: (GroupeurBonCommendToSupplierTabele) -> Unit,
    onUpdateArticleStatus: (GroupeurBonCommendToSupplierTabele) -> Unit,
    reloadKey: Long = 0,
) {
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
        Box(modifier = Modifier.fillMaxSize()) {
            DisplayeImageById(
                idArticle = article.a_c_idarticle_c,
                modifier = Modifier
                    .fillMaxSize(),
                reloadKey = reloadKey
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )

            if (article.itsInFindedAskSupplierSA) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(alpha)
                        .background(Color(0xFFFFD700).copy(alpha = 0.7f))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = article.nameArticle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "[${article.vid}] Q: ${article.totalquantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        val updatedArticle = when {
                            !article.itsInFindedAskSupplierSA -> article.copy(
                                itsInFindedAskSupplierSA = true,
                                disponibylityStatInSupplierStore = ""
                            )
                            article.disponibylityStatInSupplierStore != "Finded" -> article.copy(
                                itsInFindedAskSupplierSA = false,
                                disponibylityStatInSupplierStore = "Finded"
                            )
                            else -> article.copy(
                                itsInFindedAskSupplierSA = false,
                                disponibylityStatInSupplierStore = ""
                            )
                        }
                        onUpdateArticleStatus(updatedArticle)
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = when {
                            !article.itsInFindedAskSupplierSA -> Icons.AutoMirrored.Filled.NotListedLocation
                            article.disponibylityStatInSupplierStore != "Finded" -> Icons.Default.Check
                            else -> Icons.Default.Visibility
                        },
                        contentDescription = "Toggle Article Status",
                        tint = when {
                            !article.itsInFindedAskSupplierSA -> Color(0xFFFFD700)
                            article.disponibylityStatInSupplierStore != "Finded" -> Color.Red
                            else -> Color.White
                        },
                        modifier = Modifier.size(32.dp)
                    )
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
            Box(modifier = modifier.fillMaxSize()) {
                Column(modifier = modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search Articles") },
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    val articlesSupplier = if (searchText.isEmpty()) {
                        uiState.groupeurBonCommendToSupplierTabele.filter { article ->
                            article.idSupplierTSA.toLong() == place.idSupplierOfStore
                        }
                    } else {
                        uiState.groupeurBonCommendToSupplierTabele.filter { article ->
                            article.nameArticle.contains(searchText, ignoreCase = true)
                        }
                    }

                    val (nonPlacedArticles, placedArticles) = articlesSupplier.partition { article ->
                        !uiState.placesOfArticelsInEacheSupplierSrore.any { placedArticle ->
                            placedArticle.idCombinedIdArticleIdSupplier == "${article.a_c_idarticle_c}_${article.idSupplierTSA}"
                        }
                    }

                    Card(
                        modifier = modifier
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
                                modifier = modifier.padding(16.dp)
                            )
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(gridColumns),
                                state = gridState,
                                modifier = modifier.fillMaxSize()
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
                                            viewModel.moveArticlesToSupplier(
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
                    modifier = modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

