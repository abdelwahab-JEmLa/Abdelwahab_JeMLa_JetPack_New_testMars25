package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import c_ManageBonsClients.ArticlesAcheteModele
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentEntreBonsGro() {
    var articlesEntreBonsGrosTabele by remember { mutableStateOf<List<EntreBonsGrosTabele>>(emptyList()) }
    var articlesArticlesAcheteModele by remember { mutableStateOf<List<ArticlesAcheteModele>>(emptyList()) }
    var articlesBaseDonne by remember { mutableStateOf<List<BaseDonne>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var nowItsNameInputeTime by remember { mutableStateOf(false) }
    var suggestionsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var vidOfLastQuantityInputted by remember { mutableStateOf<Long?>(null) }
    var editionPassedMode by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showActionsDialog by remember { mutableStateOf(false) }
    var showSupplierDialog by remember { mutableStateOf(false) }
    var founisseurNowIs by rememberSaveable { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val database = Firebase.database
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")
    val articlesAcheteModeleRef = database.getReference("ArticlesAcheteModeleAdapted")
    val baseDonneRef = database.getReference("e_DBJetPackExport")

    var showFullImage by rememberSaveable { mutableStateOf(true) }
    var showSplitView by rememberSaveable { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                inputText = it
                val newVid = processInputAndInsertData(it, articlesEntreBonsGrosTabele, articlesRef, founisseurNowIs, articlesBaseDonne)
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
                } + "supp" + "passe"
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val totalSum = articlesEntreBonsGrosTabele
                        .filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
                        .sumOf { it.subTotaleBG }
                    val supplierName = when (founisseurNowIs) {
                        null -> "All Suppliers"
                        else -> "Supplier $founisseurNowIs"
                    }
                    Text("$supplierName: %.2f".format(totalSum))
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
                    IconButton(
                        onClick = {
                            if (showFullImage) {
                                showFullImage = false
                                showSplitView = true
                            } else if (showSplitView) {
                                showSplitView = false
                            } else {
                                showFullImage = true
                            }
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
                            imageVector = Icons.Default.List,
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
                        val newVid = processInputAndInsertData(newValue, articlesEntreBonsGrosTabele, articlesRef, founisseurNowIs, articlesBaseDonne)
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
                modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.weight(0.5f)
                        )
                        AfficheEntreBonsGro(
                            articlesEntreBonsGro = if (editionPassedMode) {
                                articlesEntreBonsGrosTabele.filter { it.passeToEndState }
                            } else {
                                articlesEntreBonsGrosTabele.filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
                            },
                            onDeleteArticle = { article ->
                                coroutineScope.launch {
                                    articlesRef.child(article.vid.toString()).removeValue()
                                }
                            },
                            articlesRef = articlesRef,
                            modifier = Modifier.weight(0.5f)
                        )
                    }
                }
                else -> {
                    AfficheEntreBonsGro(
                        articlesEntreBonsGro = if (editionPassedMode) {
                            articlesEntreBonsGrosTabele.filter { it.passeToEndState }
                        } else {
                            articlesEntreBonsGrosTabele.filter { founisseurNowIs == null || it.grossisstBonN == founisseurNowIs }
                        },
                        onDeleteArticle = { article ->
                            coroutineScope.launch {
                                articlesRef.child(article.vid.toString()).removeValue()
                            }
                        },
                        articlesRef = articlesRef,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    if (showActionsDialog) {
        AlertDialog(
            onDismissRequest = { showActionsDialog = false },
            title = { Text("Actions") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showDeleteConfirmDialog = true
                            showActionsDialog = false
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete all data")
                        Spacer(Modifier.width(8.dp))
                        Text("Delete all data")
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Edition Passed Mode")
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = editionPassedMode,
                            onCheckedChange = {
                                editionPassedMode = it
                                showActionsDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete all data?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            articlesRef.removeValue()
                        }
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    SupplierSelectionDialog(
        showDialog = showSupplierDialog,
        onDismiss = { showSupplierDialog = false },
        onSupplierSelected = { selected ->
            founisseurNowIs = selected
        }
    )
}
@Composable
fun SupplierSelectionDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSupplierSelected: (Int) -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Supplier") },
            text = {
                Column {
                    for (i in 1..10) {
                        TextButton(
                            onClick = {
                                onSupplierSelected(i)
                                onDismiss()
                            }
                        ) {
                            Text("Supplier $i")
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
data class ImageZoomState(
    var scale: Float = 1f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
)

@Composable
fun ZoomableImage(
    imagePath: String,
    supplierId: Int?,
    modifier: Modifier = Modifier
) {
    val zoomStateSaver = Saver<ImageZoomState, List<Float>>(
        save = { listOf(it.scale, it.offsetX, it.offsetY) },
        restore = { ImageZoomState(it[0], it[1], it[2]) }
    )

    val zoomState = rememberSaveable(saver = zoomStateSaver) {
        ImageZoomState()
    }

    var scale by remember { mutableStateOf(zoomState.scale) }
    var offsetX by remember { mutableStateOf(zoomState.offsetX) }
    var offsetY by remember { mutableStateOf(zoomState.offsetY) }

    val context = LocalContext.current
    val imageUri = remember(imagePath) {
        try {
            Uri.parse(imagePath)
        } catch (e: Exception) {
            null
        }
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(imageUri).build()
    )

    Box(modifier = modifier.clipToBounds()) {
        Image(
            painter = painter,
            contentDescription = "Zoomable image for supplier $supplierId",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                )
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 3f)
                        val maxX = (size.width * (scale - 1)) / 2
                        val minX = -maxX
                        offsetX = (offsetX + pan.x).coerceIn(minX, maxX)
                        val maxY = (size.height * (scale - 1)) / 2
                        val minY = -maxY
                        offsetY = (offsetY + pan.y).coerceIn(minY, maxY)

                        // Update the saveable state
                        zoomState.scale = scale
                        zoomState.offsetX = offsetX
                        zoomState.offsetY = offsetY
                    }
                }
        )

        when (painter.state) {
            is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            is AsyncImagePainter.State.Error -> {
                Text(
                    text = "Error loading image",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {} // Do nothing for success state
        }
    }
}

fun updateSpecificArticle(input: String, article: EntreBonsGrosTabele, articlesRef: DatabaseReference): Boolean {
    val regex = """(\d+)\s*[x+]\s*(\d+(\.\d+)?)""".toRegex()
    val matchResult = regex.find(input)

    val (quantity, price) = matchResult?.destructured?.let {
        Pair(it.component1().toIntOrNull(), it.component2().toDoubleOrNull())
    } ?: Pair(null, null)

    if (quantity != null && price != null) {
        val updatedArticle = article.copy(
            quantityAcheteBG = quantity,
            newPrixAchatBG = price,
            subTotaleBG = price * quantity
        )
        articlesRef.child(article.vid.toString()).setValue(updatedArticle)
        return true
    }
    return false
}


fun processInputAndInsertData(
    input: String,
    articlesList: List<EntreBonsGrosTabele>,
    articlesRef: DatabaseReference,
    founisseurNowIs: Int?,
    articlesBaseDonne: List<BaseDonne>
): Long? {
    // Regular expression to match quantity and price
    val regex = """(\d+)\s*[x+]\s*(\d+(\.\d+)?)""".toRegex()
    val matchResult = regex.find(input)

    val (quantity, price) = matchResult?.destructured?.let {
        Pair(it.component1().toIntOrNull(), it.component2().toDoubleOrNull())
    } ?: Pair(null, null)

    if (quantity != null && price != null) {
        // Find the maximum vid in the existing list and increment it
        val newVid = (articlesList.maxOfOrNull { it.vid } ?: 0) + 1

        // Default quantityUniterBG to 1
        var quantityUniterBG = 1

        // If newVid exists in articlesBaseDonne, update quantityUniterBG
        val baseDonneEntry = articlesBaseDonne.find { it.idArticle.toLong() == newVid }
        if (baseDonneEntry != null) {
            quantityUniterBG = baseDonneEntry.nmbrUnite.toInt()
        }

        val newArticle = EntreBonsGrosTabele(
            vid = newVid,
            idArticle = newVid,
            nomArticleBG = "",
            ancienPrixBG = 0.0,
            newPrixAchatBG = price,
            quantityAcheteBG = quantity,
            quantityUniterBG = quantityUniterBG,
            subTotaleBG = price * quantity,
            grossisstBonN = founisseurNowIs ?: 0,
            uniterCLePlusUtilise = false,
            erreurCommentaireBG = ""
        )

        // Insert the new article into Firebase
        articlesRef.child(newVid.toString()).setValue(newArticle)
            .addOnSuccessListener {
                // Optionally handle successful insertion
                println("New article inserted successfully")
            }
            .addOnFailureListener { e ->
                // Handle any errors
                println("Error inserting new article: ${e.message}")
            }

        return newVid
    }

    // If the input doesn't match the expected format, return null
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
    articlesList: List<EntreBonsGrosTabele>
) {
    val effectiveVid = if (editionPassedMode) {
        articlesList.firstOrNull { it.passeToEndState }?.vid ?: vidOfLastQuantityInputted
    } else {
        vidOfLastQuantityInputted
    }

    if (suggestion == "passe" && effectiveVid != null) {
        val articleToUpdate = articlesRef.child(effectiveVid.toString())

        articleToUpdate.child("passeToEndState").setValue(true)
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

        // Update idArticle
        articleToUpdate.child("idArticle").setValue(idArticle)

        // Find corresponding ArticlesAcheteModele
        val correspondingArticle = articlesArticlesAcheteModele.find { it.idArticle == idArticle }
        correspondingArticle?.let { article ->
            articleToUpdate.child("nomArticleBG").setValue(article.nomArticleFinale)
            articleToUpdate.child("ancienPrixBG").setValue(article.prixAchat)
        }

        // Find corresponding BaseDonne and update quantityUniterBG
        val correspondingBaseDonne = articlesBaseDonne.find { it.idArticle.toLong() == idArticle }
        correspondingBaseDonne?.let { baseDonne ->
            articleToUpdate.child("quantityUniterBG").setValue(baseDonne.nmbrUnite.toInt())
        }

        // Call the callback to set nowItsNameInputeTime to false
        onNameInputComplete()
    }
}
// Remove the companion object from EntreBonsGrosTabele
data class EntreBonsGrosTabele(
    val vid: Long = 0,
    var idArticle: Long = 0,
    var nomArticleBG: String = "",
    var ancienPrixBG: Double = 0.0,
    var newPrixAchatBG: Double = 0.0,
    var quantityAcheteBG: Int = 0,
    var quantityUniterBG: Int = 0,
    var subTotaleBG: Double = 0.0,
    var grossisstBonN: Int = 0,
    var uniterCLePlusUtilise: Boolean = false,
    var erreurCommentaireBG: String = "",
    var passeToEndState: Boolean = false
    ) {
    // No-argument constructor for Firebase
    constructor() : this(0)

}
