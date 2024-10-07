package i_SupplierArticlesRecivedManager

import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import a_MainAppCompnents.TabelleSuppliersSA
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dehaze
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Fragment_SupplierArticlesRecivedManager(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
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

                    if (articlesSupplier.isNotEmpty()  && supplier.nomSupplierSu != "Find" && supplier.nomSupplierSu != "Non Define") {
                        item(span = { GridItemSpan(gridColumns) }) {
                            SupplierHeaderSA(supplier = supplier, viewModel = viewModel, onHeaderClick = {
                            })
                        }
                        items(articlesSupplier) { article ->
                                ArticleItemSA(
                                    article = article,
                                    onClickOnImg = { clickedArticle ->
                                        dialogeDisplayeDetailleChanger = clickedArticle
                                    },
                                    viewModel = viewModel,
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
                onToggleToFilterToMove = {
                    toggleCtrlToFilterToMove = !toggleCtrlToFilterToMove
                },
                filterSuppHandledNow = toggleCtrlToFilterToMove
            )
        }

            SuppliersFloatingButtonsSA(
                allArticles = uiState.tabelleSupplierArticlesRecived,
                suppliers = uiState.tabelleSuppliersSA,
                supplierFlotBisHandled = idSupplierOfFloatingButtonClicked,
                onClickFlotButt = { idSupplier ->
                    coroutineScope.launch {
                        if (toggleCtrlToFilterToMove) {
                            val filterBytabelleSupplierArticlesRecived = uiState.tabelleSupplierArticlesRecived.filter {
                                it.itsInFindedAskSupplierSA
                            }
                            viewModel.moveArticleNonFindToSupplier(
                                articlesToMove = filterBytabelleSupplierArticlesRecived,
                                toSupp = idSupplier
                            )
                            toggleCtrlToFilterToMove = false
                        } else {
                            idSupplierOfFloatingButtonClicked = when (idSupplierOfFloatingButtonClicked) {
                                idSupplier -> null  // Deselect if the same supplier is clicked again
                                else -> idSupplier  // Select the new supplier
                            }
                        }
                    }
                }
            )



        // Use the current edited article if it matches the given article, otherwise use the original article
        val displayedArticle = currentSupplierArticle?.takeIf { it.a_c_idarticle_c.toLong() == dialogeDisplayeDetailleChanger?.a_c_idarticle_c }
            ?: dialogeDisplayeDetailleChanger

        displayedArticle?.let { article ->
            ArticleDetailWindowSA(
                article = article,
                onDismiss = {
                    dialogeDisplayeDetailleChanger = null
                },
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 3.dp),
            )
        }
    }
}

@Composable
fun SuppliersFloatingButtonsSA(
    allArticles: List<TabelleSupplierArticlesRecived>,
    suppliers: List<TabelleSuppliersSA>,
    onClickFlotButt: (Long) -> Unit,
    supplierFlotBisHandled: Long?
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
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
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
fun ArticleItemSA(
    article: TabelleSupplierArticlesRecived,
    onClickOnImg: (TabelleSupplierArticlesRecived) -> Unit,
    viewModel: HeadOfViewModels,
) {
    val cardColor = if (article.itsInFindedAskSupplierSA) {
        Color.Yellow.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { viewModel.changeAskSupplier(article) },
                contentAlignment = Alignment.Center
            ) {
                DisplayeImageSA(
                    article = article,
                    viewModel = viewModel,
                    index = 0,
                )
            }

            DisponibilityOverlaySA(article.itsInFindedAskSupplierSA.toString())
            AutoResizedTextSA(text = article.a_d_nomarticlefinale_c)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                FloatingActionButton(
                    onClick = { onClickOnImg(article) }
                ) {
                    Icon(Icons.Filled.Info, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun SupplierHeaderSA(
    supplier: TabelleSuppliersSA,
    viewModel: HeadOfViewModels,
    onHeaderClick: (TabelleSuppliersSA) -> Unit
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
                onHeaderClick(supplier)
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


