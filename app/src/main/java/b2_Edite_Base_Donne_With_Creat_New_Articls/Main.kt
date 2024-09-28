package b2_Edite_Base_Donne_With_Creat_New_Articls

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.lifecycle.viewModelScope
import h_FactoryClassemntsArticles.ClassementsArticlesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Composable
fun MainFragmentEditDatabaseWithCreateNewArticles(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(true) }
    var holdedIdCateForMove by remember { mutableStateOf<Long?>(null) }
    var gridColumns by remember { mutableStateOf(3) }

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            FloatingActionButtons(
                showFloatingButtons = showFloatingButtons,
                onToggleNavBar = onToggleNavBar,
                onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                onToggleFilter = viewModel::toggleFilter,
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
            uiState.categories.forEach { category ->
                item(span = { GridItemSpan(gridColumns) }) {
                    CategoryHeader(
                        category = category,
                        isSelected = holdedIdCateForMove == category.idCategorieCT,
                    )
                }
                items(uiState.articles.filter { it.idCategorie == category.idCategorieCT.toDouble() }) { article ->
                    ArticleItem(
                        article = article,
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    category: CategorieTabelee,
    isSelected: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = category.nomCategorieCT,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ArticleItem(
    article: ClassementsArticlesTabel,
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
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {

                ImageDisplayerWithGlide(article)

                DisponibilityOverlay(article.diponibilityState)
            }
            AutoResizedTextClas(text = article.nomArticleFinale)
        }
    }
}

@Composable
fun OverlayContent(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White)
    }
}

class HeadOfViewModels(
    private val repositoryCreatAndEditeInBaseDonne: CreatAndEditeInBaseDonneRepository
) : ViewModel() {
    val uiState = repositoryCreatAndEditeInBaseDonne.uiState

    fun toggleFilter() = repositoryCreatAndEditeInBaseDonne.toggleFilter()

    fun updateArticleDisponibility(articleId: Long, newDisponibilityState: String) {
        viewModelScope.launch { repositoryCreatAndEditeInBaseDonne.updateArticleDisponibility(articleId, newDisponibilityState) }
    }

}

class CreatAndEditeInBaseDonneRepository(private val database: FirebaseDatabase) {
    private val refClassmentsArtData = database.getReference("H_ClassementsArticlesTabel")
    private val refCategorieTabelee = database.getReference("H_CategorieTabele")
    private val refDBJetPackExport = database.getReference("e_DBJetPackExport")

    private val _uiState = MutableStateFlow(CreatAndEditeInBaseDonnModel())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {     //TODO fix Unresolved reference. None of the following candidates is applicable because of receiver type mismatch:
            //public val ViewModel.viewModelScope: CoroutineScope defined in androidx.lifecycle
            initDataFromFirebase()
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            val articlesSnapshot = refClassmentsArtData.get().await()
            val articles = articlesSnapshot.children.mapNotNull { it.getValue(ClassementsArticlesTabel::class.java) }

            val categoriesSnapshot = refCategorieTabelee.get().await()
            val categories = categoriesSnapshot.children.mapNotNull { it.getValue(CategorieTabelee::class.java) }
                .sortedBy { it.idClassementCategorieCT }

            _uiState.update { currentState ->
                currentState.copy(
                    articles = articles,
                    categories = categories
                )
            }
        } catch (e: Exception) {
            Log.e("ClassementsArticlesRepo", "Error loading data", e)
        }
    }

    fun toggleFilter() {
        _uiState.update { currentState ->
            currentState.copy(showOnlyWithFilter = !currentState.showOnlyWithFilter)
        }
    }

    suspend fun updateArticleDisponibility(articleId: Long, newDisponibilityState: String) {
        _uiState.update { currentState ->
            val updatedArticles = currentState.articles.map { article ->
                if (article.idArticle == articleId) article.copy(diponibilityState = newDisponibilityState) else article
            }
            currentState.copy(articles = updatedArticles)
        }
        refClassmentsArtData.child(articleId.toString()).child("diponibilityState").setValue(newDisponibilityState).await()
    }

}


data class ClassementsArticlesTabel(
    val idArticle: Long = 0,
    val nomArticleFinale: String = "",
    val idCategorie: Double = 0.0,
    var classementInCategoriesCT: Double = 0.0,
    val nomCategorie: String = "",
    var classementArticleAuCategorieCT: Double = 0.0,
    var itsNewArticleInCateWithID: Boolean = false,
    var classementCate: Double = 0.0,
    val diponibilityState: String = ""
)

data class CategorieTabelee(
    val idCategorieCT: Long = 0,
    var idClassementCategorieCT: Double = 0.0,
    val nomCategorieCT: String = "",
)
data class CreatAndEditeInBaseDonnModel(
    val articles: List<ClassementsArticlesTabel> = emptyList(),
    val categories: List<CategorieTabelee> = emptyList(),
    val showOnlyWithFilter: Boolean = false
)
