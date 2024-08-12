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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
                    "$nomArticleSansSymbole -> ${articleAchete.prixAchat} (${articleAchete.idArticle})"
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
                    if (newValue.contains("+")) {
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
        var selectedSupplier by remember { mutableStateOf<SupplierTabelle?>(null) }

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
                                    onClick = {
                                        selectedSupplier = supplier
                                        showBonUpdateDialog = true
                                    }
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
            onBonNumberSelected = { newBonNumber ->
                selectedSupplier?.let { supplier ->
                    updateSupplierBon(suppliersRef, supplier.idSupplierSu, newBonNumber.toString())
                }
            }
        )
    }
}

@Composable
fun SupplierBonUpdateDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onBonNumberSelected: (Int) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Update Supplier Bon Number") },
            text = {
                LazyColumn {
                    items(15) { i ->
                        TextButton(
                            onClick = {
                                onBonNumberSelected(i + 1)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("${i + 1}")
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
    }
}



suspend fun exportToFirestore() {
    withContext(Dispatchers.IO) {
        try {
            // Fetch current data from Firebase Realtime Database
            val firebase = Firebase.database
            val articlesRef = firebase.getReference("ArticlesBonsGrosTabele")
            val snapshot = articlesRef.get().await()

            val supplierArticles = snapshot.children.mapNotNull { it.getValue(EntreBonsGrosTabele::class.java) }

            // Create a reference to the F_SupplierArticlesFireS collection in Firestore
            val firestore = FirebaseFirestore.getInstance()
            val supplierArticlesRef = firestore.collection("F_SupplierArticlesFireS")

            // Create a batch to perform multiple write operations
            val batch = firestore.batch()

            // Group articles by supplier
            val supplierGroups = supplierArticles.groupBy { it.supplierIdBG }

            // Process each article and calculate totals
            supplierGroups.forEach { (supplierId, articles) ->
                var supplierTotal = 0.0
                val currentDateTime = LocalDateTime.now()
                val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = currentDateTime.format(dateTimeFormatter)

                articles.forEach { article ->
                    val lineData = hashMapOf(
                        "vidBG" to article.vidBG,
                        "idArticleBG" to article.idArticleBG,
                        "nomArticleBG" to article.nomArticleBG,
                        "ancienPrixBG" to article.ancienPrixBG,
                        "newPrixAchatBG" to article.newPrixAchatBG,
                        "quantityAcheteBG" to article.quantityAcheteBG,
                        "quantityUniterBG" to article.quantityUniterBG,
                        "subTotaleBG" to article.subTotaleBG,
                        "grossisstBonN" to article.grossisstBonN,
                        "supplierIdBG" to article.supplierIdBG,
                        "supplierNameBG" to article.supplierNameBG,
                        "uniterCLePlusUtilise" to article.uniterCLePlusUtilise,
                        "erreurCommentaireBG" to article.erreurCommentaireBG,
                        "passeToEndStateBG" to article.passeToEndStateBG,
                        "dateCreationBG" to article.dateCreationBG
                    )

                    // Add to F_SupplierArticlesFireS collection
                    val docRef = supplierArticlesRef
                        .document(supplierId.toString())
                        .collection("historiquesAchats")
                        .document(article.idArticleBG.toString())
                    batch.set(docRef, lineData)

                    // Calculate total
                    supplierTotal += article.subTotaleBG
                }
                // Fetch current totalCredit from Firestore
                val totalCreditDoc = supplierArticlesRef
                    .document(supplierId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                val lastestTotaleCredit = totalCreditDoc.getDouble("restCreditDeCetteBon") ?: 0.0

                // Calculate the new total credit
                val newTotalCredit = lastestTotaleCredit + supplierTotal
                // Prepare the updated total data
                val totalData = hashMapOf(
                    "date" to formattedDateTime,
                    "totalAmount" to supplierTotal,
                    "totalCredit" to supplierTotal,
                    "restCreditDeCetteBon" to newTotalCredit
                )

                // Update the total for the supplier
                // Use the new document ID format with date and time
                val documentId = "Bon($dayOfWeek)${formattedDateTime}=${"%.2f".format(supplierTotal)}"
                val totalDocRef = supplierArticlesRef
                    .document(supplierId.toString())
                    .collection("Totale et Credit Des Bons")
                    .document(documentId)
                batch.set(totalDocRef, totalData)

                // Update the latest document
                val latestDocRef = supplierArticlesRef
                    .document(supplierId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                batch.set(latestDocRef, totalData)
            }

            // Commit the batch
            batch.commit().await()
            println("Successfully exported articles and totals to Firestore")

        } catch (e: Exception) {
            println("Error exporting to Firestore: ${e.message}")
        }
    }
}

suspend fun updateSupplierCredit(supplierId: Int, supplierTotal: Double, supplierPayment: Double) {
    val firestore = Firebase.firestore
    val supplierArticlesRef = firestore.collection("F_SupplierArticlesFireS")
    val currentDateTime = LocalDateTime.now()
    val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)
    val restCreditDeCetteBon = supplierTotal - supplierPayment

    // Fetch current totalCredit from Firestore
    val totalCreditDoc = supplierArticlesRef
        .document(supplierId.toString())
        .collection("latest Totale et Credit Des Bons")
        .document("latest")
        .get()
        .await()

    val lastestTotaleCredit = totalCreditDoc.getDouble("totalCredit") ?: 0.0

    // Calculate the new total credit
    val newTotalCredit = lastestTotaleCredit + restCreditDeCetteBon

    val data = hashMapOf(
        "date" to formattedDateTime,
        "totalAmount" to supplierTotal,
        "totalCredit" to supplierPayment,
        "restCreditDeCetteBon" to restCreditDeCetteBon,
        "creditDuComptActuelle" to newTotalCredit
    )

    try {
        // Update the current bon document
        val documentId = "Bon($dayOfWeek)${formattedDateTime}=${"%.2f".format(supplierTotal)}"
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("Totale et Credit Des Bons")
            .document(documentId)
            .set(data)

        // Update the latest document
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")
            .set(data)
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating supplier credit: ", e)
    }
}


data class SupplierInvoice(
    val date: String,
    val totalAmount: Double,
    val totalCredit: Double,
    val restCreditDeCetteBon: Double
)

@Composable
fun SupplierCreditDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    supplierId: Long?,
    supplierName: String,
    supplierTotal: Double,
    coroutineScope: CoroutineScope
) {
    var supplierPayment by remember { mutableStateOf("") }
    var currentCredit by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<SupplierInvoice>>(emptyList()) }

    LaunchedEffect(showDialog, supplierId) {
        if (showDialog && supplierId != null) {
            isLoading = true
            val firestore = Firebase.firestore
            try {
                val latestDoc = firestore.collection("F_SupplierArticlesFireS")
                    .document(supplierId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                currentCredit = latestDoc.getDouble("restCreditDeCetteBon") ?: 0.0

                // Fetch recent invoices, excluding the "latest" document
                val invoicesQuery = firestore.collection("F_SupplierArticlesFireS")
                    .document(supplierId.toString())
                    .collection("Totale et Credit Des Bons")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(3)

                val invoicesSnapshot = invoicesQuery.get().await()
                recentInvoices = invoicesSnapshot.documents.mapNotNull { doc ->
                    SupplierInvoice(
                        date = doc.getString("date") ?: "",
                        totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                        totalCredit = doc.getDouble("totalCredit") ?: 0.0,
                        restCreditDeCetteBon = doc.getDouble("restCreditDeCetteBon") ?: 0.0
                    )
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching data: ", e)
                currentCredit = 0.0
                recentInvoices = emptyList()
            }
            isLoading = false
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Manage Supplier Credit: $supplierName") },
            text = {
                Column {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Current Credit: ${"%.2f".format(currentCredit)}")
                        Text("New Purchase Total: ${"%.2f".format(supplierTotal)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = supplierPayment,
                            onValueChange = { supplierPayment = it },
                            label = { Text("Payment Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val paymentAmount = supplierPayment.toDoubleOrNull() ?: currentCredit
                        val newCredit = currentCredit + supplierTotal - paymentAmount
                        Text("New Credit Balance: ${"%.2f".format(newCredit)}")

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Recent Invoices", style = MaterialTheme.typography.titleMedium)
                        if (recentInvoices.isEmpty()) {
                            Text("No recent invoices found")
                        } else {
                            LazyColumn(
                                modifier = Modifier.height(200.dp)
                            ) {
                                items(recentInvoices) { invoice ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Date: ${invoice.date}")
                                            Text("Total: ${"%.2f".format(invoice.totalAmount)}")
                                            Text("Credit: ${"%.2f".format(invoice.totalCredit)}")
                                            Text("Remaining: ${"%.2f".format(invoice.restCreditDeCetteBon)}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            supplierId?.let { id ->
                                val paymentAmount = if (supplierPayment.isEmpty()) currentCredit else supplierPayment.toDoubleOrNull() ?: currentCredit
                                updateSupplierCredit(id.toInt(), supplierTotal, paymentAmount)
                            }
                        }
                        onDismiss()
                    },
                    enabled = !isLoading
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
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