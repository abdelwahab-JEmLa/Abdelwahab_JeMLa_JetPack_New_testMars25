package h_FactoryClassemntsArticles

import android.util.Log
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
    var showFloatingButtons by remember { mutableStateOf(true) }
    var holdedIdCateForMove by remember { mutableStateOf<Long?>(null) }

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
                        val index = categories.indexOfFirst { it.idCategorieCT == selectedCategory.idCategorieCT }
                        if (index != -1) {
                            val position = categories.take(index).sumOf { category ->
                                1 + articles.count { it.idCategorie == category.idCategorieCT.toDouble() }
                            }
                            gridState.scrollToItem(position)
                        }
                    }
                },
                onUpdateClassement = {
                    coroutineScope.launch {
                        viewModel.updateClassementIdAuTotale()   //TODO ajoute une progress bare s affiche au nav bar suive
                    }
                }   ,
                coroutineScope=coroutineScope  ,

            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = gridState,
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
                                viewModel.goUpAndshiftsAutersDownCategoryPositions(holdedIdCateForMove!!, clickedCategory.idCategorieCT)
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

class ClassementsArticlesViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val refClassmentsArtData = database.getReference("H_ClassementsArticlesTabel")
    private val refCategorieTabelee = database.getReference("H_CategorieTabele")

    private val _articlesList = MutableStateFlow<List<ClassementsArticlesTabel>>(emptyList())
    private val _categorieList = MutableStateFlow<List<CategorieTabelee>>(emptyList())
    private val _showOnlyWithFilter = MutableStateFlow(false)



    val articlesList = combine(_articlesList, _categorieList, _showOnlyWithFilter) { articles, categories, filterKey ->
        val sortedArticles = articles.sortedWith(
            compareBy<ClassementsArticlesTabel> { article ->
                categories.find { it.idCategorieCT == article.idCategorie.toLong() }?.idClassementCategorieCT ?: Double.MAX_VALUE
            }.thenBy { it.classementIdAuCate }
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

            // Create a mutable copy of the articles list to update
            val updatedArticles = _articlesList.value.toMutableList()

            // Function to update the classementIdAuCate for a given category and its subcategories
            fun updateClassementIdAuCate(categoryId: Long, increment: Int, visitedCategories: MutableSet<Long> = mutableSetOf()) {
                // Check if the category has already been visited to prevent infinite recursion
                if (visitedCategories.contains(categoryId)) {
                    return
                }
                visitedCategories.add(categoryId)

                // Find all articles that belong to the given category
                val categoryArticles = updatedArticles.filter { it.idCategorie.toLong() == categoryId }

                // Update the classementIdAuCate for each article in the category
                categoryArticles.forEach { article ->
                    val currentClassementIdAuCate = article.classementIdAuCate
                    article.classementIdAuCate = currentClassementIdAuCate + increment
                }

                // Find all subcategories of the given category
                val subCategories = _categorieList.value.filter { it.idClassementCategorieCT.toLong() == categoryId }

                // Recursively update the classementIdAuCate for each subcategory
                subCategories.forEach { subCategory ->
                    updateClassementIdAuCate(subCategory.idCategorieCT, increment, visitedCategories)
                }
            }

            // Start the update process from the root categories
            val rootCategories = _categorieList.value.filter { it.idClassementCategorieCT.toLong() == 0L }
            rootCategories.forEach { rootCategory ->
                updateClassementIdAuCate(rootCategory.idCategorieCT, 1)
            }

            // Update the articles list with the updated data
            _articlesList.value = updatedArticles

            // Persist the changes back to Firebase
            updatedArticles.forEach { article ->
                refClassmentsArtData.child(article.idArticle.toString()).setValue(article)
            }
        }
    }    fun updateClassementIdAuTotale() {
        viewModelScope.launch {

            val sortedArticles = articlesList.value
            val updatedArticles = sortedArticles.mapIndexed { index, article ->
                article.copy(classementCate = (index + 1).toDouble())
            }
            _articlesList.value = updatedArticles

            // Update Firebase
            updatedArticles.forEach { article ->
                refClassmentsArtData.child(article.idArticle.toString())
                    .child("classementCate")
                    .setValue(article.classementCate)
                    .await()
            }

            updatee_DBJetPackExportParBaseDonne_Bakup3()

        }
    }

    private fun updatee_DBJetPackExportParBaseDonne_Bakup3() {
        viewModelScope.launch {
            val updatedArticles = articlesList.value
            updatedArticles.forEach { article ->
                database.getReference("e_DBJetPackExport")
                    .child(article.idArticle.toString())
                    .updateChildren(mapOf(
                        "idCategorie" to article.idCategorie,
                        "classementCate" to article.classementIdAuCate
                    )).await()
            }
        }
    }
    private suspend fun initDataFromFirebase() {
        try {
            val articlesSnapshot = refClassmentsArtData.get().await()
            _articlesList.value = articlesSnapshot.children.mapNotNull { it.getValue(ClassementsArticlesTabel::class.java) }

            val categoriesSnapshot = refCategorieTabelee.get().await()
            _categorieList.value = categoriesSnapshot.children.mapNotNull { it.getValue(CategorieTabelee::class.java) }
                .sortedBy { it.idClassementCategorieCT }
        } catch (e: Exception) {
            Log.e("ClassementsArticlesVM", "Error loading data", e)
        }
    }
    suspend fun updateCategorieTabelee() {
        try {
            val categories = _articlesList.value
                .groupBy { it.nomCategorie }
                .map { (nomCategorie, articles) ->
                    CategorieTabelee(
                        idCategorieCT = articles.firstOrNull()?.idCategorie?.toLong() ?: 0,
                        idClassementCategorieCT = articles.firstOrNull()?.idCategorie ?: 0.0,
                        nomCategorieCT = nomCategorie
                    )
                }
                .sortedBy { it.idClassementCategorieCT }

            _categorieList.value = categories
            refCategorieTabelee.removeValue().await()
            categories.forEach { category ->
                refCategorieTabelee.child(category.idCategorieCT.toString()).setValue(category).await()
            }
            Log.d("ClassementsArticlesVM", "CategorieTabelee updated successfully")
        } catch (e: Exception) {
            Log.e("ClassementsArticlesVM", "Error updating CategorieTabelee", e)
        }
    }
    fun delete() {
        viewModelScope.launch {
            refClassmentsArtData.removeValue().await()
        }
    }
    fun goUpAndshiftsAutersDownCategoryPositions(fromCategoryId: Long, toCategoryId: Long) {
        viewModelScope.launch {
            val updatedCategories = _categorieList.value.toMutableList()
            val fromIndex = updatedCategories.indexOfFirst { it.idCategorieCT == fromCategoryId }
            val toIndex = updatedCategories.indexOfFirst { it.idCategorieCT == toCategoryId }

            if (fromIndex != -1 && toIndex != -1) {
                val movedCategory = updatedCategories.removeAt(fromIndex)
                updatedCategories.add(toIndex, movedCategory)

                // Update idClassementCategorieCT for all affected categories
                updatedCategories.forEachIndexed { index, category ->
                    category.idClassementCategorieCT = (index + 1).toDouble()
                }

                _categorieList.value = updatedCategories

                // Fix: Call updateFirebaseCategories
                updateFirebaseCategories(updatedCategories)
            }
        }
    }

    // New method to update Firebase categories
    private suspend fun updateFirebaseCategories(categories: List<CategorieTabelee>) {
        categories.forEach { category ->
                refCategorieTabelee.child(category.idCategorieCT.toString()).setValue(category).await()
        }
    }
    fun reorderCategories(categoriesToMove: List<CategorieTabelee>) {
        viewModelScope.launch {
            val currentCategories = _categorieList.value.toMutableList()
            val firstSelectedCategoryIndex = currentCategories.indexOfFirst { it.idCategorieCT == categoriesToMove.first().idCategorieCT }

            if (firstSelectedCategoryIndex != -1) {
                // Remove the categories to be moved
                currentCategories.removeAll(categoriesToMove)
                // Insert them at the new position
                currentCategories.addAll(firstSelectedCategoryIndex, categoriesToMove)

                // Update idClassementCategorieCT for all categories
                currentCategories.forEachIndexed { index, category ->
                    category.idClassementCategorieCT = (index + 1).toDouble()
                }

                // Update the state
                _categorieList.value = currentCategories

                // Update Firebase
                updateFirebaseCategories(currentCategories)
            }
        }
    }



    fun moveCategory(categoryToMove: CategorieTabelee, targetCategory: CategorieTabelee) {
        viewModelScope.launch {
            val currentCategories = _categorieList.value.toMutableList()
            val categoryToMoveIndex = currentCategories.indexOfFirst { it.idCategorieCT == categoryToMove.idCategorieCT }
            val targetCategoryIndex = currentCategories.indexOfFirst { it.idCategorieCT == targetCategory.idCategorieCT }

            if (categoryToMoveIndex != -1 && targetCategoryIndex != -1) {
                // Remove the category to be moved
                val movedCategory = currentCategories.removeAt(categoryToMoveIndex)
                // Insert it at the new position
                currentCategories.add(targetCategoryIndex, movedCategory)

                // Update idClassementCategorieCT for all categories
                currentCategories.forEachIndexed { index, category ->
                    category.idClassementCategorieCT = (index + 1).toDouble()
                }

                // Update the state
                _categorieList.value = currentCategories

                // Update Firebase
                updateFirebaseCategories(currentCategories)
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
    var classementIdAuCate: Double = 0.0,
    val classementCate: Double = 0.0,
    val diponibilityState: String = ""
)

data class CategorieTabelee(
    val idCategorieCT: Long = 0,
    var idClassementCategorieCT: Double = 0.0,
    val nomCategorieCT: String = ""
)
