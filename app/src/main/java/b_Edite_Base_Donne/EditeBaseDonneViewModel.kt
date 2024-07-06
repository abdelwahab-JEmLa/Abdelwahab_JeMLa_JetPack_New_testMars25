package b_Edite_Base_Donne

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditeBaseDonneViewModel(private val articleDao: ArticleDao) : ViewModel() {

    private val _baseDonneStatTabel = mutableStateListOf<BaseDonneStatTabel>()
    val baseDonneStatTabel: List<BaseDonneStatTabel> get() = _baseDonneStatTabel

    init {
        initBaseDonneStatTabel()
    }

    fun updateBaseDonneStatTabel(article: BaseDonneStatTabel, newValue: String?) {
        newValue?.let {
            viewModelScope.launch(Dispatchers.Main) {
                _baseDonneStatTabel.find { it.idArticle == article.idArticle }?.monPrixVent = (if (it == "") "0.0" else it).toDouble()
            }
        }
    }

    private fun initBaseDonneStatTabel() {
        viewModelScope.launch(Dispatchers.IO) {
            val articlesFromRoom = articleDao.getAllArticlesOrder()
            val baseDonneStatTabelList = articlesFromRoom.map {
                BaseDonneStatTabel(
                    it.idArticle,
                    it.nomArticleFinale,
                    it.classementCate.toDouble(),
                    it.nomArab,
                    it.nmbrCat,
                    it.couleur1,
                    it.couleur2,
                    it.couleur3,
                    it.couleur4,
                    it.nomCategorie2,
                    it.nmbrUnite,
                    it.nmbrCaron,
                    it.affichageUniteState,
                    it.commmentSeVent,
                    it.afficheBoitSiUniter,
                    it.monPrixAchat,
                    it.clienPrixVentUnite,
                    it.minQuan,
                    it.monBenfice,
                    it.monPrixVent,
                    it.diponibilityState,
                    it.neaon2,
                    it.idCategorie,
                    it.funChangeImagsDimention,
                    it.nomCategorie,
                    it.neaon1.toDouble(),
                    it.lastUpdateState,
                    it.cartonState,
                    it.dateCreationCategorie,
                    it.prixDeVentTotaleChezClient,
                    it.benficeTotaleEntreMoiEtClien,
                    it.benificeTotaleEn2,
                    it.monPrixAchatUniter,
                    it.monPrixVentUniter
                )
            }
            withContext(Dispatchers.Main) {
                _baseDonneStatTabel.clear()
                _baseDonneStatTabel.addAll(baseDonneStatTabelList)
            }
        }
    }
}

class BaseDonneStatTabel(
    idArticle: Int,
    nomArticleFinale: String = "",
    classementCate: Double = 0.0,
    nomArab: String = "",
    nmbrCat: Int = 0,
    couleur1: String? = null,
    couleur2: String? = null,
    couleur3: String? = null,
    couleur4: String? = null,
    nomCategorie2: String? = null,
    nmbrUnite: Int = 0,
    nmbrCaron: Int = 0,
    affichageUniteState: Boolean = false,
    commmentSeVent: String? = null,
    afficheBoitSiUniter: String? = null,
    monPrixAchat: Double = 0.0,
    clienPrixVentUnite: Double = 0.0,
    minQuan: Int = 0,
    monBenfice: Double = 0.0,
    monPrixVent: Double = 0.0,
    diponibilityState: String = "",
    neaon2: String = "",
    idCategorie: Double = 0.0,
    funChangeImagsDimention: Boolean = false,
    nomCategorie: String = "",
    neaon1: Double = 0.0,
    lastUpdateState: String = "",
    cartonState: String = "",
    dateCreationCategorie: String = "",
    prixDeVentTotaleChezClient: Double = 0.0,
    benficeTotaleEntreMoiEtClien: Double = 0.0,
    benificeTotaleEn2: Double = 0.0,
    monPrixAchatUniter: Double = 0.0,
    monPrixVentUniter: Double = 0.0,
) {
    var idArticle by mutableIntStateOf(idArticle)
    var nomArticleFinale by mutableStateOf(nomArticleFinale)
    var classementCate by mutableDoubleStateOf(classementCate)
    var nomArab by mutableStateOf(nomArab)
    var nmbrCat by mutableIntStateOf(nmbrCat)
    var couleur1 by mutableStateOf(couleur1)
    var couleur2 by mutableStateOf(couleur2)
    var couleur3 by mutableStateOf(couleur3)
    var couleur4 by mutableStateOf(couleur4)
    var nomCategorie2 by mutableStateOf(nomCategorie2)
    var nmbrUnite by mutableIntStateOf(nmbrUnite)
    var nmbrCaron by mutableIntStateOf(nmbrCaron)
    var affichageUniteState by mutableStateOf(affichageUniteState)
    var commmentSeVent by mutableStateOf(commmentSeVent)
    var afficheBoitSiUniter by mutableStateOf(afficheBoitSiUniter)
    var monPrixAchat by mutableDoubleStateOf(monPrixAchat)
    var clienPrixVentUnite by mutableDoubleStateOf(clienPrixVentUnite)
    var minQuan by mutableIntStateOf(minQuan)
    var monBenfice by mutableDoubleStateOf(monBenfice)
    var monPrixVent by mutableDoubleStateOf(monPrixVent)
    var diponibilityState by mutableStateOf(diponibilityState)
    var neaon2 by mutableStateOf(neaon2)
    var idCategorie by mutableDoubleStateOf(idCategorie)
    var funChangeImagsDimention by mutableStateOf(funChangeImagsDimention)
    var nomCategorie by mutableStateOf(nomCategorie)
    var neaon1 by mutableDoubleStateOf(neaon1)
    var lastUpdateState by mutableStateOf(lastUpdateState)
    var cartonState by mutableStateOf(cartonState)
    var dateCreationCategorie by mutableStateOf(dateCreationCategorie)
    var prixDeVentTotaleChezClient by mutableDoubleStateOf(prixDeVentTotaleChezClient)
    var benficeTotaleEntreMoiEtClien by mutableDoubleStateOf(benficeTotaleEntreMoiEtClien)
    var benificeTotaleEn2 by mutableDoubleStateOf(benificeTotaleEn2)
    var monPrixAchatUniter by mutableDoubleStateOf(monPrixAchatUniter)
    var monPrixVentUniter by mutableDoubleStateOf(monPrixVentUniter)


    val nomArticleFinaleIndicator: Pair<String, String> get() = "nomArticleFinale" to "n.AF"
    val classementCateIndicator: Pair<String, String> get() = "classementCate" to "c.C"
    val nomArabIndicator: Pair<String, String> get() = "nomArab" to "n.A"
    val nmbrCatIndicator: Pair<String, String> get() = "nmbrCat" to "n.Cat"
    val couleur1Indicator: Pair<String, String> get() = "couleur1" to "c.1"
    val couleur2Indicator: Pair<String, String> get() = "couleur2" to "c.2"
    val couleur3Indicator: Pair<String, String> get() = "couleur3" to "c.3"
    val couleur4Indicator: Pair<String, String> get() = "couleur4" to "c.4"
    val nomCategorie2Indicator: Pair<String, String> get() = "nomCategorie2" to "n.Cat2"
    val nmbrUniteIndicator: Pair<String, String> get() = "nmbrUnite" to "n.U"
    val nmbrCaronIndicator: Pair<String, String> get() = "nmbrCaron" to "n.C"
    val affichageUniteStateIndicator: Pair<String, String> get() = "affichageUniteState" to "a.US"
    val commmentSeVentIndicator: Pair<String, String> get() = "commmentSeVent" to "c.SV"
    val afficheBoitSiUniterIndicator: Pair<String, String> get() = "afficheBoitSiUniter" to "a.BSU"
    val monPrixAchatIndicator: Pair<String, String> get() = "monPrixAchat" to "m.PA"
    val clienPrixVentUniteIndicator: Pair<String, String> get() = "clienPrixVentUnite" to "c.PVU"
    val minQuanIndicator: Pair<String, String> get() = "minQuan" to "m.Q"
    val monBenficeIndicator: Pair<String, String> get() = "monBenfice" to "m.B"
    val monPrixVentIndicator: Pair<String, String> get() = "monPrixVent" to "m.PV"
    val diponibilityStateIndicator: Pair<String, String> get() = "diponibilityState" to "d.S"
    val neaon2Indicator: Pair<String, String> get() = "neaon2" to "n.2"
    val idCategorieIndicator: Pair<String, String> get() = "idCategorie" to "i.C"
    val funChangeImagsDimentionIndicator: Pair<String, String> get() = "funChangeImagsDimention" to "f.CID"
    val nomCategorieIndicator: Pair<String, String> get() = "nomCategorie" to "n.Cat"
    val neaon1Indicator: Pair<String, String> get() = "neaon1" to "n.1"
    val lastUpdateStateIndicator: Pair<String, String> get() = "lastUpdateState" to "l.US"
    val cartonStateIndicator: Pair<String, String> get() = "cartonState" to "c.S"
    val dateCreationCategorieIndicator: Pair<String, String> get() = "dateCreationCategorie" to "d.CC"
    val prixDeVentTotaleChezClientIndicator: Pair<String, String> get() = "prixDeVentTotaleChezClient" to "p.VTCC"
    val benficeTotaleEntreMoiEtClienIndicator: Pair<String, String> get() = "benficeTotaleEntreMoiEtClien" to "b.TEMEC"
    val benificeTotaleEn2Indicator: Pair<String, String> get() = "benificeTotaleEn2" to "b.TE2"
    val monPrixAchatUniterIndicator: Pair<String, String> get() = "monPrixAchatUniter" to "m.PAU"
    val monPrixVentUniterIndicator: Pair<String, String> get() = "monPrixVentUniter" to "m.PVU"
}


class MainAppViewModelFactory(private val articleDao: ArticleDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditeBaseDonneViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditeBaseDonneViewModel(articleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
