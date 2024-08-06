package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import c_ManageBonsClients.ArticlesAcheteModele
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
    val coroutineScope = rememberCoroutineScope()
    val database = Firebase.database
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")
    val articlesAcheteModeleRef = database.getReference("ArticlesAcheteModeleAdapted")
    val baseDonneRef = database.getReference("e_DBJetPackExport")
    val focusRequester = remember { FocusRequester() }
    var editionPassedMode by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showActionsDialog by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                inputText = it
                val newVid = processInputAndInsertData(it, articlesEntreBonsGrosTabele, articlesRef)
                if (newVid != null) {
                    inputText = ""
                    focusRequester.requestFocus()
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

        baseDonneRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newBaseDonne = snapshot.children.mapNotNull { it.getValue(BaseDonne::class.java) }
                articlesBaseDonne = newBaseDonne
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EntreBonsGro") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                actions = {
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
                        val newVid = processInputAndInsertData(newValue, articlesEntreBonsGrosTabele, articlesRef)
                        if (newVid != null) {
                            inputText = ""
                            focusRequester.requestFocus()
                            nowItsNameInputeTime = true
                            vidOfLastQuantityInputted = newVid
                        }
                    }
                },
                nowItsNameInputeTime = nowItsNameInputeTime,
                onNameInputComplete = { nowItsNameInputeTime = false },
                focusRequester = focusRequester,
                articlesList = articlesEntreBonsGrosTabele,
                suggestionsList = suggestionsList,
                vidOfLastQuantityInputted = vidOfLastQuantityInputted,
                articlesRef = articlesRef,
                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                articlesBaseDonne = articlesBaseDonne,
                editionPassedMode = editionPassedMode,
                modifier = Modifier.fillMaxWidth()
            )

            AfficheEntreBonsGro(
                articlesEntreBonsGro = if (editionPassedMode) {
                    articlesEntreBonsGrosTabele.filter { it.passeToEndState }
                } else {
                    articlesEntreBonsGrosTabele
                },
                onDeleteArticle = { article ->
                    coroutineScope.launch {
                        articlesRef.child(article.vid.toString()).removeValue()
                    }
                },
                modifier = Modifier.weight(1f)
            )
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
}

@Composable
fun OutlineInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    nowItsNameInputeTime: Boolean,
    onNameInputComplete: () -> Unit,
    focusRequester: FocusRequester,
    articlesList: List<EntreBonsGrosTabele>,
    suggestionsList: List<String>,
    vidOfLastQuantityInputted: Long?,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    editionPassedMode: Boolean,
    modifier: Modifier = Modifier
) {
    val lastArticle = if (editionPassedMode) {
        articlesList.filter { it.passeToEndState }.maxByOrNull { it.vid }
    } else {
        articlesList.maxByOrNull { it.vid }
    }
    var showDropdown by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { newValue ->
                onInputChange(newValue)
                val cleanInput = newValue.replace(".", "").toLowerCase()
                filteredSuggestions = if (cleanInput.length >= 3) {
                    suggestionsList.filter {
                        it.replace(".", "").toLowerCase().contains(cleanInput)
                    }
                } else {
                    emptyList()
                }
                showDropdown = filteredSuggestions.isNotEmpty() && newValue.isNotEmpty()
            },
            label = {
                Text(
                    when {
                        inputText.isEmpty() && nowItsNameInputeTime && lastArticle != null ->
                            "Quantity: ${lastArticle.quantityAcheteBG} x ${lastArticle.newPrixAchatBG}"
                        inputText.isEmpty() && !nowItsNameInputeTime && vidOfLastQuantityInputted != null -> {
                            val lastInputtedArticle = articlesList.find { it.vid == vidOfLastQuantityInputted }
                            lastInputtedArticle?.let {
                                "last: ${it.quantityAcheteBG} x ${it.newPrixAchatBG} (${it.nomArticleBG})"
                            } ?: "Entrer quantité et prix"
                        }
                        inputText.isEmpty() -> "Entrer quantité et prix"
                        else -> inputText
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {

                        updateArticleIdFromSuggestion(
                            suggestion,
                            vidOfLastQuantityInputted,
                            articlesRef,
                            articlesArticlesAcheteModele,
                            articlesBaseDonne,
                            onNameInputComplete,
                            editionPassedMode,
                            articlesList
                        )
                        onInputChange("")
                        showDropdown = false
                    }
                )
            }
        }
    }
}

@Composable
fun AfficheEntreBonsGro(
    articlesEntreBonsGro: List<EntreBonsGrosTabele>,
    onDeleteArticle: (EntreBonsGrosTabele) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(articlesEntreBonsGro) { article ->
            ArticleItem(article, onDeleteArticle)
        }
    }
}


@Composable
fun ArticleItem(article: EntreBonsGrosTabele, onDelete: (EntreBonsGrosTabele) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.nomArticleBG,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onDelete(article) }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete article",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ancien prix: ${article.ancienPrixBG}")
                    Text("Nouveau prix: ${article.newPrixAchatBG}")
                }
                Column {
                    Text("Quantité: ${article.quantityAcheteBG}")
                    Text("Unités: ${article.quantityUniterBG}")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sous-total: ${article.subTotaleBG}")
            if (article.erreurCommentaireBG.isNotBlank()) {
                Text(
                    text = "Erreur: ${article.erreurCommentaireBG}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

fun processInputAndInsertData(input: String, articlesList: List<EntreBonsGrosTabele>, articlesRef: DatabaseReference): Long? {
    val regex = """(\d+)\s*[x+]\s*(\d+(\.\d+)?)""".toRegex()
    val matchResult = regex.find(input)

    val (quantity, price) = matchResult?.destructured?.let {
        Pair(it.component1().toIntOrNull(), it.component2().toDoubleOrNull())
    } ?: Pair(null, null)

    if (quantity != null && price != null) {
        // Create new article
        val newVid = (articlesList.maxByOrNull { it.vid }?.vid ?: 0) + 1
        val newArticle = EntreBonsGrosTabele(
            vid = newVid,
            idArticle = newVid,
            nomArticleBG = "",
            ancienPrixBG = 0.0,
            newPrixAchatBG = price,
            quantityAcheteBG = quantity,
            quantityUniterBG = 1,
            subTotaleBG = price * quantity,
            grossisstBonN = 0,
            uniterCLePlusUtilise = false,
            erreurCommentaireBG = ""
        )
        articlesRef.child(newVid.toString()).setValue(newArticle)
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
            articleToUpdate.child("quantityUniterBG").setValue(baseDonne.nmbrUnite)
        }

        // Call the callback to set nowItsNameInputeTime to false
        onNameInputComplete()
    }
}
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

    fun getColumnValue(columnName: String): Any = when (columnName) {
        "nomArticleBG" -> nomArticleBG
        "ancienPrixBG" -> ancienPrixBG
        "newPrixAchatBG" -> newPrixAchatBG
        "quantityAcheteBG" -> quantityAcheteBG
        "quantityUniterBG" -> quantityUniterBG
        "subTotaleBG" -> subTotaleBG
        "grossisstBonN" -> grossisstBonN
        "uniterCLePlusUtilise" -> uniterCLePlusUtilise
        "erreurCommentaireBG" -> erreurCommentaireBG
        else -> ""
    }
}