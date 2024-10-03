package a_MainAppCompnents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import b2_Edite_Base_Donne_With_Creat_New_Articls.RepositeryUpdateCalculateColumnsOfCreatAndEditeDataBase
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class HeadOfViewModels(
    private val context: Context,
    private val repositoryCreatAndEditDataBase: RepositeryUpdateCalculateColumnsOfCreatAndEditeDataBase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatAndEditeInBaseDonnRepositeryModels())
    val uiState = _uiState.asStateFlow()

    private val _currentEditedArticle = MutableStateFlow<BaseDonneECBTabelle?>(null)
    val currentEditedArticle: StateFlow<BaseDonneECBTabelle?> = _currentEditedArticle.asStateFlow()

    private val _currentSupplierArticle = MutableStateFlow<TabelleSupplierArticlesRecived?>(null)
    val currentSupplierArticle: StateFlow<TabelleSupplierArticlesRecived?> = _currentSupplierArticle.asStateFlow()

    private val refDBJetPackExport = FirebaseDatabase.getInstance().getReference("e_DBJetPackExport")
    private val refCategorieTabelee = FirebaseDatabase.getInstance().getReference("H_CategorieTabele")
    private val refTabelleSupplierArticlesRecived = FirebaseDatabase.getInstance().getReference("")
    private val refTabelleSuppliersSA = FirebaseDatabase.getInstance().getReference("")

    val dossiesStandartImages = File("/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne")
    private val storageImgsRef = Firebase.storage.reference.child("Images Articles Data Base")

    var tempImageUri: Uri? = null

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress
    // Constants
    private  val MAX_WIDTH = 1024
    private  val MAX_HEIGHT = 1024
    init {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }

    private suspend fun initDataFromFirebase() {
        try {
            _uiState.update { it.copy(isLoading = true) }//TODO fait lence le suive de _uploadProgress

            val articles = refDBJetPackExport.get().await().children.mapNotNull { snapshot ->
                snapshot.getValue(BaseDonneECBTabelle::class.java)?.apply {
                    idArticleECB = snapshot.key?.toIntOrNull() ?: 0
                }
            }

            val categories = refCategorieTabelee.get().await().children
                .mapNotNull { it.getValue(CategoriesTabelleECB::class.java) }
                .sortedBy { it.idClassementCategorieInCategoriesTabele }

            val supplierArticlesRecived = refTabelleSupplierArticlesRecived.get().await().children
                .mapNotNull { it.getValue(TabelleSupplierArticlesRecived::class.java) }

            val suppliersSA = refTabelleSuppliersSA.get().await().children
                .mapNotNull { it.getValue(TabelleSuppliersSA::class.java) }

            _uiState.update { it.copy(
                articlesBaseDonneECB = articles,
                categoriesECB = categories,
                tabelleSupplierArticlesRecived=supplierArticlesRecived,
                tabelleSuppliersSA =suppliersSA,
                isLoading = false
            ) }
        } catch (e: Exception) {
            handleError("Failed to load data from Firebase", e)
        }
    }
    fun startProgress() {
        _uploadProgress.value = 0f
    }

    fun updateProgress(progress: Float) {
        _uploadProgress.value = progress.coerceIn(0f, 100f)
    }

    fun completeProgress() {
        _uploadProgress.value = 100f
    }

    fun updateCurrentEditedArticle(article: BaseDonneECBTabelle?) {
        _currentEditedArticle.value = article
    }
/*2->Section Suppliers Commendes Manager -------------------*/


/*1->Section Creat + Handel IMGs Articles -------------------*/

    fun setImagesInStorageFireBase(articleId: Int, colorIndex: Int) {
        viewModelScope.launch {
            val fileName = "${articleId}_$colorIndex.jpg"
            val localFile = File(dossiesStandartImages, fileName)
            val storageRef = Firebase.storage.reference.child("Images Articles Data Base/$fileName")
            //TODO fait que les operations soit enregstre don une list a chaque foit termine il se coche check termine et aller au prochen pour le converti et update le stoage
            try {
                startProgress()

                // Convert image to WebP format
                val webpImage = withContext(Dispatchers.IO) {
                    convertToWebP(localFile)
                }

                if (webpImage == null) {
                    throw IllegalStateException("Failed to convert image to WebP")
                }

                // Upload the WebP image
                val uploadTask = storageRef.putBytes(webpImage)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                    updateProgress(progress.toFloat())
                }.await()

                val downloadUrl = storageRef.downloadUrl.await() //TODO pk le sortie n ai pas on webp
                //HeadOfViewModels         D  Image uploaded successfully: 1066_1.jpg, URL: https://firebasestorage.googleapis.com/v0/b/abdelwahab-jemla-com.appspot.com/o/Images%20Articles%20Data%20Base%2F1066_1.jpg?alt=media&token=611f4ed5-d094-496d-ba9e-23bdd42f7388
                Log.d(TAG, "Image uploaded successfully: $fileName, URL: $downloadUrl")

                completeProgress()

            } catch (e: Exception) {
                handleError("Failed to upload image", e)
                completeProgress() // Reset progress on error
            }
        }
    }

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

    companion object {
        private const val TAG = "HeadOfViewModels"
    }

    private suspend fun copyImage(sourceUri: Uri, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!dossiesStandartImages.exists()) {
                    dossiesStandartImages.mkdirs()
                }

                val destFile = File(dossiesStandartImages, fileName)

                context.contentResolver.openInputStream(sourceUri)?.use { input ->
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
                copyImage(uri, fileName)

                val newClassementCate = calculateNewClassementCate(category)

                val newArticle = createNewArticle(newId, category, newClassementCate)
                ensureNewArticlesCategoryExists()
                updateDataBaseWithNewArticle(newArticle)
            } catch (e: Exception) {
                handleError("Failed to process image", e)
            }
        }
    }

    private  fun calculateNewClassementCate(category: CategoriesTabelleECB): Double {
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
                copyImage(uri, fileName)

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
        val storageImgsRef = Firebase.storage.reference.child("Images Articles Data Base")

        // Delete the image from Firebase Storage
        val imageRef = storageImgsRef.child("${articleId}_${colorIndex}"/*TODO fait que ca delete au format ("jpg"ou  "webp")*/)
        imageRef.delete().addOnSuccessListener {
            // Image deleted successfully from Firebase Storage
        }.addOnFailureListener { exception ->
            // Handle any errors
            Log.e("Firebase", "Error deleting image from Firebase Storage", exception)
        }

        // Delete local files
        listOf("jpg", "webp").forEach { extension ->
            listOf(baseImagePath).forEach { path ->
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
                copyImage(uri, fileName)

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
}

data class CreatAndEditeInBaseDonnRepositeryModels(
    val articlesBaseDonneECB: List<BaseDonneECBTabelle> = emptyList(),
    val categoriesECB: List<CategoriesTabelleECB> = emptyList(),
    val tabelleSupplierArticlesRecived: List<TabelleSupplierArticlesRecived> = emptyList(),
    val tabelleSuppliersSA: List<TabelleSuppliersSA> = emptyList(),
    val showOnlyWithFilter: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HeadOfViewModelFactory(
    private val context: Context,
    private val repositeryUpdateCalculateColumnsOfCreatAndEditeDataBase: RepositeryUpdateCalculateColumnsOfCreatAndEditeDataBase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeadOfViewModels::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeadOfViewModels(context, repositeryUpdateCalculateColumnsOfCreatAndEditeDataBase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
