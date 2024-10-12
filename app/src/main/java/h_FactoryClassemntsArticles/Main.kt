package h_FactoryClassemntsArticles

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

@Composable
fun MainFactoryClassementsArticles(
    viewModel: ClassementsArticlesViewModel,
    onToggleNavBar: () -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit,
) {
    val articles by viewModel.articlesList.collectAsState()
    val categories by viewModel.categorieList.collectAsState()
    val showOnlyWithFilter by viewModel.showOnlyWithFilter.collectAsState()
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
                showOnlyWithFilter = showOnlyWithFilter,
                categories = categories,
                viewModel = viewModel,
                onCategorySelected = { selectedCategory ->
                    coroutineScope.launch {
                        val index =
                            categories.indexOfFirst { it.idCategorieInCategoriesTabele == selectedCategory.idCategorieInCategoriesTabele }
                        if (index != -1) {
                            val position = categories.take(index).sumOf { category ->
                                1 + articles.count { it.idCategorie == category.idCategorieInCategoriesTabele.toDouble() }
                            }
                            gridState.scrollToItem(position)
                        }
                    }
                },
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
            categories.forEach { category ->
                item(span = { GridItemSpan(gridColumns) }) {
                    CategoryHeader(
                        category = category,
                        isSelected = holdedIdCateForMove == category.idCategorieInCategoriesTabele,
                        onCategoryClick = { clickedCategory ->
                            if (holdedIdCateForMove == null) {
                                holdedIdCateForMove = clickedCategory.idCategorieInCategoriesTabele
                            } else if (holdedIdCateForMove != clickedCategory.idCategorieInCategoriesTabele) {
                                viewModel.goUpAndshiftsAutersDownCategoryPositions(
                                    holdedIdCateForMove!!,
                                    clickedCategory.idCategorieInCategoriesTabele
                                )
                                holdedIdCateForMove = null
                            } else {
                                holdedIdCateForMove = null
                            }
                        }
                    )
                }
                items(articles.filter { it.idCategorie == category.idCategorieInCategoriesTabele.toDouble() }) { article ->
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
fun FloatingActionButtonGroup(
    onCategorySelectionClick: () -> Unit,
    onToggleNavBar: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean,
    onDialogDataBaseEditerClick: () -> Unit,
    showDialogeDataBaseEditer: Boolean,
    onChangeGridColumns: (Int) -> Unit
) {
    FloatingActionButton(
        onClick = onCategorySelectionClick,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(Icons.Default.Category, null)
    }
    FloatingActionButton(
        onClick = onToggleNavBar,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(Icons.Default.Home, null)
    }
    FloatingActionButton(
        onClick = onToggleFilter,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff,
            null
        )
    }
    FloatingActionButton(
        onClick = onDialogDataBaseEditerClick,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(
            if (showDialogeDataBaseEditer) Icons.Default.Close else Icons.Default.PermMedia,
            null
        )
    }
    FloatingActionButton(
        onClick = { onChangeGridColumns(if (Random.nextBoolean()) 3 else 4 ) },   //TODO ajoute else 5 et 2
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Icon(Icons.Default.GridView, null)
    }
}


class ClassementsArticlesViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val refClassmentsArtData = database.getReference("H_ClassementsArticlesTabel")
    private val refCategorieTabelee = database.getReference("H_CategorieTabele")

    private val _articlesList = MutableStateFlow<List<ClassementsArticlesTabel>>(emptyList())
    private val _categorieList = MutableStateFlow<List<CategoriesTabelle>>(emptyList())
    private val _showOnlyWithFilter = MutableStateFlow(false)

    val articlesList = combine(
        _articlesList,
        _categorieList,
        _showOnlyWithFilter
    ) { articles, categories, filterKey ->
        val sortedArticles = articles.sortedWith(
            compareBy<ClassementsArticlesTabel> { article ->
                categories.find { it.idCategorieInCategoriesTabele == article.idCategorie.toLong() }?.idClassementCategorieInCategoriesTabele
                    ?: Double.MAX_VALUE
            }.thenBy { it.classementArticleAuCategorieCT }
        )
        if (filterKey) sortedArticles.filter { it.diponibilityState == "" } else sortedArticles
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categorieList = _categorieList.asStateFlow()

    val showOnlyWithFilter = _showOnlyWithFilter.asStateFlow()

    init {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }

    fun giveNumAuSubCategorieArticle() {
        viewModelScope.launch {
            val updatedArticles = updateArticlesRanking(_articlesList.value, _categorieList.value)
            _articlesList.value = updatedArticles
            updateFirebaseArticles(updatedArticles)
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            _articlesList.value = refClassmentsArtData.get().await().children.mapNotNull {
                it.getValue(ClassementsArticlesTabel::class.java)
            }
            _categorieList.value = refCategorieTabelee.get().await().children.mapNotNull {
                it.getValue(CategoriesTabelle::class.java)
            }
                .sortedBy { it.idClassementCategorieInCategoriesTabele }
        } catch (e: Exception) {
            Log.e("ClassementsArticlesVM", "Error loading data", e)
        }
    }

    suspend fun updateCategorieTabelee() {
        try {
            val categories = createCategoriesFromArticles(_articlesList.value)
            _categorieList.value = categories
            updateFirebaseCategories(categories)
            updateArticlesWithNewCategoryIds(_articlesList.value)
            Log.d("ClassementsArticlesVM", "CategoriesTabelle updated successfully")
        } catch (e: Exception) {
            Log.e("ClassementsArticlesVM", "Error updating CategoriesTabelle", e)
        }
    }

    fun delete() {
        viewModelScope.launch {
            refClassmentsArtData.removeValue().await()
        }
    }

    fun goUpAndshiftsAutersDownCategoryPositions(fromCategoryId: Long, toCategoryId: Long) {
        viewModelScope.launch {
            val updatedCategories =
                reorderCategories(_categorieList.value, fromCategoryId, toCategoryId)
            _categorieList.value = updatedCategories
            updateFirebaseCategories(updatedCategories)
        }
    }

    fun reorderCategories(categoriesToMove: List<CategoriesTabelle>) {
        viewModelScope.launch {
            val updatedCategories =
                reorderMultipleCategories(_categorieList.value, categoriesToMove)
            _categorieList.value = updatedCategories
            updateFirebaseCategories(updatedCategories)
        }
    }

    fun moveCategory(categoryToMove: CategoriesTabelle, targetCategory: CategoriesTabelle) {
        viewModelScope.launch {
            val updatedCategories =
                moveSingleCategory(_categorieList.value, categoryToMove, targetCategory)
            _categorieList.value = updatedCategories
            updateFirebaseCategories(updatedCategories)
        }
    }

    fun toggleFilter() {
        _showOnlyWithFilter.value = !_showOnlyWithFilter.value
    }

    fun updateArticleDisponibility(articleId: Long, newDisponibilityState: String) {
        viewModelScope.launch {
            val updatedArticles = updateArticleDisponibilityState(
                _articlesList.value,
                articleId,
                newDisponibilityState
            )
            _articlesList.value = updatedArticles
            refClassmentsArtData.child(articleId.toString()).child("diponibilityState")
                .setValue(newDisponibilityState).await()
        }
    }

    // Helper functions
    private fun updateArticlesRanking(
        articles: List<ClassementsArticlesTabel>,
        categories: List<CategoriesTabelle>
    ): List<ClassementsArticlesTabel> {
        return articles.groupBy { it.idCategorie.toLong() }
            .flatMap { (_, categoryArticles) ->
                categoryArticles.sortedWith(
                    compareBy(
                        { it.classementCate },
                        { it.classementArticleAuCategorieCT })
                )
                    .mapIndexed { index, article ->
                        article.copy(
                            classementArticleAuCategorieCT = (index + 1).toDouble(),
                            classementCate = (index + 1).toDouble()
                        )
                    }
            }
    }

    private suspend fun updateFirebaseArticles(articles: List<ClassementsArticlesTabel>) {
        articles.forEach { article ->
            refClassmentsArtData.child(article.idArticle.toString()).setValue(article)
        }
    }


    suspend fun updateChangeInClassmentToeDBJetPackExport(onProgress: (Float) -> Unit) {
        val articles = articlesList.value
        val categories = categorieList.value

        val totalItems = articles.size
        var processedItems = 0

        articles.forEach { article ->
            val category =
                categories.find { it.idCategorieInCategoriesTabele == article.idCategorie.toLong() }
            database.getReference("e_DBJetPackExport")
                .child(article.idArticle.toString())
                .updateChildren(
                    mapOf(
                        "idCategorie" to (category?.idClassementCategorieInCategoriesTabele ?: 0.0),
                        "classementCate" to article.classementArticleAuCategorieCT,
                        "diponibilityState" to article.diponibilityState
                    )
                ).await()

            processedItems++
            onProgress(processedItems.toFloat() / totalItems)
        }
    }

    private fun createCategoriesFromArticles(articles: List<ClassementsArticlesTabel>): List<CategoriesTabelle> {
        val maxExistingId = articles.maxOfOrNull { it.idCategorie } ?: 0.0

        val defaultCategories = listOf(
            CategoriesTabelle(
                idCategorieInCategoriesTabele = (maxExistingId + 1).toLong(),
                idClassementCategorieInCategoriesTabele = maxExistingId + 1,
                nomCategorieInCategoriesTabele = "Consmitiques"
            ),
            CategoriesTabelle(
                idCategorieInCategoriesTabele = (maxExistingId + 2).toLong(),
                idClassementCategorieInCategoriesTabele = maxExistingId + 2,
                nomCategorieInCategoriesTabele = "Confiseries"
            ),
            CategoriesTabelle(
                idCategorieInCategoriesTabele = (maxExistingId + 3).toLong(),
                idClassementCategorieInCategoriesTabele = maxExistingId + 3,
                nomCategorieInCategoriesTabele = "TeBnages"
            )
        )


        val articleCategories = articles.groupBy { it.nomCategorie }
            .map { (nomCategorie, categoryArticles) ->
                CategoriesTabelle(
                    idCategorieInCategoriesTabele = categoryArticles.firstOrNull()?.idCategorie?.toLong()
                        ?: 0,
                    idClassementCategorieInCategoriesTabele = categoryArticles.firstOrNull()?.idCategorie
                        ?: 0.0,
                    nomCategorieInCategoriesTabele = nomCategorie
                )
            }

        return (defaultCategories + articleCategories).sortedBy { it.idClassementCategorieInCategoriesTabele }
    }

    private suspend fun updateFirebaseCategories(categories: List<CategoriesTabelle>) {
        refCategorieTabelee.removeValue().await()
        categories.forEach { category ->
            refCategorieTabelee.child(category.idCategorieInCategoriesTabele.toString())
                .setValue(category).await()
        }
    }

    private suspend fun updateArticlesWithNewCategoryIds(articles: List<ClassementsArticlesTabel>) {
        articles.forEach { article ->
            article.classementInCategoriesCT = article.idCategorie
            article.classementArticleAuCategorieCT = article.classementCate
            refClassmentsArtData.child(article.idArticle.toString()).setValue(article).await()
        }
    }

    private fun reorderCategories(
        categories: List<CategoriesTabelle>,
        fromCategoryId: Long,
        toCategoryId: Long
    ): List<CategoriesTabelle> {
        val mutableCategories = categories.toMutableList()
        val fromIndex =
            mutableCategories.indexOfFirst { it.idCategorieInCategoriesTabele == fromCategoryId }
        val toIndex =
            mutableCategories.indexOfFirst { it.idCategorieInCategoriesTabele == toCategoryId }

        if (fromIndex != -1 && toIndex != -1) {
            val movedCategory = mutableCategories.removeAt(fromIndex)
            mutableCategories.add(toIndex, movedCategory)
            return mutableCategories.mapIndexed { index, category ->
                category.copy(idClassementCategorieInCategoriesTabele = (index + 1).toDouble())
            }
        }
        return categories
    }

    private fun reorderMultipleCategories(
        currentCategories: List<CategoriesTabelle>,
        categoriesToMove: List<CategoriesTabelle>
    ): List<CategoriesTabelle> {
        val mutableCategories = currentCategories.toMutableList()
        val firstSelectedCategoryIndex =
            mutableCategories.indexOfFirst { it.idCategorieInCategoriesTabele == categoriesToMove.first().idCategorieInCategoriesTabele }

        if (firstSelectedCategoryIndex != -1) {
            mutableCategories.removeAll(categoriesToMove)
            mutableCategories.addAll(firstSelectedCategoryIndex, categoriesToMove)
            return mutableCategories.mapIndexed { index, category ->
                category.copy(idClassementCategorieInCategoriesTabele = (index + 1).toDouble())
            }
        }
        return currentCategories
    }

    private fun moveSingleCategory(
        currentCategories: List<CategoriesTabelle>,
        categoryToMove: CategoriesTabelle,
        targetCategory: CategoriesTabelle
    ): List<CategoriesTabelle> {
        val mutableCategories = currentCategories.toMutableList()
        val categoryToMoveIndex =
            mutableCategories.indexOfFirst { it.idCategorieInCategoriesTabele == categoryToMove.idCategorieInCategoriesTabele }
        val targetCategoryIndex =
            mutableCategories.indexOfFirst { it.idCategorieInCategoriesTabele == targetCategory.idCategorieInCategoriesTabele }

        if (categoryToMoveIndex != -1 && targetCategoryIndex != -1) {
            val movedCategory = mutableCategories.removeAt(categoryToMoveIndex)
            mutableCategories.add(targetCategoryIndex, movedCategory)
            return mutableCategories.mapIndexed { index, category ->
                category.copy(idClassementCategorieInCategoriesTabele = (index + 1).toDouble())
            }
        }
        return currentCategories
    }

    private fun updateArticleDisponibilityState(
        articles: List<ClassementsArticlesTabel>,
        articleId: Long,
        newDisponibilityState: String
    ): List<ClassementsArticlesTabel> {
        return articles.map { article ->
            if (article.idArticle == articleId) article.copy(diponibilityState = newDisponibilityState) else article
        }
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

data class CategoriesTabelle(
    val idCategorieInCategoriesTabele: Long = 0,
    var idClassementCategorieInCategoriesTabele: Double = 0.0,
    val nomCategorieInCategoriesTabele: String = "",
)
