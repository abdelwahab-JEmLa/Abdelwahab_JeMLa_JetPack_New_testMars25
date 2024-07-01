package B_Edite_Base_Donne

import a_RoomDB.BaseDonne
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainAppViewModel(private val articleDao: ArticleDao) : ViewModel() {
    private val refFirebase = Firebase.database.getReference("d_db_jetPack")

    private val _articlesBaseDonne = MutableStateFlow<List<BaseDonne>>(emptyList())
    val articlesBaseDonne: StateFlow<List<BaseDonne>> = _articlesBaseDonne.asStateFlow()

    init {
        initBaseDonne()
    }

    private fun initBaseDonne() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val articles = articleDao.getAllArticlesOrder()
                _articlesBaseDonne.value = articles
            } catch (e: Exception) {
                Log.e("MainAppViewModel", "Failed to initialize articles", e)
            }
        }
    }

    fun updateArticle(article: BaseDonne) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                articleDao.update(article)
                val updatedArticles = articleDao.getAllArticlesOrder()
                _articlesBaseDonne.value = updatedArticles
            } catch (e: Exception) {
                Log.e("MainAppViewModel", "Failed to update article", e)
            }
        }
    }

    fun updateOrDelete(article: BaseDonne, remove: Boolean = false) {
        val taskRef = refFirebase.child(article.idArticle.toString())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!remove) {
                    taskRef.setValue(article).await()
                    articleDao.insert(article)
                } else {
                    taskRef.removeValue().await()
                    articleDao.delete(article.idArticle)
                }
                val updatedArticles = articleDao.getAllArticlesOrder()
                _articlesBaseDonne.value = updatedArticles
            } catch (e: Exception) {
                Log.e("MainAppViewModel", "Failed to sync with Firebase", e)
            }
        }
    }

    fun importFromFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dataSnapshot = refFirebase.get().await()
                val articlesFromFirebase = parseDataSnapshot(dataSnapshot)
                val sortedArticles = articlesFromFirebase.sortedWith(compareBy<BaseDonne> { it.idCategorie }.thenBy { it.classementCate })

                articleDao.deleteAll()
                articleDao.insertAll(sortedArticles)

                _articlesBaseDonne.value = sortedArticles
            } catch (e: Exception) {
                Log.e("MainAppViewModel", "Failed to import data from Firebase", e)
            }
        }
    }

    private fun parseDataSnapshot(dataSnapshot: DataSnapshot): List<BaseDonne> {
        val articles = mutableListOf<BaseDonne>()
        for (child in dataSnapshot.children) {
            val article = child.getValue(BaseDonne::class.java)
            if (article != null) {
                articles.add(article)
            } else {
                Log.e("MainAppViewModel", "Failed to parse article from snapshot: $child")
            }
        }
        return articles
    }
}

class MainAppViewModelFactory(private val articleDao: ArticleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainAppViewModel(articleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
