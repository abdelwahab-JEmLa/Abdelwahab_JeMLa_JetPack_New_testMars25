package A_Learn.A_Main_Ui

import A_Learn.Edite_Base_Donne.ArticleDao
import a_RoomDB.BaseDonne
import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainAppViewModel(private val articleDao: ArticleDao) : ViewModel() {
    private val refFirebase = Firebase.database.getReference("d_db_jetPack")

    private val _articlesBaseDonne = mutableListOf<BaseDonne>().toMutableStateList()
    val articlesBaseDonne: List<BaseDonne>
        get() = _articlesBaseDonne

    init {
        initBaseDonne()
    }

    private fun initBaseDonne() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val articles = articleDao.getAllArticlesOrder()
                withContext(Dispatchers.Main) {
                    _articlesBaseDonne.addAll(articles)
                }
            } catch (e: Exception) {
                Log.e("MainAppViewModel", "Failed to initialize articles", e)
            }
        }
    }

    fun syncWithFirebase(article: BaseDonne, remove: Boolean = false) {
        val taskRef = refFirebase.child(article.idArticle.toString())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (remove) {
                    taskRef.removeValue().await()
                } else {
                    taskRef.setValue(article).await()
                }
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

                withContext(Dispatchers.Main) {
                    _articlesBaseDonne.clear()
                    _articlesBaseDonne.addAll(sortedArticles)
                }
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

class MainAppViewModelFactory(
    private val articleDao: ArticleDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainAppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainAppViewModel(articleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
