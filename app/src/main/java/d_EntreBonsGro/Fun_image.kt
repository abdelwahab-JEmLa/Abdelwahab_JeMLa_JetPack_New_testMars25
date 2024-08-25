package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.util.Log
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
