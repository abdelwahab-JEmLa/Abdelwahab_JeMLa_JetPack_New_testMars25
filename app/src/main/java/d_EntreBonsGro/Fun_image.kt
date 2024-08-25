package d_EntreBonsGro

import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate


fun createNewArticle(articles: List<EntreBonsGrosTabele>, founisseurIdNowIs: Long?) {
    val newVid = (articles.maxOfOrNull { it.vidBG } ?: 0) + 1
    val currentDate = LocalDate.now().toString()
    val maxIdDivider = articles.maxOfOrNull { it.idArticleInSectionsOfImageBG.split("-").lastOrNull()?.toIntOrNull() ?: 0 } ?: 0
    val newIdDivider = "$founisseurIdNowIs-$currentDate-${maxIdDivider + 1}"

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
        grossisstBonN =  0,
        supplierIdBG = founisseurIdNowIs ?: 0,
        supplierNameBG = "",
        uniterCLePlusUtilise = false,
        erreurCommentaireBG = "",
        passeToEndStateBG = true,
        dateCreationBG = currentDate
    )

    // Insert the new article into Firebase
    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")

    newArticle.let {
        articlesRef.child(newVid.toString()).setValue(it)
            .addOnSuccessListener {
                println("New article inserted successfully")
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