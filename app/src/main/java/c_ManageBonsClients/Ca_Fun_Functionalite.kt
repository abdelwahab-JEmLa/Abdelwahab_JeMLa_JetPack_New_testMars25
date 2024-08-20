package c_ManageBonsClients

import android.provider.Settings.System
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun createEmptyArticle(nomClient: String) {
    val database = FirebaseDatabase.getInstance()
    val articleRef = database.getReference("ArticlesAcheteModeleAdapted")

    articleRef.orderByChild("nomClient").equalTo(nomClient).limitToFirst(1).get().addOnSuccessListener { clientSnapshot ->
        val firstClientArticle = clientSnapshot.children.firstOrNull()?.getValue(ArticlesAcheteModele::class.java)
        val clientDate = firstClientArticle?.dateDachate ?: LocalDate.now().format(
            DateTimeFormatter.ofPattern(
                System.DATE_FORMAT
            ))

        articleRef.get().addOnSuccessListener { allArticlesSnapshot ->
            val maxId = allArticlesSnapshot.children.mapNotNull { it.child("idArticle").getValue(Long::class.java) }.maxOrNull() ?: 0
            val maxVid =allArticlesSnapshot.children.mapNotNull { it.child("vid").getValue(Long::class.java) }.maxOrNull() ?: 1
            val newId = maxId + 1

            val emptyArticle = ArticlesAcheteModele(
                vid = maxVid,
                idArticle = newId,
                nomArticleFinale = "New Empty Article",
                nomClient = nomClient,
                totalQuantity = 1,
                dateDachate = clientDate,
                typeEmballage = "Boit", // Default value
                choisirePrixDepuitFireStoreOuBaseBM = "CardFireBase" // Default value
            )

            articleRef.child(maxVid.toString()).setValue(emptyArticle)
        }
    }.addOnFailureListener { exception ->
        // Handle any errors here
        println("Error creating empty article: ${exception.message}")
    }
}