package i_SupplierArticlesRecivedManager

import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import a_MainAppCompnents.TabelleSuppliersSA
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

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

    var filterNonDispo by remember { mutableStateOf(false) }
    var suppliersFloatingButtons by remember { mutableStateOf(false) }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    var supplierHeaderisHandled by remember { mutableStateOf(0L) }
    var showOnlyWithFilter by remember { mutableStateOf(false) }

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
                uiState.tabelleSuppliersSA.forEach { supplier ->
                    val articlesSupplier = uiState.tabelleSupplierArticlesRecived.filter {
                        it.idSupplierTSA.toLong() == supplier.idSupplierSu
                    }

                    if (articlesSupplier.isNotEmpty() && supplier.nomSupplierSu != "Find" && supplier.nomSupplierSu != "Non Define") {
                        item(span = { GridItemSpan(gridColumns) }) {
                            SupplierHeaderSA(supplier = supplier, viewModel = viewModel, onHeaderClick = {
                                coroutineScope.launch {
                                    val supplierIndex = uiState.tabelleSuppliersSA.indexOf(supplier)
                                    if (supplierIndex != -1) {
                                        gridState.animateScrollToItem(supplierIndex)
                                    }
                                    supplierHeaderisHandled = it.idSupplierSu
                                }
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
            FloatingActionButtonsSA(
                showFloatingButtons = showFloatingButtons,
                onToggleNavBar = onToggleNavBar,
                onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                onToggleFilter = {
                    viewModel.toggleFilter()
                    filterNonDispo = !filterNonDispo
                },
                showOnlyWithFilter = uiState.showOnlyWithFilter,
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                onChangeGridColumns = { gridColumns = it },
                onToggleDisplayeSuppButtons = { suppliersFloatingButtons = !suppliersFloatingButtons }
            )
        }

        AnimatedVisibility(
            visible = suppliersFloatingButtons,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            SuppliersFloatingButtonsSA(
                allArticles = uiState.tabelleSupplierArticlesRecived,
                suppliers = uiState.tabelleSuppliersSA,
                viewModel = viewModel,
                supplierHeaderisHandled = supplierHeaderisHandled,
                showOnlyWithFilter = showOnlyWithFilter,
                onToggleFilter = { showOnlyWithFilter = !showOnlyWithFilter }
            )
        }

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

@Composable
fun SuppliersFloatingButtonsSA(
    allArticles: List<TabelleSupplierArticlesRecived>,
    suppliers: List<TabelleSuppliersSA>,
    viewModel: HeadOfViewModels,
    supplierHeaderisHandled: Long,
    showOnlyWithFilter: Boolean,
    onToggleFilter: () -> Unit
) {
    val filteredSuppliers = if (showOnlyWithFilter) {
        suppliers.filter { supplier ->
            allArticles.any { article -> article.idSupplierTSA.toLong() == supplier.idSupplierSu }
        }
    } else {
        suppliers
    }

    Column {
        filteredSuppliers.forEach { supplier ->
            FloatingActionButton(
                onClick = {
                    viewModel.moveArticleNonFindToSupplier(fromSupp = supplierHeaderisHandled, toSupp = supplier.idSupplierSu)
                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(supplier.nomSupplierSu.take(2))
            }
        }
        FloatingActionButton(
            onClick = { onToggleFilter() },
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Icon(
                if (showOnlyWithFilter) Icons.Default.FilterAlt else Icons.Default.FilterAltOff,
                contentDescription = "Filter articles with supplier"
            )
        }
    }
}
