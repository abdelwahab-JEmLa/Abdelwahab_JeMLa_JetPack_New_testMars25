package d_EntreBonsGro

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

suspend fun setFirebaseValues(articles: List<ArticlesBonsGrosTabele>) {
    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")

    articles.forEach { article ->
        articlesRef.child(article.vid.toString()).setValue(article).await()
    }
}

// Example usage
suspend fun initializeFirebaseWithExampleData() {
    val exampleArticles = listOf(
        ArticlesBonsGrosTabele(
            vid = 1,
            idArticleBG = 101,
            nomArticleBG = "Article 1",
            ancienPrixBG = 10.0,
            newPrixAchatBG = 12.0,
            quantityAcheteBG = 5.0,
            quantityUniterBG = 1.0,
            subTotaleBG = 60.0,
            grossisstBonNumBG = 1001,
            uniterLePlusUtiliseStateBG = true,
            erreurCommentaireBG = ""
        ),
        ArticlesBonsGrosTabele(
            vid = 2,
            idArticleBG = 102,
            nomArticleBG = "Article 2",
            ancienPrixBG = 15.0,
            newPrixAchatBG = 14.5,
            quantityAcheteBG = 3.0,
            quantityUniterBG = 2.0,
            subTotaleBG = 43.5,
            grossisstBonNumBG = 1001,
            uniterLePlusUtiliseStateBG = false,
            erreurCommentaireBG = "Prix réduit"
        ),
        ArticlesBonsGrosTabele(
            vid = 3,
            idArticleBG = 103,
            nomArticleBG = "Article 3",
            ancienPrixBG = 20.0,
            newPrixAchatBG = 22.0,
            quantityAcheteBG = 4.0,
            quantityUniterBG = 1.0,
            subTotaleBG = 88.0,
            grossisstBonNumBG = 1002,
            uniterLePlusUtiliseStateBG = true,
            erreurCommentaireBG = ""
        )
    )

    setFirebaseValues(exampleArticles)
}