package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference

@Composable
fun OutlineInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    nowItsNameInputeTime: Boolean,
    onNameInputComplete: () -> Unit,
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
    speechRecognizerLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    onInputChange: (String) -> Unit,
    processInputAndInsertData: (String, List<EntreBonsGrosTabele>, DatabaseReference) -> Long?,
    articlesRef: DatabaseReference,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(articlesEntreBonsGro) { article ->
            ArticleItem(
                article = article,
                onDelete = onDeleteArticle,
                onInputChange = onInputChange,
                processInputAndInsertData = processInputAndInsertData,
                articlesEntreBonsGrosTabele = articlesEntreBonsGro,
                articlesRef = articlesRef
            )
        }
    }
}


@Composable
fun ArticleItem(
    article: EntreBonsGrosTabele,
    onDelete: (EntreBonsGrosTabele) -> Unit,
    onInputChange: (String) -> Unit,
    processInputAndInsertData: (String, List<EntreBonsGrosTabele>, DatabaseReference) -> Long?,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    articlesRef: DatabaseReference
) {
    var lastLaunchTime by remember { mutableStateOf(0L) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                onInputChange(it)
                val newVid = processInputAndInsertData(it, articlesEntreBonsGrosTabele, articlesRef)
                if (newVid != null) {
                    onInputChange("")
                    // You might want to handle nowItsNameInputeTime and vidOfLastQuantityInputted here
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastLaunchTime > 1000) { // Prevent multiple rapid launches
                    lastLaunchTime = currentTime
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
                    }
                    speechRecognizerLauncher.launch(intent)
                }
            },
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
