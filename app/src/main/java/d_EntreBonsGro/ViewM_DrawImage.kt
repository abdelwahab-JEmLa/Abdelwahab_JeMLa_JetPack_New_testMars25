package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.util.Log
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
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@Composable
fun reconnaisanceVocaleLencer(
    selectedArticleStart: EntreBonsGrosTabele?,
    articlesRef: DatabaseReference,
    coroutineScope: CoroutineScope,
    baseDonneRef: DatabaseReference,
    articleDao: ArticleDao,
    initialFilteredSuggestions: List<String>,
    suggestionsList: List<String>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    context: Context,
    itsImageClick: Boolean,
    founisseurIdNowIs: Long?
): Pair<ManagedActivityResultLauncher<Intent, ActivityResult>, List<String>> {
    var filteredSuggestions by remember { mutableStateOf(initialFilteredSuggestions) }
    var showSuggestions by remember { mutableStateOf(false) }
       var vidDernierArticleouOnaEntreQuantity  by remember{ mutableStateOf<Long?>(null) }

    fun processVoiceInput(input: String) {
        if (input.firstOrNull()?.isDigit() == true || input.contains("+") || input.startsWith("-")) {

            val selectedArticle =
                if (itsImageClick) {
                    val lastIndex = articlesEntreBonsGrosTabele.indexOfLast { it.quantityAcheteBG != 0 }
                    if (lastIndex != -1 && lastIndex + 1 < articlesEntreBonsGrosTabele.size) {
                        articlesEntreBonsGrosTabele[lastIndex + 1]
                    } else {
                        null
                    }
                } else selectedArticleStart

            if (selectedArticle != null) {
                vidDernierArticleouOnaEntreQuantity=  selectedArticle.vidBG
            }
            selectedArticle?.let {
                updateQuantuPrixArticleDI(input, it, articlesRef, coroutineScope)
            }
        } else if (input.contains("تغيير")) {
            val selectedArticle=
                if (itsImageClick ){
                    articlesEntreBonsGrosTabele.lastOrNull { it.nomArticleBG == "" }
                } else selectedArticleStart

            // Fixed: Extract the new Arabic name correctly
            val parts = input.split("تغيير")
            val newArabName = if (parts.size > 1) parts[0].trim() else ""
            if (newArabName.isNotEmpty()) {
                coroutineScope.launch {
                    selectedArticle?.let { article ->
                        baseDonneRef.child(article.idArticleBG.toString()).child("nomArab")
                            .setValue(newArabName)
                        articleDao.updateArticleArabName(article.idArticleBG, newArabName)
                    }
                }
            } else {
                // Handle the case when no new name is provided before "تغيير"
                Toast.makeText(context, "Veuillez fournir un nouveau nom avant 'تغيير'", Toast.LENGTH_SHORT).show()
            }
        } else {
            val cleanInput = input.replace(".", "").toLowerCase()
            filteredSuggestions = suggestionsList.filter { it.replace(".", "").toLowerCase().contains(cleanInput) }

            val selectedArticle =
                if (itsImageClick) {
                    // Utilisez '==' pour la comparaison
                    articlesEntreBonsGrosTabele.firstOrNull { it.vidBG == vidDernierArticleouOnaEntreQuantity }
                } else selectedArticleStart

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

        val selectedArticle =
            if (itsImageClick) {
                articlesEntreBonsGrosTabele.firstOrNull { it.vidBG == vidDernierArticleouOnaEntreQuantity }
            } else selectedArticleStart

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

fun createNewArticle(
    articlesClient: List<EntreBonsGrosTabele>,
    founisseurIdNowIs: Long?,
    sectionsDonsChaqueImage: Int,
    supplierList: List<SupplierTabelle>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>
) {
    val currentArticleCount = articlesClient.count { it.supplierIdBG == founisseurIdNowIs }
    val targetArticleCount = sectionsDonsChaqueImage * 3 // 3 images
    val articlesToCreate = maxOf(targetArticleCount - currentArticleCount, 1)

    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")

    repeat(articlesToCreate) {
        val newVid = (articlesEntreBonsGrosTabele.maxOfOrNull { it.vidBG } ?: 0) + it + 1
        val currentDate = LocalDate.now().toString()
        val maxIdDivider = articlesClient.maxOfOrNull { it.idArticleInSectionsOfImageBG.split("-").lastOrNull()?.toIntOrNull() ?: 0 } ?: 0
        val newIdDivider = "$founisseurIdNowIs-$currentDate-${maxIdDivider + it + 1}"

        // Find the supplier name from the supplierList based on founisseurIdNowIs
        val supplierName = supplierList.firstOrNull { it.idSupplierSu == founisseurIdNowIs }?.nomSupplierSu ?: ""

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
            supplierNameBG = supplierName, // Assign the found supplier name
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
    val regex = """(?:(\d+)\s*[xX]\s*)?(\d+(?:\.\d+)?)\+?""".toRegex()
    val matchResult = regex.find(input)

    return when {
        matchResult != null -> {
            val quantity = matchResult.groupValues[1].toIntOrNull() ?: 1
            val price = matchResult.groupValues[2].toDoubleOrNull()

            if (price != null) {
                val updatedArticle = article.copy(
                    quantityAcheteBG = quantity,
                    newPrixAchatBG = price,
                    subTotaleBG = price * quantity
                )
                coroutineScope.launch {
                    articlesRef.child(article.vidBG.toString()).setValue(updatedArticle)
                }
                true
            } else {
                false
            }
        }
        else -> false
    }
}


fun updateArticleIdFromSuggestionDI(
    suggestion: String,
    selectedArticle: Long?,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    onNameInputComplete: () -> Unit,
    editionPassedMode: Boolean,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    coroutineScope: CoroutineScope
) {
    val effectiveVid = if (editionPassedMode) {
        articlesEntreBonsGrosTabele.firstOrNull { it.passeToEndStateBG }?.vidBG ?: selectedArticle
    } else {
        selectedArticle
    }

    if ((suggestion == "supp" || suggestion == "محو") && effectiveVid != null) {
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
        val currentArticle = articlesEntreBonsGrosTabele.find { it.vidBG == effectiveVid }

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
