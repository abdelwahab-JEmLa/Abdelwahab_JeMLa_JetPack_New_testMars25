package b2_Edite_Base_Donne_With_Creat_New_Articls

import a_MainAppCompnents.CategoriesTabelleECB
import a_MainAppCompnents.DataBaseArticles
import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.Filter2
import androidx.compose.material.icons.filled.Filter3
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MainFragmentEditDatabaseWithCreateNewArticles(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
    onClickToOpenWinInfoDataBase: (DataBaseArticles) -> Unit,
    reloadTrigger: Int,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(2) }

    // Mémoriser la position de défilement
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    // État pour le déplacement des catégories
    var holdedIdCateForMove by remember { mutableStateOf<Long?>(null) }
    var movingCategory by remember { mutableStateOf(false) }

    // États pour le filtrage
    var filterNonDispo by remember { mutableStateOf(false) }
    var outlineFilter by remember { mutableStateOf(false) }
    var filterText by remember { mutableStateOf("") }
    var clickChangeDispoMode by remember { mutableStateOf(false) }

    // Gérer la position de défilement actuelle
    val firstVisibleItemIndex = remember { mutableStateOf(0) }
    val firstVisibleItemScrollOffset = remember { mutableStateOf(0) }

    // Effet pour sauvegarder la position de défilement
    LaunchedEffect(gridState) {
        snapshotFlow { gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                firstVisibleItemIndex.value = index
                firstVisibleItemScrollOffset.value = offset
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (outlineFilter) {
                    OutlinedTextField(
                        value = filterText,
                        onValueChange = { filterText = it },
                        label = { Text("Filtrer les articles") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                uiState.categoriesECB.forEach { category ->
                    val articlesInCategory = uiState.articlesBaseDonneECB.filter { article ->
                        article.idCategorieNewMetode == category.idCategorieInCategoriesTabele &&
                                (!filterNonDispo || article.diponibilityState == "") &&
                                (filterText.isEmpty() || article.nomArticleFinale.contains(filterText, ignoreCase = true))
                    }

                    if (articlesInCategory.isNotEmpty() || category.nomCategorieInCategoriesTabele == "New Articles") {
                        item(span = { GridItemSpan(gridColumns) }) {
                            CategoryHeaderECB(
                                category = category,
                                viewModel = viewModel,
                                onClickToOpenWinInfoDataBase = onClickToOpenWinInfoDataBase,
                                isSelected = holdedIdCateForMove == category.idCategorieInCategoriesTabele,
                                onCategoryClick = { clickedCategory ->
                                    if (!movingCategory) {
                                        if (holdedIdCateForMove == null) {
                                            holdedIdCateForMove = clickedCategory.idCategorieInCategoriesTabele
                                        } else if (holdedIdCateForMove != clickedCategory.idCategorieInCategoriesTabele) {
                                            movingCategory = true
                                            coroutineScope.launch {
                                                try {
                                                    // Sauvegarder la position actuelle
                                                    val currentIndex = gridState.firstVisibleItemIndex
                                                    val currentOffset = gridState.firstVisibleItemScrollOffset

                                                    viewModel.handleCategoryMove(
                                                        holdedIdCateForMove!!,
                                                        clickedCategory.idCategorieInCategoriesTabele
                                                    ) {
                                                        holdedIdCateForMove = null
                                                    }

                                                    // Petit délai pour laisser l'UI se mettre à jour
                                                    delay(100)

                                                    // Restaurer la position
                                                    gridState.scrollToItem(
                                                        index = currentIndex,
                                                        scrollOffset = currentOffset
                                                    )
                                                } finally {
                                                    holdedIdCateForMove = null
                                                    movingCategory = false
                                                }
                                            }
                                        } else {
                                            holdedIdCateForMove = null
                                        }
                                    }
                                }
                            )
                        }

                        items(articlesInCategory) { article ->
                            ArticleItemECB(
                                article = article,
                                onClickOnImg = { clickedArticle ->
                                    if (!clickChangeDispoMode) {
                                        onClickToOpenWinInfoDataBase(clickedArticle)
                                        viewModel.updateCurrentEditedArticle(clickedArticle)
                                    } else {
                                        viewModel.updateArticleDisponibility(
                                            clickedArticle.idArticle.toLong(),
                                            getNextDisponibilityState(clickedArticle.diponibilityState)
                                        )
                                    }
                                },
                                viewModel = viewModel,
                                reloadTrigger = reloadTrigger
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Buttons (reste inchangé)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            FloatingActionButtons(
                uiState = uiState,
                showFloatingButtons = showFloatingButtons,
                onToggleNavBar = onToggleNavBar,
                onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                onToggleFilter = {
                    viewModel.toggleFilter()
                    filterNonDispo = !filterNonDispo
                },
                onToggleOutlineFilter = { outlineFilter = !outlineFilter },
                showOnlyWithFilter = uiState.showOnlyWithFilter,
                viewModel = viewModel,
                onCategorySelected = { selectedCategory ->
                    coroutineScope.launch {
                        val index = uiState.categoriesECB.indexOfFirst {
                            it.idCategorieInCategoriesTabele == selectedCategory.idCategorieInCategoriesTabele
                        }
                        if (index != -1) {
                            val position = uiState.categoriesECB.take(index).sumOf { category ->
                                1 + uiState.articlesBaseDonneECB.count {
                                    it.idCategorie == category.idCategorieInCategoriesTabele.toDouble()
                                }
                            }
                            gridState.scrollToItem(position)
                        }
                    }
                },
                onChangeGridColumns = { gridColumns = it },
                onToggleModeClickDispo = {
                    clickChangeDispoMode = !clickChangeDispoMode
                    showFloatingButtons = false
                }
            )
        }
    }
}

fun getNextDisponibilityState(currentState: String): String = when (currentState) {
    "" -> "Non Dispo"
    "Non Dispo" -> "NonForNewsClients"
    else -> ""
}

@Composable
fun ArticleItemECB(
    article: DataBaseArticles,
    onClickOnImg: (DataBaseArticles) -> Unit,
    viewModel: HeadOfViewModels,
    reloadTrigger: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onClickOnImg(article) },
                contentAlignment = Alignment.Center
            ) {
                DisplayeImageECB(
                    article = article,
                    viewModel = viewModel,
                    index = 0,
                    reloadKey = reloadTrigger
                )
                DisponibilityOverlayECB(article.diponibilityState)
                // Status indicators row
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Image dimension indicator and selector
                    Icon(
                        imageVector = when (article.imageDimention) {
                            "Demi" -> Icons.Default.Filter2
                            "Big" -> Icons.Default.Filter3
                            else -> Icons.Default.Filter1
                        },
                        contentDescription = "Image dimensions",
                        tint = Color.Red,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                val nextDimension = getNextImageDimension(article.imageDimention)
                                viewModel.updateArticleInfoDataBase(
                                    article.copy(imageDimention = nextDimension)
                                )
                            }
                    )

                    // New arrival indicator/toggle
                    Icon(
                        imageVector = if (article.itsNewArrivale) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Toggle new arrival status",
                        tint = if (article.itsNewArrivale) Color.Yellow else Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                viewModel.updateArticleInfoDataBase(
                                    article.copy(itsNewArrivale = !article.itsNewArrivale)
                                )
                            }
                    )
                }
            }
            AutoResizedTextECB(text = article.nomArticleFinale)
        }
    }
}

// Helper function to cycle through imageDimention
private fun getNextImageDimension(currentDimension: String): String = when (currentDimension) {
    "" -> "Demi"
    "Demi" -> ""
    "Big" -> ""
    else -> ""
}
//CategoryHeaderECB
@Composable
fun CategoryHeaderECB(
    category: CategoriesTabelleECB,
    viewModel: HeadOfViewModels,
    isSelected: Boolean,
    onClickToOpenWinInfoDataBase: (DataBaseArticles) -> Unit,
    onCategoryClick: (CategoriesTabelleECB) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)

    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.nomCategorieInCategoriesTabele,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { onCategoryClick(category) }
            )
            AddArticleButton(viewModel ,category, onClickToOpenWinInfoDataBase)
        }
    }
}
