package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.ArticleDao
import c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentEntreBonsGro(articleDao: ArticleDao) {
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
    var founisseurIdNowIs by rememberSaveable { mutableStateOf<Long?>(null) }

    var modeFilterChangesDB by remember { mutableStateOf(false) }
    var showFullImage by rememberSaveable { mutableStateOf(true) }
    var showSplitView by rememberSaveable { mutableStateOf(false) }
    var showMissingArticles by remember { mutableStateOf(false) }
    var totalMissingArticles by remember { mutableStateOf(0) }
    var addedArticlesCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val database = Firebase.database
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")
    val articlesAcheteModeleRef = database.getReference("ArticlesAcheteModeleAdapted")
    val baseDonneRef = database.getReference("e_DBJetPackExport")
    val suppliersRef = database.getReference("F_Suppliers")

    var currentImagePath by remember { mutableStateOf("file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}).jpg") }


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
                }.distinct() + listOf("supp", "passe","تمرير" ,"محو" )
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
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val totalSum = articlesEntreBonsGrosTabele
                            .filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
                            .sumOf { it.subTotaleBG }
                        val supplier = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }
                        val supplierName = supplier?.nomSupplierSu ?: "All Suppliers"
                        Text(
                            text = "$supplierName: %.2f".format(totalSum),
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(android.graphics.Color.parseColor(
                        suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }?.couleurSu ?: "#FFFFFF"
                    )),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {}
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    },
                    modifier = Modifier.weight(0.1f)
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

                 suspend fun getCurrenttotaleDeCeBon(founisseurIdNowIs: Long): Double {
                    return withContext(Dispatchers.IO) {
                        try {
                            val firestore = Firebase.firestore
                            val latestDoc = firestore.collection("F_SupplierArticlesFireS")
                                .document(founisseurIdNowIs.toString())
                                .collection("latest Totale et Credit Des Bons")
                                .document("latest")
                                .get()
                                .await()

                            latestDoc.getDouble("totaleDeCeBon") ?: 0.0
                        } catch (e: Exception) {
                            Log.e("Firestore", "Error fetching current credit balance: ", e)
                            0.0
                        }
                    }
                }

                // Use a LaunchedEffect to fetch the data asynchronously
                var totalDeCeBon by remember { mutableDoubleStateOf(0.0) }
                LaunchedEffect(key1 = founisseurIdNowIs) { // Re-fetch when founisseurNowIs changes
                    totalDeCeBon = getCurrenttotaleDeCeBon(founisseurIdNowIs ?: 0L)
                }

                var totaleProvisoire by remember { mutableStateOf("") }
                val totalSumNow = articlesEntreBonsGrosTabele
                    .filter { founisseurNowIs == null || it.grossisstBonN.toLong() == founisseurIdNowIs }
                    .sumOf { it.subTotaleBG }

                OutlinedTextField(
                    value = totaleProvisoire,
                    onValueChange = { newVal ->
                        totaleProvisoire = newVal
                        if (founisseurIdNowIs != null) {
                            updateSupplierCredit(founisseurIdNowIs!!, newVal.toDouble(), totalDeCeBon, totalSumNow)
                        }
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.Red,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.weight(0.7f)
                )
                IconButton(onClick = { showSupplierDialog = true },
                    modifier = Modifier.weight(0.1f)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Select Supplier"
                    )
                }
                IconButton(onClick = { showActionsDialog = true },
                    modifier = Modifier.weight(0.1f)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More actions"
                    )
                }
            }
        },
        floatingActionButton = {
            VoiceInputButton(
                articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                articlesRef = articlesRef,
                baseDonneRef = baseDonneRef,
                founisseurNowIs = founisseurNowIs,
                articlesBaseDonne = articlesBaseDonne,
                suppliersList = suppliersList,
                suggestionsList = suggestionsList,
                onInputProcessed = { newVid ->
                    if (newVid != null) {
                        vidOfLastQuantityInputted = newVid
                        nowItsNameInputeTime = true
                    }
                    inputText = ""
                },
                updateArticleIdFromSuggestion = ::updateArticleIdFromSuggestion,
                vidOfLastQuantityInputted = vidOfLastQuantityInputted,
                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                editionPassedMode = editionPassedMode,
                coroutineScope = coroutineScope,
                articleDao = articleDao
            )
        },
        floatingActionButtonPosition = FabPosition.Start
    )
{ innerPadding ->
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
                        imagePath = currentImagePath,
                        supplierId = founisseurNowIs,
                        modifier = Modifier.weight(1f)
                    )
                }
                showSplitView -> {
                    Column(modifier = Modifier.weight(1f)) {
                        ZoomableImage(
                            imagePath = currentImagePath,
                            supplierId = founisseurNowIs,
                            modifier = Modifier.weight(1f)
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
        },
        founisseurNowIs = founisseurNowIs,
        onImagePathChange = { newPath ->
            currentImagePath = newPath
        },
        suppliersList =suppliersList,
        articlesEntreBonsGrosTabele=articlesEntreBonsGrosTabele,
        coroutineScope=coroutineScope
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
        onSupplierSelected = { number,idSupp ->
            founisseurNowIs = number
            founisseurIdNowIs = idSupp
            editionPassedMode = false
            modeFilterChangesDB = false
        },
        suppliersList = suppliersList,
        suppliersRef = suppliersRef
    )
}



@Composable
fun ActionsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onDeleteAllData: () -> Unit,
    editionPassedMode: Boolean,
    onEditionPassedModeChange: (Boolean) -> Unit,
    modeFilterChangesDB: Boolean,
    onModeFilterChangesDBChange: (Boolean) -> Unit,
    onExportToFirestore: () -> Unit,
    showMissingArticles: Boolean,
    onShowMissingArticlesChange: (Boolean) -> Unit,
    addedArticlesCount: Int,
    totalMissingArticles: Int,
    onDeleteReferencesWithSupplierId100: () -> Unit,
    founisseurNowIs: Int?,
    onImagePathChange: (String) -> Unit,
    suppliersList: List<SupplierTabelle>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    coroutineScope: CoroutineScope
) {
    var showImageSelectDialog by remember { mutableStateOf(false) }
    var showExportConfirmDialog by remember { mutableStateOf(false) }
    var showCreditDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Actions") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            onDeleteAllData()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete all data")
                        Spacer(Modifier.width(8.dp))
                        Text("Delete all data")
                    }
                    IconButton(onClick = { showCreditDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = "Manage Credit"
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Edition Passed Mode")
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = editionPassedMode,
                            onCheckedChange = {
                                onEditionPassedModeChange(it)
                                onDismiss()
                            }
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Changed Prices")
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = modeFilterChangesDB,
                            onCheckedChange = {
                                onModeFilterChangesDBChange(it)
                                onDismiss()
                            }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Missing Articles")
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = showMissingArticles,
                            onCheckedChange = {
                                onShowMissingArticlesChange(it)
                            }
                        )
                    }
                    if (showMissingArticles && totalMissingArticles > 0) {
                        Column {
                            Text("Adding missing articles: $addedArticlesCount / $totalMissingArticles")
                            LinearProgressIndicator(
                                progress = { addedArticlesCount.toFloat() / totalMissingArticles.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            )
                        }
                    } else if (addedArticlesCount > 0) {
                        Text("Added $addedArticlesCount missing articles", color = Color.Green)
                    }
                    TextButton(
                        onClick = {
                            onDeleteReferencesWithSupplierId100()
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete references with supplierIdBG = 100")
                        Spacer(Modifier.width(8.dp))
                        Text("Delete references with supplierIdBG = 100")
                    }
                    TextButton(
                        onClick = {
                            showExportConfirmDialog = true
                        }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Export to Firestore")
                        Spacer(Modifier.width(8.dp))
                        Text("Export to Firestore")
                    }
                    TextButton(
                        onClick = { showImageSelectDialog = true }
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Select Image")
                        Spacer(Modifier.width(8.dp))
                        Text("Select Image")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
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


    if (showImageSelectDialog) {
        AlertDialog(
            onDismissRequest = { showImageSelectDialog = false },
            title = { Text("Select Image Number") },
            text = {
                Row {
                    (1..5).forEach { num ->
                        TextButton(
                            onClick = {
                                val newImagePath = if (num == 1) {
                                    "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}).jpg"
                                } else {
                                    "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}.$num).jpg"
                                }
                                onImagePathChange(newImagePath)
                                showImageSelectDialog = false
                            }
                        ) {
                            Text(if (num == 1) "Default" else num.toString())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageSelectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showExportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExportConfirmDialog = false },
            title = { Text("Confirm Export to Firestore") },
            text = { Text("Are you sure you want to export the data to Firestore?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onExportToFirestore()
                        showExportConfirmDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
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
    var ancienCredit by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var recentInvoices by remember { mutableStateOf<List<SupplierInvoice>>(emptyList()) }

    // Reset supplierPayment when dialog is opened
    LaunchedEffect(showDialog) {
        if (showDialog) {
            supplierPayment = ""
        }
    }

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

                ancienCredit = latestDoc.getDouble("ancienCredits") ?: 0.0

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
                        totaleDeCeBon = doc.getDouble("totaleDeCeBon") ?: 0.0,
                        payeCetteFoit = doc.getDouble("payeCetteFoit") ?: 0.0,
                        creditFaitDonCeBon = doc.getDouble("creditFaitDonCeBon") ?: 0.0,
                        ancienCredits = doc.getDouble("ancienCredits") ?: 0.0
                    )
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching data: ", e)
                ancienCredit = 0.0
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
                        Text("Current Credit + New Purchase Total: ${"%.2f".format(ancienCredit + supplierTotal)}")
                        Text("Total of Current Invoice: ${"%.2f".format(supplierTotal)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = supplierPayment,
                            onValueChange = { supplierPayment = it },
                            label = { Text("Payment Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val paymentAmount = if((supplierPayment.toDoubleOrNull() ?: 0.0) == 0.0) ancienCredit else supplierPayment.toDoubleOrNull() ?: 0.0
                        val newCredit = ancienCredit + supplierTotal - paymentAmount
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
                                            Text("Total: ${"%.2f".format(invoice.totaleDeCeBon)}")
                                            Text("Paid: ${"%.2f".format(invoice.payeCetteFoit)}")
                                            Text("Credit: ${"%.2f".format(invoice.creditFaitDonCeBon)}")
                                            Text("Previous Balance: ${"%.2f".format(invoice.ancienCredits)}")
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
                                val paymentAmount = supplierPayment.toDoubleOrNull() ?: 0.0
                                updateSupplierCredit(id, supplierTotal, paymentAmount,ancienCredit)
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


