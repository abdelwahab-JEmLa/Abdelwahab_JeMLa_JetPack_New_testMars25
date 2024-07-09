package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import androidx.compose.runtime.mutableStateListOf
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

    fun updateCalculated(
        textFieldValue: String,
        columnToChange: String,
        article: BaseDonneStatTabel,
    ) {
        val newValue = textFieldValue.toDoubleOrNull()

        if (newValue != null) {
            // Update the specified columns
            calculateWithoutCondition(columnToChange, textFieldValue, newValue, article)
            calculateWithCondition(columnToChange, newValue, article)
        }
    }

    private fun calculateWithoutCondition(
        columnToChange: String,
        textFieldValue: String,
        newValue: Double?,
        article: BaseDonneStatTabel
    ) {
        val updatedColumns = mutableListOf<Pair<String, String>>()

        updatedColumns.add(columnToChange to textFieldValue)
        calculateUnitPurchasePrice(article, updatedColumns)

        val monPrixVent = if (columnToChange == "monPrixVent") newValue else article.monPrixVent
        val monPrixVentUniterCal = monPrixVent?.div(article.nmbrUnite)
        updatedColumns.add("monPrixVentUniter" to monPrixVentUniterCal.toString())

        val clienPrixVentUniteCal = if (columnToChange == "clienPrixVentUnite") newValue else article.clienPrixVentUnite
        val prixDeVentTotaleChezClientCal = clienPrixVentUniteCal?.times(article.nmbrUnite)
        updatedColumns.add("prixDeVentTotaleChezClient" to prixDeVentTotaleChezClientCal.toString())

        val benficeTotaleEntreMoiEtClienCal = prixDeVentTotaleChezClientCal?.minus(article.monPrixAchat)
        updatedColumns.add("benficeTotaleEntreMoiEtClien" to benficeTotaleEntreMoiEtClienCal.toString())

        val benificeTotaleEn2Cal = benficeTotaleEntreMoiEtClienCal?.div(2)
        updatedColumns.add("benificeTotaleEn2" to benificeTotaleEn2Cal.toString())

        for ((column, value) in updatedColumns) {
            updateBaseDonneStatTabel(column, article, value)
        }
    }

    private fun calculateUnitPurchasePrice(
        article: BaseDonneStatTabel,
        updatedColumns: MutableList<Pair<String, String>>
    ) {
        val monPrixAchatUniterCal = article.monPrixAchat.div(article.nmbrUnite)
        updatedColumns.add("monPrixAchatUniter" to monPrixAchatUniterCal.toString())
    }

    private fun calculateWithCondition(
        columnToChange: String,
        newValue: Double?,
        article: BaseDonneStatTabel
    ) {
        val updatedColumns = mutableListOf<Pair<String, String>>()

        monPrixVent(columnToChange, newValue, updatedColumns, article)

        calculateMyBenefit(columnToChange, newValue, article, updatedColumns)
        calculateClientBenefit(columnToChange, newValue, article, updatedColumns)

        for ((column, value) in updatedColumns) {
            updateBaseDonneStatTabel(column, article, value)
        }
    }

    private fun monPrixVent(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        when (columnToChange) {
            "benificeClient" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVent" to ((article.prixDeVentTotaleChezClient
                            ?: 0.0) - it).toString()
                    )
                }
            }

            "monBenfice" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVent" to (it + (article.monPrixAchat ?: 0.0)).toString()
                    )
                }
            }
        }
    }

    private fun calculateMyBenefit(
        columnToChange: String,
        newValue: Double?,
        article: BaseDonneStatTabel,
        updatedColumns: MutableList<Pair<String, String>>
    ) {
        if (columnToChange != "monBenfice") {
            val monPrixVent = if (columnToChange == "monPrixVent") newValue else article.monPrixVent
            val monBenficeCal = monPrixVent?.let { it - (article.monPrixAchat ?: 0.0) }
            monBenficeCal?.let { updatedColumns.add("monBenfice" to it.toString()) }
        }
    }

    private fun calculateClientBenefit(
        columnToChange: String,
        newValue: Double?,
        article: BaseDonneStatTabel,
        updatedColumns: MutableList<Pair<String, String>>
    ) {
        if (columnToChange != "benificeClient") {
            val monPrixVent = if (columnToChange == "monPrixVent") newValue else article.monPrixVent
            val benificeClientCal = monPrixVent?.let { (article.prixDeVentTotaleChezClient ?: 0.0) - it }
            benificeClientCal?.let { updatedColumns.add("benificeClient" to it.toString()) }
        }
    }
    private fun updateBaseDonneStatTabel(
        columnToChangeInString: String,
        article: BaseDonneStatTabel,
        newValue: String?
    ) {
        newValue?.let {
            viewModelScope.launch(Dispatchers.Main) {
                _baseDonneStatTabel.find { it.idArticle == article.idArticle }?.apply {
                    when (columnToChangeInString) {
                        "nomArticleFinale" -> nomArticleFinale = it.ifEmpty { "0.0" }
                        "classementCate" -> classementCate = if (it.isEmpty()) 0.0 else it.toDouble()
                        "nomArab" -> nomArab = it.ifEmpty { "0.0" }
                        "nmbrCat" -> nmbrCat = if (it.isEmpty()) 0 else it.toInt()
                        "couleur1" -> couleur1 = it.ifEmpty { null }
                        "couleur2" -> couleur2 = if (it.isEmpty()) null else it
                        "couleur3" -> couleur3 = if (it.isEmpty()) null else it
                        "couleur4" -> couleur4 = if (it.isEmpty()) null else it
                        "nomCategorie2" -> nomCategorie2 = if (it.isEmpty()) null else it
                        "nmbrUnite" -> nmbrUnite = if (it.isEmpty()) 0 else it.toInt()
                        "nmbrCaron" -> nmbrCaron = if (it.isEmpty()) 0 else it.toInt()
                        "affichageUniteState" -> affichageUniteState = it.toBoolean()
                        "commmentSeVent" -> commmentSeVent = if (it.isEmpty()) null else it
                        "afficheBoitSiUniter" -> afficheBoitSiUniter = if (it.isEmpty()) null else it
                        "monPrixAchat" -> monPrixAchat = if (it.isEmpty()) 0.0 else it.toDouble()
                        "clienPrixVentUnite" -> clienPrixVentUnite = if (it.isEmpty()) 0.0 else it.toDouble()
                        "minQuan" -> minQuan = if (it.isEmpty()) 0 else it.toInt()
                        "monBenfice" -> monBenfice = if (it.isEmpty()) 0.0 else it.toDouble()
                        "monPrixVent" -> monPrixVent = if (it.isEmpty()) 0.0 else it.toDouble()
                        "diponibilityState" -> diponibilityState = if (it.isEmpty()) "" else it
                        "neaon2" -> neaon2 = if (it.isEmpty()) "" else it
                        "idCategorie" -> idCategorie = if (it.isEmpty()) 0.0 else it.toDouble()
                        "funChangeImagsDimention" -> funChangeImagsDimention = it.toBoolean()
                        "nomCategorie" -> nomCategorie = if (it.isEmpty()) "" else it
                        "neaon1" -> neaon1 = if (it.isEmpty()) 0.0 else it.toDouble()
                        "lastUpdateState" -> lastUpdateState = if (it.isEmpty()) "" else it
                        "cartonState" -> cartonState = if (it.isEmpty()) "" else it
                        "dateCreationCategorie" -> dateCreationCategorie = if (it.isEmpty()) "" else it
                        "prixDeVentTotaleChezClient" -> prixDeVentTotaleChezClient = if (it.isEmpty()) 0.0 else it.toDouble()
                        "benficeTotaleEntreMoiEtClien" -> benficeTotaleEntreMoiEtClien = if (it.isEmpty()) 0.0 else it.toDouble()
                        "benificeTotaleEn2" -> benificeTotaleEn2 = if (it.isEmpty()) 0.0 else it.toDouble()
                        "monPrixAchatUniter" -> monPrixAchatUniter = if (it.isEmpty()) 0.0 else it.toDouble()
                        "monPrixVentUniter" -> monPrixVentUniter = if (it.isEmpty()) 0.0 else it.toDouble()
                        "benificeClient" -> benificeClient = if (it.isEmpty()) 0.0 else it.toDouble()
                    }
                    articleDao.update(toBaseDonne(this))
                }
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
                    it.classementCate,
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
                    it.neaon1,
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

    private fun toBaseDonne(baseDonneStatTabel: BaseDonneStatTabel): BaseDonne {
        return BaseDonne(
            baseDonneStatTabel.idArticle,
            baseDonneStatTabel.nomArticleFinale,
            baseDonneStatTabel.classementCate,
            baseDonneStatTabel.nomArab,
            baseDonneStatTabel.nmbrCat,
            baseDonneStatTabel.couleur1,
            baseDonneStatTabel.couleur2,
            baseDonneStatTabel.couleur3,
            baseDonneStatTabel.couleur4,
            baseDonneStatTabel.nomCategorie2,
            baseDonneStatTabel.nmbrUnite,
            baseDonneStatTabel.nmbrCaron,
            baseDonneStatTabel.affichageUniteState,
            baseDonneStatTabel.commmentSeVent,
            baseDonneStatTabel.afficheBoitSiUniter,
            baseDonneStatTabel.monPrixAchat,
            baseDonneStatTabel.clienPrixVentUnite,
            baseDonneStatTabel.minQuan,
            baseDonneStatTabel.monBenfice,
            baseDonneStatTabel.monPrixVent,
            baseDonneStatTabel.diponibilityState,
            baseDonneStatTabel.neaon2,
            baseDonneStatTabel.idCategorie,
            baseDonneStatTabel.funChangeImagsDimention,
            baseDonneStatTabel.nomCategorie,
            baseDonneStatTabel.neaon1,
            baseDonneStatTabel.lastUpdateState,
            baseDonneStatTabel.cartonState,
            baseDonneStatTabel.dateCreationCategorie,
            baseDonneStatTabel.prixDeVentTotaleChezClient,
            baseDonneStatTabel.benficeTotaleEntreMoiEtClien,
            baseDonneStatTabel.benificeTotaleEn2,
            baseDonneStatTabel.monPrixAchatUniter,
            baseDonneStatTabel.monPrixVentUniter
        )
    }
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
