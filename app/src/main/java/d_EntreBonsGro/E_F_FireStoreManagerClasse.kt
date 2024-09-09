package d_EntreBonsGro

import a_RoomDB.BaseDonne
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

suspend fun exportToFirestore() {
    withContext(Dispatchers.IO) {
        try {
            // Fetch current data from Firebase Realtime Database
            val firebase = Firebase.database
            val articlesRef = firebase.getReference("ArticlesBonsGrosTabele")
            val snapshot = articlesRef.get().await()

            val supplierArticles = snapshot.children.mapNotNull { it.getValue(EntreBonsGrosTabele::class.java) }

            // Create a reference to the F_SupplierArticlesFireS collection in Firestore
            val firestore = FirebaseFirestore.getInstance()
            val supplierArticlesRef = firestore.collection("F_SupplierArticlesFireS")

            // Create a batch to perform multiple write operations
            val batch = firestore.batch()

            // Group articles by supplier
            val supplierGroups = supplierArticles.groupBy { it.supplierIdBG }

            // Process each article and calculate totals
            supplierGroups.forEach { (supplierId, articles) ->
                var totaleDeCeBon = 0.0
                val currentDateTime = LocalDateTime.now()
                val dayOfWeek = currentDateTime.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.FRENCH)
                val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = currentDateTime.format(dateTimeFormatter)

                articles.forEach { article ->
                    val lineData = hashMapOf(
                        "vidBG" to article.vidBG,
                        "idArticleBG" to article.idArticleBG,
                        "nomArticleBG" to article.nomArticleBG,
                        "ancienPrixBG" to article.ancienPrixBG,
                        "newPrixAchatBG" to article.newPrixAchatBG,
                        "quantityAcheteBG" to article.quantityAcheteBG,
                        "quantityUniterBG" to article.quantityUniterBG,
                        "subTotaleBG" to article.subTotaleBG,
                        "grossisstBonN" to article.grossisstBonN,
                        "supplierIdBG" to article.supplierIdBG,
                        "supplierNameBG" to article.supplierNameBG,
                        "uniterCLePlusUtilise" to article.uniterCLePlusUtilise,
                        "erreurCommentaireBG" to article.erreurCommentaireBG,
                        "passeToEndStateBG" to article.passeToEndStateBG,
                        "dateCreationBG" to article.dateCreationBG
                    )

                    // Add to F_SupplierArticlesFireS collection
                    val docRef = supplierArticlesRef
                        .document(supplierId.toString())
                        .collection("historiquesAchats")
                        .document(article.idArticleBG.toString())
                    batch.set(docRef, lineData)

                    // Calculate total
                    totaleDeCeBon += article.subTotaleBG
                }
                // Fetch current totalCredit from Firestore
                val totalCreditDoc = supplierArticlesRef
                    .document(supplierId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                val ancienCredit = totalCreditDoc.getDouble("ancienCredit") ?: 0.0

                // Calculate the new total credit
                val newTotalCredit = ancienCredit + totaleDeCeBon
                // Prepare the updated total data
                val totalData = hashMapOf(
                    "date" to formattedDateTime,
                    "totaleDeCeBon" to totaleDeCeBon,
                    "payeCetteFoit" to 0.0,
                    "creditFaitDonCeBon" to totaleDeCeBon,
                    "ancienCredits" to newTotalCredit
                )

                // Update the total for the supplier
                // Use the new document ID format with date and time
                val documentId = documentIdFireStoreClientCredit()
                val totalDocRef = supplierArticlesRef
                    .document(supplierId.toString())
                    .collection("Totale et Credit Des Bons")
                    .document(documentId)
                batch.set(totalDocRef, totalData)

                // Update the latest document
                val latestDocRef = supplierArticlesRef
                    .document(supplierId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                batch.set(latestDocRef, totalData)
            }

            // Commit the batch
            batch.commit().await()
            println("Successfully exported articles and totals to Firestore")

        } catch (e: Exception) {
            println("Error exporting to Firestore: ${e.message}")
        }
    }
}
fun deleteReferencesWithSupplierId100(articlesRef: DatabaseReference, coroutineScope: CoroutineScope) {
    coroutineScope.launch(Dispatchers.IO) {
        try {
            val snapshot = articlesRef.orderByChild("supplierIdBG").equalTo(100.0).get().await()
            snapshot.children.forEach { childSnapshot ->
                childSnapshot.ref.removeValue().await()
            }
            println("Successfully deleted all references with supplierIdBG = 100")
        } catch (e: Exception) {
            println("Error deleting references: ${e.message}")
        }
    }
}
fun findAndAddMissingArticles(
    articlesRef: DatabaseReference,
    articlesAcheteModeleRef: DatabaseReference,
    coroutineScope: CoroutineScope,
    onProgress: (Int, Int) -> Unit
) {
    coroutineScope.launch(Dispatchers.IO) {
        try {
            // Fetch existing articles
            val entreBonsGrosSnapshot = articlesRef.get().await()
            val acheteModeleSnapshot = articlesAcheteModeleRef.get().await()

            // Extract IDs
            val entreBonsGrosIds = entreBonsGrosSnapshot.children
                .mapNotNull { it.getValue(EntreBonsGrosTabele::class.java)?.idArticleBG }
                .toSet()
            val acheteModeleIds = acheteModeleSnapshot.children
                .mapNotNull { it.getValue(ArticlesAcheteModele::class.java)?.idArticle?.toLong() }
                .toSet()

            // Find missing IDs
            val missingIds = acheteModeleIds - entreBonsGrosIds
            val totalMissing = missingIds.size

            var addedCount = 0

            // Get the current maximum VID
            var maxVid = entreBonsGrosSnapshot.children
                .mapNotNull { it.getValue(EntreBonsGrosTabele::class.java)?.vidBG }
                .maxOrNull() ?: 0

            missingIds.forEach { id ->
                val acheteModeleArticle = acheteModeleSnapshot.children
                    .find { it.getValue(ArticlesAcheteModele::class.java)?.idArticle?.toLong() == id }
                    ?.getValue(ArticlesAcheteModele::class.java)

                acheteModeleArticle?.let { article ->
                    // Increment maxVid for each new article
                    maxVid++

                    val newArticle = EntreBonsGrosTabele(
                        vidBG = maxVid,
                        idArticleBG = id,
                        nomArticleBG = article.nomArticleFinale,
                        ancienPrixBG = article.prixAchat,
                        newPrixAchatBG = article.prixAchat,
                        quantityAcheteBG = 0,
                        quantityUniterBG = 1,
                        subTotaleBG = 0.0,
                        grossisstBonN = 0,
                        supplierIdBG = 100,
                        supplierNameBG = "MissingArticles",
                        dateCreationBG = LocalDate.now().toString()
                    )

                    // Use a transaction to ensure atomic write
                    articlesRef.child(maxVid.toString()).runTransaction(object : Transaction.Handler {
                        override fun doTransaction(mutableData: MutableData): Transaction.Result {
                            if (mutableData.getValue(EntreBonsGrosTabele::class.java) == null) {
                                mutableData.value = newArticle
                                return Transaction.success(mutableData)
                            }
                            return Transaction.abort()
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {
                            if (committed) {
                                addedCount++
                                onProgress(addedCount, totalMissing)
                            } else {
                                println("Failed to add article with ID $id: ${error?.message}")
                            }
                        }
                    })
                }
            }

            println("Successfully added $addedCount missing articles")
        } catch (e: Exception) {
            println("Error finding and adding missing articles: ${e.message}")
            onProgress(0, 0)
        }
    }
}
suspend fun trensfertBonSuppAuDataBaseArticles(
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    articlesBaseDonne: List<BaseDonne>,
    onProgressUpdate: (Float) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            val firebase = Firebase.database
            val refArticlesAcheteModele = firebase.getReference("ArticlesAcheteModeleAdapted")
            val dbJetPackExportRef = firebase.getReference("e_DBJetPackExport")

            val dateFormat = SimpleDateFormat("yyyy/MM/dd")
            val dateCreationCategorie = dateFormat.format(Date())

            val articlesEntreBonsGrosTabeleChoisi = articlesEntreBonsGrosTabele.filter { it.idArticleBG.toInt() != 0
            }
            val totalArticles = articlesEntreBonsGrosTabeleChoisi.size

            articlesEntreBonsGrosTabeleChoisi.forEachIndexed { index, article ->
                val calculatedPrice = if (article.uniterCLePlusUtilise) {
                    article.newPrixAchatBG * article.quantityUniterBG
                } else {
                    article.newPrixAchatBG
                }

                val articleBaseDonne = articlesBaseDonne.find { it.idArticle.toLong() == article.idArticleBG }
                val calculatedPriceVent = calculatedPrice + (articleBaseDonne?.monBenfice ?: 0.0)
                val calculatedPriceVentUniter = calculatedPrice + ((articleBaseDonne?.monBenfice ?: 0.0)/(articleBaseDonne?.nmbrUnite ?: 1))

                refArticlesAcheteModele.orderByChild("idArticle").equalTo(article.idArticleBG.toDouble()).get().await().children.forEach { childSnapshot ->
                    childSnapshot.ref.child("prixAchat").setValue(calculatedPrice)
                    childSnapshot.ref.child("monPrixVent").setValue(calculatedPriceVent)
                    childSnapshot.ref.child("warningRecentlyChanged").setValue(true)
                }

                dbJetPackExportRef.child(article.idArticleBG.toString()).apply {
                    child("monPrixAchat").setValue(calculatedPrice)
                    child("monPrixVent").setValue(calculatedPriceVent)
                    child("monPrixVentUniter").setValue(calculatedPriceVentUniter)
                    child("dateCreationCategorie").setValue(dateCreationCategorie)
                }

                // Update progress
                onProgressUpdate((index + 1).toFloat() / totalArticles)
            }

            println("Successfully updated e_DBJetPackExport and all matching entries in ArticlesAcheteModeleAdapted")
        } catch (e: Exception) {
            println("Error updating databases: ${e.message}")
        }
    }
}

fun updateSupplierBon(suppliersRef: DatabaseReference, supplierId: Int, bonNumber: String) {
    suppliersRef.child(supplierId.toString()).child("bonDuSupplierSu").setValue(bonNumber)
}
