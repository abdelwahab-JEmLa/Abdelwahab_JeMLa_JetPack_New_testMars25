package a_MainAppCompnents

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import kotlin.math.roundToInt

class HeadOfViewModels(private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatAndEditeInBaseDonnRepositeryModels())
    val uiState = _uiState.asStateFlow()

    private val _currentEditedArticle = MutableStateFlow<BaseDonneECBTabelle?>(null)
    val currentEditedArticle: StateFlow<BaseDonneECBTabelle?> = _currentEditedArticle.asStateFlow()

    private val _currentSupplierArticle = MutableStateFlow<TabelleSupplierArticlesRecived?>(null)
    val currentSupplierArticle: StateFlow<TabelleSupplierArticlesRecived?> = _currentSupplierArticle.asStateFlow()

    private val _uploadProgress = MutableStateFlow(0f)
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()

    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")
    private val refCategorieTabelee = firebaseDatabase.getReference("H_CategorieTabele")
    private val refTabelleSupplierArticlesRecived = firebaseDatabase.getReference("K_SupplierArticlesRecived")
    private val refTabelleSuppliersSA = firebaseDatabase.getReference("F_Suppliers")
    val dossiesStandartOFImages = File("/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne")
    private val fireBaseStorageImgsRef = Firebase.storage.reference.child("Images Articles Data Base")

    var tempImageUri: Uri? = null

    companion object {
        private const val MAX_WIDTH = 1024
        private const val MAX_HEIGHT = 1024
        private const val PROGRESS_UPDATE_DELAY = 100L
        private const val TAG = "HeadOfViewModels"

    }

    init {
        viewModelScope.launch {
            initDataFromFirebase()
        }
    }

    suspend fun initDataFromFirebase() {
        try {
            _uiState.update { it.copy(isLoading = true) }
            updateProgressWithDelay(0f)

            val articles = fetchArticles()
            updateProgressWithDelay(20f)

            val categories = fetchCategories()
            updateProgressWithDelay(40f)

            val supplierArticlesRecived = fetchSupplierArticles()
            updateProgressWithDelay(60f)

            val suppliersSA = fetchSuppliers()
            updateProgressWithDelay(80f)

            updateUiState(articles, categories, supplierArticlesRecived, suppliersSA)
            updateProgressWithDelay(100f)
        } catch (e: Exception) {
            handleError("Failed to load data from Firebase", e)
        } finally {
            _uploadProgress.value = 0f
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun fetchArticles() = refDBJetPackExport.get().await().children.mapNotNull { snapshot ->
        snapshot.getValue(BaseDonneECBTabelle::class.java)?.apply {
            idArticleECB = snapshot.key?.toIntOrNull() ?: 0
        }
    }

    private suspend fun fetchCategories() = refCategorieTabelee.get().await().children
        .mapNotNull { it.getValue(CategoriesTabelleECB::class.java) }
        .sortedBy { it.idClassementCategorieInCategoriesTabele }

    private suspend fun fetchSupplierArticles() = refTabelleSupplierArticlesRecived.get().await().children
        .mapNotNull { it.getValue(TabelleSupplierArticlesRecived::class.java) }

    private suspend fun fetchSuppliers() = refTabelleSuppliersSA.get().await().children
        .mapNotNull { it.getValue(TabelleSuppliersSA::class.java) }

    private fun updateUiState(
        articles: List<BaseDonneECBTabelle>,
        categories: List<CategoriesTabelleECB>,
        supplierArticlesRecived: List<TabelleSupplierArticlesRecived>,
        suppliersSA: List<TabelleSuppliersSA>
    ) {
        _uiState.update { it.copy(
            articlesBaseDonneECB = articles,
            categoriesECB = categories,
            tabelleSupplierArticlesRecived = supplierArticlesRecived,
            tabelleSuppliersSA = suppliersSA,
            isLoading = false
        ) }
    }

    suspend fun updateProgressWithDelay(progress: Float, delay: Long = PROGRESS_UPDATE_DELAY) {
        _uploadProgress.value = progress.coerceIn(0f, 100f)
        delay(delay)
    }

    private fun updateTotalProgress(currentStep: Int, totalSteps: Int) {
        _uploadProgress.value = (currentStep.toFloat() / totalSteps.toFloat() * 100f).roundToInt().toFloat()
    }

    private fun updateStepProgress(currentStep: Int, totalSteps: Int, stepProgress: Float) {
        val stepSize = 100f / totalSteps
        val baseProgress = stepSize * (currentStep - 1)
        val additionalProgress = stepSize * (stepProgress / 100f)
        _uploadProgress.value = (baseProgress + additionalProgress).roundToInt().toFloat()
    }

    /*2->Section Suppliers Commendes Manager -------------------*/

    /*3->Section Imports FireBase And Stor-------------------*/

    fun trensfertData(
        articleDao: ArticleDao,
        editeBaseDonneViewModel: EditeBaseDonneViewModel,
        context: Context
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                _uploadProgress.value = 0f

                val totalSteps = 3
                var currentStep = 0

                // Step 1: Import from Firebase to DataBaseDonne
                updateTotalProgress(currentStep++, totalSteps)
                importFromFirebaseToDataBaseDonne("e_DBJetPackExport", editeBaseDonneViewModel)
                updateStepProgress(currentStep, totalSteps, 100f)

                // Step 2: Transfer Firebase Data ArticlesAcheteModele
                updateTotalProgress(currentStep++, totalSteps)
                transferFirebaseDataArticlesAcheteModele(context, articleDao) { progress ->
                    updateStepProgress(currentStep, totalSteps, progress)
                }

                // Step 3: Transfer from Telegram to SupplierArticlesRecived
                updateTotalProgress(currentStep++, totalSteps)
                transfertFromeTelegramToSupplierArticlesRecived(context) { progress ->
                    updateStepProgress(currentStep, totalSteps, progress)
                }

                _uploadProgress.value = 100f
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
                    val correspondingArticle = _uiState.value.articlesBaseDonneECB.find { it.idArticleECB.toLong() == idArticle }
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
        correspondingArticle: BaseDonneECBTabelle?,
        itsNewArticleFromeBacKE: Boolean
    ): TabelleSupplierArticlesRecived {
        return TabelleSupplierArticlesRecived(
            aa_vid = nextVid++,
            a_c_idarticle_c = if (itsNewArticleFromeBacKE) nextVid + 2500 else ((map["a01"] as? String)?.toLong() ?: 0L),
            a_d_nomarticlefinale_c = map["a02"] as? String ?: "",
            idSupplierTSA = correspondingArticle?.lastSupplierIdBuyedFrom?.toInt() ?: 0,
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

    private suspend fun transferFirebaseDataArticlesAcheteModele(
        context: android.content.Context,
        articleDao: ArticleDao,
        onProgressUpdate: (Float) -> Unit
    ) {
        var fireStorHistoriqueDesFactures: List<ArticlesAcheteModele> = emptyList()

        try {
            val firestore = Firebase.firestore
            val querySnapshot = firestore.collection("HistoriqueDesFactures").get().await()
            fireStorHistoriqueDesFactures = querySnapshot.documents.mapNotNull { document ->
                document.toObject(ArticlesAcheteModele::class.java)
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting documents: ", e)
        }

        val refSource = Firebase.database.getReference("ArticlesAcheteModele")
        val refDestination = Firebase.database.getReference("ArticlesAcheteModeleAdapted")
        try {
            refDestination.removeValue().await()

            val dataSnapshot = refSource.get().await()
            val dataMap = dataSnapshot.value as? Map<String, Map<String, Any>> ?: emptyMap()

            val totalItems = dataMap.size
            var processedItems = 0
            var maxIdArticle = 0

            // Find the maximum idArticle
            dataMap.forEach { (_, value) ->
                val idArticle = (value["idarticle_c"] as? Long) ?: 0
                if (idArticle > maxIdArticle) {
                    maxIdArticle = idArticle.toInt()
                }
            }

            dataMap.forEach { (_, value) ->
                val idArticle = (value["idarticle_c"] as? Long) ?: 0
                val nomClient = value["nomclient_c"] as? String ?: ""

                val baseDonne = articleDao.getArticleById(idArticle)

                val matchingHistorique = fireStorHistoriqueDesFactures.find { it.idArticle == idArticle && it.nomClient == nomClient  }
                val monPrixVentFireStoreBM = matchingHistorique?.monPrixVentFireStoreBM ?: 0.0

                val itsNewArticleFromeBacKE = value["nomarticlefinale_c_1"] as? String == "" &&
                        value["nomarticlefinale_c_2"] as? String == "" &&
                        value["nomarticlefinale_c_3"] as? String == "" &&
                        value["nomarticlefinale_c_4"] as? String == ""
                val generatedID=  if (itsNewArticleFromeBacKE) maxIdArticle + 2000 else idArticle

                // Filter entries where totalquantity is empty or null
                val totalQuantity = (value["totalquantity"] as? Number)?.toInt()
                if (totalQuantity != null && totalQuantity > 0) {
                    val article = baseDonne?.let {
                        ArticlesAcheteModele(
                            vid = (value["id"] as? Long) ?: 0,
                            idArticle = generatedID.toLong(),
                            nomArticleFinale = if (itsNewArticleFromeBacKE) (value["nomarticlefinale_c"] as? String)?.uppercase() ?: "" else value["nomarticlefinale_c"] as? String ?: "",
                            prixAchat = if (itsNewArticleFromeBacKE) 0.0 else it.monPrixAchat,
                            nmbrunitBC = roundToOneDecimal((value["nmbunite_c"] as? Number)?.toDouble() ?: 0.0),
                            clientPrixVentUnite = roundToOneDecimal((value["prixdevent_c"] as? Number)?.toDouble() ?: 0.0),
                            nomClient = nomClient,
                            dateDachate = value["datedachate"] as? String ?: "",
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
                            typeEmballage = if (baseDonne.cartonState == "itsCarton"|| baseDonne.cartonState == "Carton") "Carton" else "Boit",
                            choisirePrixDepuitFireStoreOuBaseBM = if (monPrixVentFireStoreBM == 0.0) "CardFireBase" else "CardFireStor",
                            monPrixVentBM = roundToOneDecimal((value["prix_1_q1_c"] as? Number)?.toDouble() ?: 0.0),
                            monPrixVentFireStoreBM = monPrixVentFireStoreBM ,

                            ).apply {
                            monPrixVentUniterFireStoreBM =  roundToOneDecimal(if (nmbrunitBC != 0.0) monPrixVentFireStoreBM / nmbrunitBC else 0.0)

                            monBenificeFireStoreBM =   roundToOneDecimal(monPrixVentFireStoreBM - prixAchat)
                            monBenificeUniterFireStoreBM =  roundToOneDecimal(if (nmbrunitBC != 0.0) monBenificeFireStoreBM / nmbrunitBC else 0.0)
                            totalProfitFireStoreBM =  roundToOneDecimal((totalQuantity * monBenificeFireStoreBM))

                            monBenificeBM = roundToOneDecimal(monPrixVentBM - prixAchat)
                            monBenificeUniterBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monBenificeBM / nmbrunitBC else 0.0)
                            totalProfitBM = roundToOneDecimal(totalQuantity*monBenificeBM)

                            monPrixAchatUniterBC = roundToOneDecimal(if (nmbrunitBC != 0.0) prixAchat / nmbrunitBC else 0.0)
                            monPrixVentUniterBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monPrixVentBM / nmbrunitBC else 0.0)
                            benificeDivise = roundToOneDecimal(((clientPrixVentUnite * nmbrunitBC) - prixAchat) / 2)
                            clientBenificeBM = roundToOneDecimal((clientPrixVentUnite * nmbrunitBC) - monPrixVentBM)
                        }
                    }

                    if (article != null) {
                        refDestination.child(article.vid.toString()).setValue(article).await()
                    }
                }

                processedItems++
                onProgressUpdate(processedItems.toFloat() / totalItems)
            }

            // Display success message when data transfer is completed
            withContext(Dispatchers.Main) {
                if (processedItems == totalItems) {
                    Toast.makeText(context, "Data transfer completed successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Data transfer failed", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("transferFirebaseData", "Failed to transfer data", e)

            // Display failure message with error details
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Data transfer failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }




    /*1->Section Creat + Handel IMGs Articles -------------------*/

    fun updateAndCalculateAuthersField(textFieldValue: String, columnToChange: String, article: BaseDonneECBTabelle) {
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
                if (it.idArticleECB == updatedArticle.idArticleECB) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }

        updateCurrentEditedArticle(updatedArticle)

        viewModelScope.launch {
            try {
                val articleRef = refDBJetPackExport.child(updatedArticle.idArticleECB.toString())
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
    fun toggleAffichageUniteState(article: BaseDonneECBTabelle) {
        val updatedArticle = article.copy(affichageUniteState = !article.affichageUniteState)

        _uiState.update { state ->
            val updatedArticles = state.articlesBaseDonneECB.map {
                if (it.idArticleECB == updatedArticle.idArticleECB) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }
        updateCurrentEditedArticle(updatedArticle)

        viewModelScope.launch {
            try {
                val articleRef = refDBJetPackExport.child(updatedArticle.idArticleECB.toString())
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
    private fun updateLocalAndFireBaseArticle(updatedArticle: BaseDonneECBTabelle) {
        _uiState.update { state ->
            val updatedArticles = state.articlesBaseDonneECB.map {
                if (it.idArticleECB == updatedArticle.idArticleECB) updatedArticle else it
            }
            state.copy(articlesBaseDonneECB = updatedArticles)
        }

        updateCurrentEditedArticle(updatedArticle)

        viewModelScope.launch {
            try {
                val articleRef = refDBJetPackExport.child(updatedArticle.idArticleECB.toString())
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
        return _uiState.value.articlesBaseDonneECB.firstOrNull()?.idArticleECB == id
    }

    fun isLastArticle(id: Int): Boolean {
        return _uiState.value.articlesBaseDonneECB.lastOrNull()?.idArticleECB == id
    }
    fun updateCurrentEditedArticle(article: BaseDonneECBTabelle?) {
        _currentEditedArticle.value = article
    }

    fun getArticleById(id: Int): BaseDonneECBTabelle? {
        return _uiState.value.articlesBaseDonneECB.find { it.idArticleECB == id }
    }

    fun getPreviousArticleId(currentId: Int): Int {
        val articles = _uiState.value.articlesBaseDonneECB
        val currentIndex = articles.indexOfFirst { it.idArticleECB == currentId }
        return if (currentIndex > 0) articles[currentIndex - 1].idArticleECB else articles.last().idArticleECB
    }

    fun getNextArticleId(currentId: Int): Int {
        val articles = _uiState.value.articlesBaseDonneECB
        val currentIndex = articles.indexOfFirst { it.idArticleECB == currentId }
        return if (currentIndex < articles.size - 1) articles[currentIndex + 1].idArticleECB else articles.first().idArticleECB
    }












    fun setImagesInStorageFireBase(articleId: Int, colorIndex: Int) {
        viewModelScope.launch {
            val fileName = "${articleId}_$colorIndex.jpg"
            val localFile = File(dossiesStandartOFImages, fileName)
            val storageRef = Firebase.storage.reference.child("Images Articles Data Base/$fileName")
            //TODO fait que les operations soit enregstre don une list a chaque foit termine il se coche check termine et aller au prochen pour le converti et update le stoage
            try {
                updateProgressWithDelay(0f)

                // Convert image to WebP format
                val webpImage = withContext(Dispatchers.IO) {
                    convertToWebP(localFile)
                }

                if (webpImage == null) {
                    throw IllegalStateException("Failed to convert image to WebP")
                }

                // Upload the WebP image
                val uploadTask = storageRef.putBytes(webpImage)

                updateProgressWithDelay(50f)

//                uploadTask.addOnProgressListener { taskSnapshot ->
//                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
//                    updateProgressWithDelay(progress.toFloat())
//                }.await()

                val downloadUrl = storageRef.downloadUrl.await() //TODO pk le sortie n ai pas on webp
                //HeadOfViewModels         D  Image uploaded successfully: 1066_1.jpg, URL: https://firebasestorage.googleapis.com/v0/b/abdelwahab-jemla-com.appspot.com/o/Images%20Articles%20Data%20Base%2F1066_1.jpg?alt=media&token=611f4ed5-d094-496d-ba9e-23bdd42f7388
                Log.d(TAG, "Image uploaded successfully: $fileName, URL: $downloadUrl")

                updateProgressWithDelay(100f)

            } catch (e: Exception) {
                handleError("Failed to upload image", e)
                updateProgressWithDelay(100f)
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



    private suspend fun copyImage(sourceUri: Uri, fileName: String) {
        withContext(Dispatchers.IO) {
            try {
                if (!dossiesStandartOFImages.exists()) {
                    dossiesStandartOFImages.mkdirs()
                }

                val destFile = File(dossiesStandartOFImages, fileName)

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


    fun toggleFilter() {
        _uiState.update { currentState ->
            currentState.copy(showOnlyWithFilter = !currentState.showOnlyWithFilter)
        }
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
                updateLocalAndFireBaseArticle(updatedArticle)
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
                2 -> updateLocalAndFireBaseArticle(article.copy(couleur2 = ""))
                3 -> updateLocalAndFireBaseArticle(article.copy(couleur3 = ""))
                4 -> updateLocalAndFireBaseArticle(article.copy(couleur4 = ""))
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
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeadOfViewModels::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeadOfViewModels(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
