package h_FactoryClassemntsArticles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import b_Edite_Base_Donne.LoadImageFromPath
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MainFactoryClassementsArticles(viewModel: ClassementsArticlesViewModel, onToggleNavBar: () -> Unit) {
    val articles by viewModel.articlesList.collectAsState()
    val categories by viewModel.categorieList.collectAsState()
    val showOnlyWithFilter by viewModel.showOnlyWithFilter.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(false) }
    var holdedIdCateForMove by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButtons(
                showFloatingButtons = showFloatingButtons,
                onToggleNavBar = onToggleNavBar,
                onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                onToggleFilter = viewModel::toggleFilter,
                showOnlyWithFilter = showOnlyWithFilter
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(padding)
        ) {
            categories.forEach { category ->
                item(span = { GridItemSpan(3) }) {
                    CategoryHeader(
                        category = category,
                        isSelected = holdedIdCateForMove == category.idCategorieCT,
                        onCategoryClick = { clickedCategory ->
                            if (holdedIdCateForMove == null) {
                                holdedIdCateForMove = clickedCategory.idCategorieCT
                            } else if (holdedIdCateForMove != clickedCategory.idCategorieCT) {
                                viewModel.swapCategoryPositions(holdedIdCateForMove!!, clickedCategory.idCategorieCT)
                                holdedIdCateForMove = null
                            } else {
                                holdedIdCateForMove = null
                            }
                        }
                    )
                }
                items(articles.filter { it.idCategorie == category.idCategorieCT.toDouble() }) { article ->
                    ArticleItem(
                        article = article,
                        onDisponibilityChange = { newState ->
                            viewModel.updateArticleDisponibility(article.idArticle, newState)
                        }
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
    onCategoryClick: (CategorieTabelee) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .clickable { onCategoryClick(category) }
    ) {
        Text(
            text = category.nomCategorieCT,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ArticleItem(article: ClassementsArticlesTabel, onDisponibilityChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clickable {
                val newState = when (article.diponibilityState) {
                    "" -> "Non Dispo"
                    "Non Dispo" -> "NonForNewsClients"
                    else -> ""
                }
                onDisponibilityChange(newState)
            }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(contentAlignment = Alignment.Center) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                LoadImageFromPath(imagePath = imagePath)

                when (article.diponibilityState) {
                    "Non Dispo" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.TextDecrease, "Not Available For all", tint = Color.White)
                        }
                    }
                    "NonForNewsClients" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, "Not Available For New Clients", tint = Color.White)
                        }
                    }
                }
            }
            Text(text = article.nomArticleFinale, style = MaterialTheme.typography.bodyLarge)
            Text(text = article.nomCategorie, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun FloatingActionButtons(
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean
) {
    Column {
        if (showFloatingButtons) {
            FloatingActionButton(onClick = onToggleNavBar) {
                Icon(Icons.Default.Home, "Toggle Navigation Bar")
            }
            FloatingActionButton(onClick = onToggleFilter) {
                Icon(
                    if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff,
                    "Toggle Filter"
                )
            }
        }
        FloatingActionButton(onClick = onToggleFloatingButtons) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                if (showFloatingButtons) "Hide Buttons" else "Show Buttons"
            )
        }
    }
}

class ClassementsArticlesViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val refClassmentsArtData = database.getReference("BaseDonne_Bakup3")
    private val refCategorieTabelee = database.getReference("H_CategorieTabele")

    private val _articlesList = MutableStateFlow<List<ClassementsArticlesTabel>>(emptyList())
    private val _categorieList = MutableStateFlow<List<CategorieTabelee>>(emptyList())
    private val _showOnlyWithFilter = MutableStateFlow(false)

    val articlesList = combine(_articlesList, _showOnlyWithFilter) { articles, filterKey ->
        if (filterKey) articles.filter { it.diponibilityState == "" } else articles
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categorieList = _categorieList.asStateFlow()
    val showOnlyWithFilter = _showOnlyWithFilter.asStateFlow()

    init {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            val articlesSnapshot = refClassmentsArtData.get().await()
            _articlesList.value = articlesSnapshot.children.mapNotNull { it.getValue(ClassementsArticlesTabel::class.java) }
                .sortedWith(compareBy<ClassementsArticlesTabel> { it.idCategorie }.thenBy { it.classementIdAuCate })

            val categoriesSnapshot = refCategorieTabelee.get().await()
            _categorieList.value = categoriesSnapshot.children.mapNotNull { it.getValue(CategorieTabelee::class.java) }
                .sortedBy { it.idClassementCategorieCT }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun swapCategoryPositions(fromCategoryId: Long, toCategoryId: Long) {
        viewModelScope.launch {
            val updatedCategories = _categorieList.value.toMutableList()
            val fromIndex = updatedCategories.indexOfFirst { it.idCategorieCT == fromCategoryId }
            val toIndex = updatedCategories.indexOfFirst { it.idCategorieCT == toCategoryId }

            if (fromIndex != -1 && toIndex != -1) {
                val tempClassement = updatedCategories[fromIndex].idClassementCategorieCT
                updatedCategories[fromIndex] = updatedCategories[fromIndex].copy(idClassementCategorieCT = updatedCategories[toIndex].idClassementCategorieCT)
                updatedCategories[toIndex] = updatedCategories[toIndex].copy(idClassementCategorieCT = tempClassement)

                updatedCategories.sortBy { it.idClassementCategorieCT }
                _categorieList.value = updatedCategories

                updateCategoriesInFirebase(updatedCategories)
                updateArticlesToFollowCategories(fromCategoryId, toCategoryId)
            }
        }
    }

    private suspend fun updateCategoriesInFirebase(categories: List<CategorieTabelee>) {
        categories.forEach { category ->
            refCategorieTabelee.child(category.idCategorieCT.toString()).setValue(category).await()
        }
    }

    private fun updateArticlesToFollowCategories(fromCategoryId: Long, toCategoryId: Long) {
        val updatedArticles = _articlesList.value.map { article ->
            when (article.idCategorie) {
                fromCategoryId.toDouble() -> article.copy(idCategorie = toCategoryId.toDouble())
                toCategoryId.toDouble() -> article.copy(idCategorie = fromCategoryId.toDouble())
                else -> article
            }
        }

        _articlesList.value = updatedArticles
        viewModelScope.launch {
            updatedArticles.forEach { article ->
                refClassmentsArtData.child(article.idArticle.toString()).setValue(article).await()
            }
        }
    }

    fun toggleFilter() {
        _showOnlyWithFilter.value = !_showOnlyWithFilter.value
    }

    fun updateArticleDisponibility(articleId: Long, newDisponibilityState: String) {
        viewModelScope.launch {
            val updatedArticles = _articlesList.value.map { article ->
                if (article.idArticle == articleId) article.copy(diponibilityState = newDisponibilityState) else article
            }
            _articlesList.value = updatedArticles
            refClassmentsArtData.child(articleId.toString()).child("diponibilityState").setValue(newDisponibilityState).await()
        }
    }
}

data class ClassementsArticlesTabel(
    val idArticle: Long = 0,
    val nomArticleFinale: String = "",
    val idCategorie: Double = 0.0,
    val nomCategorie: String = "",
    val classementIdAuCate: Double = 0.0,
    val diponibilityState: String = ""
)

data class CategorieTabelee(
    val idCategorieCT: Long = 0,
    val idClassementCategorieCT: Double = 0.0,
    val nomCategorieCT: String = ""
)
