package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentEntreBonsGro() {
    var articlesEntreBonsGrosTabele by remember { mutableStateOf<List<EntreBonsGrosTabele>>(emptyList()) }
    var articlesArticlesAcheteModele by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var articlesBaseDonne by remember { mutableStateOf<List<BaseDonne>>(emptyList()) }
    var suppliersList by remember { mutableStateOf<List<SupplierTabelle>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var nowItsNameInputeTime by remember { mutableStateOf(false) }
    var suggestionsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var vidOfLastQuantityInputted by remember { mutableStateOf<Long?>(null) }
    var editionPassedMode by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var showSupplierDialog by remember { mutableStateOf(false) }
    var founisseurNowIs by rememberSaveable { mutableStateOf<Int?>(null) }
    var modeFilterChangesDB by remember { mutableStateOf(false) }
    var showFullImage by rememberSaveable { mutableStateOf(true) }
    var showSplitView by rememberSaveable { mutableStateOf(false) }
    var showMissingArticles by remember { mutableStateOf(false) }
    var totalMissingArticles by remember { mutableStateOf(0) }
    var showCreditDialog by remember { mutableStateOf(false) }
    var addedArticlesCount by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val database = Firebase.database
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")
    val articlesAcheteModeleRef = database.getReference("ArticlesAcheteModeleAdapted")
    val baseDonneRef = database.getReference("e_DBJetPackExport")
    val suppliersRef = database.getReference("F_Suppliers")

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                inputText = it
                val newVid = processInputAndInsertData(it, articlesEntreBonsGrosTabele, articlesRef, founisseurNowIs, articlesBaseDonne, suppliersList)
                if (newVid != null) {
                    inputText = ""
                    nowItsNameInputeTime = true
                    vidOfLastQuantityInputted = newVid
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        articlesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newArticles = snapshot.children.mapNotNull { it.getValue(EntreBonsGrosTabele::class.java) }
                articlesEntreBonsGrosTabele = newArticles
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        baseDonneRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newBaseDonne = snapshot.children.mapNotNull { childSnapshot ->
                    try {
                        childSnapshot.getValue(BaseDonne::class.java)
                    } catch (e: Exception) {
                        println("Error parsing BaseDonne: ${e.message}")
                        null
                    }
                }
                articlesBaseDonne = newBaseDonne
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase read failed: ${error.message}")
            }
        })

        articlesAcheteModeleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newArticlesAcheteModele = snapshot.children.mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }
                articlesArticlesAcheteModele = newArticlesAcheteModele
                suggestionsList = newArticlesAcheteModele.map { articleAchete ->
                    val nomArticleSansSymbole = articleAchete.nomArticleFinale.toLowerCase().replace("®", "")
                    val baseDonneArticle = articlesBaseDonne.find { it.idArticle.toLong() == articleAchete.idArticle }
                    val nomArabe = baseDonneArticle?.nomArab ?: ""
                    "$nomArticleSansSymbole -> ${articleAchete.prixAchat} $nomArabe (${articleAchete.idArticle})"
                }.distinct() + listOf("supp", "passe")
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })


        suppliersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newSuppliers = snapshot.children.mapNotNull { it.getValue(SupplierTabelle::class.java) }
                suppliersList = newSuppliers
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    fun handleSupplierChange(vidBG: Long, newSupplierId: Int) {
        val updatedList = articlesEntreBonsGrosTabele.map { article ->
            if (article.vidBG == vidBG) {
                article.copy(grossisstBonN = newSupplierId)
            } else {
                article
            }
        }
        articlesEntreBonsGrosTabele = updatedList
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val totalSum = articlesEntreBonsGrosTabele
                        .filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
                        .sumOf { it.subTotaleBG }
                    val supplier = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }
                    val supplierName = supplier?.nomSupplierSu ?: "All Suppliers"
                    Text("$supplierName: %.2f".format(totalSum))
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(android.graphics.Color.parseColor(
                        suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }?.couleurSu ?: "#FFFFFF"
                    )),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(onClick = { showCreditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Manage Credit"
                        )
                    }
                    IconButton(
                        onClick = {
                            when {
                                showFullImage -> {
                                    showFullImage = false
                                    showSplitView = true
                                }
                                showSplitView -> showSplitView = false
                                else -> showFullImage = true
                            }
                            modeFilterChangesDB = false
                        }
                    ) {
                        Icon(
                            imageVector = when {
                                showFullImage -> Icons.AutoMirrored.Filled.List
                                showSplitView -> Icons.AutoMirrored.Filled.List
                                else -> Icons.Default.Image
                            },
                            contentDescription = when {
                                showFullImage -> "Show Split View"
                                showSplitView -> "Hide Image"
                                else -> "Show Image"
                            },
                            tint = when {
                                showFullImage -> Color.Red
                                showSplitView -> Color.Yellow
                                else -> MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    }
                    IconButton(onClick = { showSupplierDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Select Supplier"
                        )
                    }
                    IconButton(onClick = { showActionsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More actions"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
                    }
                    speechRecognizerLauncher.launch(intent)
                }
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Input")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlineInput(
                inputText = inputText,
                onInputChange = { newValue ->
                    inputText = newValue
                    if (newValue.endsWith("تغيير")) {
                        val newArabName = newValue.removeSuffix("تغيير").trim()
                        coroutineScope.launch {
                            vidOfLastQuantityInputted?.let { vid ->
                                // Find the article in articlesEntreBonsGrosTabele by vid
                                val article = articlesEntreBonsGrosTabele.find { it.vidBG == vid }
                                article?.let { foundArticle ->
                                    // Use the idArticleBG to update the corresponding entry in baseDonneRef
                                    baseDonneRef.child(foundArticle.idArticleBG.toString()).child("nomArab").setValue(newArabName)
                                }
                            }
                        }
                        inputText = ""
                    } else if (newValue.contains("+")) {
                        val newVid = processInputAndInsertData(newValue, articlesEntreBonsGrosTabele, articlesRef, founisseurNowIs, articlesBaseDonne, suppliersList)
                        if (newVid != null) {
                            inputText = ""
                            nowItsNameInputeTime = true
                            vidOfLastQuantityInputted = newVid
                        }
                    }
                },
                nowItsNameInputeTime = nowItsNameInputeTime,
                onNameInputComplete = { nowItsNameInputeTime = false },
                articlesList = articlesEntreBonsGrosTabele,
                suggestionsList = suggestionsList,
                vidOfLastQuantityInputted = vidOfLastQuantityInputted,
                articlesRef = articlesRef,
                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                articlesBaseDonne = articlesBaseDonne,
                editionPassedMode = editionPassedMode,
                modifier = Modifier.fillMaxWidth(),
                coroutineScope = coroutineScope
            )
            when {
                showFullImage -> {
                    ZoomableImage(
                        imagePath = "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}).jpg",
                        supplierId = founisseurNowIs,
                        modifier = Modifier.weight(1f)
                    )
                }
                showSplitView -> {
                    Column(modifier = Modifier.weight(1f)) {
                        ZoomableImage(
                            imagePath = "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}).jpg",
                            supplierId = founisseurNowIs,
                            modifier = Modifier.weight(0.35f)
                        )
                        AfficheEntreBonsGro(
                            articlesEntreBonsGro = articlesEntreBonsGrosTabele.filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs },
                            onDeleteArticle = { article ->
                                coroutineScope.launch {
                                    articlesRef.child(article.vidBG.toString()).removeValue()
                                }
                            },
                            articlesRef = articlesRef,
                            articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                            modifier = Modifier.weight(1f),
                            coroutineScope = coroutineScope,
                            onDeleteFromFirestore = {},
                            suppliersList = suppliersList,
                            onSupplierChanged = ::handleSupplierChange ,
                            suppliersRef=suppliersRef
                        )
                    }
                }
                else -> {
                    AfficheEntreBonsGro(
                        articlesEntreBonsGro = articlesEntreBonsGrosTabele.filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs },
                        onDeleteArticle = { article ->
                            coroutineScope.launch {
                                articlesRef.child(article.vidBG.toString()).removeValue()
                            }
                        },
                        articlesRef = articlesRef,
                        articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                        modifier = Modifier.weight(1f),
                        coroutineScope = coroutineScope,
                        onDeleteFromFirestore = {},
                        suppliersList = suppliersList,
                        onSupplierChanged = ::handleSupplierChange ,
                        suppliersRef=suppliersRef
                    )
                }
            }
        }
    }
    SupplierCreditDialog(
        showDialog = showCreditDialog,
        onDismiss = { showCreditDialog = false },
        supplierId = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }?.idSupplierSu,
        supplierName = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }?.nomSupplierSu ?: "Unknown Supplier",
        supplierTotal = articlesEntreBonsGrosTabele
            .filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
            .sumOf { it.subTotaleBG },
        coroutineScope = coroutineScope
    )
    ActionsDialog(
        showDialog = showActionsDialog,
        onDismiss = { showActionsDialog = false },
        onDeleteAllData = { showDeleteConfirmDialog = true },
        editionPassedMode = editionPassedMode,
        onEditionPassedModeChange = {
            editionPassedMode = it
            modeFilterChangesDB = false
            founisseurNowIs = null
            showFullImage = false
        },
        modeFilterChangesDB = modeFilterChangesDB,
        onModeFilterChangesDBChange = {
            modeFilterChangesDB = it
            editionPassedMode = false
            founisseurNowIs = null
            showFullImage = false
        },
        showMissingArticles = showMissingArticles,
        onExportToFirestore = {
            coroutineScope.launch {
                exportToFirestore()
                trensfertBonSuppAuDataBaseArticles()
            }
        },
        addedArticlesCount = addedArticlesCount,
        totalMissingArticles = totalMissingArticles,
        onShowMissingArticlesChange = { newValue ->
            showMissingArticles = newValue
            if (newValue) {
                findAndAddMissingArticles(articlesRef, articlesAcheteModeleRef, coroutineScope) { count, total ->
                    addedArticlesCount = count
                    totalMissingArticles = total
                }
            }
        },
        onDeleteReferencesWithSupplierId100 = {
            deleteReferencesWithSupplierId100(articlesRef, coroutineScope)
        }
    )

    DeleteConfirmationDialog(
        showDialog = showDeleteConfirmDialog,
        onDismiss = { showDeleteConfirmDialog = false },
        onConfirm = {
            coroutineScope.launch {
                articlesRef.removeValue()
            }
            showDeleteConfirmDialog = false
        }
    )

    SupplierSelectionDialog(
        showDialog = showSupplierDialog,
        onDismiss = { showSupplierDialog = false },
        onSupplierSelected = { selected ->
            founisseurNowIs = selected
            editionPassedMode = false
            modeFilterChangesDB = false
        },
        suppliersList = suppliersList,
        suppliersRef=suppliersRef

    )
}

@Composable
fun SupplierSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSupplierSelected: (Int) -> Unit,
    suppliersList: List<SupplierTabelle>,
    suppliersRef: DatabaseReference
) {
    if (showDialog) {
        var showBonUpdateDialog by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Supplier") },
            text = {
                LazyColumn {
                    items(16) { i ->
                        val supplierNumber = if (i == 15) 100 else i + 1
                        val supplier = suppliersList.find { it.bonDuSupplierSu == supplierNumber.toString() }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        onSupplierSelected(supplierNumber)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (supplier != null && supplier.bonDuSupplierSu.isNotEmpty()) {
                                        Text("$supplierNumber->.(${supplier.idSupplierSu}) ${supplier.nomSupplierSu}")
                                    } else {
                                        Text("$supplierNumber->.")
                                    }
                                }
                                IconButton(
                                    onClick = { showBonUpdateDialog = true }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Update Bon Number")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )

        SupplierBonUpdateDialog(
            showDialog = showBonUpdateDialog,
            onDismiss = { showBonUpdateDialog = false },
            onBonNumberSelected = { supplierId, newBonNumber ->
                updateSupplierBon(suppliersRef, supplierId, newBonNumber.toString())
            },
            suppliersList = suppliersList
        )
    }
}


data class EntreBonsGrosTabele(
    val vidBG: Long = 0,
    var idArticleBG: Long = 0,
    var nomArticleBG: String = "",
    var ancienPrixBG: Double = 0.0,
    var ancienPrixOnUniterBG: Double = 0.0,
    var newPrixAchatBG: Double = 0.0,
    var quantityAcheteBG: Int = 0,
    var quantityUniterBG: Int = 0,
    var subTotaleBG: Double = 0.0,
    var grossisstBonN: Int = 0,
    var supplierIdBG: Long = 0,
    var supplierNameBG: String = "",
    var uniterCLePlusUtilise: Boolean = false,
    var erreurCommentaireBG: String = "",
    var passeToEndStateBG: Boolean = false,
    var dateCreationBG: String = ""

){
    // Secondary constructor for Firebase
    constructor() : this(0)
}
fun processInputAndInsertData(
    input: String,
    articlesList: List<EntreBonsGrosTabele>,
    articlesRef: DatabaseReference,
    founisseurNowIs: Int?,
    articlesBaseDonne: List<BaseDonne>,
    suppliersList: List<SupplierTabelle>
): Long? {
    val regex = """(\d+)\s*[x+]\s*(\d+(\.\d+)?)""".toRegex()
    val matchResult = regex.find(input)

    val (quantity, price) = matchResult?.destructured?.let {
        Pair(it.component1().toIntOrNull(), it.component2().toDoubleOrNull())
    } ?: Pair(null, null)

    if (quantity != null && price != null) {
        val newVid = (articlesList.maxOfOrNull { it.vidBG } ?: 0) + 1
        var quantityUniterBG = 1

        val baseDonneEntry = articlesBaseDonne.find { it.idArticle.toLong() == newVid }
        if (baseDonneEntry != null) {
            quantityUniterBG = baseDonneEntry.nmbrUnite.toInt()
        }

        val supplier = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }
        val currentDate = LocalDate.now().toString()

        val newArticle = supplier?.idSupplierSu?.let {
            EntreBonsGrosTabele(
                vidBG = newVid,
                idArticleBG = 0,
                nomArticleBG = "",
                ancienPrixBG = 0.0,
                newPrixAchatBG = price,
                quantityAcheteBG = quantity,
                quantityUniterBG = quantityUniterBG,
                subTotaleBG = price * quantity,
                grossisstBonN = founisseurNowIs ?: 0,
                supplierIdBG = it,
                supplierNameBG = supplier.nomSupplierSu ,
                uniterCLePlusUtilise = false,
                erreurCommentaireBG = "",
                passeToEndStateBG = false,
                dateCreationBG = currentDate
            )
        }
        articlesRef.child(newVid.toString()).setValue(newArticle)
            .addOnSuccessListener {
                println("New article inserted successfully")
            }
            .addOnFailureListener { e ->
                println("Error inserting new article: ${e.message}")
            }

        return newVid
    }

    return null
}
fun updateArticleIdFromSuggestion(
    suggestion: String,
    vidOfLastQuantityInputted: Long?,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    onNameInputComplete: () -> Unit,
    editionPassedMode: Boolean,
    articlesList: List<EntreBonsGrosTabele>,
    coroutineScope: CoroutineScope
) {
    val effectiveVid = if (editionPassedMode) {
        articlesList.firstOrNull { it.passeToEndStateBG }?.vidBG ?: vidOfLastQuantityInputted
    } else {
        vidOfLastQuantityInputted
    }

    if (suggestion == "passe" && effectiveVid != null) {
        val articleToUpdate = articlesRef.child(effectiveVid.toString())
        articleToUpdate.child("passeToEndStateBG").setValue(true)
        articleToUpdate.child("nomArticleBG").setValue("Passe A La Fin")
        onNameInputComplete()
        return
    }
    if (suggestion == "supp" && effectiveVid != null) {
        val articleToUpdate = articlesRef.child(effectiveVid.toString())
        articleToUpdate.child("nomArticleBG").setValue("New Article")
        onNameInputComplete()
        return
    }

    val idArticleRegex = """\((\d+)\)$""".toRegex()
    val matchResult = idArticleRegex.find(suggestion)

    val idArticle = matchResult?.groupValues?.get(1)?.toLongOrNull()

    if (idArticle != null && effectiveVid != null) {
        val articleToUpdate = articlesRef.child(effectiveVid.toString())
        val currentArticle = articlesList.find { it.vidBG == effectiveVid }

        articleToUpdate.child("idArticleBG").setValue(idArticle)

        val correspondingArticle = articlesArticlesAcheteModele.find { it.idArticle == idArticle }
        correspondingArticle?.let { article ->
            articleToUpdate.child("nomArticleBG").setValue(article.nomArticleFinale)
        }
        val lastDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"))

        val correspondingBaseDonne = articlesBaseDonne.find { it.idArticle.toLong() == idArticle }
        correspondingBaseDonne?.let { baseDonne ->
            articleToUpdate.child("quantityUniterBG").setValue(baseDonne.nmbrUnite.toInt())
            articleToUpdate.child("ancienPrixBG").setValue(baseDonne.monPrixAchat)
            articleToUpdate.child("ancienPrixOnUniterBG").setValue((baseDonne.monPrixAchat / baseDonne.nmbrUnite).roundToTwoDecimals())
            articleToUpdate.child("lastDateCreationBG").setValue(lastDate)
        }

        coroutineScope.launch {
            var fireStorEntreBonsGrosTabele: EntreBonsGrosTabele? = null

            try {
                val firestore = Firebase.firestore
                val documentSnapshot = firestore
                    .collection("F_SupplierArticlesFireS")
                    .document(currentArticle?.supplierIdBG.toString())
                    .collection("historiquesAchats")
                    .document(idArticle.toString())
                    .get()
                    .await()

                if (documentSnapshot.exists()) {
                    fireStorEntreBonsGrosTabele = documentSnapshot.toObject(EntreBonsGrosTabele::class.java)
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting document: ", e)
            }

            val uniterCLePlusUtiliseFireStore = fireStorEntreBonsGrosTabele?.uniterCLePlusUtilise ?: false

            articleToUpdate.child("uniterCLePlusUtilise").setValue(uniterCLePlusUtiliseFireStore)
            println("DEBUG: Updated uniterCLePlusUtilise to $uniterCLePlusUtiliseFireStore and lastDateCreationBG to $lastDate")
        }
        onNameInputComplete()
    }
}
