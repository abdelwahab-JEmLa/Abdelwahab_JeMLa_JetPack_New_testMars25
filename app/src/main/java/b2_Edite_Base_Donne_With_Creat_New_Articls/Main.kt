package b2_Edite_Base_Donne_With_Creat_New_Articls

import a_MainAppCompnents.BaseDonneECBTabelle
import a_MainAppCompnents.HeadOfViewModels
import android.util.Log
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

@Composable
fun MainFragmentEditDatabaseWithCreateNewArticles(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
    onNewArticleAdded: (BaseDonneECBTabelle) -> Unit,
    reloadTrigger: Int,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(2) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var filterNonDispo by remember { mutableStateOf(false) }



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
                            CategoryHeaderECB(
                                category = category,
                                viewModel = viewModel,
                                onNewArticleAdded = { newArticle ->
                                    onNewArticleAdded(newArticle)
                                    Log.d("MainFragment", "New article added: $newArticle")
                                }
                            )
                        }
                        items(articlesInCategory) { article ->
                            ArticleItemECB(
                                article = article,
                                onClickOnImg = { clickedArticle ->
                                    Log.d("MainFragment", "Article clicked: $clickedArticle")
                                    onNewArticleAdded(clickedArticle)

                                    viewModel.updateCurrentEditedArticle(clickedArticle)
                                },
                                viewModel = viewModel,
                                reloadTrigger = reloadTrigger
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
                onChangeGridColumns = { gridColumns = it },
                uiState=uiState
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
