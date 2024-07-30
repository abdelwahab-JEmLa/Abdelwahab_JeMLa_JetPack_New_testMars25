package c_ManageBonsClients

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.database
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Constants
const val TAG = "ClientManagement"
private const val ARTICLES_REF = "ArticlesAcheteModeleAdapted"
private const val FACTURES_COLLECTION = "HistoriqueDesFactures"
private const val CLIENTS_COLLECTION = "clientsList"
private const val DATE_FORMAT = "dd/MM/yyyy"
private const val TIME_FORMAT = "HH:mm"
private const val PRINT_INTENT = "pe.diegoveloper.printing"

/**
 * Processes client data, prepares text for printing, and exports data to Firestore.
 *
 * @param context The application context.
 * @param nomClient The name of the client.
 */
suspend fun processClientData(context: Context, nomClient: String) {
    val fireStore = Firebase.firestore
    val articlesRef = Firebase.database.getReference(ARTICLES_REF)
    val date = Date()
    val dateString = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)
    val timeString = SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(date)

    try {
        val clientArticles = articlesRef
            .orderByChild("nomClient")
            .equalTo(nomClient)
            .get()
            .await()

        val (texteImprimable, totaleBon) = prepareTexteToPrint(nomClient, timeString, clientArticles)

        exportToFirestore(fireStore, clientArticles, nomClient, dateString, timeString)

        // Add this line to update the clients list
        updateClientsList(fireStore, nomClient)

        imprimerDonnees(context, texteImprimable.toString(), totaleBon)

        Log.d(TAG, "Données imprimées:\n$texteImprimable")

    } catch (e: Exception) {
        Log.e(TAG, "Erreur lors du traitement des données client", e)
    }
}

private fun prepareTexteToPrint(nomClient: String, timeString: String, clientArticles: DataSnapshot): Pair<StringBuilder, Double> {
    val texteImprimable = StringBuilder()
    var totaleBon = 0.0
    var pageCounter = 0

    texteImprimable.apply {
        append("<BIG><CENTER>Abdelwahab<BR>")
        append("<BIG><CENTER>JeMla.Com<BR>")
        append("<SMALL><CENTER>0553885037<BR>")
        append("<SMALL><CENTER>Facture<BR>")
        append("<BR>")
        append("<SMALL><CENTER>$nomClient                        $timeString<BR>")
        append("<BR>")
        append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
        append("<SMALL><BOLD>    Quantité      Prix         <NORMAL>Sous-total<BR>")
        append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
    }
    clientArticles.children
        .mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }
        .filter { it.verifieState }
        .forEachIndexed { index, article ->
            val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor")  article.monPrixVentBM else article.monPrixVentFireStoreBM
            val subtotal = monPrixVentDetermineBM * article.totalQuantity
            if (subtotal != 0.0) {
                texteImprimable.apply {
                    append("<MEDIUM1><LEFT>${article.nomArticleFinale}<BR>")
                    append("    <MEDIUM1><LEFT>${article.totalQuantity}   ")
                    append("<MEDIUM1><LEFT>${monPrixVentDetermineBM}Da   ")
                    append("<SMALL>$subtotal<BR>")
                    append("<LEFT><NORMAL><MEDIUM1>---------------------<BR>")
                }

                totaleBon += subtotal

                if ((index + 1) % 15 == 0) {
                    pageCounter++
                    texteImprimable.append("<BR><CENTER>PAGE $pageCounter<BR><BR><BR>")
                }
            }
        }

    texteImprimable.apply {
        append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
        append("<BR><BR>")
        append("<MEDIUM1><CENTER>Total<BR>")
        append("<MEDIUM3><CENTER>${totaleBon}Da<BR>")
        append("<CENTER>---------------------<BR>")
        append("<BR><BR><BR>>")
    }

    return Pair(texteImprimable, totaleBon)
}

/**
 * Exports client data to Firestore.
 */
suspend fun exportToFirestore(
    fireStore: FirebaseFirestore,
    clientArticles: DataSnapshot,
    nomClient: String,
    dateString: String,
    timeString: String
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
            clientArticles.children
                .mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }
                .filter { it.verifieState }
                .forEach { article ->
                    val subtotal = article.monPrixVentUniterBM * article.totalQuantity
                    if (subtotal != 0.0) {
                        val lineData = hashMapOf(
                            "vid" to article.vid,
                            "idArticle" to article.idArticle,
                            "nomArticleFinale" to article.nomArticleFinale,
                            "prixAchat" to article.prixAchat,
                            "nmbrunitBC" to article.nmbrunitBC,
                            "clientPrixVentUnite" to article.clientPrixVentUnite,
                            "nomClient" to article.nomClient,
                            "idClient" to getClientId(fireStore,nomClient),
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
                            "subtotal" to subtotal,
                            "time" to timeString
                        )

                        fireStore.collection(FACTURES_COLLECTION).add(lineData).await()
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de l'exportation vers Firestore", e)
        }
    }
}

/**
 * Retrieves the client ID from Firestore.
 */
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

/**
 * Updates or creates a client in the clients list.
 */
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


