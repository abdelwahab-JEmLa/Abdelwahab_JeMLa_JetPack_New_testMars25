package b2_Edite_Base_Donne_With_Creat_New_Articls

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class HeadOfViewModels(
    private val context: Context,
    private val repositoryCreatAndEditDataBase: RepositeryCreatAndEditeDataBase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatAndEditeInBaseDonnRepositeryModels())
    val uiState = _uiState.asStateFlow()

    private val _currentEditedArticle = MutableStateFlow<BaseDonneECBTabelle?>(null)
    val currentEditedArticle: StateFlow<BaseDonneECBTabelle?> = _currentEditedArticle.asStateFlow()

    private val refDBJetPackExport = FirebaseDatabase.getInstance().getReference("e_DBJetPackExport")
    private val refCategorieTabelee = FirebaseDatabase.getInstance().getReference("H_CategorieTabele")
    private val storage = Firebase.storage
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

    fun updateCurrentEditedArticle(article: BaseDonneECBTabelle?) {
        _currentEditedArticle.value = article
    }

    fun updateAndCalculateAuthersField(textFieldValue: String, columnToChange: String, article: BaseDonneECBTabelle) {
        val updatedArticle = repositoryCreatAndEditDataBase.updateAndCalculateAuthersField(textFieldValue, columnToChange, article)
        updateLocalAndRemoteArticle(updatedArticle)
    }

    private fun updateLocalAndRemoteArticle(updatedArticle: BaseDonneECBTabelle) {
        _uiState.update { state ->
            val updatedArticles = state.articlesBaseDonneECB.map {
                if (it.idArticleECB == updatedArticle.idArticleECB) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }

        updateCurrentEditedArticle(updatedArticle)

        viewModelScope.launch {
            try {
                refDBJetPackExport.child(updatedArticle.idArticleECB.toString()).setValue(updatedArticle).await()
                Log.d(TAG, "Article updated successfully in Firebase")
            } catch (e: Exception) {
                handleError("Failed to update article in Firebase", e)
            }
        }
    }

    fun toggleFilter() {
        _uiState.update { repositoryCreatAndEditDataBase.toggleFilter(it) }
    }

    fun addNewParentArticle(uri: Uri, category: CategoriesTabelleECB) {
        viewModelScope.launch {
            try {
                val newId = getNextArticleId()
                val fileName = "${newId}_1.jpg"
                copyImage(context, uri, fileName)

                val newClassementCate = calculateNewClassementCate(category)

                val newArticle = createNewArticle(newId, category, newClassementCate)
                ensureNewArticlesCategoryExists()
                updateDataBaseWithNewArticle(newArticle)
            } catch (e: Exception) {
                handleError("Failed to process image", e)
            }
        }
    }

    private suspend fun calculateNewClassementCate(category: CategoriesTabelleECB): Double {
        return (uiState.value.articlesBaseDonneECB
            .filter { it.nomCategorie == category.nomCategorieInCategoriesTabele }
            .minOfOrNull { it.classementCate }
            ?.minus(1.0)
            ?: 0.0)
    }

    private fun createNewArticle(newId: Int, category: CategoriesTabelleECB, newClassementCate: Double): BaseDonneECBTabelle {
        return BaseDonneECBTabelle(
            idArticleECB = newId,
            nomArticleFinale = "New Article $newId",
            nomCategorie = category.nomCategorieInCategoriesTabele,
            diponibilityState = "",
            couleur1 = "Couleur 1",
            dateCreationCategorie = System.currentTimeMillis().toString(),
            classementCate = newClassementCate
        )
    }

    private suspend fun updateDataBaseWithNewArticle(article: BaseDonneECBTabelle) {
        try {
            refDBJetPackExport.child(article.idArticleECB.toString()).setValue(article).await()

            _uiState.update { currentState ->
                currentState.copy(
                    articlesBaseDonneECB = currentState.articlesBaseDonneECB + article
                )
            }

            Log.d(TAG, "New article added successfully: ${article.idArticleECB}")
        } catch (e: Exception) {
            handleError("Failed to add new article", e)
        }
    }

    fun addColorToArticle(uri: Uri, article: BaseDonneECBTabelle) {
        viewModelScope.launch {
            try {
                val nextColorField = getNextAvailableColorField(article)
                val fileName = "${article.idArticleECB}_${nextColorField.removePrefix("couleur")}.jpg"
                copyImage(context, uri, fileName)

                val updatedArticle = updateArticleWithNewColor(article, nextColorField)
                updateLocalAndRemoteArticle(updatedArticle)
            } catch (e: Exception) {
                handleError("Failed to add color to article", e)
            }
        }
    }

    private fun getNextAvailableColorField(article: BaseDonneECBTabelle): String {
        return when {
            article.couleur2.isNullOrEmpty() -> "couleur2"
            article.couleur3.isNullOrEmpty() -> "couleur3"
            article.couleur4.isNullOrEmpty() -> "couleur4"
            else -> throw IllegalStateException("All color fields are filled")
        }
    }

    private fun updateArticleWithNewColor(article: BaseDonneECBTabelle, colorField: String): BaseDonneECBTabelle {
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

    fun getDownloadsDirectory(): File {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    suspend fun copyImage(context: Context, sourceUri: Uri, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                val resolver = context.contentResolver
                val destinationUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IOException("Failed to create new MediaStore record.")

                resolver.openOutputStream(destinationUri)?.use { outputStream ->
                    resolver.openInputStream(sourceUri)?.use { inputStream ->
                        inputStream.copyTo(outputStream)
                    } ?: throw IOException("Failed to open input stream for URI: $sourceUri")
                } ?: throw IOException("Failed to open output stream for URI: $destinationUri")

                Log.d(TAG, "Image copied successfully to $destinationUri")
            } catch (e: IOException) {
                Log.e(TAG, "Failed to copy image", e)
                throw Exception("Failed to copy image: ${e.message}")
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

    fun deleteColor(article: BaseDonneECBTabelle, colorIndex: Int) {
        viewModelScope.launch {
            when (colorIndex) {
                1 -> deleteArticle(article)
                2 -> updateLocalAndRemoteArticle(article.copy(couleur2 = ""))
                3 -> updateLocalAndRemoteArticle(article.copy(couleur3 = ""))
                4 -> updateLocalAndRemoteArticle(article.copy(couleur4 = ""))
            }
            deleteColorImage(article.idArticleECB, colorIndex)
        }
    }

    private suspend fun deleteArticle(article: BaseDonneECBTabelle) {
        try {
            refDBJetPackExport.child(article.idArticleECB.toString()).removeValue().await()
            _uiState.update { currentState ->
                currentState.copy(
                    articlesBaseDonneECB = currentState.articlesBaseDonneECB.filter { it.idArticleECB != article.idArticleECB }
                )
            }
            for (i in 1..4) {
                deleteColorImage(article.idArticleECB, i)
            }
        } catch (e: Exception) {
            handleError("Failed to delete article", e)
        }
    }

    private fun deleteColorImage(articleId: Int, colorIndex: Int) {
        val baseImagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${articleId}_${colorIndex}"
        val downloadsImagePath = "${getDownloadsDirectory()}/${articleId}_${colorIndex}"

        listOf("jpg", "webp").forEach { extension ->
            listOf(baseImagePath, downloadsImagePath).forEach { path ->
                val file = File("$path.$extension")
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    fun processNewImage(uri: Uri, article: BaseDonneECBTabelle, colorIndex: Int) {
        viewModelScope.launch {
            try {
                val fileName = "${article.idArticleECB}_${colorIndex}.jpg"
                copyImage(context, uri, fileName)

                val updatedArticle = when (colorIndex) {
                    1 -> article.copy(couleur1 = "Couleur_1")
                    2 -> article.copy(couleur2 = "Couleur_2")
                    3 -> article.copy(couleur3 = "Couleur_3")
                    4 -> article.copy(couleur4 = "Couleur_4")
                    else -> article
                }
                updateLocalAndRemoteArticle(updatedArticle)
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

    fun setImagesInStorageFireBase(articleId: Int, colorIndex: Int) {
        viewModelScope.launch {
            val fileName = "${articleId}_$colorIndex.jpg"
            val localFile = File(getDownloadsDirectory(), fileName)
            val storageRef = storage.reference.child("Images Articles Data Base/$fileName")

            try {
                val uploadTask = storageRef.putFile(localFile.toUri())
                uploadTask.await()
                val downloadUrl = storageRef.downloadUrl.await()
                Log.d(TAG, "Image uploaded successfully: $fileName, URL: $downloadUrl")
            } catch (e: Exception) {
                handleError("Failed to upload image", e)
            }
        }
    }

    private fun handleError(message: String, exception: Exception) {
        Log.e(TAG, message, exception)
        _uiState.update { it.copy(error = "$message: ${exception.message}") }
    }

    companion object {
        private const val TAG = "HeadOfViewModels"
    }
}

data class CreatAndEditeInBaseDonnRepositeryModels(
    val articlesBaseDonneECB: List<BaseDonneECBTabelle> = emptyList(),
    val categoriesECB: List<CategoriesTabelleECB> = emptyList(),
    val showOnlyWithFilter: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
