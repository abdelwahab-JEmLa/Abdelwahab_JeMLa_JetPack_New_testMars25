package a_MainAppCompnents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import b2_Edite_Base_Donne_With_Creat_New_Articls.CategoriesRepository
import b2_Edite_Base_Donne_With_Creat_New_Articls.CategoriesTabelleECB
import b_Edite_Base_Donne.ArticleDao
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import c_ManageBonsClients.roundToOneDecimal
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.example.abdelwahabjemlajetpack.importFromFirebaseToDataBaseDonne
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import h_FactoryClassemntsArticles.ClassementsArticlesTabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.BreakIterator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

data class CreatAndEditeInBaseDonnRepositeryModels(
    val articlesBaseDonneECB: List<DataBaseArticles> = emptyList(),
    val categoriesECB: List<CategoriesTabelleECB> = emptyList(),
    val colorsArticles: List<ColorsArticles> = emptyList(),
    val articlesAcheteModele: List<ArticlesAcheteModele> = emptyList(),
    val tabelleSupplierArticlesRecived: List<TabelleSupplierArticlesRecived> = emptyList(),
    val tabelleSuppliersSA: List<TabelleSuppliersSA> = emptyList(),
    val mapArticleInSupplierStore: List<MapArticleInSupplierStore> = emptyList(),
    val placesOfArticelsInEacheSupplierSrore: List<PlacesOfArticelsInEacheSupplierSrore> = emptyList(),
    val placesOfArticelsInCamionette: List<PlacesOfArticelsInCamionette> = emptyList(),
    val showOnlyWithFilter: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)


class HeadOfViewModels(
    private val context: Context,
    private val repository: CategoriesRepository
) : ViewModel() {

    val _uiState = MutableStateFlow(CreatAndEditeInBaseDonnRepositeryModels())
    val uiState = _uiState.asStateFlow()

    private val _currentEditedArticle = MutableStateFlow<DataBaseArticles?>(null)
    val currentEditedArticle: StateFlow<DataBaseArticles?> = _currentEditedArticle.asStateFlow()

    private val _currentSupplierArticle = MutableStateFlow<TabelleSupplierArticlesRecived?>(null)
    val currentSupplierArticle: StateFlow<TabelleSupplierArticlesRecived?> = _currentSupplierArticle.asStateFlow()


    private val _uploadProgress = MutableStateFlow(100f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val _textProgress = MutableStateFlow("")
    val textProgress: StateFlow<String> = _textProgress.asStateFlow()

    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive: StateFlow<Boolean> = _isTimerActive.asStateFlow()

    private var timerJob: Job? = null

    var totalSteps = 100 // Total number of steps for the timer
    var currentStep = 0 // Current step in the process

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")
    private val refCategorieTabelee = firebaseDatabase.getReference("H_CategorieTabele")
    private val refColorsArticles = firebaseDatabase.getReference("H_ColorsArticles")
    private val refArticlesAcheteModele = firebaseDatabase.getReference("ArticlesAcheteModeleAdapted")
    val refTabelleSupplierArticlesRecived = firebaseDatabase.getReference("K_SupplierArticlesRecived")
    private val refTabelleSuppliersSA = firebaseDatabase.getReference("F_Suppliers")
    private val refMapArticleInSupplierStore = firebaseDatabase.getReference("L_MapArticleInSupplierStore")
    private val refClassmentsArtData = firebaseDatabase.getReference("H_ClassementsArticlesTabel")
    private val refPlacesOfArticelsInEacheSupplierSrore = firebaseDatabase.getReference("M_PlacesOfArticelsInEacheSupplierSrore")
    private val refPlacesOfArticelsInCamionette = firebaseDatabase.getReference("N_PlacesOfArticelsInCamionette")
    val viewModelImagesPath = File("/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne")

    var tempImageUri: Uri? = null
    private val currentDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    companion object {
        private const val MAX_WIDTH = 1024
        private const val MAX_HEIGHT = 1024
        private const val TAG = "HeadOfViewModels"
    }

    fun toggleFilter() {
        _uiState.update { currentState ->
            currentState.copy(showOnlyWithFilter = !currentState.showOnlyWithFilter)
        }
    }

    fun updateSmothUploadProgressBarCounterAndItText(nameFunInProgressBar: String = "",
                                                             progressDimunuentDe100A0: Int=100,
                                                             end:Boolean=false,
                                                             delayUi: Long = 0) {
        viewModelScope.launch {
            _uploadProgress.value = if (end) 0f else progressDimunuentDe100A0 .toFloat()
            _textProgress.value = nameFunInProgressBar

            delay(delayUi)
        }
    }

    init {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categoriesECB = categories) }
            }
        }
    }
    fun syncCategoriesFromFirebase() {
        viewModelScope.launch {
            try {
                repository.importCategoriesFromFirebase()
                // Optionally update UI or show a success message
            } catch (e: Exception) {
                // Handle any errors, update UI or show an error message
            }
        }
    }
    fun moveCategory(fromCategoryId: Long, toCategoryId: Long) {
        viewModelScope.launch {
            try {
                repository.moveCategory(
                    fromCategoryId = fromCategoryId,
                    toCategoryId = toCategoryId
                )
            } catch (e: Exception) {
                // Handle error if needed
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun reorderCategories(fromCategoryId: Long, toCategoryId: Long) {
        viewModelScope.launch {
            try {
                repository.reorderCategories(
                    fromCategoryId = fromCategoryId,
                    toCategoryId = toCategoryId
                )
            } catch (e: Exception) {
                // Handle error if needed
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateArticleDisponibility(articleId: Long, newDisponibilityState: String) {
        viewModelScope.launch {
            try {
                // Update the state with new article disponibility
                _uiState.update { currentState ->
                    val updatedArticles = updateArticleDisponibilityState(
                        currentState.articlesBaseDonneECB,
                        articleId,
                        newDisponibilityState
                    )
                    currentState.copy(articlesBaseDonneECB = updatedArticles)
                }

                // Update Firebase
                refClassmentsArtData
                    .child(articleId.toString())
                    .child("diponibilityState")
                    .setValue(newDisponibilityState)
                    .await()
            } catch (e: Exception) {
                // Handle error if needed
                _uiState.update { currentState ->
                    currentState.copy(error = e.message)
                }
            }
        }
    }

    private fun updateArticleDisponibilityState(
        articles: List<DataBaseArticles>,
        articleId: Long,
        newDisponibilityState: String
    ): List<DataBaseArticles> {
        return articles.map { article ->
            if (article.idArticle.toLong() == articleId) {
                article.copy(diponibilityState = newDisponibilityState)
            } else {
                article
            }
        }
    }
    fun goUpAndshiftsAutersDownCategoryPositions(fromCategoryId: Long, toCategoryId: Long) {
        viewModelScope.launch {
            // Get current categories from uiState
            val currentCategories = _uiState.value.categoriesECB

            // Reorder categories
            val updatedCategories = reorderCategories(currentCategories, fromCategoryId, toCategoryId)

            // Update the state with new categories
            _uiState.update { currentState ->
                currentState.copy(categoriesECB = updatedCategories)
            }

            // Update Firebase
            updateFirebaseCategories(updatedCategories)
        }
    }

    fun reorderCategories(
        categories: List<CategoriesTabelleECB>,
        fromCategoryId: Long,
        toCategoryId: Long
    ): List<CategoriesTabelleECB> {
        val mutableCategories = categories.toMutableList()
        val fromIndex = mutableCategories.indexOfFirst { it.idCategorieInCategoriesTabele == fromCategoryId }
        val toIndex = mutableCategories.indexOfFirst { it.idCategorieInCategoriesTabele == toCategoryId }

        if (fromIndex != -1 && toIndex != -1) {
            val movedCategory = mutableCategories.removeAt(fromIndex)
            mutableCategories.add(toIndex, movedCategory)
            return mutableCategories.mapIndexed { index, category ->
                category.copy(idClassementCategorieInCategoriesTabele = (index + 1))
            }
        }
        return categories
    }

    private suspend fun updateFirebaseCategories(categories: List<CategoriesTabelleECB>) {
        refCategorieTabelee.removeValue().await()
        categories.forEach { category ->
            refCategorieTabelee.child(category.idCategorieInCategoriesTabele.toString())
                .setValue(category).await()
        }
    }

    fun updateArticleStatus(article: TabelleSupplierArticlesRecived) {
        viewModelScope.launch {
            try {
                _uiState.update { currentState ->
                    val updatedArticles = currentState.tabelleSupplierArticlesRecived.map {
                        if (it.aa_vid == article.aa_vid) article else it
                    }
                    currentState.copy(tabelleSupplierArticlesRecived = updatedArticles)
                }

                _currentSupplierArticle.update {
                    it?.takeIf { it.aa_vid == article.aa_vid }?.let { _ -> article }
                }

                refTabelleSupplierArticlesRecived.child(article.aa_vid.toString()).apply {
                    child("itsInFindedAskSupplierSA").setValue(article.itsInFindedAskSupplierSA)
                    child("disponibylityStatInSupplierStore").setValue(article.disponibylityStatInSupplierStore)
                }
            } catch (e: Exception) {
                // Log.e(TAG, "Error in updateArticleStatus", e)
            }
        }
    }

    fun startTimer() {
        if (_isTimerActive.value) return

        _isTimerActive.value = true
        currentStep = 0
        timerJob = viewModelScope.launch {
            val totalDuration = 2000L // 2 seconds
            val updateInterval = 20L // Update every 20ms for smooth animation

            for (elapsedTime in 0L..totalDuration step updateInterval) {
                if (!isActive) break

                val progress = ((totalDuration - elapsedTime) / totalDuration.toFloat()) * 100
                val remainingTime = (totalDuration - elapsedTime) / 1000f

                updateSmothUploadProgressBarCounterAndItText(
                    nameFunInProgressBar = "Timer: ${String.format("%.1f", remainingTime)}s",
                    progressDimunuentDe100A0 = progress.toInt(),
                    delayUi = updateInterval
                )

                delay(updateInterval)
            }

            if (isActive) {
                // Timer completed
                updateSmothUploadProgressBarCounterAndItText(
                    nameFunInProgressBar = "Timer completed!",
                    progressDimunuentDe100A0 = 0,
                    end = true
                )
            }
            _isTimerActive.value = false
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _isTimerActive.value = false
        updateSmothUploadProgressBarCounterAndItText(
            nameFunInProgressBar = "Timer stopped",
            progressDimunuentDe100A0 = 0,
            end = true
        )
    }



    fun importFromFirebase(refFireBase: String ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }
                updateSmothUploadProgressBarCounterAndItText("Importing data from $refFireBase", 90)

                val snapshot = firebaseDatabase.getReference(refFireBase).get().await()
                val importedArticles = snapshot.children.mapNotNull { it.getValue(DataBaseArticles::class.java) }

                _uiState.update { currentState ->
                    currentState.copy(
                        articlesBaseDonneECB = importedArticles,
                        isLoading = false
                    )
                }

                updateSmothUploadProgressBarCounterAndItText("Import completed", 0, true)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Import failed: ${e.message}") }
                updateSmothUploadProgressBarCounterAndItText("Import failed", 0, true)
            }
        }
    }
    /** Start [HeadOfViewModels]
     *
     * --------------------------------------------------"PS:To Claude Dont Delet This Comment"
     * ------------------------------------------------*/

    fun exportUiStateArticlesToFirebase(refFireBase: String ) {
        viewModelScope.launch {
            try {
                val articlesToExport = _uiState.value.articlesBaseDonneECB
                val ref = firebaseDatabase.getReference(refFireBase)

                updateSmothUploadProgressBarCounterAndItText()

                articlesToExport.forEachIndexed { index, article ->
                    ref.child(article.idArticle.toString()).setValue(article)

                    val progress = ((index + 1) / articlesToExport.size) * 100
                    updateSmothUploadProgressBarCounterAndItText("Exporting articles to $refFireBase",progress)

                }


                updateSmothUploadProgressBarCounterAndItText("Exporting articles to $refFireBase",end=true)
            } catch (e: Exception) {
                Log.e(TAG, "Error exporting articles to Firebase: ${e.message}")
                _uiState.update { it.copy(error = "Failed to export articles: ${e.message}") }
            }
        }
    }


    fun updateUploadProgressBarCounterAndItText(nameFunInProgressBar: String = "", addPLusTOCurrentStep: Int, stepProgress: Float = 100f, delayUi: Long = 0) {
        viewModelScope.launch {
            val stepSize = 100f / totalSteps
            val baseProgress = stepSize * (addPLusTOCurrentStep - 1)
            val additionalProgress = stepSize * (stepProgress / 100f)
            _uploadProgress.value = 100f - (baseProgress + additionalProgress).roundToInt().toFloat()
            _textProgress.value = nameFunInProgressBar

            delay(delayUi)
        }
    }



    fun updateColorsFromArticles() {
        viewModelScope.launch {
            val articles = _uiState.value.articlesBaseDonneECB
            val colors = mutableSetOf<String>()

            totalSteps = 4 // 1. Collecting colors, 2. Updating UI, 3. Updating Firebase, 4. Finalizing
            currentStep = 0

            // Step 1: Collecting colors
            updateUploadProgressBarCounterAndItText("Collecting Colors", ++currentStep, 0f)
            articles.forEachIndexed { index, article ->
                collectColors(article, colors)
                val progress = (index + 1).toFloat() / articles.size * 100f
                updateUploadProgressBarCounterAndItText("Collecting Colors", currentStep, progress, 10)
            }

            // Step 2: Updating UI
            updateUploadProgressBarCounterAndItText("Updating UI", ++currentStep, 0f)
            val updatedColors = createUpdatedColors(colors)
            updateUiState(updatedColors)
            updateUploadProgressBarCounterAndItText("Updating UI", currentStep, 100f)

            // Step 3: Updating Firebase
            updateUploadProgressBarCounterAndItText("Updating Colors in Firebase", ++currentStep, 0f)
            updateFirebase(updatedColors)

            // Step 4: Finalizing
            finalize()
        }
    }

    private fun collectColors(article: DataBaseArticles, colors: MutableSet<String>) {
        listOfNotNull(
            article.couleur1,
            article.couleur2,
            article.couleur3,
            article.couleur4
        ).forEach { color ->
            if (color.isNotEmpty()) {
                colors.add(color)
            }
        }
    }

    private fun createUpdatedColors(colors: Set<String>): List<ColorsArticles> {
        return colors.mapIndexed { index, colorName ->
            val (cleanColorName, emojis) = extractEmojisAndCleanName(colorName)
            ColorsArticles(
                idColore = index.toLong() + 1,
                nameColore = cleanColorName.trim(),
                iconColore = emojis.firstOrNull() ?: "",
                classementColore = index + 1
            )
        }
    }

    private fun extractEmojisAndCleanName(colorName: String): Pair<String, List<String>> {
        val emojis = mutableListOf<String>()
        val cleanNameBuilder = StringBuilder()

        var iterator = BreakIterator.getCharacterInstance()
        iterator.setText(colorName)

        var start = iterator.first()
        var end = iterator.next()

        while (end != BreakIterator.DONE) {
            val grapheme = colorName.substring(start, end)
            if (grapheme.any { it.isEmoji() }) {
                emojis.add(grapheme)
            } else {
                cleanNameBuilder.append(grapheme)
            }
            start = end
            end = iterator.next()
        }

        return Pair(cleanNameBuilder.toString(), emojis)
    }

    private fun Char.isEmoji(): Boolean {
        val type = Character.getType(this).toByte()
        return type == Character.SURROGATE.toByte() || type == Character.OTHER_SYMBOL.toByte()
    }

    private fun updateUiState(updatedColors: List<ColorsArticles>) {
        _uiState.update { currentState ->
            currentState.copy(colorsArticles = updatedColors)
        }
    }

    private suspend fun updateFirebase(updatedColors: List<ColorsArticles>) {
        updatedColors.forEachIndexed { index, color ->
            refColorsArticles.child(color.idColore.toString()).setValue(color)
            val progress = (index + 1).toFloat() / updatedColors.size * 100f
            updateUploadProgressBarCounterAndItText("Updating Colors in Firebase", currentStep, progress)
            delay(10) // Small delay to avoid blocking the UI
        }
    }

    private suspend fun finalize() {
        updateUploadProgressBarCounterAndItText("Finalizing Update", ++currentStep, 0f)
        repeat(20) {
            val progress = (it + 1).toFloat() / 20 * 100f
            updateUploadProgressBarCounterAndItText("Finalizing Update", currentStep, progress, 50)
        }
        updateUploadProgressBarCounterAndItText("Update Complete", totalSteps, 100f)
    }


    /**  [updateColorName]
     *
     * --------------------------------------------------"PS:To Claude Dont Delet This Comment"
     * ------------------------------------------------*/

    fun updateColorName(article: DataBaseArticles, index: Int, newColorName: String, ecraseLeDernie: Boolean = false) {
        val updatedArticle = when (index) {
            0 -> article.copy(couleur1 = newColorName)
            1 -> article.copy(couleur2 = newColorName)
            2 -> article.copy(couleur3 = newColorName)
            3 -> article.copy(couleur4 = newColorName)
            else -> article
        }

        // Update the article in the database
        refDBJetPackExport.child(updatedArticle.idArticle.toString()).setValue(updatedArticle)

        // Update or add the color to ColorsArticles
        val existingColor = _uiState.value.colorsArticles.find { it.nameColore == newColorName }
        if (existingColor == null) {
            val newColor = ColorsArticles(
                idColore = _uiState.value.colorsArticles.maxOfOrNull { it.idColore }?.plus(if (ecraseLeDernie) 0 else 1) ?: 1,
                nameColore = newColorName,
                classementColore = _uiState.value.colorsArticles.maxOfOrNull { it.classementColore }?.plus(if (ecraseLeDernie) 0 else 1) ?: 1
            )
            refColorsArticles.child(newColor.idColore.toString()).setValue(newColor)
        }

        // Update the _currentEditedArticle
        _currentEditedArticle.update { currentArticle ->
            if (currentArticle?.idArticle == article.idArticle) {
                when (index) {
                    0 -> currentArticle.copy(couleur1 = newColorName)
                    1 -> currentArticle.copy(couleur2 = newColorName)
                    2 -> currentArticle.copy(couleur3 = newColorName)
                    3 -> currentArticle.copy(couleur4 = newColorName)
                    else -> currentArticle
                }
            } else {
                currentArticle
            }
        }

        // Update the UI state
        _uiState.update { currentState ->
            currentState.copy(
                colorsArticles = currentState.colorsArticles.toMutableList().apply {
                    if (existingColor == null) {
                        add(ColorsArticles(
                            idColore = maxOfOrNull { it.idColore }?.plus(if (ecraseLeDernie) 0 else 1) ?: 1,
                            nameColore = newColorName,
                            classementColore = maxOfOrNull { it.classementColore }?.plus(if (ecraseLeDernie) 0 else 1) ?: 1
                        ))
                    }
                }
            )
        }
    }

/** Places Dialoge[PlacesOfArticelsInCamionette]
 * "PS:To Claude Dont Delet This Comment"*/

suspend fun updatePlaceInCamionette(editedPlace: PlacesOfArticelsInCamionette) {
    // Update the place in Firebase
    refPlacesOfArticelsInCamionette.child(editedPlace.idPlace.toString()).setValue(editedPlace)
        .addOnSuccessListener {
            // Update local state
            _uiState.update { currentState ->
                val updatedPlaces = currentState.placesOfArticelsInCamionette.map { place ->
                    if (place.idPlace == editedPlace.idPlace) editedPlace else place
                }
                currentState.copy(placesOfArticelsInCamionette = updatedPlaces)
            }
        }
        .addOnFailureListener { e ->
            Log.e(TAG, "Failed to update place in camionette", e)
        }
}

    suspend fun deletePlaceInCamionette(placeToDelete: PlacesOfArticelsInCamionette) {
        // Delete the place from Firebase
        refPlacesOfArticelsInCamionette.child(placeToDelete.idPlace.toString()).removeValue()
            .addOnSuccessListener {
                // Update local state
                _uiState.update { currentState ->
                    val updatedPlaces = currentState.placesOfArticelsInCamionette.filter { it.idPlace != placeToDelete.idPlace }
                    currentState.copy(placesOfArticelsInCamionette = updatedPlaces)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to delete place from camionette", e)
            }
    }
fun updatePlacesOrder(newOrder: List<PlacesOfArticelsInCamionette>) {
    viewModelScope.launch {
        try {
            // Update local state
            _uiState.update { currentState ->
                currentState.copy(placesOfArticelsInCamionette = newOrder)
            }

            // Prepare updates for Firebase
            val updates = mutableMapOf<String, Any>()
            newOrder.forEachIndexed { index, place ->
                val updatedPlace = place.copy(classement = index + 1)
                updates[updatedPlace.idPlace.toString()] = updatedPlace
            }

            // Update Firebase
            refPlacesOfArticelsInCamionette.updateChildren(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully updated places order in Firebase")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update places order in Firebase", e)
                    // Revert local state if Firebase update fails
                    revertLocalPlacesOrder()
                }

        } catch (e: Exception) {
            Log.e(TAG, "Error updating places order", e)
            // Revert local state if an exception occurs
            revertLocalPlacesOrder()
        }
    }
}

    private fun revertLocalPlacesOrder() {
        viewModelScope.launch {
            // Fetch the current order from Firebase and update local state
            refPlacesOfArticelsInCamionette.get()
                .addOnSuccessListener { snapshot ->
                    val currentOrder = snapshot.children.mapNotNull { it.getValue(PlacesOfArticelsInCamionette::class.java) }
                        .sortedBy { it.classement }
                    _uiState.update { currentState ->
                        currentState.copy(placesOfArticelsInCamionette = currentOrder)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to revert local places order", e)
                }
        }
    }

    fun updateArticlePackaging(articleToUpdate: ArticlesAcheteModele, packagingId: Long) {
        viewModelScope.launch {
            // Update local state
            _uiState.update { currentState ->
                val updatedArticles = currentState.articlesAcheteModele.map { article ->
                    if (article.vid == articleToUpdate.vid) {
                        article.copy(idArticlePlaceInCamionette = packagingId)
                    } else {
                        article
                    }
                }
                currentState.copy(articlesAcheteModele = updatedArticles)
            }
            refArticlesAcheteModele.child(articleToUpdate.vid.toString()).child("idArticlePlaceInCamionette").setValue(packagingId)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update article packaging", e)
                }            // Update Firebase
            refDBJetPackExport.child(articleToUpdate.idArticle.toString()).child("idArticlePlaceInCamionette").setValue(packagingId)
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to update article packaging", e)
                }
        }
    }

    fun addNewPlaceInCamionette(newPlace: PlacesOfArticelsInCamionette) {
        viewModelScope.launch {
            // Generate a new ID
            val newId = (_uiState.value.placesOfArticelsInCamionette.maxByOrNull { it.idPlace }?.idPlace ?: 0) + 1
            val newPlaceWithId = newPlace.copy(idPlace = newId)

            // Update local state
            _uiState.update { currentState ->
                currentState.copy(
                    placesOfArticelsInCamionette = currentState.placesOfArticelsInCamionette + newPlaceWithId
                )
            }

            // Update Firebase
            refPlacesOfArticelsInCamionette.child(newId.toString()).setValue(newPlaceWithId)
                .addOnSuccessListener {
                    // Handle success if needed
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add new place in camionette", e)
                }
        }
    }

    fun updateArticleCategory(articleId: Int, categoryId: Long, categoryName: String) {
        viewModelScope.launch {
            val articleRef = refDBJetPackExport.child(articleId.toString())
            articleRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    val article = mutableData.getValue(DataBaseArticles::class.java)
                    article?.let {
                        it.idCategorieNewMetode = categoryId
                        it.nomCategorie = categoryName
                        mutableData.value = it
                    }
                    return Transaction.success(mutableData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                    if (error != null) {
                        // Handle error
                        println("Failed to update article category: ${error.message}")
                    } else {
                        // Update UI state
                        _uiState.update { currentState ->
                            val updatedArticles = currentState.articlesBaseDonneECB.map { article ->
                                if (article.idArticle == articleId) {
                                    article.copy(
                                        idCategorieNewMetode = categoryId,
                                        nomCategorie = categoryName
                                    )
                                } else {
                                    article
                                }
                            }
                            currentState.copy(articlesBaseDonneECB = updatedArticles)
                        }

                        // Update refClassmentsArtData
                        updateClassmentsArtData(articleId, categoryId, categoryName)
                    }
                }
            })
        }
    }

    private fun updateClassmentsArtData(articleId: Int, categoryId: Long, categoryName: String) {
        val classementsRef = refClassmentsArtData.child(articleId.toString())
        classementsRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val classementsArticle = mutableData.getValue(ClassementsArticlesTabel::class.java)
                classementsArticle?.let {
                    it.idCategorie = categoryId.toDouble()
                    it.nomCategorie = categoryName
                    mutableData.value = it
                }
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (error != null) {
                    println("Failed to update ClassementsArticlesTabel: ${error.message}")
                } else {
                    println("Successfully updated ClassementsArticlesTabel for article $articleId")
                }
            }
        })
    }
    fun addNewCategory(newCategory: CategoriesTabelleECB) {
        viewModelScope.launch {
            // Update local state
            _uiState.update { currentState ->
                currentState.copy(categoriesECB = currentState.categoriesECB + newCategory)
            }

            // Update Firebase
            refCategorieTabelee.child(newCategory.idCategorieInCategoriesTabele.toString()).setValue(newCategory)
                .addOnSuccessListener {
                    // Handle success if needed
                }
                .addOnFailureListener { e ->
                    // Handle failure if needed
                    Log.e(TAG, "Failed to add new category", e)
                }
        }
    }
    fun updateSupplierVocalFrencheName(supplierId: Long, newName: String) {
        viewModelScope.launch {
            val currentSuppliers = _uiState.value.tabelleSuppliersSA
            val updatedSuppliers = currentSuppliers.map { supplier ->
                if (supplier.idSupplierSu == supplierId) {
                    supplier.copy(nameInFrenche = newName)
                } else {
                    supplier
                }
            }

            // Update local state
            _uiState.update { currentState ->
                currentState.copy(tabelleSuppliersSA = updatedSuppliers)
            }

            // Update Firebase
            refTabelleSuppliersSA.child(supplierId.toString()).child("supplierNameInFrenche").setValue(newName)
                .addOnSuccessListener {
                    // Handle success if needed
                }
                .addOnFailureListener { e ->
                    // Handle failure if needed
                    Log.e("HeadOfViewModels", "Failed to update supplier vocal Arab name", e)
                }
        }
    }

    fun updateSupplierVocalArabName(supplierId: Long, newName: String) {
        viewModelScope.launch {
            val currentSuppliers = _uiState.value.tabelleSuppliersSA
            val updatedSuppliers = currentSuppliers.map { supplier ->
                if (supplier.idSupplierSu == supplierId) {
                    supplier.copy(nomVocaleArabeDuSupplier = newName)
                } else {
                    supplier
                }
            }

            // Update local state
            _uiState.update { currentState ->
                currentState.copy(tabelleSuppliersSA = updatedSuppliers)
            }

            // Update Firebase
            refTabelleSuppliersSA.child(supplierId.toString()).child("nomVocaleArabeDuSupplier").setValue(newName)
                .addOnSuccessListener {
                    // Handle success if needed
                }
                .addOnFailureListener { e ->
                    // Handle failure if needed
                    Log.e("HeadOfViewModels", "Failed to update supplier vocal Arab name", e)
                }
        }
    }
        fun addOrUpdatePlacesOfArticelsInEacheSupplierSrore(
        placeId: Long,
        idCombinedIdArticleIdSupplier: String,
        idArticle: Long,
        idSupp: Long
    ) {
        viewModelScope.launch {
            try {
                // Check if the article exists in the current list
                val existingArticle = _uiState.value.placesOfArticelsInEacheSupplierSrore.find {
                    it.idCombinedIdArticleIdSupplier == idCombinedIdArticleIdSupplier
                }

                val updatedArticles = if (existingArticle != null) {
                    // Update existing article
                    _uiState.value.placesOfArticelsInEacheSupplierSrore.map { article ->
                        if (article.idCombinedIdArticleIdSupplier == idCombinedIdArticleIdSupplier) {
                            article.copy(idPlace = placeId)
                        } else {
                            article
                        }
                    }
                } else {
                    // Add new article
                    val newArticle = PlacesOfArticelsInEacheSupplierSrore(
                        idCombinedIdArticleIdSupplier = idCombinedIdArticleIdSupplier,
                        idPlace = placeId,
                        idArticle = idArticle,
                        idSupplierSu = idSupp
                    )
                    _uiState.value.placesOfArticelsInEacheSupplierSrore + newArticle
                }

                // Update the local state
                _uiState.update { it.copy(placesOfArticelsInEacheSupplierSrore = updatedArticles) }

                // Update or add to Firebase Realtime Database
                val articleToUpdate = updatedArticles.find { it.idCombinedIdArticleIdSupplier == idCombinedIdArticleIdSupplier }
                if (articleToUpdate != null) {
                    refPlacesOfArticelsInEacheSupplierSrore.child(idCombinedIdArticleIdSupplier).setValue(articleToUpdate)
                } else {
                    throw Exception("Article not found for updating in Firebase")
                }
            } catch (e: Exception) {
            }
        }
    }

    fun addNewPlace(name: String, idSupplierOfFloatingButtonClicked: Long?) {
        viewModelScope.launch {
            idSupplierOfFloatingButtonClicked?.let { supplierId ->
                val currentMaxId = _uiState.value.mapArticleInSupplierStore.maxOfOrNull { it.idPlace } ?: 0

                val newPlace = MapArticleInSupplierStore(
                    idPlace = currentMaxId + 1,
                    namePlace = name,
                    idSupplierOfStore = supplierId,
                    inRightOfPlace = false ,
                    itClassement = (currentMaxId + 1) .toInt()
                )


                // Ajouter à Firebase
                refMapArticleInSupplierStore.child(newPlace.idPlace.toString()).setValue(newPlace)

                // Mettre à jour l'état local
                _uiState.update { currentState ->
                    currentState.copy(
                        mapArticleInSupplierStore = currentState.mapArticleInSupplierStore + listOf(newPlace)
                    )
                }
            }
        }
    }



    fun moveArticlesToSupplier(
        articlesToMove: List<TabelleSupplierArticlesRecived>,
        toSupp: Long
    ) {
        viewModelScope.launch {
            try {

                articlesToMove.forEach { article ->
                    // Update the article in the local state
                    _uiState.update { currentState ->
                        val updatedArticles = currentState.tabelleSupplierArticlesRecived.map {
                            if (it.aa_vid == article.aa_vid) {
                                it.copy(idSupplierTSA = toSupp.toInt(), itsInFindedAskSupplierSA = false)
                            } else it
                        }
                        currentState.copy(tabelleSupplierArticlesRecived = updatedArticles)
                    }

                    // Update the article in the TabelleSupplierArticlesRecived database
                    refTabelleSupplierArticlesRecived.child(article.aa_vid.toString()).apply {
                        child("idSupplierTSA").setValue(toSupp.toInt())
                        child("itsInFindedAskSupplierSA").setValue(false)
                    }

                    // Update the corresponding BaseDonneECBTabelle entry
                    refDBJetPackExport.child(article.a_c_idarticle_c.toString()).apply {
                        child("lastIdSupplierChoseToBuy").setValue(toSupp)
                        child("dateLastIdSupplierChoseToBuy").setValue(currentDate)
                    }

                    // Update the currentEditedArticle if it matches the updated article
                    _currentEditedArticle.update { currentArticle ->
                        if (currentArticle?.idArticle?.toLong() == article.a_c_idarticle_c) {
                            currentArticle.copy(
                                lastIdSupplierChoseToBuy = toSupp,
                                dateLastIdSupplierChoseToBuy = currentDate
                            )
                        } else {
                            currentArticle
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in moveArticlesToSupplier", e)
            }
        }
    }
    fun reorderSuppliers(firstClickedSupplierId: Long, secondClickedSupplierId: Long) {
        val currentSuppliers = _uiState.value.tabelleSuppliersSA
        val reorderedSuppliers = reorderSuppliers(currentSuppliers, firstClickedSupplierId, secondClickedSupplierId)

        // Mettre à jour le classement en fonction de la nouvelle position
        val updatedSuppliers = reorderedSuppliers.mapIndexed { index, supplier ->
            supplier.copy(classmentSupplier = (index + 1).toDouble())
        }

        // Mettre à jour l'état de l'UI avec le nouvel ordre des fournisseurs
        _uiState.update { currentState ->
            currentState.copy(tabelleSuppliersSA = updatedSuppliers)
        }

        // Mettre à jour Firebase et le stockage local
        viewModelScope.launch {
            updateFirebaseAndLocaleSuppliers(updatedSuppliers)
        }
    }

    private fun reorderSuppliers(suppliers: List<TabelleSuppliersSA>, fromSupplierId: Long, toSupplierId: Long): List<TabelleSuppliersSA> {
        val mutableList = suppliers.toMutableList()

        // Always move supplier with ID 10 to the beginning
        val supplier10 = mutableList.find { it.idSupplierSu == 10L }
        if (supplier10 != null) {
            mutableList.remove(supplier10)
            mutableList.add(0, supplier10)
        }

        // Move supplier with ID 9 to the second position
        val supplier9 = mutableList.find { it.idSupplierSu == 9L }
        if (supplier9 != null) {
            mutableList.remove(supplier9)
            mutableList.add(1.coerceAtMost(mutableList.size), supplier9)
        }

        // Perform the requested reordering for other suppliers
        if (fromSupplierId != 10L && fromSupplierId != 9L && toSupplierId != 10L && toSupplierId != 9L) {
            val fromIndex = mutableList.indexOfFirst { it.idSupplierSu == fromSupplierId }
            val toIndex = mutableList.indexOfFirst { it.idSupplierSu == toSupplierId }

            if (fromIndex != -1 && toIndex != -1) {
                val movedSupplier = mutableList.removeAt(fromIndex)
                mutableList.add(toIndex, movedSupplier)
            }
        }

        return mutableList
    }

    private suspend fun updateFirebaseAndLocaleSuppliers(suppliers: List<TabelleSuppliersSA>) {
        suppliers.forEach { supplier ->
            refTabelleSuppliersSA.child(supplier.idSupplierSu.toString()).setValue(supplier)
        }
    }

/*2->Section Suppliers Commendes Manager -------------------*/

    /* Start*/

    init {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            _uiState.update { it.copy(isLoading = true) }
            currentStep = 0
            totalSteps = 10 // Update this if you change the number of steps

            updateUploadProgressBarCounterAndItText("Starting data fetch", ++currentStep, 0f)

            val articles = fetchArticles()
            updateUploadProgressBarCounterAndItText("Fetched articles", ++currentStep, 100f)

            val colorsArticles = fetchColorsArticles()
            updateUploadProgressBarCounterAndItText("Fetched colors", ++currentStep, 100f)

            val supplierArticlesRecived = fetchSupplierArticles()
            updateUploadProgressBarCounterAndItText("Fetched supplier articles", ++currentStep, 100f)

            val suppliersSA = fetchSuppliers()
            updateUploadProgressBarCounterAndItText("Fetched suppliers", ++currentStep, 100f)

            val mapArticleInSupplierStore = fetchMapArticleInSupplierStore()
            updateUploadProgressBarCounterAndItText("Fetched article map", ++currentStep, 100f)

            val placesOfArticelsInEacheSupplierSrore = fetchPlacesOfArticelsInEacheSupplierSrore()
            updateUploadProgressBarCounterAndItText("Fetched supplier store places", ++currentStep, 100f)

            val placesOfArticelsInCamionette = fetchPlacesOfArticelsInCamionette()
            updateUploadProgressBarCounterAndItText("Fetched camionette places", ++currentStep, 100f)

            val articlesAcheteModele = fetchArticlesAcheteModele()
            updateUploadProgressBarCounterAndItText("Fetched purchased articles", ++currentStep, 100f)

            updateUiState(
                articles, supplierArticlesRecived, suppliersSA,
                mapArticleInSupplierStore, placesOfArticelsInEacheSupplierSrore,
                placesOfArticelsInCamionette, articlesAcheteModele, colorsArticles
            )
            updateUploadProgressBarCounterAndItText("Data fetch complete", totalSteps, 100f)
        } catch (e: Exception) {
            handleError("Failed to load data from Firebase", e)
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun fetchArticles() = refDBJetPackExport.get().await().children.mapNotNull { snapshot ->
        snapshot.getValue(DataBaseArticles::class.java)?.apply {
            idArticle = snapshot.key?.toIntOrNull() ?: 0
        }
    }
    private suspend fun fetchColorsArticles() = refColorsArticles.get().await().children
        .mapNotNull { it.getValue(ColorsArticles::class.java) }

    private suspend fun fetchSuppliers() = refTabelleSuppliersSA.get().await().children
        .mapNotNull { it.getValue(TabelleSuppliersSA::class.java) }
        .sortedBy{ it.classmentSupplier }

    private suspend fun fetchMapArticleInSupplierStore() = refMapArticleInSupplierStore.get().await().children
        .mapNotNull { it.getValue(MapArticleInSupplierStore::class.java) }
        .sortedBy { it.itClassement }


    private suspend fun fetchSupplierArticles() = refTabelleSupplierArticlesRecived.get().await().children
        .mapNotNull { it.getValue(TabelleSupplierArticlesRecived::class.java) }

    private suspend fun fetchPlacesOfArticelsInEacheSupplierSrore() = refPlacesOfArticelsInEacheSupplierSrore.get().await().children
        .mapNotNull { it.getValue(PlacesOfArticelsInEacheSupplierSrore::class.java) }

    private suspend fun fetchPlacesOfArticelsInCamionette() = refPlacesOfArticelsInCamionette.get().await().children
        .mapNotNull { it.getValue(PlacesOfArticelsInCamionette::class.java) }

    private suspend fun fetchArticlesAcheteModele() = refArticlesAcheteModele.get().await().children
        .mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }

    private fun updateUiState(
        articles: List<DataBaseArticles>,
        supplierArticlesRecived: List<TabelleSupplierArticlesRecived>,
        suppliersSA: List<TabelleSuppliersSA>,
        mapArticleInSupplierStore: List<MapArticleInSupplierStore>,
        placesOfArticelsInEacheSupplierSrore: List<PlacesOfArticelsInEacheSupplierSrore>,
        placesOfArticelsInCamionette: List<PlacesOfArticelsInCamionette>,
        articlesAcheteModele: List<ArticlesAcheteModele>,
        colorsArticles: List<ColorsArticles>,
        ) {
        _uiState.update { it.copy(
            articlesBaseDonneECB = articles,
            tabelleSupplierArticlesRecived = supplierArticlesRecived,
            tabelleSuppliersSA = suppliersSA,
            mapArticleInSupplierStore = mapArticleInSupplierStore,
            placesOfArticelsInEacheSupplierSrore = placesOfArticelsInEacheSupplierSrore,
            placesOfArticelsInCamionette=placesOfArticelsInCamionette,
            articlesAcheteModele =articlesAcheteModele,
            colorsArticles =colorsArticles,
            isLoading = false
        ) }
    }





    /*3->Section Imports FireBase And Stor-------------------*/

    fun trensfertData(
        articleDao: ArticleDao,
        editeBaseDonneViewModel: EditeBaseDonneViewModel,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                totalSteps = 3
                currentStep = 0

                // Step 1: Import from Firebase to DataBaseDonne
                updateUploadProgressBarCounterAndItText("Importing from Firebase to DataBaseDonne", ++currentStep, 0f)
                importFromFirebaseToDataBaseDonne("e_DBJetPackExport", editeBaseDonneViewModel)
                updateUploadProgressBarCounterAndItText("Completed Firebase to DataBaseDonne import", currentStep, 100f)

                // Step 2: Transfer Firebase Data ArticlesAcheteModele
                updateUploadProgressBarCounterAndItText("Transferring ArticlesAcheteModele data", ++currentStep, 0f)
                transferFirebaseDataArticlesAcheteModele(context) { progress ->
                    updateUploadProgressBarCounterAndItText("Transferring ArticlesAcheteModele data", currentStep, progress)
                }

                // Step 3: Transfer from Telegram to SupplierArticlesRecived
                updateUploadProgressBarCounterAndItText("Transferring from Telegram to SupplierArticlesRecived", ++currentStep, 0f)
                transfertFromeTelegramToSupplierArticlesRecived(context) { progress ->
                    updateUploadProgressBarCounterAndItText("Transferring from Telegram to SupplierArticlesRecived", currentStep, progress)
                }

                updateUploadProgressBarCounterAndItText("Data transfer completed", totalSteps, 100f)
            } catch (e: Exception) {
                handleError("Import process failed", e)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private var nextVid = 1L

    private suspend fun transfertFromeTelegramToSupplierArticlesRecived(
        context: Context,
        onProgressUpdate: (Float) -> Unit
    ) {
        try {
            Log.d("TransferData", "Starting data transfer from Telegram to SupplierArticlesRecived")
            val refSource = firebaseDatabase.getReference("telegram")
            val refDestination = firebaseDatabase.getReference("K_SupplierArticlesRecived")

            Log.d("TransferData", "Removing existing data from destination")
            refDestination.removeValue().await()

            Log.d("TransferData", "Fetching data from source")
            val dataSnapshot = refSource.get().await()

            val dataMap = dataSnapshot.value as? Map<String, Map<String, Any>> ?: emptyMap()
            Log.d("TransferData", "Fetched ${dataMap.size} items from source")

            val totalItems = dataMap.size
            var processedItems = 0
            var skippedItems = 0

            dataMap.forEach { (key, value) ->
                val idArticle = (value["a01"] as? String)?.toLong() ?: 0L
                Log.d("TransferData", "Processing item with idArticle: $idArticle")

                val itsNewArticleFromeBacKE = value["a10"] as? String == "" &&
                        value["a12"] as? String == "" &&
                        value["a14"] as? String == "" &&
                        value["a16"] as? String == ""

                // Safely parse totalQuantity, defaulting to 0 if null or invalid
                val totalQuantity = (value["a18"] as? String)?.toIntOrNull() ?: 0

                if (totalQuantity > 0) {
                    Log.d("TransferData", "Valid total quantity: $totalQuantity")

                    // Find the corresponding article in articlesBaseDonneECB
                    val correspondingArticle = _uiState.value.articlesBaseDonneECB.find { it.idArticle.toLong() == idArticle }
                    val article = fromMap(value, correspondingArticle, itsNewArticleFromeBacKE)
                    Log.d("TransferData", "Created article with aa_vid: ${article.aa_vid}")
                    refDestination.child(article.aa_vid.toString()).setValue(article).await()

                    processedItems++
                } else {
                    Log.d("TransferData", "Skipping item $key due to invalid total quantity: $totalQuantity")
                    skippedItems++
                }

                val progress = (processedItems + skippedItems).toFloat() / totalItems * 100
                Log.d("TransferData", "Progress: $progress%")
                onProgressUpdate(progress)
            }
            initDataFromFirebase()

            Log.d("TransferData", "Data transfer completed. Processed: $processedItems, Skipped: $skippedItems")
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data transfer completed. Processed: $processedItems, Skipped: $skippedItems", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("TransferData", "Failed to transfer data", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data transfer failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fromMap(
        map: Map<String, Any?>,
        correspondingArticle: DataBaseArticles?,
        itsNewArticleFromeBacKE: Boolean
    ): TabelleSupplierArticlesRecived {
        return TabelleSupplierArticlesRecived(
            aa_vid = nextVid++,
            a_c_idarticle_c = if (itsNewArticleFromeBacKE) nextVid + 2500 else ((map["a01"] as? String)?.toLong() ?: 0L),
            a_d_nomarticlefinale_c = map["a02"] as? String ?: "",
            idSupplierTSA = generate(correspondingArticle),
            nmbrCat = correspondingArticle?.nmbrCat ?: 0,
            trouve_c = false,
            a_u_prix_1_q1_c = correspondingArticle?.monPrixVent ?: 0.0,
            a_q_prixachat_c = correspondingArticle?.monPrixAchat ?: 0.0,
            a_l_nmbunite_c = correspondingArticle?.nmbrUnite ?: 0,
            a_r_prixdevent_c = correspondingArticle?.monPrixVent ?: 0.0,
            nomclient = map["a08"] as? String ?: "",
            datedachate = map["a09"] as? String ?: "",
            a_d_nomarticlefinale_c_1 = map["a10"] as? String ?: "",
            quantityachete_c_1 = (map["a11"] as? String)?.toIntOrNull() ?: 0,
            a_d_nomarticlefinale_c_2 = map["a12"] as? String ?: "",
            quantityachete_c_2 = (map["a13"] as? String)?.toIntOrNull() ?: 0,
            a_d_nomarticlefinale_c_3 = map["a14"] as? String ?: "",
            quantityachete_c_3 = (map["a15"] as? String)?.toIntOrNull() ?: 0,
            a_d_nomarticlefinale_c_4 = map["a16"] as? String ?: "",
            quantityachete_c_4 = (map["a17"] as? String)?.toIntOrNull() ?: 0,
            totalquantity = (map["a18"] as? String)?.toIntOrNull() ?: 0,
            etatdecommendcolum = (map["a19"] as? String)?.toIntOrNull() ?: 0
        )
    }

    private fun generate(correspondingArticle: DataBaseArticles?): Int {
        if (correspondingArticle == null) {
            return 10 // Default value if correspondingArticle is null
        }

        val lastSupplierIdBuyedFrom = correspondingArticle.lastSupplierIdBuyedFrom ?: 0
        val lastIdSupplierChoseToBuy = correspondingArticle.lastIdSupplierChoseToBuy

        // Parse dates, defaulting to epoch time if parsing fails
        val dateLastSupplierIdBuyedFrom = correspondingArticle.dateLastSupplierIdBuyedFrom.let {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
            } catch (e: Exception) {
                0L
            }
        } ?: 0L

        val dateLastIdSupplierChoseToBuy = correspondingArticle.dateLastIdSupplierChoseToBuy.let {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)?.time
            } catch (e: Exception) {
                0L
            }
        } ?: 0L

        return when {
            dateLastSupplierIdBuyedFrom > dateLastIdSupplierChoseToBuy -> lastSupplierIdBuyedFrom.toInt()
            dateLastIdSupplierChoseToBuy > dateLastSupplierIdBuyedFrom -> lastIdSupplierChoseToBuy.toInt()
            lastSupplierIdBuyedFrom.toInt() != 0 -> lastSupplierIdBuyedFrom.toInt()
            lastIdSupplierChoseToBuy.toInt() != 0 -> lastIdSupplierChoseToBuy.toInt()
            else -> 10 // Default value if both IDs are 0
        }
    }

    private suspend fun transferFirebaseDataArticlesAcheteModele(
        context: android.content.Context,
        onProgressUpdate: (Float) -> Unit
    ) {
        var fireStorHistoriqueDesFactures: List<ArticlesAcheteModele> = emptyList()
        var totalItems = 0
        var processedItems = 0
        var skippedItems = 0

        try {
            val firestore = Firebase.firestore
            val querySnapshot = firestore.collection("HistoriqueDesFactures").get().await()
            fireStorHistoriqueDesFactures = querySnapshot.documents.mapNotNull { document ->
                document.toObject(ArticlesAcheteModele::class.java)
            }
            Log.d("TransferDebug", "Fetched ${fireStorHistoriqueDesFactures.size} items from HistoriqueDesFactures")
        } catch (e: Exception) {
            Log.e("TransferDebug", "Error getting documents from Firestore: ", e)
        }

        val refSource = Firebase.database.getReference("ArticlesAcheteModele")
        val refDestination = Firebase.database.getReference("ArticlesAcheteModeleAdapted")
        try {
            refDestination.removeValue().await()
            Log.d("TransferDebug", "Cleared destination reference")

            val dataSnapshot = refSource.get().await()
            val dataMap = dataSnapshot.value as? Map<String, Map<String, Any>> ?: emptyMap()

            totalItems = dataMap.size
            Log.d("TransferDebug", "Total items to process: $totalItems")

            var maxIdArticle = 0

            // Find the maximum idArticle
            dataMap.forEach { (_, value) ->
                val idArticle = (value["idarticle_c"] as? Long) ?: 0
                if (idArticle > maxIdArticle) {
                    maxIdArticle = idArticle.toInt()
                }
            }
            Log.d("TransferDebug", "Max idArticle found: $maxIdArticle")

            dataMap.forEach { (key, value) ->
                Log.d("TransferDebug", "Processing item with key: $key")

                val idArticle = (value["idarticle_c"] as? Long) ?: 0
                val nomClient = value["nomclient_c"] as? String ?: ""
                Log.d("TransferDebug", "idArticle: $idArticle, nomClient: $nomClient")

                // Instead of querying DAO, find the matching article in articlesBaseDonneECB
                val baseDonne =_uiState.value.articlesBaseDonneECB.find { it.idArticle.toLong() == idArticle }
                if (baseDonne == null) {
                    Log.w("TransferDebug", "Article not found in articlesBaseDonneECB for idArticle: $idArticle")
                }

                val matchingHistorique = fireStorHistoriqueDesFactures.find { it.idArticle == idArticle && it.nomClient == nomClient }
                val monPrixVentFireStoreBM = matchingHistorique?.monPrixVentFireStoreBM ?: 0.0
                Log.d("TransferDebug", "Matching historique found: ${matchingHistorique != null}, monPrixVentFireStoreBM: $monPrixVentFireStoreBM")

                val itsNewArticleFromeBacKE = value["nomarticlefinale_c_1"] as? String == "" &&
                        value["nomarticlefinale_c_2"] as? String == "" &&
                        value["nomarticlefinale_c_3"] as? String == "" &&
                        value["nomarticlefinale_c_4"] as? String == ""
                val generatedID = if (itsNewArticleFromeBacKE) maxIdArticle + 2000 else idArticle
                Log.d("TransferDebug", "itsNewArticleFromeBacKE: $itsNewArticleFromeBacKE, generatedID: $generatedID")

                // Filter entries where totalquantity is empty or null
                val totalQuantity = (value["totalquantity"] as? Number)?.toInt()
                if (totalQuantity != null && totalQuantity > 0) {
                    Log.d("TransferDebug", "Valid totalQuantity: $totalQuantity")

                    val article = baseDonne?.let {
                        val vid = (value["id"] as? Long) ?: 0
                        val nomArticleFinale = if (itsNewArticleFromeBacKE) (value["nomarticlefinale_c"] as? String)?.uppercase() ?: "" else value["nomarticlefinale_c"] as? String ?: ""
                        val prixAchat = if (itsNewArticleFromeBacKE) 0.0 else it.monPrixAchat
                        val nmbrunitBC = roundToOneDecimal((value["nmbunite_c"] as? Number)?.toDouble() ?: 0.0)
                        val clientPrixVentUnite = roundToOneDecimal((value["prixdevent_c"] as? Number)?.toDouble() ?: 0.0)
                        val dateDachate = value["datedachate"] as? String ?: ""

                        Log.d("TransferDebug", "ArticlesAcheteModele creation values:")
                        Log.d("TransferDebug", "vid: $vid, nomArticleFinale: $nomArticleFinale")
                        Log.d("TransferDebug", "prixAchat: $prixAchat, nmbrunitBC: $nmbrunitBC, clientPrixVentUnite: $clientPrixVentUnite")
                        Log.d("TransferDebug", "dateDachate: $dateDachate")

                        val article = ArticlesAcheteModele(
                            vid = vid,
                            idArticle = generatedID.toLong(),
                            nomArticleFinale = nomArticleFinale,
                            prixAchat = prixAchat,
                            nmbrunitBC = nmbrunitBC,
                            clientPrixVentUnite = clientPrixVentUnite,
                            nomClient = nomClient,
                            dateDachate = dateDachate,
                            nomCouleur1 = value["nomarticlefinale_c_1"] as? String ?: "",
                            quantityAcheteCouleur1 = (value["quantityachete_c_1"] as? Number)?.toInt() ?: 0,
                            nomCouleur2 = value["nomarticlefinale_c_2"] as? String ?: "",
                            quantityAcheteCouleur2 = (value["quantityachete_c_2"] as? Number)?.toInt() ?: 0,
                            nomCouleur3 = value["nomarticlefinale_c_3"] as? String ?: "",
                            quantityAcheteCouleur3 = (value["quantityachete_c_3"] as? Number)?.toInt() ?: 0,
                            nomCouleur4 = value["nomarticlefinale_c_4"] as? String ?: "",
                            quantityAcheteCouleur4 = (value["quantityachete_c_4"] as? Number)?.toInt() ?: 0,
                            totalQuantity = totalQuantity,
                            nonTrouveState = false,
                            verifieState = false,
                            typeEmballage = if (it.cartonState == "itsCarton" || it.cartonState == "Carton") "Carton" else "Boit",
                            choisirePrixDepuitFireStoreOuBaseBM = if (monPrixVentFireStoreBM == 0.0) "CardFireBase" else "CardFireStor",
                            monPrixVentBM = roundToOneDecimal((value["prix_1_q1_c"] as? Number)?.toDouble() ?: 0.0),
                            monPrixVentFireStoreBM = monPrixVentFireStoreBM,
                        )

                        article.apply {
                            monPrixVentUniterFireStoreBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monPrixVentFireStoreBM / nmbrunitBC else 0.0)
                            monBenificeFireStoreBM = roundToOneDecimal(monPrixVentFireStoreBM - prixAchat)
                            monBenificeUniterFireStoreBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monBenificeFireStoreBM / nmbrunitBC else 0.0)
                            totalProfitFireStoreBM = roundToOneDecimal((totalQuantity * monBenificeFireStoreBM))
                            monBenificeBM = roundToOneDecimal(monPrixVentBM - prixAchat)
                            monBenificeUniterBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monBenificeBM / nmbrunitBC else 0.0)
                            totalProfitBM = roundToOneDecimal(totalQuantity * monBenificeBM)
                            monPrixAchatUniterBC = roundToOneDecimal(if (nmbrunitBC != 0.0) prixAchat / nmbrunitBC else 0.0)
                            monPrixVentUniterBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monPrixVentBM / nmbrunitBC else 0.0)
                            benificeDivise = roundToOneDecimal(((clientPrixVentUnite * nmbrunitBC) - prixAchat) / 2)
                            clientBenificeBM = roundToOneDecimal((clientPrixVentUnite * nmbrunitBC) - monPrixVentBM)

                            Log.d("TransferDebug", "Calculated values:")
                            Log.d("TransferDebug", "monPrixVentUniterFireStoreBM: $monPrixVentUniterFireStoreBM")
                            Log.d("TransferDebug", "monBenificeFireStoreBM: $monBenificeFireStoreBM")
                            Log.d("TransferDebug", "monBenificeUniterFireStoreBM: $monBenificeUniterFireStoreBM")
                            Log.d("TransferDebug", "totalProfitFireStoreBM: $totalProfitFireStoreBM")
                            Log.d("TransferDebug", "monBenificeBM: $monBenificeBM")
                            Log.d("TransferDebug", "monBenificeUniterBM: $monBenificeUniterBM")
                            Log.d("TransferDebug", "totalProfitBM: $totalProfitBM")
                            Log.d("TransferDebug", "monPrixAchatUniterBC: $monPrixAchatUniterBC")
                            Log.d("TransferDebug", "monPrixVentUniterBM: $monPrixVentUniterBM")
                            Log.d("TransferDebug", "benificeDivise: $benificeDivise")
                            Log.d("TransferDebug", "clientBenificeBM: $clientBenificeBM")
                        }

                        Log.d("TransferDebug", "Created ArticlesAcheteModele object")
                        article
                    }

                    if (article != null) {
                        refDestination.child(article.vid.toString()).setValue(article).await()
                        Log.d("TransferDebug", "Article transferred successfully: ${article.vid}")
                        processedItems++
                    } else {
                        Log.w("TransferDebug", "Article object is null, skipping transfer for idArticle: $idArticle")
                        skippedItems++
                    }
                } else {
                    Log.w("TransferDebug", "Skipping item due to invalid totalQuantity: $totalQuantity")
                    skippedItems++
                }

                onProgressUpdate(processedItems.toFloat() / totalItems)
            }

            Log.d("TransferDebug", "Transfer completed. Processed: $processedItems, Skipped: $skippedItems, Total: $totalItems")

            // Display success message when data transfer is completed
            withContext(Dispatchers.Main) {
                if (processedItems == totalItems) {
                    Toast.makeText(context, "Data transfer completed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Data transfer completed with some issues. Check logs.", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("TransferDebug", "Failed to transfer data", e)

            // Display failure message with error details
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data transfer failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /*1->Section Creat + Handel IMGs Articles -------------------*/

    fun updateAndCalculateAuthersField(textFieldValue: String, columnToChange: String, article: DataBaseArticles) {
        val updatedArticle = article.copy().apply {
            // Update the specific field
            when (columnToChange) {
                "nomArticleFinale" -> nomArticleFinale = textFieldValue
                "nmbrUnite" -> nmbrUnite = textFieldValue.toIntOrNull() ?: nmbrUnite
                "clienPrixVentUnite" -> clienPrixVentUnite = textFieldValue.toDoubleOrNull() ?: clienPrixVentUnite
                "monPrixVent" -> monPrixVent = textFieldValue.toDoubleOrNull() ?: monPrixVent
                "monBenfice" -> monBenfice = textFieldValue.toDoubleOrNull() ?: monBenfice
                "benificeClient" -> benificeClient = textFieldValue.toDoubleOrNull() ?: benificeClient
                "monPrixAchat" -> monPrixAchat = textFieldValue.toDoubleOrNull() ?: monPrixAchat
                "monPrixAchatUniter" -> monPrixAchatUniter = textFieldValue.toDoubleOrNull() ?: monPrixAchatUniter
                "monPrixVentUniter" -> monPrixVentUniter = textFieldValue.toDoubleOrNull() ?: monPrixVentUniter
                "monBeneficeUniter" -> monBeneficeUniter = textFieldValue.toDoubleOrNull() ?: monBeneficeUniter
                else -> {
                    Log.w(TAG, "Unhandled column: $columnToChange")
                }
            }

            // Only recalculate if it's not the nomArticleFinale field
            if (columnToChange != "nomArticleFinale") {
                // Calculate derived values
                prixDeVentTotaleChezClient = nmbrUnite * clienPrixVentUnite
                benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
                benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2

                // Recalculate based on the updated field
                when (columnToChange) {
                    "monPrixVent" -> {
                        monBenfice = monPrixVent - monPrixAchat
                        monPrixVentUniter = monPrixVent / nmbrUnite
                        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
                        benificeClient = prixDeVentTotaleChezClient - monPrixVent
                    }
                    "monBenfice" -> {
                        monPrixVent = monBenfice + monPrixAchat
                        monPrixVentUniter = monPrixVent / nmbrUnite
                        monBeneficeUniter = monBenfice / nmbrUnite
                        benificeClient = prixDeVentTotaleChezClient - monPrixVent
                    }
                    "benificeClient" -> {
                        monPrixVent = prixDeVentTotaleChezClient - benificeClient
                        monBenfice = monPrixVent - monPrixAchat
                        monPrixVentUniter = monPrixVent / nmbrUnite
                        monBeneficeUniter = monBenfice / nmbrUnite
                    }
                    "monPrixAchat" -> {
                        monPrixAchatUniter = monPrixAchat / nmbrUnite
                        monBenfice = monPrixVent - monPrixAchat
                        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
                        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
                        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
                    }
                    "monPrixAchatUniter" -> {
                        monPrixAchat = monPrixAchatUniter * nmbrUnite
                        monBenfice = monPrixVent - monPrixAchat
                        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
                        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
                        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
                    }
                    "monPrixVentUniter" -> {
                        monPrixVent = monPrixVentUniter * nmbrUnite
                        monBenfice = monPrixVent - monPrixAchat
                        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
                        benificeClient = prixDeVentTotaleChezClient - monPrixVent
                    }
                    "monBeneficeUniter" -> {
                        monBenfice = monBeneficeUniter * nmbrUnite
                        monPrixVentUniter = monPrixAchatUniter + monBeneficeUniter
                        monPrixVent = monPrixVentUniter * nmbrUnite
                        benificeClient = prixDeVentTotaleChezClient - monPrixVent
                    }
                }

                // Validate calculations
                if (nmbrUnite == 0) {
                    monPrixAchatUniter = 0.0
                    monPrixVentUniter = 0.0
                    monBeneficeUniter = 0.0
                } else {
                    monPrixAchatUniter = monPrixAchat / nmbrUnite
                    monPrixVentUniter = monPrixVent / nmbrUnite
                    monBeneficeUniter = monBenfice / nmbrUnite
                }
            }
        }

        _uiState.update { state ->
            val updatedArticles = state.articlesBaseDonneECB.map {
                if (it.idArticle == updatedArticle.idArticle) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }

        updateCurrentEditedArticle(updatedArticle)

        viewModelScope.launch {
            try {
                val articleRef = refDBJetPackExport.child(updatedArticle.idArticle.toString())
                articleRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        mutableData.value = updatedArticle
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                        if (error != null) {
                            handleError("Failed to update article in Firebase", error.toException())
                        } else {
                            Log.d(TAG, "Article updated successfully in Firebase")
                        }
                    }
                })
            } catch (e: Exception) {
                handleError("Failed to update article in Firebase", e)
                // Implement retry logic here if needed
            }
        }
    }
    fun toggleAffichageUniteState(article: DataBaseArticles) {
        val updatedArticle = article.copy(affichageUniteState = !article.affichageUniteState)

        _uiState.update { state ->
            val updatedArticles = state.articlesBaseDonneECB.map {
                if (it.idArticle == updatedArticle.idArticle) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }
        updateCurrentEditedArticle(updatedArticle)

        viewModelScope.launch {
            try {
                val articleRef = refDBJetPackExport.child(updatedArticle.idArticle.toString())
                articleRef.child("affichageUniteState").setValue(updatedArticle.affichageUniteState)
                    .addOnSuccessListener {
                        Log.d(TAG, "Article affichageUniteState updated successfully in Firebase")
                    }
                    .addOnFailureListener { e ->
                        handleError("Failed to update article affichageUniteState in Firebase", e)
                    }
            } catch (e: Exception) {
                handleError("Failed to update article affichageUniteState in Firebase", e)
            }
        }
    }
    private fun updateLocalAndFireBaseArticle(updatedArticle: DataBaseArticles) {
        _uiState.update { state ->
            val updatedArticles = state.articlesBaseDonneECB.map {
                if (it.idArticle == updatedArticle.idArticle) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }

        updateCurrentEditedArticle(updatedArticle)

        viewModelScope.launch {
            try {
                val articleRef = refDBJetPackExport.child(updatedArticle.idArticle.toString())
                articleRef.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(mutableData: MutableData): Transaction.Result {
                        mutableData.value = updatedArticle
                        return Transaction.success(mutableData)
                    }

                    override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                        if (error != null) {
                            handleError("Failed to update article in Firebase", error.toException())
                        } else {
                            Log.d(TAG, "Article updated successfully in Firebase")
                        }
                    }
                })
            } catch (e: Exception) {
                handleError("Failed to update article in Firebase", e)
                // Implement retry logic here if needed
            }
        }
    }
    fun isFirstArticle(id: Int): Boolean {
        return _uiState.value.articlesBaseDonneECB.firstOrNull()?.idArticle == id
    }

    fun isLastArticle(id: Int): Boolean {
        return _uiState.value.articlesBaseDonneECB.lastOrNull()?.idArticle == id
    }
    fun updateCurrentEditedArticle(article: DataBaseArticles?) {
        _currentEditedArticle.value = article
    }

    fun getArticleById(id: Int): DataBaseArticles? {
        return _uiState.value.articlesBaseDonneECB.find { it.idArticle == id }
    }

    fun getPreviousArticleId(currentId: Int): Int {
        val articles = _uiState.value.articlesBaseDonneECB
        val currentIndex = articles.indexOfFirst { it.idArticle == currentId }
        return if (currentIndex > 0) articles[currentIndex - 1].idArticle else articles.last().idArticle
    }

    fun getNextArticleId(currentId: Int): Int {
        val articles = _uiState.value.articlesBaseDonneECB
        val currentIndex = articles.indexOfFirst { it.idArticle == currentId }
        return if (currentIndex < articles.size - 1) articles[currentIndex + 1].idArticle else articles.first().idArticle
    }












    fun setImagesInStorageFireBase(articleId: Int, colorIndex: Int) {
        viewModelScope.launch {
            val fileName = "${articleId}_$colorIndex.jpg"
            val localFile = File(viewModelImagesPath, fileName)
            val storageRef = Firebase.storage.reference.child("Images Articles Data Base/$fileName")
            //TODO fait que les operations soit enregstre don une list a chaque foit termine il se coche check termine et aller au prochen pour le converti et update le stoage
            try {
                updateUploadProgressBarCounterAndItText("setImagesInStorageFireBase", totalSteps, 100f)


                // Convert image to WebP format
                val webpImage = withContext(Dispatchers.IO) {
                    convertToWebP(localFile)
                }

                if (webpImage == null) {
                    throw IllegalStateException("Failed to convert image to WebP")
                }

                // Upload the WebP image
                val uploadTask = storageRef.putBytes(webpImage)

                updateUploadProgressBarCounterAndItText("setImagesInStorageFireBase", totalSteps, 100f)

//                uploadTask.addOnProgressListener { taskSnapshot ->
//                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
//                    updateProgressWithDelay(progress.toFloat())
//                }.await()

                val downloadUrl = storageRef.downloadUrl.await() //TODO pk le sortie n ai pas on webp
                //HeadOfViewModels         D  Image uploaded successfully: 1066_1.jpg, URL: https://firebasestorage.googleapis.com/v0/b/abdelwahab-jemla-com.appspot.com/o/Images%20Articles%20Data%20Base%2F1066_1.jpg?alt=media&token=611f4ed5-d094-496d-ba9e-23bdd42f7388
                Log.d(TAG, "Image uploaded successfully: $fileName, URL: $downloadUrl")

                updateUploadProgressBarCounterAndItText("setImagesInStorageFireBase", totalSteps, 100f)

            } catch (e: Exception) {
                handleError("Failed to upload image", e)
                updateUploadProgressBarCounterAndItText("setImagesInStorageFireBase", totalSteps, 100f)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private suspend fun convertToWebP(file: File): ByteArray? {
        return withContext(Dispatchers.IO) {
            try {
                if (!file.exists()) {
                    Log.e(TAG, "File does not exist: ${file.absolutePath}")
                    return@withContext null
                }

                Log.d(TAG, "Starting image conversion for file: ${file.absolutePath}")

                // Get image dimensions without loading the full bitmap
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, options)

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
                Log.d(TAG, "Calculated inSampleSize: ${options.inSampleSize}")

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false
                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

                if (bitmap == null) {
                    Log.e(TAG, "Failed to decode bitmap from file: ${file.absolutePath}")
                    return@withContext null
                }

                Log.d(TAG, "Successfully decoded bitmap. Size: ${bitmap.width}x${bitmap.height}")

                val outputStream = ByteArrayOutputStream()

                val compressionResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Use ImageDecoder for API 30+
                    val imageDecoder = ImageDecoder.createSource(file)
                    val decodedBitmap = ImageDecoder.decodeBitmap(imageDecoder)
                    decodedBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, outputStream)
                } else {
                    // Fallback for older Android versions
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream)
                }

                if (!compressionResult) {
                    Log.e(TAG, "Failed to compress bitmap to WebP")
                    return@withContext null
                }

                Log.d(TAG, "Successfully converted image to WebP")
                outputStream.toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, "Error converting image to WebP", e)
                null
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun handleError(message: String, exception: Exception) {
        Log.e(TAG, "$message: ${exception.message}")
        // You might want to update your UI or error state here
    }



    private suspend fun copyImage(sourceUri: Uri, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!viewModelImagesPath.exists()) {
                    viewModelImagesPath.mkdirs()
                }

                val destFile = File(viewModelImagesPath, fileName)

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
                    //TODO fix Unresolved reference: context
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IOException("Failed to open input stream for URI: $sourceUri")

                Log.d(TAG, "Image copied successfully to ${destFile.absolutePath}")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to copy image", e)
                throw Exception("Failed to copy image: ${e.message}")
            }
        }
    }




    suspend fun addNewParentArticle(uri: Uri, category: CategoriesTabelleECB): DataBaseArticles {
        return withContext(Dispatchers.IO) {
            try {
                val newId = getNextArticleId()
                val fileName = "${newId}_1.jpg"
                copyImage(uri, fileName)

                val newClassementCate = calculateNewClassementCate(category)

                val newArticle = createNewArticle(newId, category, newClassementCate)
                ensureNewArticlesCategoryExists()
                updateDataBaseWithNewArticle(newArticle)
                updateClassementsArticlesTabel(newArticle, category)
                newArticle
            } catch (e: Exception) {
                handleError("Failed to process image", e)
                throw IllegalStateException("Failed to create new article", e)
            }
        }
    }

    private suspend fun updateClassementsArticlesTabel(article: DataBaseArticles, category: CategoriesTabelleECB) {
        try {
            val classementsArticle = ClassementsArticlesTabel(
                idArticle = article.idArticle.toLong(),
                nomArticleFinale = article.nomArticleFinale,
                idCategorie = category.idCategorieInCategoriesTabele.toDouble(),
                classementInCategoriesCT = 0.0, // You may want to calculate this value
                nomCategorie = article.nomCategorie,
                classementArticleAuCategorieCT = 0.0, // You may want to calculate this value
                itsNewArticleInCateWithID = true,
                classementCate = article.classementCate,
                diponibilityState = article.diponibilityState
            )

            refClassmentsArtData.child(article.idArticle.toString()).setValue(classementsArticle).await()
            Log.d(TAG, "ClassementsArticlesTabel updated successfully for article: ${article.idArticle}")
        } catch (e: Exception) {
            handleError("Failed to update ClassementsArticlesTabel", e)
        }
    }
    fun getCategoryByName(categoryName: String): CategoriesTabelleECB {
        return uiState.value.categoriesECB.find { it.nomCategorieInCategoriesTabele == categoryName }
            ?: throw IllegalArgumentException("Category not found: $categoryName")
    }
    private  fun calculateNewClassementCate(category: CategoriesTabelleECB): Double {
        return (uiState.value.articlesBaseDonneECB
            .filter { it.nomCategorie == category.nomCategorieInCategoriesTabele }
            .minOfOrNull { it.classementCate }
            ?.minus(1.0)
            ?: 0.0)
    }

    private fun createNewArticle(newId: Int, category: CategoriesTabelleECB, newClassementCate: Double): DataBaseArticles {
        return DataBaseArticles(
            idArticle = newId,
            nomArticleFinale = "New Article $newId",
            nomCategorie = category.nomCategorieInCategoriesTabele,
            diponibilityState = "",
            couleur1 = "Couleur 1",
            dateCreationCategorie = System.currentTimeMillis().toString(),
            classementCate = newClassementCate
        )
    }

    private suspend fun updateDataBaseWithNewArticle(article: DataBaseArticles) {
        try {
            refDBJetPackExport.child(article.idArticle.toString()).setValue(article).await()

            _uiState.update { currentState ->
                currentState.copy(
                    articlesBaseDonneECB = currentState.articlesBaseDonneECB + article
                )
            }

            Log.d(TAG, "New article added successfully: ${article.idArticle}")
        } catch (e: Exception) {
            handleError("Failed to add new article", e)
        }
    }

    fun addColorToArticle(uri: Uri, article: DataBaseArticles) {
        viewModelScope.launch {
            try {
                val nextColorField = getNextAvailableColorField(article)
                val fileName = "${article.idArticle}_${nextColorField.removePrefix("couleur")}.jpg"
                copyImage(uri, fileName)

                val updatedArticle = updateArticleWithNewColor(article, nextColorField)
                updateLocalAndFireBaseArticle(updatedArticle)
            } catch (e: Exception) {
                handleError("Failed to add color to article", e)
            }
        }
    }

    private fun getNextAvailableColorField(article: DataBaseArticles): String {
        return when {
            article.couleur2.isNullOrEmpty() -> "couleur2"
            article.couleur3.isNullOrEmpty() -> "couleur3"
            article.couleur4.isNullOrEmpty() -> "couleur4"
            else -> throw IllegalStateException("All color fields are filled")
        }
    }

    private fun updateArticleWithNewColor(article: DataBaseArticles, colorField: String): DataBaseArticles {
        return article.copy(
            couleur2 = if (colorField == "couleur2") "Couleur_2" else article.couleur2,
            couleur3 = if (colorField == "couleur3") "Couleur_3" else article.couleur3,
            couleur4 = if (colorField == "couleur4") "Couleur_4" else article.couleur4
        )
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



    private suspend fun ensureNewArticlesCategoryExists() {
        val newArticlesCategory = "New Articles"
        val existingCategories = _uiState.value.categoriesECB

        if (!existingCategories.any { it.nomCategorieInCategoriesTabele == newArticlesCategory }) {
            val newCategory = CategoriesTabelleECB(
                idClassementCategorieInCategoriesTabele = 1,
                nomCategorieInCategoriesTabele = newArticlesCategory
            )

            try {
                refCategorieTabelee.push().setValue(newCategory).await()

                _uiState.update { currentState ->
                    currentState.copy(
                        categoriesECB = currentState.categoriesECB + newCategory
                    )
                }
                Log.d(TAG, "'New Articles' category created successfully")
            } catch (e: Exception) {
                handleError("Failed to create 'New Articles' category", e)
            }
        }
    }

    fun deleteColor(article: DataBaseArticles, colorIndex: Int) {
        viewModelScope.launch {
            when (colorIndex) {
                0 -> updateLocalAndFireBaseArticle(article.copy(articleHaveUniteImages = false))
                1 -> deleteArticle(article)
                2 -> updateLocalAndFireBaseArticle(article.copy(couleur2 = ""))
                3 -> updateLocalAndFireBaseArticle(article.copy(couleur3 = ""))
                4 -> updateLocalAndFireBaseArticle(article.copy(couleur4 = ""))
            }
            deleteColorImage(article.idArticle, colorIndex)
        }
    }

    private fun deleteColorImage(articleId: Int, colorIndex: Int) {
        val baseImagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${articleId}_${colorIndex}"
        val uniteImagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${articleId}_Unite"

        val storageImgsRef = Firebase.storage.reference.child("Images Articles Data Base")

        // Delete the image from Firebase Storage for both jpg and webp
        listOf("jpg", "webp").forEach { extension ->
            val imageRef = storageImgsRef.child("${articleId}_${colorIndex}.$extension")
            val uniteRef = storageImgsRef.child("${articleId}_Unite.$extension")

            // Delete unite image only if colorIndex is 0
            if (colorIndex == 0) {
                uniteRef.delete().addOnFailureListener { exception ->
                    Log.e("Firebase", "Error deleting unite image from Firebase Storage", exception)
                }
            }

            imageRef.delete().addOnSuccessListener {
                // Image deleted successfully from Firebase Storage
            }.addOnFailureListener { exception ->
                Log.e("Firebase", "Error deleting image from Firebase Storage", exception)
            }
        }

        // Delete local files
        listOf("jpg", "webp").forEach { extension ->
            val file = File("$baseImagePath.$extension")
            if (file.exists()) {
                file.delete()
            }

            // Delete unite image file only if colorIndex is 0
            if (colorIndex == 0) {
                val uniteFile = File("$uniteImagePath.$extension")
                if (uniteFile.exists()) {
                    uniteFile.delete()
                }
            }
        }
    }

    fun addUniteImageToArticle(uri: Uri, article: DataBaseArticles) {
        viewModelScope.launch {
            try {
                val fileName = "${article.idArticle}_Unite.jpg"
                copyImage(uri, fileName)

                val updatedArticle = article.copy(articleHaveUniteImages = true)
                updateLocalAndFireBaseArticle(updatedArticle)
            } catch (e: Exception) {
                handleError("Failed to add unite image to article", e)
            }
        }
    }

    private suspend fun deleteArticle(article: DataBaseArticles) {
        try {
            refDBJetPackExport.child(article.idArticle.toString()).removeValue().await()
            _uiState.update { currentState ->
                currentState.copy(
                    articlesBaseDonneECB = currentState.articlesBaseDonneECB.filter { it.idArticle != article.idArticle }
                )
            }
            for (i in 0..4) {  // Changed to include 0 for unite image
                deleteColorImage(article.idArticle, i)
            }
        } catch (e: Exception) {
            handleError("Failed to delete article", e)
        }
    }

    fun processNewImage(uri: Uri, article: DataBaseArticles, colorIndex: Int) {
        viewModelScope.launch {
            try {
                val fileName = "${article.idArticle}_${if (colorIndex == 0) "Unite" else colorIndex}.jpg"
                copyImage(uri, fileName)

                val updatedArticle = when (colorIndex) {
                    0 -> article.copy(articleHaveUniteImages = true)
                    1 -> article.copy(couleur1 = "Couleur_1")
                    2 -> article.copy(couleur2 = "Couleur_2")
                    3 -> article.copy(couleur3 = "Couleur_3")
                    4 -> article.copy(couleur4 = "Couleur_4")
                    else -> article
                }
                updateLocalAndFireBaseArticle(updatedArticle)
            } catch (e: Exception) {
                handleError("Failed to process new image", e)
            }
        }
    }

    fun clearTempImage(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                tempImageUri?.let { uri ->
                    context.contentResolver.delete(uri, null, null)
                    tempImageUri = null
                }

                context.cacheDir.listFiles()?.forEach { file ->
                    if (file.name.startsWith("temp_image")) {
                        file.delete()
                    }
                }

                _currentEditedArticle.value?.let { article ->
                    _currentEditedArticle.value = article.copy()
                }

                Log.d(TAG, "Temporary image cleared successfully")
            } catch (e: Exception) {
                handleError("Failed to clear temporary image", e)
            }
        }
    }
}


