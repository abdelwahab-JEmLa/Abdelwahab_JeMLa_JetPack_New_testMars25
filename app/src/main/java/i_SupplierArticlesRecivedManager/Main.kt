package i_SupplierArticlesRecivedManager


import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import a_MainAppCompnents.TabelleSuppliersSA
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex


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
    var suppliersFlotingButtons by remember { mutableStateOf(false) }

    var reloadImageTrigger by remember { mutableStateOf(0) }  // Add this line

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

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
                        it.idSupplierTSA.toLong() == supplier.vidSupplierSA
                    }
                    if (articlesSupplier.isNotEmpty() || supplier.nomSupplierSA != "Finde") {
                        item(span = { GridItemSpan(gridColumns) }) {
                            SupplierHeader(supplier = supplier, viewModel = viewModel)
                        }
                        items(articlesSupplier) { article ->
                            ArticleItem(
                                article = article,
                                onClickOnImg = { clickedArticle ->
                                    dialogeDisplayeDetailleChanger = clickedArticle
                                }  ,
                                viewModel,
                                reloadImageTrigger
                            )
                        }
                    }
                }
            }
        }
           //TODO Ajoute un autre  Floating Action Buttons  dragable qui s affiche suppliersFlotingButtons et fait qui soit on fun separe
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
                onToggleDisplayeSuppButtons =  {suppliersFlotingButtons!=suppliersFlotingButtons}
            )
        }


        // Use the current edited article if it matches the given article, otherwise use the original article
        val displayedArticle = currentSupplierArticle?.takeIf { it.a_c_idarticle_c.toLong() == dialogeDisplayeDetailleChanger?.a_c_idarticle_c }
            ?: dialogeDisplayeDetailleChanger

        displayedArticle?.let { article ->
            ArticleDetailWindow(
                article = article,
                onDismiss = {
                    dialogeDisplayeDetailleChanger = null
                    viewModel.updateCurrentEditedArticle(null)
                 },
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 3.dp), onReloadTrigger = {reloadImageTrigger += 1}, relodeTigger = reloadImageTrigger
            )
        }
    }
}

@Composable
fun ArticleItem(
    article: TabelleSupplierArticlesRecived,
    onClickOnImg: (TabelleSupplierArticlesRecived) -> Unit,
    viewModel: HeadOfViewModels,
    reloadTrigger: Int
) {
    Card(   //TODO fait que si article itsInFindedAskSupplierSA = "Ask Supplier"
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

                DisplayeImage(
                    article = article,
                    viewModel = viewModel,
                    index = 0,
                    reloadKey = reloadTrigger
                )
            }
            article.itsInFindedAskSupplierSA?.let { DisponibilityOverlayECB(it.toString()) }
            AutoResizedTextECB(text = article.a_d_nomarticlefinale_c)
            //TODO ajoute une Floating button start botton au click
              //  .clickable { onClickDisplayeInfoWin (article) },

        }
    }
}

@Composable
fun SupplierHeader(
    supplier: TabelleSuppliersSA,
    viewModel: HeadOfViewModels,
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = supplier.nomSupplierSA,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
@Composable
fun OverlayContentECB(color: Color, icon: ImageVector) {
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
fun DisponibilityOverlayECB(state: String) {
    when (state) {
        "Ask WHer" -> OverlayContentECB(color = Color.Black, icon = Icons.Default.TextDecrease)
        "TO Find" -> OverlayContentECB(color = Color.Gray, icon = Icons.Default.Person)
    }
}


