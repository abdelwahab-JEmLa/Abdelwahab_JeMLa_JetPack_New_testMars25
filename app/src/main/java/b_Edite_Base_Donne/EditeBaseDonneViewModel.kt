package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditeBaseDonneViewModel(
    private val articleDao: ArticleDao,
) : ViewModel() {
    private val _dataBaseDonne = mutableStateListOf<BaseDonne>()
    val dataBaseDonne: List<BaseDonne> get() = _dataBaseDonne

    private val _baseDonneStatTabel = MutableStateFlow<List<BaseDonneStatTabel>>(emptyList())
    private val _originalBaseDonneStatTabel = mutableListOf<BaseDonneStatTabel>()
    val baseDonneStatTabel: StateFlow<List<BaseDonneStatTabel>> = _baseDonneStatTabel.asStateFlow()

    private val _isFilterApplied = MutableStateFlow(false)
    val isFilterApplied: StateFlow<Boolean> get() = _isFilterApplied

    init {
        initBaseDonneStatTabel()
        initDataBaseDonneForNewByStatInCompos()
    }

    fun toggleFilter() {
        viewModelScope.launch(Dispatchers.IO) {
            val filterApplied = _isFilterApplied.value
            _isFilterApplied.value = !filterApplied
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
                    it.monPrixVentUniter,
                    it.benificeClient,
                    it.monBeneficeUniter,
                )
            }

            withContext(Dispatchers.Main) {
                if (!filterApplied) {
                    _originalBaseDonneStatTabel.clear()
                    _originalBaseDonneStatTabel.addAll(baseDonneStatTabelList)
                    _baseDonneStatTabel.value = _originalBaseDonneStatTabel.filter { it.monPrixVent == 0.0 }
                } else {
                    _baseDonneStatTabel.value = _originalBaseDonneStatTabel
                }
            }
        }
    }
    fun updateDataBaseDonne(articleDataBaseDonne: BaseDonne) {
        val itemIndex = _dataBaseDonne.indexOfFirst { it.idArticle == articleDataBaseDonne.idArticle }
        if (itemIndex != -1) {
            _dataBaseDonne[itemIndex] = articleDataBaseDonne

            viewModelScope.launch {
                articleDao.updateFromeDataBaseDonne(articleDataBaseDonne)
            }
        }
    }

    fun insertAllDataBaseDonne(articles: List<BaseDonne>) {
        viewModelScope.launch {
            articleDao.upsert(articles)
        }
    }

    fun initDataBaseDonneForNewByStatInCompos() {
        viewModelScope.launch(Dispatchers.IO) {
            val articlesFromRoom = articleDao.getAllArticlesOrder()
            val dataBaseDonneList = articlesFromRoom.map {
                BaseDonne(
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
                    it.monPrixVentUniter,
                    it.benificeClient,
                )
            }
            withContext(Dispatchers.Main) {
                _dataBaseDonne.clear()
                _dataBaseDonne.addAll(dataBaseDonneList)
            }
        }
    }

    fun initBaseDonneStatTabel() {
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
                    it.monPrixVentUniter,
                    it.benificeClient,
                    it.monBeneficeUniter,
                )
            }
            withContext(Dispatchers.Main) {
                _baseDonneStatTabel.value = baseDonneStatTabelList
                _originalBaseDonneStatTabel.clear()
                _originalBaseDonneStatTabel.addAll(baseDonneStatTabelList)
            }
        }
    }

    ///////////////////////////////////////////////////////////

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

        val nmbrUnite = if (columnToChange == "nmbrUnite") newValue else article.nmbrUnite

        val clienPrixVentUniteCal =
            if (columnToChange == "clienPrixVentUnite") newValue else article.clienPrixVentUnite
        val prixDeVentTotaleChezClientCal =
            nmbrUnite?.let { clienPrixVentUniteCal?.times(it.toDouble()) }
        updatedColumns.add("prixDeVentTotaleChezClient" to prixDeVentTotaleChezClientCal.toString())

        val benficeTotaleEntreMoiEtClienCal =
            prixDeVentTotaleChezClientCal?.minus(article.monPrixAchat)
        updatedColumns.add("benficeTotaleEntreMoiEtClien" to benficeTotaleEntreMoiEtClienCal.toString())

        val benificeTotaleEn2Cal = benficeTotaleEntreMoiEtClienCal?.div(2)
        updatedColumns.add("benificeTotaleEn2" to benificeTotaleEn2Cal.toString())

        for ((column, value) in updatedColumns) {
            updateBaseDonneStatTabel(column, article, value)
        }
    }

    private fun calculateWithCondition(
        columnToChange: String,
        newValue: Double?,
        article: BaseDonneStatTabel
    ) {
        val updatedColumns = mutableListOf<Pair<String, String>>()
        if (columnToChange != "monPrixVent") {
            monPrixVent(columnToChange, newValue, updatedColumns, article)
        }
        if (columnToChange != "monBenfice") {
            calculateMyBenefit(columnToChange, newValue, updatedColumns, article)
        }
        if (columnToChange != "benificeClient") {
            calculateClientBenefit(columnToChange, newValue, updatedColumns, article)
        }
        if (columnToChange != "monPrixAchat") {
            monPrixAchat(columnToChange, newValue, updatedColumns, article)
        }
        if (columnToChange != "monPrixAchatUniter") {
            monPrixAchatUniter(columnToChange, newValue, updatedColumns, article)
        }
        if (columnToChange != "monPrixVentUniter") {
            monPrixVentUniter(columnToChange, newValue, updatedColumns, article)
        }
        if (columnToChange != "monBeneficeUniter") {
            monBeneficeUniter(columnToChange, newValue, updatedColumns, article)
        }

        for ((column, value) in updatedColumns) {
            updateBaseDonneStatTabel(column, article, value)
        }
    }

    private fun monPrixVentUniter(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        if (columnToChange == "nmbrUnite") {
            val monPrixVentUniterCal =
                newValue?.let { article.monPrixVent?.div(it.toDouble()) }
            updatedColumns.add("monPrixVentUniter" to monPrixVentUniterCal.toString())
        }
    }

    private fun monBeneficeUniter(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        if (columnToChange == "monPrixVentUniter" || columnToChange == "monPrixAchatUniter") {
            val prixVentUniterCal =
                if (columnToChange == "monPrixVentUniter") newValue else article.monPrixVentUniter
            val prixAchatUniterCal =
                if (columnToChange == "monPrixAchatUniter") newValue else article.monPrixAchatUniter
            val monBeneficeUniterCal = prixVentUniterCal?.minus(prixAchatUniterCal ?: 0.0)
            updatedColumns.add("monBeneficeUniter" to monBeneficeUniterCal.toString())
        }
    }

    private fun monPrixAchatUniter(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        if (columnToChange == "nmbrUnite") {
            val monPrixAchatUniterCal =
                newValue?.let { article.monPrixAchat?.div(it.toDouble()) }
            updatedColumns.add("monPrixAchatUniter" to monPrixAchatUniterCal.toString())
        }
    }

    private fun monPrixAchat(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        if (columnToChange == "monPrixVent" || columnToChange == "monBenfice") {
            val monPrixVentCal = if (columnToChange == "monPrixVent") newValue else article.monPrixVent
            val monBenficeCal = if (columnToChange == "monBenfice") newValue else article.monBenfice
            val monPrixAchatCal = monPrixVentCal?.minus(monBenficeCal ?: 0.0)
            updatedColumns.add("monPrixAchat" to monPrixAchatCal.toString())
        }
    }

    private fun calculateClientBenefit(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        if (columnToChange == "prixDeVentTotaleChezClient" || columnToChange == "benificeTotaleEn2") {
            val prixDeVentTotaleChezClientCal =
                if (columnToChange == "prixDeVentTotaleChezClient") newValue else article.prixDeVentTotaleChezClient
            val benificeTotaleEn2Cal =
                if (columnToChange == "benificeTotaleEn2") newValue else article.benificeTotaleEn2
            val benificeClientCal =
                prixDeVentTotaleChezClientCal?.minus(benificeTotaleEn2Cal ?: 0.0)
            updatedColumns.add("benificeClient" to benificeClientCal.toString())
        }
    }

    private fun calculateMyBenefit(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        if (columnToChange == "monPrixVent" || columnToChange == "monPrixAchat") {
            val monPrixVentCal = if (columnToChange == "monPrixVent") newValue else article.monPrixVent
            val monPrixAchatCal = if (columnToChange == "monPrixAchat") newValue else article.monPrixAchat
            val monBenficeCal = monPrixVentCal?.minus(monPrixAchatCal ?: 0.0)
            updatedColumns.add("monBenfice" to monBenficeCal.toString())
        }
    }
    private fun monPrixVent(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {

        when (columnToChange) {
            "monBeneficeUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVent" to ((it * article.nmbrUnite)+ article.monPrixAchat).toString()
                    )
                }
            }
            "monPrixVentUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVent" to ((it) * article.nmbrUnite).toString()
                    )
                }
            }

            "monPrixAchatUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVent" to ((it*article.nmbrUnite) + article.monBenfice).toString()
                    )
                }
            }

            "monPrixAchat" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVent" to ((article.monBenfice) + it).toString()
                    )
                }
            }

            "benificeClient" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVent" to ((article.prixDeVentTotaleChezClient) - it).toString()
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



    private fun updateBaseDonneStatTabel(
        columnToChangeInString: String,
        article: BaseDonneStatTabel,
        newValue: String?
    ) {
        newValue?.let {
            viewModelScope.launch(Dispatchers.Main) {
                _baseDonneStatTabel.value.find { it.idArticle == article.idArticle }?.apply {
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
                        "afficheBoitSiUniter" -> afficheBoitSiUniter =
                            if (it.isEmpty()) null else it

                        "monPrixAchat" -> monPrixAchat = if (it.isEmpty()) 0.0 else it.toDouble()
                        "clienPrixVentUnite" -> clienPrixVentUnite =
                            if (it.isEmpty()) 0.0 else it.toDouble()

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
                        "dateCreationCategorie" -> dateCreationCategorie =
                            if (it.isEmpty()) "" else it

                        "prixDeVentTotaleChezClient" -> prixDeVentTotaleChezClient =
                            if (it.isEmpty()) 0.0 else it.toDouble()

                        "benficeTotaleEntreMoiEtClien" -> benficeTotaleEntreMoiEtClien =
                            if (it.isEmpty()) 0.0 else it.toDouble()

                        "benificeTotaleEn2" -> benificeTotaleEn2 =
                            if (it.isEmpty()) 0.0 else it.toDouble()

                        "monPrixAchatUniter" -> monPrixAchatUniter =
                            if (it.isEmpty()) 0.0 else it.toDouble()

                        "monPrixVentUniter" -> monPrixVentUniter =
                            if (it.isEmpty()) 0.0 else it.toDouble()

                        "benificeClient" -> benificeClient =
                            if (it.isEmpty()) 0.0 else it.toDouble()
                        "monBeneficeUniter" -> monBeneficeUniter =
                            if (it.isEmpty()) 0.0 else it.toDouble()}
                    articleDao.update(toBaseDonne(this))
                }
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
            baseDonneStatTabel.monPrixVentUniter,
            baseDonneStatTabel.benificeClient,
            baseDonneStatTabel.monBeneficeUniter,
        )
    }
}

class MainAppViewModelFactory(
    private val articleDao: ArticleDao,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditeBaseDonneViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditeBaseDonneViewModel(articleDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

