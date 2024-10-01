package b2_Edite_Base_Donne_With_Creat_New_Articls

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun MainFragmentEditDatabaseWithCreateNewArticles(
    viewModel: HeadOfViewModels,
    onToggleNavBar: () -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFloatingButtons by remember { mutableStateOf(false) }
    var gridColumns by remember { mutableStateOf(2) }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var filterNonDispo by remember { mutableStateOf(false) }

    var dialogeDisplayeDetailleChanger by remember { mutableStateOf<BaseDonneECBTabelle?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { padding ->
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                uiState.categoriesECB.forEach { category ->
                    val articlesInCategory = uiState.articlesBaseDonneECB.filter {
                        it.nomCategorie == category.nomCategorieInCategoriesTabele &&
                                (!filterNonDispo || it.diponibilityState == "")
                    }
                    if (articlesInCategory.isNotEmpty() || category.nomCategorieInCategoriesTabele == "New Articles") {
                        item(span = { GridItemSpan(gridColumns) }) {
                            CategoryHeaderECB(category = category, viewModel = viewModel)
                        }
                        items(articlesInCategory) { article ->
                            ArticleItemECB(
                                article = article,
                                onClickOnImg = { clickedArticle ->
                                    dialogeDisplayeDetailleChanger = clickedArticle
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Buttons
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(1f)
        ) {
            FloatingActionButtons(
                showFloatingButtons = showFloatingButtons,
                onToggleNavBar = onToggleNavBar,
                onToggleFloatingButtons = { showFloatingButtons = !showFloatingButtons },
                onToggleFilter = {
                    viewModel.toggleFilter()
                    filterNonDispo = !filterNonDispo
                },
                showOnlyWithFilter = uiState.showOnlyWithFilter,
                viewModel = viewModel,
                coroutineScope = coroutineScope,
                onUpdateStart = onUpdateStart,
                onUpdateProgress = onUpdateProgress,
                onUpdateComplete = onUpdateComplete,
                onChangeGridColumns = { gridColumns = it } ,
            )
        }

        // Dialog
        dialogeDisplayeDetailleChanger?.let { article ->
            ArticleDetailWindow(
                article = article,
                onDismiss = { dialogeDisplayeDetailleChanger = null },
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 3.dp)
            )
        }
    }
}

@Composable
fun ArticleItemECB(
    article: BaseDonneECBTabelle,
    onClickOnImg :(BaseDonneECBTabelle) ->Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onClickOnImg(article) },
                contentAlignment = Alignment.Center
            ) {
                //Affiche Image
                ImageDisplayerWithGlideECB(article)
                //Affiche diponibilityState
                DisponibilityOverlayECB(article.diponibilityState)
            }
            AutoResizedTextECB(text = article.nomArticleFinale)
        }
    }
}

data class CreatAndEditeInBaseDonnRepositeryModels(
    val articlesBaseDonneECB: List<BaseDonneECBTabelle> = emptyList(),
    val categoriesECB: List<CategoriesTabelleECB> = emptyList(),
    val showOnlyWithFilter: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)




class HeadOfViewModels(
    private val context: Context,
    private val creatAndEditeInBaseDonneRepositery: CreatAndEditeInBaseDonneRepositery
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreatAndEditeInBaseDonnRepositeryModels())
    val uiState = _uiState.asStateFlow()

    private val _currentEditedArticle = mutableStateOf<BaseDonneECBTabelle?>(null)
    val currentEditedArticle: State<BaseDonneECBTabelle?> = _currentEditedArticle

    private val refDBJetPackExport = FirebaseDatabase.getInstance().getReference("e_DBJetPackExport")
    private val refCategorieTabelee = FirebaseDatabase.getInstance().getReference("H_CategorieTabele")
    var tempImageUri: Uri? = null

    init {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            _uiState.update { it.copy(isLoading = true) }

            val articles = refDBJetPackExport.get().await().children.mapNotNull { snapshot ->
                snapshot.getValue(BaseDonneECBTabelle::class.java)?.apply {
                    idArticleECB = snapshot.key?.toIntOrNull() ?: 0
                }
            }

            val categories = refCategorieTabelee.get().await().children
                .mapNotNull { it.getValue(CategoriesTabelleECB::class.java) }
                .sortedBy { it.idClassementCategorieInCategoriesTabele }

            _uiState.update { it.copy(
                articlesBaseDonneECB = articles,
                categoriesECB = categories,
                isLoading = false
            ) }
        } catch (e: Exception) {
            handleError("Failed to load data from Firebase", e)
        }
    }

    fun updateAndCalculateAuthersField(textFieldValue: String, columnToChange: String, article: BaseDonneECBTabelle) {
        val updatedArticle = creatAndEditeInBaseDonneRepositery.updateAndCalculateAuthersField(textFieldValue, columnToChange, article)

        // Update local state
        _uiState.update { state ->
            val updatedArticles = state.articlesBaseDonneECB.map {
                if (it.idArticleECB == updatedArticle.idArticleECB) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }

        // Update the current edited article
        _currentEditedArticle.value = updatedArticle

        // Perform Firebase update asynchronously
        viewModelScope.launch {
            try {
                refDBJetPackExport.child(updatedArticle.idArticleECB.toString()).setValue(updatedArticle).await()
                Log.d("HeadOfViewModels", "Article updated successfully in Firebase")
            } catch (e: Exception) {
                handleError("Failed to update article in Firebase", e)
            }
        }
    }

    fun toggleFilter() {
        _uiState.update { creatAndEditeInBaseDonneRepositery.toggleFilter(it) }
    }


    fun addNewParentArticle(uri: Uri, category: CategoriesTabelleECB) {
        viewModelScope.launch {
            try {
                val newId = getNextArticleId()
                val destinationFile = File(getDownloadsDirectory(), "${newId}_1.jpg")

                copyImage(uri, destinationFile)

                val newClassementCate = (uiState.value.articlesBaseDonneECB
                    .filter { it.nomCategorie == category.nomCategorieInCategoriesTabele }
                    .minOfOrNull { it.classementCate }
                    ?.minus(1.0)
                    ?: 0.0)

                val newArticle = BaseDonneECBTabelle(
                    idArticleECB = newId,
                    nomArticleFinale = "New Article $newId",
                    nomCategorie = category.nomCategorieInCategoriesTabele,
                    diponibilityState = "",
                    couleur1 = "Couleur 1",
                    dateCreationCategorie = System.currentTimeMillis().toString(),
                    classementCate = newClassementCate
                )
                ensureNewArticlesCategoryExists()
                addNewArticle(newArticle)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to process image: ${e.message}") }
            }
        }
    }


    private suspend fun addNewArticle(article: BaseDonneECBTabelle) {
        try {
            // Ajouter l'article à Firebase
            refDBJetPackExport.child(article.idArticleECB.toString()).setValue(article).await()

            // Mettre à jour l'état local
            _uiState.update { currentState ->
                currentState.copy(
                    articlesBaseDonneECB = currentState.articlesBaseDonneECB + article
                )
            }

            Log.d("HeadOfViewModels", "New article added successfully: ${article.idArticleECB}")
        } catch (e: Exception) {
            handleError("Failed to add new article", e)
        }
    }
    fun addColoreToArticle(uri: Uri, article: BaseDonneECBTabelle) {
        viewModelScope.launch {
            try {
                val nextColorField = when {
                    article.couleur2.isNullOrEmpty() -> "couleur2"
                    article.couleur3.isNullOrEmpty() -> "couleur3"
                    article.couleur4.isNullOrEmpty() -> "couleur4"
                    else -> throw IllegalStateException("All color fields are filled")
                }

                val fileName = "${article.idArticleECB}_${nextColorField.removePrefix("couleur")}.jpg"
                val destinationFile = File(getDownloadsDirectory(), fileName)

                copyImageToDownloads(uri, destinationFile)

                val updatedArticle = article.copy(
                    couleur2 = if (nextColorField == "couleur2") "Couleur_2" else article.couleur2,
                    couleur3 = if (nextColorField == "couleur3") "Couleur_3" else article.couleur3,
                    couleur4 = if (nextColorField == "couleur4") "Couleur_4" else article.couleur4
                )

                updateArticle(updatedArticle)
            } catch (e: Exception) {
                handleError("Failed to add color to article", e)
            }
        }
    }

    fun createTempImageUri(context: Context): Uri {
        val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
    }
    private suspend fun getNextArticleId(): Int {
        val articles = refDBJetPackExport.get().await().children.mapNotNull { snapshot ->
            snapshot.key?.toIntOrNull()
        }
        return (articles.maxOrNull() ?: 0) + 1
    }

    private suspend fun copyImage(sourceUri: Uri, destinationFile: File) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(error = "Failed to copy image: ${e.message}") }
            }
        }
    }

    private suspend fun updateArticle(article: BaseDonneECBTabelle) {
        try {
            refDBJetPackExport.child(article.idArticleECB.toString()).setValue(article).await()

            _uiState.update { currentState ->
                val updatedArticles = currentState.articlesBaseDonneECB.map {
                    if (it.idArticleECB == article.idArticleECB) article else it
                }
                currentState.copy(articlesBaseDonneECB = updatedArticles)
            }
            Log.d("HeadOfViewModels", "Article updated successfully: ${article.idArticleECB}")
        } catch (e: Exception) {
            handleError("Failed to update article", e)
        }
    }

    fun getDownloadsDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    private suspend fun copyImageToDownloads(sourceUri: Uri, destinationFile: File) {
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    FileOutputStream(destinationFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            } catch (e: IOException) {
                _uiState.update { it.copy(error = "Failed to copy image: ${e.message}") }
            }
        }
    }

    private suspend fun ensureNewArticlesCategoryExists() {
        val newArticlesCategory = "New Articles"
        val existingCategories = _uiState.value.categoriesECB

        if (!existingCategories.any { it.nomCategorieInCategoriesTabele == newArticlesCategory }) {
            val newCategory = CategoriesTabelleECB(
                idClassementCategorieInCategoriesTabele = 0.5,
                nomCategorieInCategoriesTabele = newArticlesCategory
            )

            try {
                // Add to Firebase
                refCategorieTabelee.push().setValue(newCategory).await()

                // Update local state
                _uiState.update { currentState ->
                    currentState.copy(
                        categoriesECB = currentState.categoriesECB + newCategory
                    )
                }
                Log.d("HeadOfViewModels", "'New Articles' category created successfully")
            } catch (e: Exception) {
                handleError("Failed to create 'New Articles' category", e)
            }
        }
    }

    private fun handleError(message: String, exception: Exception) {
        Log.e("HeadOfViewModels", message, exception)
        _uiState.update { it.copy(error = "$message: ${exception.message}") }
    }
}


////////////////////////////////////////////////////////////////////////////////
class CreatAndEditeInBaseDonneRepositery {
    fun toggleFilter(currentState: CreatAndEditeInBaseDonnRepositeryModels) =
        currentState.copy(showOnlyWithFilter = !currentState.showOnlyWithFilter)

    fun updateAndCalculateAuthersField(
        textFieldValue: String,
        columnToChange: String,
        article: BaseDonneECBTabelle
    ): BaseDonneECBTabelle {
        val newValue = textFieldValue.toDoubleOrNull() ?: return article
        return article.copy().apply {
            calculateWithoutCondition(columnToChange, textFieldValue, newValue)
            calculateWithCondition(columnToChange, newValue)
        }
    }

    private fun BaseDonneECBTabelle.calculateWithoutCondition(
        columnToChange: String,
        textFieldValue: String,
        newValue: Double
    ) {
        when (columnToChange) {
            "nmbrUnite" -> nmbrUnite = newValue.toInt()
            "clienPrixVentUnite" -> clienPrixVentUnite = newValue
            else -> setField(columnToChange, textFieldValue)
        }

        prixDeVentTotaleChezClient = nmbrUnite * clienPrixVentUnite
        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
    }

    private fun BaseDonneECBTabelle.calculateWithCondition(columnToChange: String, newValue: Double) {
        when (columnToChange) {
            "monPrixVent" -> updateMonPrixVent(newValue)
            "monBenfice" -> updateMonBenfice(newValue)
            "benificeClient" -> updateBenificeClient(newValue)
            "monPrixAchat" -> updateMonPrixAchat(newValue)
            "monPrixAchatUniter" -> updateMonPrixAchatUniter(newValue)
            "monPrixVentUniter" -> updateMonPrixVentUniter(newValue)
            "monBeneficeUniter" -> updateMonBeneficeUniter(newValue)
        }
    }

    private fun BaseDonneECBTabelle.updateMonPrixVent(newValue: Double) {
        monPrixVent = newValue
        monBenfice = monPrixVent - monPrixAchat
        monPrixVentUniter = monPrixVent / nmbrUnite
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.updateMonBenfice(newValue: Double) {
        monBenfice = newValue
        monPrixVent = monBenfice + monPrixAchat
        monPrixVentUniter = monPrixVent / nmbrUnite
        monBeneficeUniter = monBenfice / nmbrUnite
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.updateBenificeClient(newValue: Double) {
        benificeClient = newValue
        monPrixVent = prixDeVentTotaleChezClient - benificeClient
        monBenfice = monPrixVent - monPrixAchat
        monPrixVentUniter = monPrixVent / nmbrUnite
        monBeneficeUniter = monBenfice / nmbrUnite
    }

    private fun BaseDonneECBTabelle.updateMonPrixAchat(newValue: Double) {
        monPrixAchat = newValue
        monPrixAchatUniter = monPrixAchat / nmbrUnite
        monBenfice = monPrixVent - monPrixAchat
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
    }

    private fun BaseDonneECBTabelle.updateMonPrixAchatUniter(newValue: Double) {
        monPrixAchatUniter = newValue
        monPrixAchat = monPrixAchatUniter * nmbrUnite
        monBenfice = monPrixVent - monPrixAchat
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
    }

    private fun BaseDonneECBTabelle.updateMonPrixVentUniter(newValue: Double) {
        monPrixVentUniter = newValue
        monPrixVent = monPrixVentUniter * nmbrUnite
        monBenfice = monPrixVent - monPrixAchat
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.updateMonBeneficeUniter(newValue: Double) {
        monBeneficeUniter = newValue
        monBenfice = monBeneficeUniter * nmbrUnite
        monPrixVentUniter = monPrixAchatUniter + monBeneficeUniter
        monPrixVent = monPrixVentUniter * nmbrUnite
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.setField(fieldName: String, value: String) {
        val field = this::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        when (field.type) {
            Int::class.java -> field.setInt(this, value.toIntOrNull() ?: 0)
            Double::class.java -> field.setDouble(this, value.toDoubleOrNull() ?: 0.0)
            String::class.java -> field.set(this, value)
            Boolean::class.java -> field.setBoolean(this, value.toBoolean())
        }
    }

}

data class BaseDonneECBTabelle(
    var idArticleECB: Int = 0,
    var nomArticleFinale: String = "",
    var classementCate: Double = 0.0,
    var nomArab: String = "",
    var autreNomDarticle: String? = null,
    var nmbrCat: Int = 0,
    var couleur1: String? = null,
    var couleur2: String? = null,
    var couleur3: String? = null,
    var couleur4: String? = null,
    var nomCategorie2: String? = null,
    var nmbrUnite: Int = 0,
    var nmbrCaron: Int = 0,
    var affichageUniteState: Boolean = false,
    var commmentSeVent: String? = null,
    var afficheBoitSiUniter: String? = null,
    var monPrixAchat: Double = 0.0,
    var clienPrixVentUnite: Double = 0.0,
    var minQuan: Int = 0,
    var monBenfice: Double = 0.0,
    var monPrixVent: Double = 0.0,
    var diponibilityState: String = "",
    var neaon2: String = "",
    var idCategorie: Double = 0.0,
    var funChangeImagsDimention: Boolean = false,
    var nomCategorie: String = "",
    var neaon1: Double = 0.0,
    var lastUpdateState: String = "",
    var cartonState: String = "",
    var dateCreationCategorie: String = "",
    var prixDeVentTotaleChezClient: Double = 0.0,
    var benficeTotaleEntreMoiEtClien: Double = 0.0,
    var benificeTotaleEn2: Double = 0.0,
    var monPrixAchatUniter: Double = 0.0,
    var monPrixVentUniter: Double = 0.0,
    var benificeClient: Double = 0.0,
    var monBeneficeUniter: Double = 0.0
) {
    fun getColumnValue(columnName: String): Any? {
        val value = when (columnName) {
            "nomArticleFinale" -> nomArticleFinale
            "classementCate" -> classementCate
            "nomArab" -> nomArab
            "nmbrCat" -> nmbrCat
            "couleur1" -> couleur1
            "couleur2" -> couleur2
            "couleur3" -> couleur3
            "couleur4" -> couleur4
            "nomCategorie2" -> nomCategorie2
            "nmbrUnite" -> nmbrUnite
            "nmbrCaron" -> nmbrCaron
            "affichageUniteState" -> affichageUniteState
            "commmentSeVent" -> commmentSeVent
            "afficheBoitSiUniter" -> afficheBoitSiUniter
            "monPrixAchat" -> monPrixAchat
            "clienPrixVentUnite" -> clienPrixVentUnite
            "minQuan" -> minQuan
            "monBenfice" -> monBenfice
            "monPrixVent" -> monPrixVent
            "diponibilityState" -> diponibilityState
            "neaon2" -> neaon2
            "idCategorie" -> idCategorie
            "funChangeImagsDimention" -> funChangeImagsDimention
            "nomCategorie" -> nomCategorie
            "neaon1" -> neaon1
            "lastUpdateState" -> lastUpdateState
            "cartonState" -> cartonState
            "dateCreationCategorie" -> dateCreationCategorie
            "prixDeVentTotaleChezClient" -> prixDeVentTotaleChezClient
            "benficeTotaleEntreMoiEtClien" -> benficeTotaleEntreMoiEtClien
            "benificeTotaleEn2" -> benificeTotaleEn2
            "monPrixAchatUniter" -> monPrixAchatUniter
            "monPrixVentUniter" -> monPrixVentUniter
            "benificeClient" -> benificeClient
            "monBeneficeUniter" -> monBeneficeUniter
            else -> null
        }

        return when (value) {
            is Double -> if (value % 1 == 0.0) value.toInt() else value
            else -> value
        }
    }
}

data class CategoriesTabelleECB(
    val idCategorieInCategoriesTabele: Long = 0,
    var idClassementCategorieInCategoriesTabele: Double = 0.0,
    val nomCategorieInCategoriesTabele: String = "",
)
