package d_EntreBonsGro

import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate


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