package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                val newBaseDonne = snapshot.children.mapNotNull { childSnapshot ->
                    try {
                        val idArticle = childSnapshot.child("idArticle").getValue(Int::class.java)
                        val nmbrUnite = childSnapshot.child("nmbrUnite").getValue(Int::class.java)
                        if (idArticle != null && nmbrUnite != null) {
                            BaseDonne(idArticle, nmbrUnite.toString())
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        println("Error deserializing: ${e.message}")
                        null
                    }
                }
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
                speechRecognizerLauncher = speechRecognizerLauncher,
                onInputChange = { newValue -> inputText = newValue },
                processInputAndInsertData = ::processInputAndInsertData,
                articlesRef = articlesRef,
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
