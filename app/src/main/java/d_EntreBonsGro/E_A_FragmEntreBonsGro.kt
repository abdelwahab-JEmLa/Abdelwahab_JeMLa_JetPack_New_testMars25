package d_EntreBonsGro

import a_MainAppCompnents.ArticlesAcheteModele
import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VoiceChat
import androidx.compose.material.icons.filled.VoiceOverOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.ArticleDao
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
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentEntreBonsGro(articleDao: ArticleDao) {
    var articlesEntreBonsGrosTabele by remember { mutableStateOf<List<EntreBonsGrosTabele>>(emptyList()) }
    var articlesArticlesAcheteModele by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var articlesBaseDonne by remember { mutableStateOf<List<BaseDonne>>(emptyList()) }
    var suppliersList by remember { mutableStateOf<List<SupplierTabelle>>(emptyList()) }
    var suggestionsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var suggestionsListFromAutreNom by remember { mutableStateOf<List<String>>(emptyList()) }
    var editionPassedMode by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var showSupplierDialog by remember { mutableStateOf(false) }
    var founisseurNowIs by rememberSaveable { mutableStateOf<Int?>(2) }
    var founisseurIdNowIs by rememberSaveable { mutableStateOf<Long?>(null) }

    var modeFilterChangesDB by remember { mutableStateOf(false) }
    var showFullImage by rememberSaveable { mutableStateOf(true) }
    val showSplitView by rememberSaveable { mutableStateOf(false) }
    var showMissingArticles by remember { mutableStateOf(false) }
    var totalMissingArticles by remember { mutableStateOf(0) }
    var addedArticlesCount by remember { mutableStateOf(0) }
    var showTotaleBar by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val database = Firebase.database
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")
    val articlesAcheteModeleRef = database.getReference("ArticlesAcheteModeleAdapted")
    val baseDonneRef = database.getReference("e_DBJetPackExport")
    val suppliersRef = database.getReference("F_Suppliers")

    var currentImagePath by remember { mutableStateOf("file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${founisseurNowIs ?: 1}).jpg") }

    val configuration = LocalConfiguration.current
    val isPortraitLandscap = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    var showOutline by remember { mutableStateOf(false) }
    var showDivider by remember { mutableStateOf(true) }
    var showDialogeNbrIMGs by remember { mutableStateOf(false) }
    var heightOfImageAndRelatedDialogEditer by remember { mutableStateOf(false) }

    var voiceFrancai by remember { mutableStateOf(false) }

    var modeVerificationAvantUpdateBD by remember { mutableStateOf(false) }

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
                    val nomVocale = baseDonneArticle?.nomArab ?: ""
                    "$nomArticleSansSymbole -> ${articleAchete.prixAchat} $nomVocale (${articleAchete.idArticle})"
                }.distinct() + listOf("supp","محو" )
                suggestionsListFromAutreNom = newArticlesAcheteModele.map { articleAchete ->
                    val nomArticleSansSymbole = articleAchete.nomArticleFinale.toLowerCase().replace("®", "")
                    val baseDonneArticle = articlesBaseDonne.find { it.idArticle.toLong() == articleAchete.idArticle }
                    val nomVocale = baseDonneArticle?.autreNomDarticle ?: ""
                    "$nomArticleSansSymbole -> ${articleAchete.prixAchat} $nomVocale (${articleAchete.idArticle})"
                }.distinct() + listOf("supp","محو" )
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

    val floatingActionButtons = listOf(
        Triple(
            if (modeVerificationAvantUpdateBD) Icons.Default.Close else Icons.Default.ShoppingCart,
            if (modeVerificationAvantUpdateBD) "Hide Outline" else "Show Outline"
        ) { modeVerificationAvantUpdateBD = !modeVerificationAvantUpdateBD },
        Triple(
            Icons.Default.Edit,
            "Toggle Image Height"
        ) { heightOfImageAndRelatedDialogEditer = !heightOfImageAndRelatedDialogEditer },
        Triple(
            if (showOutline) Icons.Default.Close else Icons.Default.Keyboard,
            if (showOutline) "Hide Outline" else "Show Outline"
        ) { showOutline = !showOutline },
        Triple(
            if (showDivider) Icons.Default.Close else Icons.Default.ProductionQuantityLimits,
            if (showDivider) "Hide Divider" else "Show Divider"
        ) { showDivider = !showDivider },
        Triple(
            if (showDialogeNbrIMGs) Icons.Default.Close else Icons.Default.Image,
            if (showDialogeNbrIMGs) "Hide Image Dialog" else "Show Image Dialog"
        ) { showDialogeNbrIMGs = !showDialogeNbrIMGs }   ,
        Triple(
            if (voiceFrancai) Icons.Default.Close else Icons.Default.VoiceChat,
            if (voiceFrancai) "Voice Input Fr" else "Voice Input Ar"
        ) { voiceFrancai = !voiceFrancai }
    )

    Scaffold(
        topBar = {
            if (isPortraitLandscap) {
                TopAppBar(
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val filteredArticles = articlesEntreBonsGrosTabele
                                .filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
                            val totalSum = filteredArticles.sumOf { it.subTotaleBG }
                            val articleCount = filteredArticles.size
                            val supplier = suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }
                            val supplierName = supplier?.nomSupplierSu ?: "All Suppliers"
                            Text(
                                text = "$articleCount articles - $supplierName: %.2f".format(totalSum),
                                textAlign = TextAlign.Center,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(android.graphics.Color.parseColor(
                            suppliersList.find { it.bonDuSupplierSu == founisseurNowIs?.toString() }?.couleurSu ?: "#FFFFFF"
                        )),
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    actions = {}
                )
            }
        },
        bottomBar = {
            if (isPortraitLandscap) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
            }
        },
        floatingActionButton = {
            Column {
                FloatingActionButtonsSection(
                    buttons = floatingActionButtons,
                    showTotaleBarButton = { showTotaleBar = !showTotaleBar }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                if (showTotaleBar) {
                    OutlineQuichangeLeTotaleProvisoire(
                        founisseurIdNowIs = founisseurIdNowIs,
                        articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                        founisseurNowIs = founisseurNowIs,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                when {
                    showFullImage -> {
                        DessinableImage(
                            modifier = Modifier.weight(1f),
                            articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                            articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                            articlesBaseDonne = articlesBaseDonne,
                            founisseurIdNowIs = founisseurIdNowIs,
                            soquetteBonNowIs = founisseurNowIs,
                            showDiviseurDesSections = showDivider,
                            articlesRef = articlesRef,
                            baseDonneRef = baseDonneRef,
                            suggestionsList = suggestionsList,
                            articleDao = articleDao,
                            coroutineScope = coroutineScope,
                            showOutline = showOutline,
                            showDialogeNbrIMGs = showDialogeNbrIMGs,
                            onDissmiss = { showDialogeNbrIMGs = false },
                            heightOfImageAndRelatedDialogEditer = heightOfImageAndRelatedDialogEditer,
                            supplierList = suppliersList,
                            modeVerificationAvantUpdateBD = modeVerificationAvantUpdateBD,
                            voiceFrancai,
                            suggestionsListFromAutreNom=suggestionsListFromAutreNom,
                        )
                    }
                    showSplitView -> {
                        Column(modifier = Modifier.weight(1f)) {
                            DessinableImage(
                                modifier = Modifier.weight(1f),
                                articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                                articlesBaseDonne = articlesBaseDonne,
                                founisseurIdNowIs = founisseurIdNowIs,
                                soquetteBonNowIs = founisseurNowIs,
                                showDiviseurDesSections = showDivider,
                                articlesRef = articlesRef,
                                baseDonneRef = baseDonneRef,
                                suggestionsList = suggestionsList,
                                articleDao = articleDao,
                                coroutineScope = coroutineScope,
                                showOutline = showOutline,
                                showDialogeNbrIMGs = showDialogeNbrIMGs,
                                onDissmiss = { showDialogeNbrIMGs = false },
                                heightOfImageAndRelatedDialogEditer = heightOfImageAndRelatedDialogEditer,
                                supplierList = suppliersList,
                                modeVerificationAvantUpdateBD = modeVerificationAvantUpdateBD,
                                voiceFrancais = voiceFrancai,
                                suggestionsListFromAutreNom = suggestionsListFromAutreNom
                            )
                            AfficheEntreBonsGro(
                                articlesEntreBonsGro = articlesEntreBonsGrosTabele,
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
                                onSupplierChanged = ::handleSupplierChange,
                                suppliersRef = suppliersRef,
                            )
                        }
                    }
                    else -> {
                        AfficheEntreBonsGro(
                            articlesEntreBonsGro = articlesEntreBonsGrosTabele,
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
                            onSupplierChanged = ::handleSupplierChange,
                            suppliersRef = suppliersRef
                        )
                    }
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
        suppliersList = suppliersList,
        articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
        coroutineScope = coroutineScope,
        articlesBaseDonne = articlesBaseDonne  // Add this line
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
fun FloatingActionButtonsSection(
    buttons: List<Triple<ImageVector, String, () -> Unit>>,
    showTotaleBarButton: () -> Unit
) {
    var showFloatingButtons by remember { mutableStateOf(true) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    offset += dragAmount
                }
            }
    ) {
        Column {
            FloatingActionButton(
                onClick = { showFloatingButtons = !showFloatingButtons }
            ) {
                Icon(
                    imageVector = if (showFloatingButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (showFloatingButtons) "Hide Buttons" else "Show Buttons"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showFloatingButtons) {
                LazyColumn {
                    items(buttons) { (icon, contentDescription, onClick) ->
                        FloatingActionButton(onClick = onClick) {
                            Icon(
                                imageVector = icon,
                                contentDescription = contentDescription
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    item {
                        FloatingActionButton(onClick = showTotaleBarButton) {
                            Icon(
                                imageVector = Icons.Default.VoiceOverOff,
                                contentDescription = "Toggle Totale Bar"
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun OutlineQuichangeLeTotaleProvisoire(
    founisseurIdNowIs: Long?,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    founisseurNowIs: Int?,
    modifier: Modifier
) {
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
    var ancienTotaleDepuitFireStore by remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(key1 = founisseurIdNowIs) { // Re-fetch when founisseurNowIs changes
        ancienTotaleDepuitFireStore = getCurrenttotaleDeCeBon(founisseurIdNowIs ?: 0L)
    }

    var totaleProvisoire by remember { mutableStateOf("") }
    val totalSumNow = articlesEntreBonsGrosTabele
        .filter { founisseurNowIs == null || it.supplierIdBG.toLong() == founisseurIdNowIs }
        .sumOf { it.subTotaleBG }

    var lastLaunchTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0) ?: ""
            totaleProvisoire = spokenText
            if (founisseurIdNowIs != null) {
                updateSupplierCredit(
                    supplierId = founisseurIdNowIs,
                    supplierTotal = spokenText.toDoubleOrNull() ?: 0.0,
                    supplierPayment = 0.0,
                    ancienCredit = spokenText.toDoubleOrNull() ?: 0.0,
                    fromeOutlineSupInput = true
                )
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        OutlinedTextField(
            value = totaleProvisoire,
            onValueChange = { newVal ->
                totaleProvisoire = newVal
                if (founisseurIdNowIs != null) {
                    updateSupplierCredit(
                        supplierId = founisseurIdNowIs,
                        supplierTotal = newVal.toDoubleOrNull() ?: 0.0,
                        supplierPayment = 0.0,
                        ancienCredit = newVal.toDoubleOrNull() ?: 0.0,
                        fromeOutlineSupInput = true
                    )
                }
            },
            label = {
                Text(
                    (ancienTotaleDepuitFireStore.minus(totalSumNow)).toString()
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Red,
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color.Red,
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLaunchTime > 1000) {
                    lastLaunchTime = currentTime
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour cet article...")
                    }
                    speechRecognizerLauncher.launch(intent)
                }
            }
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Lancer la reconnaissance vocale"
            )
        }
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


