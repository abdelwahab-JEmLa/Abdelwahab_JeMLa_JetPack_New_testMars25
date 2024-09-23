package h_FactoryClassemntsArticles

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import b_Edite_Base_Donne.LoadImageFromPath
import com.google.firebase.database.*
import kotlinx.coroutines.flow.*
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
        LazyColumn(
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
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(230.dp)
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
                                modifier = Modifier.size(64.dp),
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
                                modifier = Modifier.size(64.dp),
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Catégorie: ${article.nomCategorie}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Classement: ${article.classementCate}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Disponible: ${if (article.diponibilityState == "") "Oui" else "Non"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Switch(
                    checked = article.diponibilityState == "",
                    onCheckedChange = { onDisponibilityChange(if (it) "" else "Non Dispo") }
                )
            }
        }
    }
}

class ClassementsArticlesViewModel : ViewModel() {
    private val _articlesList = MutableStateFlow<List<ClassementsArticlesTabel>>(emptyList())
    private val _showOnlyWithFilter = MutableStateFlow(false)
    private val database = FirebaseDatabase.getInstance()
    private val refClassmentsArtData = database.getReference("e_DBJetPackExport")

    val articlesList: StateFlow<List<ClassementsArticlesTabel>> = combine(_articlesList, _showOnlyWithFilter) { articles, filterKey ->
        if (filterKey) {
            articles.filter { it.diponibilityState == "" }
        } else {
            articles
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val showOnlyWithFilter: StateFlow<Boolean> = _showOnlyWithFilter.asStateFlow()

    init {
        initDataFromFirebase()
    }

    private fun initDataFromFirebase() {
        refClassmentsArtData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _articlesList.value = dataSnapshot.children.mapNotNull { it.getValue(ClassementsArticlesTabel::class.java) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("ClassementsArticlesVM", "Error loading data", databaseError.toException())
            }
        })
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
    val classementCate: Double = 0.0,
    val idCategorie: Double = 0.0,
    val nomCategorie: String = "",
    val lastUpdateState: String = "",
    val diponibilityState: String = "",
) {
    constructor() : this(0)
}
