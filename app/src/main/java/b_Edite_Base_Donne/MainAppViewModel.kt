package b_Edite_Base_Donne

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
import kotlin.reflect.KMutableProperty1

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

    fun <T : Any> updateViewModelWhithCalulationColumes(
        newValue: String?,
        article: BaseDonne,
        nomColonne: KMutableProperty1<BaseDonne, T>,
        type: (String) -> T?,
    ) {
        val newValueTyped = newValue?.let(type)
        if (newValueTyped != null) {
            nomColonne.set(article, newValueTyped)
        }

        val monPrixAchat = article.monPrixAchat.toDouble()
        when (nomColonne) {
            BaseDonne::monPrixVent -> {
                val newBenfice = (newValueTyped as? Number)?.toDouble()?.minus(monPrixAchat)
                if (newBenfice != null) {
                    article.monBenfice = newBenfice
                }
            }
            BaseDonne::monBenfice -> {
                val newPrixVent = (newValueTyped as? Number)?.toDouble()?.plus(monPrixAchat)
                if (newPrixVent != null) {
                    article.monPrixVent = newPrixVent
                }
            }
        }

        _articlesBaseDonne.value = _articlesBaseDonne.value.map {
            if (it.idArticle == article.idArticle) article else it
        }
        updateArticle(article)

    }

    fun updateArticle(article: BaseDonne, remove: Boolean = false) {
        val taskRef = refFirebase.child(article.idArticle.toString())
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (remove) {
                    taskRef.removeValue().await()
                    articleDao.delete(article.idArticle)
                } else {
                    taskRef.setValue(article).await()
                    articleDao.insert(article)
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