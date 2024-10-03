package i_SupplierArticlesRecivedManager

import a_MainAppCompnents.BaseDonneECBTabelle
import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun Fragment_SupplierArticlesRecivedManager(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentEditedArticle by viewModel.currentEditedArticle.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(2) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var filterNonDispo by remember { mutableStateOf(false) }
    var reloadTrigger by remember { mutableStateOf(0) }  // Add this line

    var dialogeDisplayeDetailleChanger by remember { mutableStateOf<BaseDonneECBTabelle?>(null) }

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
                uiState.categoriesECB.forEach { category ->
                    val articlesInCategory = uiState.articlesBaseDonneECB.filter {
                        it.nomCategorie == category.nomCategorieInCategoriesTabele &&
                                (!filterNonDispo || it.diponibilityState == "")
                    }
                    if (articlesInCategory.isNotEmpty() || category.nomCategorieInCategoriesTabele == "New Articles") {
                        item(span = { GridItemSpan(gridColumns) }) {
                            CategoryHeaderECB(category = category, viewModel = viewModel)
                        }
                        items(articlesInCategory) { article ->
                            ArticleItemECB(
                                article = article,
                                onClickOnImg = { clickedArticle ->
                                    dialogeDisplayeDetailleChanger = clickedArticle
                                }  ,
                                viewModel,
                                reloadTrigger
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
            FloatingActionButtons(
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
                onChangeGridColumns = { gridColumns = it } ,
            )
        }


        // Use the current edited article if it matches the given article, otherwise use the original article
        val displayedArticle = currentEditedArticle?.takeIf { it.idArticleECB == dialogeDisplayeDetailleChanger?.idArticleECB }
            ?: dialogeDisplayeDetailleChanger

        displayedArticle?.let { article ->
            ArticleDetailWindow(
                article = article,
                onDismiss = {
                    dialogeDisplayeDetailleChanger = null
                    viewModel.updateCurrentEditedArticle(null)

                    // Check if the article is new or if key changes occurred
                    if (article.nomCategorie.contains("New", ignoreCase = true) ||
                        article.idArticleECB != dialogeDisplayeDetailleChanger?.idArticleECB) {
                        // Trigger image reload
                        coroutineScope.launch {
                            for (i in 1..4) {
                                val fileName = "${article.idArticleECB}_$i.jpg"
                                val sourceFile = File(viewModel.dossiesStandartImages, fileName)
                                if (sourceFile.exists()) {
                                    viewModel.setImagesInStorageFireBase(article.idArticleECB, i)
                                }
                            }
                            // Increment reloadTrigger to force recomposition
                            reloadTrigger += 1
                        }
                    }
                },
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 3.dp), onReloadTrigger = {reloadTrigger += 1}, relodeTigger = reloadTrigger
            )
        }
    }
}

@Composable
fun ArticleItemECB(
    article: BaseDonneECBTabelle,
    onClickOnImg: (BaseDonneECBTabelle) -> Unit,
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
            }
            AutoResizedTextECB(text = article.nomArticleFinale)
        }
    }
}
