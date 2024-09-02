package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import b_Edite_Base_Donne.ArticleDao
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Composable
fun reconnaisanceVocaleLencer(
    selectedArticle: EntreBonsGrosTabele?,
    articlesRef: DatabaseReference,
    coroutineScope: CoroutineScope,
    baseDonneRef: DatabaseReference,
    articleDao: ArticleDao,
    initialFilteredSuggestions: List<String>,
    suggestionsList: List<String>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    context: Context
): Pair<ManagedActivityResultLauncher<Intent, ActivityResult>, List<String>> {
    var filteredSuggestions by remember { mutableStateOf(initialFilteredSuggestions) }
    var showSuggestions by remember { mutableStateOf(false) }

    fun processVoiceInput(input: String) {
        if (input.firstOrNull()?.isDigit() == true || input.contains("+") || input.startsWith("-")) {
            selectedArticle?.let {
                updateQuantuPrixArticleDI(input, it, articlesRef, coroutineScope)
            }
        } else if (input.contains("تغيير")) {
            // Fixed: Extract the new Arabic name correctly
            val newArabName = input.substringAfter("تغيير").trim()
            if (newArabName.isNotEmpty()) {
                coroutineScope.launch {
                    selectedArticle?.let { article ->
                        baseDonneRef.child(article.idArticleBG.toString()).child("nomArab")
                            .setValue(newArabName)
                        articleDao.updateArticleArabName(article.idArticleBG, newArabName)
                    }
                }
            } else {
                // Handle the case when no new name is provided after "تغيير"
                Toast.makeText(context, "Veuillez fournir un nouveau nom après 'تغيير'", Toast.LENGTH_SHORT).show()
            }
        } else {
            val cleanInput = input.replace(".", "").toLowerCase()
            filteredSuggestions = suggestionsList.filter { it.replace(".", "").toLowerCase().contains(cleanInput) }

            when {
                filteredSuggestions.size == 1 -> {
                    updateArticleIdFromSuggestionDI(
                        suggestion = filteredSuggestions[0],
                        selectedArticle = selectedArticle?.vidBG,
                        articlesRef = articlesRef,
                        articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                        articlesBaseDonne = articlesBaseDonne,
                        onNameInputComplete = { /* Implement if needed */ },
                        editionPassedMode = false,
                        articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                        coroutineScope = coroutineScope
                    )
                }
                filteredSuggestions.isEmpty() -> {
                    val filteredSuggestions3Sentence = suggestionsList.filter {
                        it.replace(".", "").toLowerCase().contains(cleanInput.take(3))
                    }
                    showSuggestions = true
                    filteredSuggestions = filteredSuggestions3Sentence
                }
                else -> {
                    showSuggestions = true
                }
            }
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                processVoiceInput(it)
            }
        } else {
            Toast.makeText(
                context,
                "La reconnaissance vocale a échoué. Veuillez réessayer.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    if (showSuggestions) {
        SuggestionsDialog(
            filteredSuggestions = filteredSuggestions,
            onSuggestionSelected = { suggestion ->
                updateArticleIdFromSuggestionDI(
                    suggestion = suggestion,
                    selectedArticle = selectedArticle?.vidBG,
                    articlesRef = articlesRef,
                    articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                    articlesBaseDonne = articlesBaseDonne,
                    onNameInputComplete = { /* Implement if needed */ },
                    editionPassedMode = false,
                    articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                    coroutineScope = coroutineScope
                )
                showSuggestions = false
            },
            onDismiss = { showSuggestions = false }
        )
    }
    return Pair(speechRecognizerLauncher, filteredSuggestions)
}
