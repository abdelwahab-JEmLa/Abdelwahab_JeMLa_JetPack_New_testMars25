package d_EntreBonsGro

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate

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

suspend fun trensfertBonSuppAuDataBaseArticles() {
    withContext(Dispatchers.IO) {
        try {
            val firebase = Firebase.database
            val articlesEntreBonsGrosTabeleRef = firebase.getReference("ArticlesBonsGrosTabele")
            val snapshotEntreBonsGrosTabele = articlesEntreBonsGrosTabeleRef.get().await()
            val articlesEntreBonsGrosTabele = snapshotEntreBonsGrosTabele.children.mapNotNull { it.getValue(EntreBonsGrosTabele::class.java) }

            val dbJetPackExportRef = firebase.getReference("e_DBJetPackExport")
            val refArticlesAcheteModele = firebase.getReference("ArticlesAcheteModeleAdapted")

            articlesEntreBonsGrosTabele.forEach { article ->
                // Calculate the price based on uniterCLePlusUtilise
                val calculatedPrice = if (article.uniterCLePlusUtilise) {
                    article.newPrixAchatBG * article.quantityUniterBG
                } else {
                    article.newPrixAchatBG
                }

                // Update all matching entries in ArticlesAcheteModeleAdapted
                refArticlesAcheteModele.orderByChild("idArticle").equalTo(article.idArticleBG.toDouble()).get().addOnSuccessListener { snapshot ->
                    snapshot.children.forEach { childSnapshot ->
                        childSnapshot.ref.child("prixAchat").setValue(calculatedPrice)
                    }
                }

                // Update e_DBJetPackExport
                dbJetPackExportRef.child(article.idArticleBG.toString()).child("monPrixAchat")
                    .setValue(calculatedPrice)
            }

            println("Successfully updated e_DBJetPackExport and all matching entries in ArticlesAcheteModeleAdapted")
        } catch (e: Exception) {
            println("Error updating databases: ${e.message}")
        }
    }
}
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
                var supplierTotal = 0.0
                val currentDate = LocalDate.now().toString()

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
                    supplierTotal += article.subTotaleBG
                }

                // Export the total for the supplier
                val totalData = hashMapOf(
                "date" to currentDate,
                "totalAmount" to supplierTotal
            )//TODO regle cette parti pour quelle utilise updateSupplierCredit
                //fait que ici      si  totalCredit dons fire store = 0  "totalCredit" to supplierCredit, =         "totalAmount" to supplierTotal,
    // et le rest         "restCredit" to restCredit = totalAmount
                val totalDocRef = supplierArticlesRef
                    .document(supplierId.toString())
                    .collection("totaleDesBons")
                    .document(currentDate)
                batch.set(totalDocRef, totalData)
            }

            // Commit the batch
            batch.commit().await()
            println("Successfully exported articles and totals to Firestore")

        } catch (e: Exception) {
            println("Error exporting to Firestore: ${e.message}")
        }
    }
}
@Composable
fun SupplierCreditDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    supplierId: Int?,
    supplierName: String,
    supplierTotal: Double,
    coroutineScope: CoroutineScope
) {
    var supplierCredit by remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Manage Supplier Credit: $supplierName") },
            text = {
                Column {
                    Text("Total Amount: ${"%.2f".format(supplierTotal)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = supplierCredit,
                        onValueChange = { supplierCredit = it },
                        label = { Text("Supplier Credit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Rest Credit: ${"%.2f".format(supplierTotal - (supplierCredit.toDoubleOrNull() ?: 0.0))}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        supplierId?.let { id ->
                            updateSupplierCredit(id, supplierTotal, supplierCredit.toDoubleOrNull() ?: 0.0)
                        }
                    }
                    onDismiss()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
fun updateSupplierCredit(supplierId: Int?, supplierTotal: Double, supplierCredit: Double) {
    val firestore = Firebase.firestore
    val currentDate = LocalDate.now().toString()
    val restCredit = supplierTotal - supplierCredit

    val data = hashMapOf(
        "date" to currentDate,
        "totalAmount" to supplierTotal,
        "totalCredit" to supplierCredit,
        "restCredit" to restCredit
    )

    try {
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("Totale et Credit Des Bons")
            .document(currentDate)
            .set(data)
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating supplier credit: ", e)
    }
}
fun updateSupplierBon(suppliersRef: DatabaseReference, supplierId: Long, bonNumber: String) {
    suppliersRef.child(supplierId.toString()).child("bonDuSupplierSu").setValue(bonNumber)
}