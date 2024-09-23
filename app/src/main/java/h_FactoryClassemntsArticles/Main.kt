package h_FactoryClassemntsArticles

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MainFactoryClassementsArticles(
    viewModel: ClassementsArticlesViewModel,
    onToggleNavBar: () -> Unit
) {
    val articles by viewModel.articlesList.collectAsState()
    val showOnlyWithFilter by viewModel.showOnlyWithFilter.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(false) }
    var holdedIdCateForMove by remember { mutableStateOf(Double) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButtons(
                showFloatingButtons = showFloatingButtons,
                onToggleNavBar = onToggleNavBar,
                onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                onToggleFilter = viewModel::toggleFilter,
                showOnlyWithFilter = showOnlyWithFilter
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(articles) { article ->
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

@Composable
fun FloatingActionButtons(
    showFloatingButtons: Boolean,
    onToggleNavBar: () -> Unit,
    onToggleFloatingButtons: () -> Unit,
    onToggleFilter: () -> Unit,
    showOnlyWithFilter: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End
    ) {
        if (showFloatingButtons) {
            FloatingActionButton(
                onClick = onToggleNavBar,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Home, "Toggle Navigation Bar")
            }
            FloatingActionButton(
                onClick = onToggleFilter,
                containerColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    if (showOnlyWithFilter) Icons.Default.FilterList else Icons.Default.FilterListOff,
                    "Toggle Filter"
                )
            }
        }
        FloatingActionButton(
            onClick = onToggleFloatingButtons,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                if (showFloatingButtons) "Hide Buttons" else "Show Buttons"
            )
        }
    }
}

@Composable
fun ArticleItem(
    article: ClassementsArticlesTabel,
    onDisponibilityChange: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
            ) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                LoadImageFromPath(
                    imagePath = imagePath,
                    modifier = Modifier.clickable {
                        val newDisponibilityState = when (article.diponibilityState) {
                            "" -> "Non Dispo"
                            "Non Dispo" -> "NonForNewsClients"
                            "NonForNewsClients" -> ""
                            else -> ""
                        }
                        onDisponibilityChange(newDisponibilityState)
                    }
                )

                when (article.diponibilityState) {
                    "Non Dispo" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TextDecrease,
                                "Not Available For all",
                                modifier = Modifier,
                                tint = Color.White
                            )
                        }
                    }
                    "NonForNewsClients" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                "Not Available For New Clients",
                                modifier = Modifier,
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            Text(
                text = article.nomArticleFinale,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = article.nomCategorie,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${article.classementIdAuCate}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
class ClassementsArticlesViewModel : ViewModel() {
    private val _articlesList = MutableStateFlow<List<ClassementsArticlesTabel>>(emptyList())
    private val _categorieList = MutableStateFlow<List<CategorieTabelee>>(emptyList())
    private val _showOnlyWithFilter = MutableStateFlow(false)
    private val database = FirebaseDatabase.getInstance()
    private val refClassmentsArtData = database.getReference("BaseDonne_Bakup3")
    private val refCategorieTabelee = database.getReference("H_CategorieTabele")

    val articlesList: StateFlow<List<ClassementsArticlesTabel>> = combine(_articlesList, _showOnlyWithFilter) { articles, filterKey ->
        if (filterKey) {
            articles.filter { it.diponibilityState == "" }
        } else {
            articles
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categorieList: StateFlow<List<CategorieTabelee>> = _categorieList.asStateFlow()
    val showOnlyWithFilter: StateFlow<Boolean> = _showOnlyWithFilter.asStateFlow()

    init {
        viewModelScope.launch {
            initDataFromFirebase()
            updateCategorieTabelee()
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            val articlesSnapshot = refClassmentsArtData.get().await()
            _articlesList.value = articlesSnapshot.children.mapNotNull { it.getValue(ClassementsArticlesTabel::class.java) }
                .sortedWith(compareBy<ClassementsArticlesTabel> { it.idCategorie }.thenBy { it.classementIdAuCate })

            val categoriesSnapshot = refCategorieTabelee.get().await()
            _categorieList.value = categoriesSnapshot.children.mapNotNull { it.getValue(CategorieTabelee::class.java) }
                .sortedBy { it.idClassementCategorie }
        } catch (e: Exception) {
            Log.e("ClassementsArticlesVM", "Error loading data", e)
        }
    }

    private suspend fun updateCategorieTabelee() {
        try {
            val categories = _articlesList.value
                .groupBy { it.nomCategorie }
                .map { (nomCategorie, articles) ->
                    CategorieTabelee(
                        vid = articles.firstOrNull()?.idCategorie?.toLong() ?: 0,
                        idClassementCategorie = articles.firstOrNull()?.idCategorie ?: 0.0,
                        nomCategorie = nomCategorie
                    )
                }
                .sortedBy { it.idClassementCategorie }

            _categorieList.value = categories

            categories.forEach { category ->
                refCategorieTabelee.child(category.vid.toString()).setValue(category).await()
            }
            Log.d("ClassementsArticlesVM", "CategorieTabelee updated successfully")
        } catch (e: Exception) {
            Log.e("ClassementsArticlesVM", "Error updating CategorieTabelee", e)
        }
    }

    fun toggleFilter() {
        _showOnlyWithFilter.value = !_showOnlyWithFilter.value
    }

    fun updateArticleDisponibility(articleId: Long, newDisponibilityState: String) {
        updateFirebase(articleId) { article ->
            article.copy(diponibilityState = newDisponibilityState)
        }
    }

    private fun updateFirebase(articleId: Long, update: (ClassementsArticlesTabel) -> ClassementsArticlesTabel) {
        viewModelScope.launch {
            val updatedArticles = _articlesList.value.map { article ->
                if (article.idArticle == articleId) update(article) else article
            }

            _articlesList.value = updatedArticles

            val updatedArticle = updatedArticles.find { it.idArticle == articleId }
            updatedArticle?.let {
                try {
                    refClassmentsArtData.child(articleId.toString()).setValue(it).await()
                    Log.d("ClassementsArticlesVM", "Article updated successfully in Firebase")
                } catch (e: Exception) {
                    Log.e("ClassementsArticlesVM", "Error updating article in Firebase", e)
                }
            }
        }
    }
}
data class ClassementsArticlesTabel(
    val idArticle: Long = 0,
    val nomArticleFinale: String = "",
    val idCategorie: Double = 0.0,
    val nomCategorie: String = "",
    val classementIdAuCate: Double = 0.0,
    val lastUpdateState: String = "",
    val diponibilityState: String = "",
) {
    constructor() : this(0)
}
data class CategorieTabelee(
    val vid: Long = 0,
    val idClassementCategorie: Double = 0.0,
    val nomCategorie: String = "",
) {
    constructor() : this(0)
}
