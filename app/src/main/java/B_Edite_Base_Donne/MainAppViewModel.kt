package B_Edite_Base_Donne

import a_RoomDB.BaseDonne
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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

    fun changeColumeValue(article: BaseDonne, newValue: String) {
        val newList = _articlesBaseDonne.value.map {
            if (it.idArticle == article.idArticle)
                it.copy(monPrixVent = newValue.toDoubleOrNull() ?: it.monPrixVent)
            else
                it
        }
        _articlesBaseDonne.value = newList
        updateArticle(article)
    }
    fun changeColumemonBenficeValue(article: BaseDonne, newValue: String) {
        val newList = _articlesBaseDonne.value.map {
            if (it.idArticle == article.idArticle)
                it.copy(monBenfice = newValue.toDoubleOrNull() ?: it.monBenfice)
            else
                it
        }
        _articlesBaseDonne.value = newList
        updateArticle(article)
    }
    fun updateArticle(article: BaseDonne, remove: Boolean = false) {
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
