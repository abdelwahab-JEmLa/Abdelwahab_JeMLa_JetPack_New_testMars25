package c_ManageBonsClients

import a_MainAppCompnents.ArticlesAcheteModele
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

// Constants
private const val FACTURES_COLLECTION = "HistoriqueDesFactures"
private const val CLIENTS_COLLECTION = "clientsList"
private const val PRINT_INTENT = "pe.diegoveloper.printing"

fun updateTotalProfitInFirestore(totalProfit: Double) {
    val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    val db = com.google.firebase.Firebase.firestore
    db.collection("Benifice Du Jour").document(currentDate)
        .set(mapOf("benifice" to totalProfit))
        .addOnSuccessListener { println("Bénéfice mis à jour avec succès") }
        .addOnFailureListener { e -> println("Erreur lors de la mise à jour du bénéfice: $e") }
}

suspend fun exportToFirestore(
    fireStore: FirebaseFirestore,
    clientArticles: List<ArticlesAcheteModele>,
    nomClient: String,
    dateString: String,
) {
    withContext(Dispatchers.IO) {
        try {
            // Delete existing documents
            val existingDocs = fireStore.collection(FACTURES_COLLECTION)
                .whereEqualTo("nomClient", nomClient)
                .whereEqualTo("dateDachate", dateString)
                .get()
                .await()

            existingDocs.documents.forEach { document ->
                fireStore.collection(FACTURES_COLLECTION).document(document.id).delete().await()
            }

            // Insert new documents
            clientArticles
                .filter { it.verifieState }
                .forEach { article ->

                    val lineData = hashMapOf(
                        "vid" to article.vid,
                        "idArticle" to article.idArticle,
                        "nomArticleFinale" to article.nomArticleFinale,
                        "prixAchat" to article.prixAchat,
                        "nmbrunitBC" to article.nmbrunitBC,
                        "clientPrixVentUnite" to article.clientPrixVentUnite,
                        "nomClient" to article.nomClient,
                        "idClient" to getClientId(fireStore, nomClient),
                        "dateDachate" to article.dateDachate,
                        "nomCouleur1" to article.nomCouleur1,
                        "quantityAcheteCouleur1" to article.quantityAcheteCouleur1,
                        "nomCouleur2" to article.nomCouleur2,
                        "quantityAcheteCouleur2" to article.quantityAcheteCouleur2,
                        "nomCouleur3" to article.nomCouleur3,
                        "quantityAcheteCouleur3" to article.quantityAcheteCouleur3,
                        "nomCouleur4" to article.nomCouleur4,
                        "quantityAcheteCouleur4" to article.quantityAcheteCouleur4,
                        "totalQuantity" to article.totalQuantity,
                        "nonTrouveState" to article.nonTrouveState,
                        "verifieState" to article.verifieState,
                        "changeCaronState" to article.changeCaronState,
                        "monPrixAchatUniterBC" to article.monPrixAchatUniterBC,
                        "benificeDivise" to article.benificeDivise,
                        "typeEmballage" to article.typeEmballage,
                        "choisirePrixDepuitFireStoreOuBaseBM" to article.choisirePrixDepuitFireStoreOuBaseBM,
                        "monPrixVentBM" to article.monPrixVentBM,
                        "monPrixVentUniterBM" to article.monPrixVentUniterBM,
                        "monBenificeBM" to article.monBenificeBM,
                        "monBenificeUniterBM" to article.monBenificeUniterBM,
                        "clientBenificeBM" to article.clientBenificeBM,
                        "monPrixVentFireStoreBM" to article.monPrixVentFireStoreBM,
                        "monPrixVentUniterFireStoreBM" to article.monPrixVentUniterFireStoreBM,
                        "monBenificeFireStoreBM" to article.monBenificeFireStoreBM,
                        "monBenificeUniterFireStoreBM" to article.monBenificeUniterFireStoreBM,
                        "clientBenificeFireStoreBM" to article.clientBenificeFireStoreBM,
                    )
                    try {
                        fireStore.collection(FACTURES_COLLECTION).add(lineData).await()
                        Log.d(TAG, "Article successfully added to Firestore: ${article.nomArticleFinale}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to add article to Firestore: ${article.nomArticleFinale}", e)
                    }
                }

            // Log articles that were in the list but not added to Firestore
            val addedArticles = fireStore.collection(FACTURES_COLLECTION)
                .whereEqualTo("nomClient", nomClient)
                .whereEqualTo("dateDachate", dateString)
                .get()
                .await()
                .documents
                .mapNotNull { it.getString("nomArticleFinale") }
                .toSet()

            clientArticles
                .filter { it.verifieState }
                .forEach { article ->
                    if (article.nomArticleFinale !in addedArticles) {
                        Log.d(TAG, "Article in list but not in Firestore: ${article.nomArticleFinale}, " +
                                "Subtotal: ${article.monPrixVentUniterBM * article.totalQuantity}, " +
                                "VerifieState: ${article.verifieState}")
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'exportation vers Firestore", e)
        }
    }
}


suspend fun getClientId(fireStore: FirebaseFirestore, nomClient: String): String {
    return try {
        val clientDoc = fireStore.collection(CLIENTS_COLLECTION)
            .whereEqualTo("nameClient", nomClient)
            .get()
            .await()
            .documents
            .firstOrNull()

        val idClient = clientDoc?.get("idClient")?.toString() ?: "0"

        when {
            idClient.toIntOrNull() == null -> "00"
            idClient.toInt() < 10 -> "0$idClient"
            else -> idClient
        }
    } catch (e: Exception) {
        Log.e(TAG, "Erreur lors de la récupération de l'ID client", e)
        "00"
    }
}

suspend fun updateClientsList(fireStore: FirebaseFirestore, nomClient: String) {
    val clientsCollection = fireStore.collection(CLIENTS_COLLECTION)
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    try {
        val existingClient = clientsCollection
            .whereEqualTo("nameClient", nomClient)
            .get()
            .await()

        if (existingClient.isEmpty) {
            val newClientData = hashMapOf(
                "idClient" to getNextClientId(clientsCollection),
                "nameClient" to nomClient,
                "dateDuDernierBon" to currentDate
            )
            clientsCollection.add(newClientData).await()
        } else {
            val clientDoc = existingClient.documents.first()
            clientDoc.reference.update("dateDuDernierBon", currentDate).await()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Erreur lors de la mise à jour de la liste des clients", e)
    }
}

private suspend fun getNextClientId(clientsCollection: CollectionReference): Int {
    return try {
        val snapshot = clientsCollection
            .orderBy("idClient")
            .limitToLast(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            1
        } else {
            val highestId = snapshot.documents.first().getLong("idClient") ?: 0
            (highestId + 1).toInt()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Erreur lors de la récupération du prochain ID client", e)
        1
    }
}

fun imprimerDonnees(context: Context, texteImprimable: String, totaleBon: Double) {
    val intent = Intent(PRINT_INTENT).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texteImprimable)
    }
    ContextCompat.startActivity(context, intent, null)
    Log.d(TAG, "Impression lancée. Total: $totaleBon")
}
