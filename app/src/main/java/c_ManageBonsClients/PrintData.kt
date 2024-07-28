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


suspend fun processClientData(context: Context, nomClient: String) {
    val fireStore = Firebase.firestore
    val articlesRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted")
    val date = Date()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(date)
    val timeString = timeFormat.format(date)

    try {
        val clientArticles = articlesRef
            .orderByChild("nomClient")
            .equalTo(nomClient)
            .get()
            .await()

        val (texteImprimable, totaleBon) = prepareTexteToPrint(nomClient, timeString, clientArticles)

        exportToFirestore(fireStore, clientArticles, nomClient, dateString, timeString)

        imprimerDonnees(context, texteImprimable.toString(), totaleBon)

        // Log des données imprimées
        Log.d("ProcessClientData", "Données imprimées:\n$texteImprimable")

    } catch (e: Exception) {
        Log.e("ProcessClientData", "Erreur lors du traitement des données client", e)
    }
}

private fun prepareTexteToPrint(nomClient: String, timeString: String, clientArticles: DataSnapshot): Pair<StringBuilder, Double> {
    val texteImprimable = StringBuilder()
    var totaleBon = 0.0
    var pageCounter = 0

    texteImprimable
        .append("<BIG><CENTER>Abdelwahab<BR>")
        .append("<BIG><CENTER>JeMla.Com<BR>")
        .append("<SMALL><CENTER>0553885037<BR>")
        .append("<SMALL><CENTER>Facture<BR>")
        .append("<BR>")
        .append("<SMALL><CENTER>$nomClient                        $timeString<BR>")
        .append("<BR>")
        .append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
        .append("<SMALL><BOLD>    Quantite      Prix         <NORMAL>Subtoale<BR>")
        .append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")

    clientArticles.children
        .mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }
        .filter { it.verifieState }
        .forEachIndexed { index, article ->
            val subtotal = article.monPrixVentUniterBC * article.totalQuantity
            if (subtotal != 0.0) {
                texteImprimable
                    .append("<MEDIUM1><LEFT>${article.nomArticleFinale}<BR>")
                    .append("    <MEDIUM1><LEFT>${article.totalQuantity}   ")
                    .append("<MEDIUM1><LEFT>${article.monPrixVentUniterBC}Da   ")
                    .append("<SMALL>$subtotal<BR>")
                    .append("<LEFT><NORMAL><MEDIUM1>---------------------<BR>")

                totaleBon += subtotal

                if ((index + 1) % 15 == 0) {
                    pageCounter++
                    texteImprimable.append("<BR><CENTER>PAGE $pageCounter<BR><BR><BR>")
                }
            }
        }

    texteImprimable
        .append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
        .append("<BR><BR>")
        .append("<MEDIUM1><CENTER>Totale<BR>")
        .append("<MEDIUM3><CENTER>${totaleBon}Da<BR>")
        .append("<CENTER>---------------------<BR>")
        .append("<BR><BR><BR>>")

    return Pair(texteImprimable, totaleBon)
}


suspend fun exportToFirestore(
    fireStore: FirebaseFirestore,
    clientArticles: DataSnapshot,
    nomClient: String,
    dateString: String,
    timeString: String
) {
    withContext(Dispatchers.IO) {
        // Supprimer les documents existants
        val existingDocs = fireStore.collection("HistoruqieDesFacturesDao")
            .whereEqualTo("clientName", nomClient)
            .whereEqualTo("date", dateString)
            .get()
            .await()

        for (document in existingDocs) {
            fireStore.collection("HistoruqieDesFacturesDao").document(document.id).delete().await()
        }

        // Insérer les nouveaux documents
        clientArticles.children
            .mapNotNull { it.getValue(ArticlesAcheteModele::class.java) }
            .filter { it.verifieState }
            .forEach { article ->
                val subtotal = article.monPrixVentUniterBC * article.totalQuantity
                if (subtotal != 0.0) {
                    val lineData = hashMapOf(
                        "idArticle" to article.idArticle,
                        "articleName" to article.nomArticleFinale,
                        "quantity" to article.totalQuantity,
                        "price" to article.monPrixVentUniterBC,
                        "subtotal" to subtotal,
                        "clientName" to nomClient,
                        "date" to dateString,
                        "time" to timeString
                    )

                    fireStore.collection("HistoruqieDesFacturesDao").add(lineData).await()
                }
            }
    }
    // Corrected order of parameters
    clientsList(fireStore, nomClient)
}



suspend fun clientsList(fireStore: FirebaseFirestore, nomClient: String) {
    val clientsCollection = fireStore.collection("clientsList")
    val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    try {
        // Check if the client already exists
        val existingClient = clientsCollection
            .whereEqualTo("nameClient", nomClient)
            .get()
            .await()

        if (existingClient.isEmpty) {
            // Client doesn't exist, create a new document
            val newClientData = hashMapOf(
                "idClient" to getNextClientId(clientsCollection),
                "nameClient" to nomClient,
                "dateDuDernierBon" to currentDate
            )
            clientsCollection.add(newClientData).await()
        } else {
            // Client exists, update the date
            val clientDoc = existingClient.documents.first()
            clientDoc.reference.update("dateDuDernierBon", currentDate).await()
        }
    } catch (e: Exception) {
        println("Error updating client list: ${e.message}")
    }
}
private suspend fun getNextClientId(clientsCollection: CollectionReference): Int {
    val snapshot = clientsCollection
        .orderBy("idClient")
        .limitToLast(1)
        .get()
        .await()

    return if (snapshot.isEmpty) {
        1 // Start with 1 if no clients exist
    } else {
        val highestId = snapshot.documents.first().getLong("idClient") ?: 0
        (highestId + 1).toInt()
    }
}

fun imprimerDonnees(context: Context, texteImprimable: String, totaleBon: Double) {
    val intent = Intent("pe.diegoveloper.printing")
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, texteImprimable)
    ContextCompat.startActivity(context, intent, null)

    Log.d("ImprimerDonnees", "Impression lancée. Total: $totaleBon")
}

// Update Firebase functions
fun updateNonTrouveState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())

    articleRef.child("nonTrouveState").setValue(!article.nonTrouveState)
}

fun updateVerifieState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child("verifieState").setValue(!article.verifieState)
}


fun updateRelatedFields(ar: ArticlesAcheteModele, columnChanged: String, newValue: String) {
    val newValueDouble = newValue.toDoubleOrNull() ?: return
    when (columnChanged) {
        "benificeClient" -> {
            up("benificeDivise", ((newValueDouble / ar.nmbrunitBC) - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monBenificeUniterBC", (((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBC", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("monPrixVentUniterBC", ((ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentBons", (ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble).toString(), ar.idArticle)
        }
        "monBenificeUniterBC" -> {
            up("monBenificeBC", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterBC", (newValueDouble + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentBons", (newValueDouble * ar.nmbrunitBC + ar.prixAchat).toString(), ar.idArticle)
        }
        "monBenificeBC" -> {
            up("monBenificeUniterBC", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterBC", ((newValueDouble / ar.nmbrunitBC) + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentBons", (newValueDouble + ar.prixAchat).toString(), ar.idArticle)
            up("benificeClient", ((ar.clientPrixVentUnite * ar.nmbrunitBC)-(newValueDouble + ar.prixAchat)).toString(), ar.idArticle)
        }
        "monPrixAchatUniterBC" -> {
            up("prixAchat", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentBons", (newValueDouble * ar.nmbrunitBC + ar.monBenificeBC).toString(), ar.idArticle)
        }
        "prixAchat" -> {
            up("monPrixVentBons", (newValueDouble + ar.monBenificeBC).toString(), ar.idArticle)
        }
        "monPrixVentUniterBC" -> {
            up("monPrixVentBons", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBC", (newValueDouble * ar.nmbrunitBC - ar.prixAchat).toString(), ar.idArticle)
        }
        "monPrixVentBons" -> {
            up("monPrixVentUniterBC", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBC", (newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("benificeClient", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble).toString(), ar.idArticle)
        }
    }
}

//updateFireBase
fun up(columnChanged: String, newValue: String, articleId: Long) {
    val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(articleId.toString())
    val articleUpdate = articleFromFireBase.child(columnChanged)
    articleUpdate.setValue(newValue.toDoubleOrNull() ?: 0.0)
}

fun updateTypeEmballage(article: ArticlesAcheteModele, newType: String) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child("typeEmballage").setValue(newType)
    val baseDoneRef = Firebase.database.getReference("e_DBJetPackExport").child(article.idArticle.toString())
    baseDoneRef.child("cartonState").setValue(newType)
}
