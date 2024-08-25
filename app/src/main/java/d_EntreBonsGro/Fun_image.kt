package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.ArticleDao
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun createNewArticle(articles: List<EntreBonsGrosTabele>, founisseurIdNowIs: Long?, sectionsDonsChaqueImage: Int) {
    val currentArticleCount = articles.count { it.supplierIdBG == founisseurIdNowIs }
    val targetArticleCount = sectionsDonsChaqueImage * 3 // 3 images
    val articlesToCreate = maxOf(targetArticleCount - currentArticleCount, 1)

    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")

    repeat(articlesToCreate) {
        val newVid = (articles.maxOfOrNull { it.vidBG } ?: 0) + it + 1
        val currentDate = LocalDate.now().toString()
        val maxIdDivider = articles.maxOfOrNull { it.idArticleInSectionsOfImageBG.split("-").lastOrNull()?.toIntOrNull() ?: 0 } ?: 0
        val newIdDivider = "$founisseurIdNowIs-$currentDate-${maxIdDivider + it + 1}"

        val newArticle = EntreBonsGrosTabele(
            vidBG = newVid,
            idArticleInSectionsOfImageBG = newIdDivider,
            idArticleBG = 0,
            nomArticleBG = "",
            ancienPrixBG = 0.0,
            newPrixAchatBG = 0.0,
            quantityAcheteBG = 0,
            quantityUniterBG = 1,
            subTotaleBG = 0.0,
            grossisstBonN = 0,
            supplierIdBG = founisseurIdNowIs ?: 0,
            supplierNameBG = "",
            uniterCLePlusUtilise = false,
            erreurCommentaireBG = "",
            passeToEndStateBG = true,
            dateCreationBG = currentDate
        )

        articlesRef.child(newVid.toString()).setValue(newArticle)
            .addOnSuccessListener {
                println("New article inserted successfully: $newVid")
            }
            .addOnFailureListener { e ->
                println("Error inserting new article: ${e.message}")
            }
    }
}
fun deleteTheNewArticleIZ(vidBG: Long) {
    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")

    articlesRef.child(vidBG.toString()).removeValue()
        .addOnSuccessListener {
            println("Article deleted successfully")
        }
        .addOnFailureListener { e ->
            println("Error deleting article: ${e.message}")
        }
}
fun updateQuantuPrixArticleDI(input: String, article: EntreBonsGrosTabele, articlesRef: DatabaseReference, coroutineScope: CoroutineScope): Boolean {
    // Updated regex to handle both implicit and explicit quantity
    val regex = """(?:(\d+)\s*[xX]\s*)?(\d+(?:\.\d+)?)?""".toRegex()
    val matchResult = regex.find(input)
//TODO fix it that worck
    // Handle cases where only price or quantity is provided
    val (quantityStr, priceStr) = when {
        matchResult?.groupValues?.get(1)?.isNotEmpty() == true -> {
            // Both quantity and price provided (explicit case)
            Pair(matchResult.groupValues[1], matchResult.groupValues[2])
        }
        matchResult?.groupValues?.get(2)?.isNotEmpty() == true -> {
            // Only price provided (implicit quantity of 1)
            Pair("1", matchResult.groupValues[2])
        }
        else -> {
            // Invalid input
            return null
        }
    }



        val updatedArticle = article.copy(
            quantityAcheteBG = quantity,
            newPrixAchatBG = price,
            subTotaleBG = price * quantity
        )
        articlesRef.child(article.vidBG.toString()).setValue(updatedArticle)



        return true
    }
    return false
}

@Composable
fun VoiceInputDI(
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    articlesRef: DatabaseReference,
    baseDonneRef: DatabaseReference,
    founisseurNowIs: Int?,
    articlesBaseDonne: List<BaseDonne>,
    suppliersList: List<SupplierTabelle>,
    suggestionsList: List<String>,
    onInputProcessed: (Long?) -> Unit,
    updateArticleIdFromSuggestion: (String, Long?, DatabaseReference, List<ArticlesAcheteModele>, List<BaseDonne>, () -> Unit, Boolean, List<EntreBonsGrosTabele>, CoroutineScope) -> Unit,
    vidOfLastQuantityInputted: Long?,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    editionPassedMode: Boolean,
    coroutineScope: CoroutineScope,
    articleDao: ArticleDao
) {
    var inputText by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }

    fun processVoiceInput(input: String) {
        if (input.contains("+")) {
            val newVid = processInputAndInsertData(input, articlesEntreBonsGrosTabele, articlesRef, founisseurNowIs, articlesBaseDonne, suppliersList)
            onInputProcessed(newVid)
        } else if (input.contains("تغيير")) {
            val newArabName = input.substringAfter("تغيير").trim()
            coroutineScope.launch {
                vidOfLastQuantityInputted?.let { vid ->
                    val article = articlesEntreBonsGrosTabele.find { it.vidBG == vid }
                    article?.let { foundArticle ->
                        baseDonneRef.child(foundArticle.idArticleBG.toString()).child("nomArab").setValue(newArabName)
                        articleDao.updateArticleArabName(foundArticle.idArticleBG, newArabName)
                    }
                }
            }
            onInputProcessed(null)
        } else {
            val cleanInput = input.replace(".", "").toLowerCase()
            filteredSuggestions = suggestionsList.filter { it.replace(".", "").toLowerCase().contains(cleanInput) }

            when {
                filteredSuggestions.size == 1 -> {
                    updateArticleIdFromSuggestionDI(
                        filteredSuggestions[0],
                        vidOfLastQuantityInputted,
                        articlesRef,
                        articlesArticlesAcheteModele,
                        articlesBaseDonne,
                        { onInputProcessed(null) },
                        editionPassedMode,
                        articlesEntreBonsGrosTabele,
                        coroutineScope
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

    val context = LocalContext.current
    var isRecognizing by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecognizing = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                inputText = it
                processVoiceInput(it)
            }
        } else {
            Toast.makeText(context, "La reconnaissance vocale a échoué. Veuillez réessayer.", Toast.LENGTH_SHORT).show()
        }
    }

    FloatingActionButton(
        onClick = {
            if (!isRecognizing) {
                isRecognizing = true
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
                }
                try {
                    speechRecognizerLauncher.launch(intent)
                } catch (e: Exception) {
                    isRecognizing = false
                    Toast.makeText(context, "Erreur lors du lancement de la reconnaissance vocale: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Reconnaissance vocale en cours...", Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Icon(Icons.Default.Mic, contentDescription = "Voice Input")
    }
    if (showSuggestions) {
        AlertDialog(
            onDismissRequest = { showSuggestions = false },
            title = { Text("Suggestions") },
            text = {
                LazyColumn {
                    items(filteredSuggestions) { suggestion ->
                        val randomColor = Color(
                            red = (0..255).random(),
                            green = (0..255).random(),
                            blue = (0..255).random()
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = randomColor
                            )
                        ) {
                            TextButton(
                                onClick = {
                                    updateArticleIdFromSuggestion(
                                        suggestion,
                                        vidOfLastQuantityInputted,
                                        articlesRef,
                                        articlesArticlesAcheteModele,
                                        articlesBaseDonne,
                                        { onInputProcessed(null) },
                                        editionPassedMode,
                                        articlesEntreBonsGrosTabele,
                                        coroutineScope
                                    )
                                    showSuggestions = false
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(suggestion)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuggestions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
fun updateArticleIdFromSuggestionDI(
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

    if (suggestion == "supp" || suggestion == "محو" && effectiveVid != null) {
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
            articleToUpdate.child("passeToEndStateBG").setValue(false)
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



