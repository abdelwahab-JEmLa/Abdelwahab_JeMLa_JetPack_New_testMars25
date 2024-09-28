package b2_Edite_Base_Donne_With_Creat_New_Articls

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MainFragmentEditDatabaseWithCreateNewArticles(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit,
) {
    val uiState by viewModel.uiStateHeaderViewsModel.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(true) }
    var gridColumns by remember { mutableStateOf(1) }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var filterNonDispo by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
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
                onChangeGridColumns = { gridColumns = it }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridColumns),
            state = gridState,
            modifier = Modifier.padding(padding)
        ) {
            uiState.categoriesECB.forEach { category ->
                item(span = { GridItemSpan(gridColumns) }) {
                    CategoryHeaderECB(category = category)
                }
                items(uiState.articlesBaseDonneECB.filter {
                    it.nomCategorie == category.nomCategorieInCategoriesTabele &&
                            (!filterNonDispo || it.diponibilityState != "")
                }) { article ->
                    ArticleItemECB(article = article)
                }
            }
        }
    }
}

class HeadOfViewModels(
    private val modifierCreatAndEditeInBaseDonne: CreatAndEditeInBaseDonneModifier
) : ViewModel() {
    private val _uiStateHeaderViewsModel = MutableStateFlow(CreatAndEditeInBaseDonnRepositeryModels())
    val uiStateHeaderViewsModel = _uiStateHeaderViewsModel.asStateFlow()

    private val refDBJetPackExport = FirebaseDatabase.getInstance().getReference("e_DBJetPackExport")
    private val refCategorieTabelee = FirebaseDatabase.getInstance().getReference("H_CategorieTabele")

    init {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            _uiStateHeaderViewsModel.update { it.copy(isLoading = true) }

            val articlesClassementSnapshot = refDBJetPackExport.get().await()
            val articles = articlesClassementSnapshot.children.mapNotNull { snapshot ->
                snapshot.getValue(BaseDonneECBTabelle::class.java)?.apply {
                    updateIdArticle(snapshot.value as? Map<String, Any?> ?: emptyMap())
                }
            }

            val categoriesSnapshot = refCategorieTabelee.get().await()
            val categories = categoriesSnapshot.children.mapNotNull { it.getValue(CategoriesTabelleECB::class.java) }
                .sortedBy { it.idClassementCategorieInCategoriesTabele }

            _uiStateHeaderViewsModel.update { currentState ->
                currentState.copy(
                    articlesBaseDonneECB = articles,
                    categoriesECB = categories,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Log.e("ClassementsArticlesRepo", "Error loading data", e)
            _uiStateHeaderViewsModel.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun toggleFilter() {
        val newState = modifierCreatAndEditeInBaseDonne.toggleFilter(_uiStateHeaderViewsModel.value)
        _uiStateHeaderViewsModel.update { newState }
    }

    fun refreshData() {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }
}

data class CreatAndEditeInBaseDonnRepositeryModels(
    val articlesBaseDonneECB: List<BaseDonneECBTabelle> = emptyList(),
    val categoriesECB: List<CategoriesTabelleECB> = emptyList(),
    val showOnlyWithFilter: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CreatAndEditeInBaseDonneModifier() {
    fun toggleFilter(currentState: CreatAndEditeInBaseDonnRepositeryModels): CreatAndEditeInBaseDonnRepositeryModels {
        return currentState.copy(showOnlyWithFilter = !currentState.showOnlyWithFilter)
    }
}
class HeadOfViewModelFactory() : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeadOfViewModels::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeadOfViewModels(CreatAndEditeInBaseDonneModifier()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
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
    constructor() : this(0)

    fun updateIdArticle(value: Map<String, Any?>) {
        idArticleECB = (value["idArticle"] as? Long)?.toInt() ?: 0
    }
}

data class CategoriesTabelleECB(
    val idCategorieInCategoriesTabele: Long = 0,
    var idClassementCategorieInCategoriesTabele: Double = 0.0,
    val nomCategorieInCategoriesTabele: String = "",
)
