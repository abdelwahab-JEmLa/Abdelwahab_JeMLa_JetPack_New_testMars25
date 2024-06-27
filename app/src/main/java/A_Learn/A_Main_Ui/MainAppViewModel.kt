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
            _articlesBaseDonne.addAll(articlesFromFirebase.sortedWith(compareBy<BaseDonne> { it.a_x_idcategorie }.thenBy { it.a_b_classementc }))
        }
    }

    private fun syncWithFirebase(article: BaseDonne, remove: Boolean = false) {
        val taskRef = refFirebase.child(article.a_c_idarticle_c.toString())
        if (remove) {
            taskRef.removeValue()
        } else {
            taskRef.setValue(article)
        }
    }
}

data class BaseDonne(
    val a_c_idarticle_c: Int = 0,
    var a_d_nomarticlefinale_c: String = "",
    var a_b_classementc: Double = 0.0,
    val a_e_nomarab_c: String = "",
    val a_f_nombrcat_c: Int = 0,
    var a_g_cat1_c: String? = null,
    var a_h_cat2_c: String? = null,
    var a_i_cat3_c: String? = null,
    var a_j_cat4_c: String? = null,
    var a_k_catego_c: String? = null,
    val a_l_nmbunite_c: Int = 0,
    val a_m_nmbucaron_c: Int = 0,
    var a_n_affichageu_c: Boolean = false,
    val a_o_commment_se_vent_c: String? = null,
    val a_p_affiche_boit_si_uniter_sidispo_c: String? = null,
    var a_q_prixachat_c: Double = 0.0,
    var a_r_prixdevent_c: Double = 0.0,
    val a_s_quan__1_c: Int = 0,
    var a_t_benfice_prix_1_q1_c: Double = 0.0,
    var a_u_prix_1_q1_c: Double = 0.0,
    var a_v_nomvocale: String = "",
    val a_w_idcatalogue_categorie: String = "",
    var a_x_idcategorie: Double = 0.0,
    var funChangeImagsDimention: Boolean = false,
    var a_z_namecate: String = "",
    var b_a_idcatalogue: Double = 0.0,
    val b_b_idcatalogueac0: String = "",
    var b_c_namecatalogue: String = "",
    val b_d_datecreationcategorie: String = ""
)
