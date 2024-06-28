package com.example.mycomposeapp.ui

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainAppViewModel : ViewModel() {
    private val database = Firebase.database
    private val refFirebase = database.getReference("d_db_jetPack")

    private val _articlesBaseDonne = mutableListOf<BaseDonne>().toMutableStateList()
    val articlesBaseDonne: List<BaseDonne>
        get() = _articlesBaseDonne

    init {
        importFromFirebase()
    }

    private fun importFromFirebase() {
        viewModelScope.launch(Dispatchers.IO) {
            val dataSnapshot = refFirebase.get().await()
            val articlesFromFirebase = dataSnapshot.children.mapNotNull { it.getValue(BaseDonne::class.java) }
            _articlesBaseDonne.clear()
            _articlesBaseDonne.addAll(articlesFromFirebase.sortedWith(compareBy<BaseDonne> { it.idCategorie }.thenBy { it.classementCate }))
        }
    }

    private fun syncWithFirebase(article: BaseDonne, remove: Boolean = false) {
        val taskRef = refFirebase.child(article.idArticle.toString())
        if (remove) {
            taskRef.removeValue()
        } else {
            taskRef.setValue(article)
        }
    }
}

data class BaseDonne(
    val idArticle: Int = 0,
    var nomArticleFinale: String = "",
    var classementCate: Double = 0.0,
    val nomArab: String = "",
    val nmbrCat: Int = 0,
    var couleur1: String? = null,
    var couleur2: String? = null,
    var couleur3: String? = null,
    var couleur4: String? = null,
    var nomCategorie2: String? = null,
    val nmbrUnite: Int = 0,
    val nmbrCaron: Int = 0,
    var affichageUniteState: Boolean = false,
    val commmentSeVent: String? = null,
    val afficheBoitSiUniter: String? = null,
    var monPrixAchat: Double = 0.0,
    var clienPrixVentUnite: Double = 0.0,
    val minQuan: Int = 0,
    var monBenfice: Double = 0.0,
    var monPrixVent: Double = 0.0,
    var diponibilityState: String = "",
    val neaon2: String = "",
    var idCategorie: Double = 0.0,
    var funChangeImagsDimention: Boolean = false,
    var nomCategorie: String = "",
    var neaon1: Double = 0.0,
    val lastUpdateState: String = "",
    var cartonState: String = "",
    val dateCreationCategorie: String = ""
)
