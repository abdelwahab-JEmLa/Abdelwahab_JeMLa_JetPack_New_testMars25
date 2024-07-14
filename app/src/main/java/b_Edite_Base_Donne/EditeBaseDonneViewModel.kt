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

    fun orderByDateCreation() {
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
            }.sortedBy { it.dateCreationCategorie }

            withContext(Dispatchers.Main) {
                _baseDonneStatTabel.value = baseDonneStatTabelList
            }
        }
    }
    fun updateDataBaseDonne(articleDataBaseDonne: BaseDonne?) {
        val itemIndex = _dataBaseDonne.indexOfFirst {
            it.idArticle == (articleDataBaseDonne?.idArticle ?: 0)
        }
        if (itemIndex != -1) {
            if (articleDataBaseDonne != null) {
                _dataBaseDonne[itemIndex] = articleDataBaseDonne
            }
            viewModelScope.launch {
                if (articleDataBaseDonne != null) {
                    articleDao.updateFromeDataBaseDonne(articleDataBaseDonne)
                }
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
        ///////////////////////////////////////////////////////////////////
        for ((column, value) in updatedColumns) {
            updateBaseDonneStatTabel(column, article, value)
        }
        ///////////////////////////////////////////////////////////////////
    }

    private fun monPrixVentUniter(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {

        when (columnToChange) {
            "nmbrUnite" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVentUniter" to ((article.monPrixVent / it)).toString()
                    )
                }
            }
            "monPrixVent" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVentUniter" to (it / article.nmbrUnite).toString()
                    )
                }
            }

            "monPrixAchatUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVentUniter" to ((((article.monBenfice/ article.nmbrUnite)) + it)).toString()
                    )
                }
            }

            "monPrixAchat" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVentUniter" to (((article.monBenfice + it))/ article.nmbrUnite).toString()
                    )
                }
            }

            "benificeClient" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVentUniter" to ((((article.prixDeVentTotaleChezClient) - it))/article.nmbrUnite).toString()
                    )
                }
            }

            "monBenfice" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixVentUniter" to ((it + article.monPrixAchat)/article.nmbrUnite).toString()
                    )
                }
            }

        }
    }
    private fun monBeneficeUniter(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {

        when (columnToChange) {
            "benificeClient" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBeneficeUniter" to (((article.prixDeVentTotaleChezClient -it) - article.monPrixAchat)/ article.nmbrUnite).toString()
                    )
                }
            }
            "monPrixVentUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBeneficeUniter" to (it - article.monPrixAchatUniter).toString()
                    )
                }
            }
            "nmbrUnite" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBeneficeUniter" to ((article.monBenfice / it)).toString()
                    )
                }
            }
            "monBenfice" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBeneficeUniter" to (it / article.nmbrUnite).toString()
                    )
                }
            }


            "monPrixVent" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBeneficeUniter" to ((it / article.nmbrUnite) - article.monPrixAchatUniter).toString()
                    )
                }
            }
        }
    }
    private fun monPrixAchat(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {

        when (columnToChange) {
            "monPrixAchatUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixAchat" to (((it)) * article.nmbrUnite).toString()
                    )
                }
            }

        }
    }

    private fun monPrixAchatUniter(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {

        when (columnToChange) {
            "monPrixAchat" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixAchatUniter" to (it / article.nmbrUnite).toString()
                    )
                }
            }
            "nmbrUnite" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monPrixAchatUniter" to (((article.monPrixAchat / it))).toString()
                    )
                }
            }

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

    private fun calculateMyBenefit(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        when (columnToChange) {
            "monBeneficeUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBenfice" to (it * article.nmbrUnite).toString()
                    )
                }
            }
            "monPrixVent" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBenfice" to (it - (article.monPrixAchat)).toString()
                    )
                }
            }
            "monPrixVentUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBenfice" to ((it*article.nmbrUnite) - (article.monPrixAchat)).toString()
                    )
                }
            }
            "benificeClient" -> {
                newValue?.let {
                    updatedColumns.add(
                        "monBenfice" to ((article.prixDeVentTotaleChezClient - it) - (article.monPrixAchat)).toString()
                    )
                }
            }
        }
    }

    private fun calculateClientBenefit(
        columnToChange: String,
        newValue: Double?,
        updatedColumns: MutableList<Pair<String, String>>,
        article: BaseDonneStatTabel
    ) {
        when (columnToChange) {
            "monBeneficeUniter" -> {
                newValue?.let {
                    updatedColumns.add(
                        "benificeClient" to ((it * article.nmbrUnite) - article.benificeTotaleEn2).toString()
                    )
                }
            }
            "nmbrUnite" -> {
                newValue?.let {
                    updatedColumns.add(
                        "benificeClient" to ((it * article.clienPrixVentUnite) - article.monPrixVent).toString()
                    )
                }
            }

            "clienPrixVentUnite" -> {
                newValue?.let {
                    updatedColumns.add(
                        "benificeClient" to ((it * article.nmbrUnite) - article.monPrixVent).toString()
                    )
                }
            }

            "monPrixVent" -> {
                newValue?.let {
                    updatedColumns.add(
                        "benificeClient" to ((article.prixDeVentTotaleChezClient) - it).toString()
                    )
                }
            }

            "monBenfice" -> {
                newValue?.let {
                    updatedColumns.add(
                        "benificeClient" to ((article.prixDeVentTotaleChezClient) - (article.monPrixAchat + it)).toString()
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
                val originalItem = _originalBaseDonneStatTabel.find { it.idArticle == article.idArticle }
                val baseItem = _baseDonneStatTabel.value.find { it.idArticle == article.idArticle }

                val itemsToUpdate = listOfNotNull(originalItem, baseItem)

                itemsToUpdate.forEach { item ->
                    when (columnToChangeInString) {
                        "nomArticleFinale" -> item.nomArticleFinale = it.ifEmpty { "0.0" }
                        "classementCate" -> item.classementCate = if (it.isEmpty()) 0.0 else it.toDouble()
                        "nomArab" -> item.nomArab = it.ifEmpty { "0.0" }
                        "nmbrCat" -> item.nmbrCat = if (it.isEmpty()) 0 else it.toInt()
                        "couleur1" -> item.couleur1 = it.ifEmpty { null }
                        "couleur2" -> item.couleur2 = if (it.isEmpty()) null else it
                        "couleur3" -> item.couleur3 = if (it.isEmpty()) null else it
                        "couleur4" -> item.couleur4 = if (it.isEmpty()) null else it
                        "nomCategorie2" -> item.nomCategorie2 = if (it.isEmpty()) null else it
                        "nmbrUnite" -> item.nmbrUnite = if (it.isEmpty()) 0 else it.toInt()
                        "nmbrCaron" -> item.nmbrCaron = if (it.isEmpty()) 0 else it.toInt()
                        "affichageUniteState" -> item.affichageUniteState = it.toBoolean()
                        "commmentSeVent" -> item.commmentSeVent = if (it.isEmpty()) null else it
                        "afficheBoitSiUniter" -> item.afficheBoitSiUniter = if (it.isEmpty()) null else it
                        "monPrixAchat" -> item.monPrixAchat = if (it.isEmpty()) 0.0 else it.toDouble()
                        "clienPrixVentUnite" -> item.clienPrixVentUnite = if (it.isEmpty()) 0.0 else it.toDouble()
                        "minQuan" -> item.minQuan = if (it.isEmpty()) 0 else it.toInt()
                        "monBenfice" -> item.monBenfice = if (it.isEmpty()) 0.0 else it.toDouble()
                        "monPrixVent" -> item.monPrixVent = if (it.isEmpty()) 0.0 else it.toDouble()
                        "diponibilityState" -> item.diponibilityState = if (it.isEmpty()) "" else it
                        "neaon2" -> item.neaon2 = if (it.isEmpty()) "" else it
                        "idCategorie" -> item.idCategorie = if (it.isEmpty()) 0.0 else it.toDouble()
                        "funChangeImagsDimention" -> item.funChangeImagsDimention = it.toBoolean()
                        "nomCategorie" -> item.nomCategorie = if (it.isEmpty()) "" else it
                        "neaon1" -> item.neaon1 = if (it.isEmpty()) 0.0 else it.toDouble()
                        "lastUpdateState" -> item.lastUpdateState = if (it.isEmpty()) "" else it
                        "cartonState" -> item.cartonState = if (it.isEmpty()) "" else it
                        "dateCreationCategorie" -> item.dateCreationCategorie = if (it.isEmpty()) "" else it
                        "prixDeVentTotaleChezClient" -> item.prixDeVentTotaleChezClient = if (it.isEmpty()) 0.0 else it.toDouble()
                        "benficeTotaleEntreMoiEtClien" -> item.benficeTotaleEntreMoiEtClien = if (it.isEmpty()) 0.0 else it.toDouble()
                        "benificeTotaleEn2" -> item.benificeTotaleEn2 = if (it.isEmpty()) 0.0 else it.toDouble()
                        "monPrixAchatUniter" -> item.monPrixAchatUniter = if (it.isEmpty()) 0.0 else it.toDouble()
                        "monPrixVentUniter" -> item.monPrixVentUniter = if (it.isEmpty()) 0.0 else it.toDouble()
                        "benificeClient" -> item.benificeClient = if (it.isEmpty()) 0.0 else it.toDouble()
                        "monBeneficeUniter" -> item.monBeneficeUniter = if (it.isEmpty()) 0.0 else it.toDouble()
                    }
                }

                // Si vous devez enregistrer les changements dans la base de données pour chaque article mis à jour
                itemsToUpdate.forEach { articleDao.update(toBaseDonne(it)) }
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

