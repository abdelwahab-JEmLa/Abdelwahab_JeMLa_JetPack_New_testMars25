package b2_Edite_Base_Donne_With_Creat_New_Articls

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
fun MainFragmentEditDatabaseWithCreateNewArticles(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit,
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
                onUpdateStart = onUpdateStart,
                onUpdateProgress = onUpdateProgress,
                onUpdateComplete = onUpdateComplete,
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
                                val sourceFile = File(viewModel.getDownloadsDirectory(), fileName)
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

                DisplayeImageGlideECB(
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

data class BaseDonneECBTabelle(
    var idArticleECB: Int = 0,
    var nomArticleFinale: String = "",
    var classementCate: Double = 0.0,
    var nomArab: String = "",
    var autreNomDarticle: String? = null,
    var nmbrCat: Int = 0,
    var couleur1: String? = null,
    var couleur2: String? = null,
    var couleur3: String? = null,
    var couleur4: String? = null,
    var nomCategorie2: String? = null,
    var nmbrUnite: Int = 0,
    var nmbrCaron: Int = 0,
    var affichageUniteState: Boolean = false,
    var commmentSeVent: String? = null,
    var afficheBoitSiUniter: String? = null,
    var monPrixAchat: Double = 0.0,
    var clienPrixVentUnite: Double = 0.0,
    var minQuan: Int = 0,
    var monBenfice: Double = 0.0,
    var monPrixVent: Double = 0.0,
    var diponibilityState: String = "",
    var neaon2: String = "",
    var idCategorie: Double = 0.0,
    var funChangeImagsDimention: Boolean = false,
    var nomCategorie: String = "",
    var neaon1: Double = 0.0,
    var lastUpdateState: String = "",
    var cartonState: String = "",
    var dateCreationCategorie: String = "",
    var prixDeVentTotaleChezClient: Double = 0.0,
    var benficeTotaleEntreMoiEtClien: Double = 0.0,
    var benificeTotaleEn2: Double = 0.0,
    var monPrixAchatUniter: Double = 0.0,
    var monPrixVentUniter: Double = 0.0,
    var benificeClient: Double = 0.0,
    var monBeneficeUniter: Double = 0.0
) {
    fun getColumnValue(columnName: String): Any? {
        val value = when (columnName) {
            "nomArticleFinale" -> nomArticleFinale
            "classementCate" -> classementCate
            "nomArab" -> nomArab
            "nmbrCat" -> nmbrCat
            "couleur1" -> couleur1
            "couleur2" -> couleur2
            "couleur3" -> couleur3
            "couleur4" -> couleur4
            "nomCategorie2" -> nomCategorie2
            "nmbrUnite" -> nmbrUnite
            "nmbrCaron" -> nmbrCaron
            "affichageUniteState" -> affichageUniteState
            "commmentSeVent" -> commmentSeVent
            "afficheBoitSiUniter" -> afficheBoitSiUniter
            "monPrixAchat" -> monPrixAchat
            "clienPrixVentUnite" -> clienPrixVentUnite
            "minQuan" -> minQuan
            "monBenfice" -> monBenfice
            "monPrixVent" -> monPrixVent
            "diponibilityState" -> diponibilityState
            "neaon2" -> neaon2
            "idCategorie" -> idCategorie
            "funChangeImagsDimention" -> funChangeImagsDimention
            "nomCategorie" -> nomCategorie
            "neaon1" -> neaon1
            "lastUpdateState" -> lastUpdateState
            "cartonState" -> cartonState
            "dateCreationCategorie" -> dateCreationCategorie
            "prixDeVentTotaleChezClient" -> prixDeVentTotaleChezClient
            "benficeTotaleEntreMoiEtClien" -> benficeTotaleEntreMoiEtClien
            "benificeTotaleEn2" -> benificeTotaleEn2
            "monPrixAchatUniter" -> monPrixAchatUniter
            "monPrixVentUniter" -> monPrixVentUniter
            "benificeClient" -> benificeClient
            "monBeneficeUniter" -> monBeneficeUniter
            else -> null
        }

        return when (value) {
            is Double -> if (value % 1 == 0.0) value.toInt() else value
            else -> value
        }
    }
}

data class CategoriesTabelleECB(
    val idCategorieInCategoriesTabele: Long = 0,
    var idClassementCategorieInCategoriesTabele: Double = 0.0,
    val nomCategorieInCategoriesTabele: String = "",
)
