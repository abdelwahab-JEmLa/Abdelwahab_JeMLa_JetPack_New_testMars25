package com.example.abdelwahabjemlajetpack

import a_RoomDB.BaseDonne
import android.util.Log
import android.widget.Toast
import b_Edite_Base_Donne.ArticleDao
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import c_ManageBonsClients.ArticlesAcheteModele
import c_ManageBonsClients.roundToOneDecimal
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

suspend fun importFromFirebaseToDataBaseDonne(
    refFireBase: String,
    viewModel: EditeBaseDonneViewModel
) {
    try {
        val dataSnapshot = Firebase.database.getReference(refFireBase).get().await()
        val articlesFromFirebase = parseDataSnapshotDataBaseDonne(dataSnapshot)
        val sortedArticles = articlesFromFirebase.sortedWith(compareBy<BaseDonne> { it.idCategorie }.thenBy { it.classementCate })

        viewModel.insertAllDataBaseDonne(sortedArticles)
        viewModel.initDataBaseDonneForNewByStatInCompos()

    } catch (e: Exception) {
        Log.e("MainAppViewModel", "Failed to import data from Firebase", e)
    }
}

fun parseDataSnapshotDataBaseDonne(dataSnapshot: DataSnapshot): List<BaseDonne> {
    val articlesList = mutableListOf<BaseDonne>()
    for (snapshot in dataSnapshot.children) {
        try {
            val article = snapshot.getValue(BaseDonne::class.java)
            article?.let { articlesList.add(it) }
        } catch (e: DatabaseException) {
            Log.e("parseDataSnapshot", "Error parsing article: ${e.message}")
        }
    }
    return articlesList
}


fun parseDataSnapshot(dataSnapshot: DataSnapshot): List<BaseDonne> {
    val articlesList = mutableListOf<BaseDonne>()
    for (snapshot in dataSnapshot.children) {
        try {
            val article = snapshot.getValue(BaseDonne::class.java)
            article?.let { articlesList.add(it) }
        } catch (e: DatabaseException) {
            Log.e("parseDataSnapshot", "Error parsing article: ${e.message}")
        }
    }
    return articlesList
}



suspend fun importFromFirebase(
    refFireBase: String,
    articleDao: ArticleDao,
    viewModel: EditeBaseDonneViewModel
) {
    try {
        val dataSnapshot = Firebase.database.getReference(refFireBase).get().await()
        val articlesFromFirebase = parseDataSnapshot(dataSnapshot)
        val sortedArticles = articlesFromFirebase.sortedWith(compareBy<BaseDonne> { it.idCategorie }.thenBy { it.classementCate })

        articleDao.deleteAll()
        articleDao.insertAll(sortedArticles)
        viewModel.initBaseDonneStatTabel()
        viewModel.initDataBaseDonneForNewByStatInCompos()

    } catch (e: Exception) {
        Log.e("MainAppViewModel", "Failed to import data from Firebase", e)
    }
}


suspend fun exportToFireBase(articleDao: ArticleDao) {
    // Récupérer les articles depuis Room
    val articlesFromRoom = withContext(Dispatchers.IO) {
        articleDao.getAllArticlesOrder()
    }

    // Référence Firebase
    val refFirebase = FirebaseDatabase.getInstance().getReference("e_DBJetPackExport")

    // Parcourir les articles et les envoyer à Firebase
    articlesFromRoom.forEach { article ->
        val articleMap = article.toMap()
        refFirebase.child(article.idArticle.toString()).setValue(articleMap)
    }
}

// Extension function pour convertir un article en Map
fun BaseDonne.toMap(): Map<String, Any?> {
    return mapOf(
        "idArticle" to idArticle,
        "nomArticleFinale" to nomArticleFinale,
        "classementCate" to classementCate,
        "nomArab" to nomArab,
        "nmbrCat" to nmbrCat,
        "couleur1" to couleur1,
        "couleur2" to couleur2,
        "couleur3" to couleur3,
        "couleur4" to couleur4,
        "nomCategorie2" to nomCategorie2,
        "nmbrUnite" to nmbrUnite,
        "nmbrCaron" to nmbrCaron,
        "affichageUniteState" to affichageUniteState,
        "commmentSeVent" to commmentSeVent,
        "afficheBoitSiUniter" to afficheBoitSiUniter,
        "monPrixAchat" to monPrixAchat,
        "clienPrixVentUnite" to clienPrixVentUnite,
        "minQuan" to minQuan,
        "monBenfice" to monBenfice,
        "monPrixVent" to monPrixVent,
        "diponibilityState" to diponibilityState,
        "neaon2" to neaon2,
        "idCategorie" to idCategorie,
        "funChangeImagsDimention" to funChangeImagsDimention,
        "nomCategorie" to nomCategorie,
        "neaon1" to neaon1,
        "lastUpdateState" to lastUpdateState,
        "cartonState" to cartonState,
        "dateCreationCategorie" to dateCreationCategorie,
        "prixDeVentTotaleChezClient" to prixDeVentTotaleChezClient,
        "benficeTotaleEntreMoiEtClien" to benficeTotaleEntreMoiEtClien,
        "benificeTotaleEn2" to benificeTotaleEn2,
        "monPrixAchatUniter" to monPrixAchatUniter,
        "monPrixVentUniter" to monPrixVentUniter
    )
}

suspend fun transferFirebaseData() {
    val refSource = Firebase.database.getReference("c_db_de_base_down_test")
    val refDestination = Firebase.database.getReference("d_db_jetPack")

    try {
        // Clear existing data in the destination reference
        refDestination.removeValue().await()

        // Retrieve data from the source reference
        val dataSnapshot = refSource.get().await()
        val dataMap = dataSnapshot.value as? Map<String, Map<String, Any>> ?: emptyMap()

        // Map the data to a HashMap of BaseDonne objects
        val baseDonneMap = dataMap.mapValues { (_, value) ->
            val nmbrUnite = (value["a11"] as? String)?.toIntOrNull() ?: 1
            val clienPrixVentUnite = (value["a17"] as? String)?.toDoubleOrNull() ?: 0.0
            val monPrixVent = (value["a20"] as? String)?.toDoubleOrNull() ?: 0.0
            val prixDeVentTotaleChezClient = nmbrUnite * clienPrixVentUnite
            val benificeClient = prixDeVentTotaleChezClient / nmbrUnite

            BaseDonne(
                idArticle = (value["a00"] as? Long)?.toInt() ?: 0,
                nomArticleFinale = value["a03"] as? String ?: "",
                classementCate = (value["a02"] as? String)?.toDoubleOrNull() ?: 0.0,
                nomArab = value["a04"] as? String ?: "",
                nmbrCat = (value["a05"] as? String)?.toIntOrNull() ?: 0,
                couleur1 = value["a06"] as? String,
                couleur2 = value["a07"] as? String,
                couleur3 = value["a08"] as? String,
                couleur4 = value["a09"] as? String,
                nomCategorie2 = value["a10"] as? String,
                nmbrUnite = nmbrUnite,
                nmbrCaron = (value["a12"] as? String)?.toIntOrNull() ?: 0,
                affichageUniteState = (value["a13"] as? String)?.toBoolean() ?: false,
                commmentSeVent = value["a14"] as? String,
                afficheBoitSiUniter = value["a15"] as? String,
                monPrixAchat = (value["a16"] as? String)?.toDoubleOrNull() ?: 0.0,
                clienPrixVentUnite = clienPrixVentUnite,
                minQuan = (value["a18"] as? String)?.toIntOrNull() ?: 0,
                monBenfice = (value["a19"] as? String)?.toDoubleOrNull() ?: 0.0,
                monPrixVent = monPrixVent,
                diponibilityState = value["a21"] as? String ?: "",
                neaon2 = value["a22"] as? String ?: "",
                idCategorie = (value["a23"] as? String)?.toDoubleOrNull() ?: 0.0,
                funChangeImagsDimention = (value["a24"] as? String)?.toBoolean() ?: false,
                nomCategorie = value["a25"] as? String ?: "",
                neaon1 = (value["a26"] as? String)?.toDoubleOrNull() ?: 0.0,
                lastUpdateState = value["a27"] as? String ?: "",
                cartonState = value["a28"] as? String ?: "",
                dateCreationCategorie = value["a29"] as? String ?: "",
                prixDeVentTotaleChezClient = prixDeVentTotaleChezClient,
                benificeTotaleEn2 = prixDeVentTotaleChezClient - monPrixVent,
                monPrixAchatUniter =  monPrixVent / nmbrUnite ,
                monPrixVentUniter = monPrixVent / 2,
                benificeClient= benificeClient,
            )
        }

        // Insert the HashMap of BaseDonne objects into the destination reference
        baseDonneMap.forEach { (_, baseDonne) ->
            refDestination.child(baseDonne.idArticle.toString()).setValue(baseDonne).await()
        }
    } catch (e: Exception) {
        Log.e("transferFirebaseData", "Failed to transfer data", e)
    }
}

suspend fun transferFirebaseDataArticlesAcheteModele(
    context: android.content.Context,
    articleDao: ArticleDao,
    onProgressUpdate: (Float) -> Unit
) {
    var fireStorHistoriqueDesFactures: List<ArticlesAcheteModele> = emptyList()

    try {
        val firestore = Firebase.firestore
        val querySnapshot = firestore.collection("HistoriqueDesFactures").get().await()
        fireStorHistoriqueDesFactures = querySnapshot.documents.mapNotNull { document ->
            document.toObject(ArticlesAcheteModele::class.java)
        }
    } catch (e: Exception) {
        Log.e("Firestore", "Error getting documents: ", e)
    }

    val refSource = Firebase.database.getReference("ArticlesAcheteModele")
    val refDestination = Firebase.database.getReference("ArticlesAcheteModeleAdapted")
    try {
        refDestination.removeValue().await()

        val dataSnapshot = refSource.get().await()
        val dataMap = dataSnapshot.value as? Map<String, Map<String, Any>> ?: emptyMap()

        val totalItems = dataMap.size
        var processedItems = 0

        dataMap.forEach { (_, value) ->
            val idArticle = (value["idarticle_c"] as? Long) ?: 0
            val nomClient = value["nomclient_c"] as? String ?: ""

            val baseDonne = articleDao.getArticleById(idArticle)

            val matchingHistorique = fireStorHistoriqueDesFactures.find { it.idArticle == idArticle && it.nomClient == nomClient  }
            val monPrixVentFireStoreBM = matchingHistorique?.monPrixVentFireStoreBM ?: 0.0

            // Filtre les entrées où totalquantity est vide ou null
            val totalQuantity = (value["totalquantity"] as? Number)?.toInt()
            if (totalQuantity != null && totalQuantity > 0) {
                val article = ArticlesAcheteModele(
                    vid = (value["id"] as? Long) ?: 0,
                    idArticle = idArticle,
                    nomArticleFinale = value["nomarticlefinale_c"] as? String ?: "",
                    prixAchat = roundToOneDecimal((value["prixachat_c"] as? Number)?.toDouble() ?: 0.0),
                    nmbrunitBC = roundToOneDecimal((value["nmbunite_c"] as? Number)?.toDouble() ?: 0.0),
                    clientPrixVentUnite = roundToOneDecimal((value["prixdevent_c"] as? Number)?.toDouble() ?: 0.0),
                    nomClient = nomClient,
                    dateDachate = value["datedachate"] as? String ?: "",
                    nomCouleur1 = value["nomarticlefinale_c_1"] as? String ?: "",
                    quantityAcheteCouleur1 = (value["quantityachete_c_1"] as? Number)?.toInt() ?: 0,
                    nomCouleur2 = value["nomarticlefinale_c_2"] as? String ?: "",
                    quantityAcheteCouleur2 = (value["quantityachete_c_2"] as? Number)?.toInt() ?: 0,
                    nomCouleur3 = value["nomarticlefinale_c_3"] as? String ?: "",
                    quantityAcheteCouleur3 = (value["quantityachete_c_3"] as? Number)?.toInt() ?: 0,
                    nomCouleur4 = value["nomarticlefinale_c_4"] as? String ?: "",
                    quantityAcheteCouleur4 = (value["quantityachete_c_4"] as? Number)?.toInt() ?: 0,
                    totalQuantity = totalQuantity,
                    nonTrouveState = false,
                    verifieState = false,
                    //Stats
                    typeEmballage = if (baseDonne?.cartonState == "itsCarton"||baseDonne?.cartonState == "Carton") "Carton" else "Boit",
                    choisirePrixDepuitFireStoreOuBaseBM = if (monPrixVentFireStoreBM == 0.0) "CardFireBase" else "CardFireStor",
                    //baseDonne
                    monPrixVentBM = roundToOneDecimal((value["prix_1_q1_c"] as? Number)?.toDouble() ?: 0.0),
                    //FireStore
                    monPrixVentFireStoreBM = monPrixVentFireStoreBM
                ).apply {
                    monPrixVentUniterFireStoreBM =  roundToOneDecimal(if (nmbrunitBC != 0.0) monPrixVentFireStoreBM / nmbrunitBC else 0.0)

                    monBenificeFireStoreBM =   roundToOneDecimal(monPrixVentFireStoreBM - prixAchat)
                    monBenificeUniterFireStoreBM =  roundToOneDecimal(if (nmbrunitBC != 0.0) monBenificeFireStoreBM / nmbrunitBC else 0.0)
                    totalProfitFireStoreBM =  roundToOneDecimal((totalQuantity * monBenificeFireStoreBM))

                    //FireBAse
                    monBenificeBM = roundToOneDecimal(monPrixVentBM - prixAchat)
                    monBenificeUniterBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monBenificeBM / nmbrunitBC else 0.0)
                    totalProfitBM = roundToOneDecimal(totalQuantity*monBenificeBM)

                    monPrixAchatUniterBC = roundToOneDecimal(if (nmbrunitBC != 0.0) prixAchat / nmbrunitBC else 0.0)
                    monPrixVentUniterBM = roundToOneDecimal(if (nmbrunitBC != 0.0) monPrixVentBM / nmbrunitBC else 0.0)
                    benificeDivise = roundToOneDecimal(((clientPrixVentUnite * nmbrunitBC) - prixAchat) / 2)
                    clientBenificeBM = roundToOneDecimal((clientPrixVentUnite * nmbrunitBC) - monPrixVentBM)
                }

                refDestination.child(article.idArticle.toString()).setValue(article).await()
            }

            processedItems++
            onProgressUpdate(processedItems.toFloat() / totalItems)
        }

        Log.d("transferFirebaseData", "Data transfer completed successfully")

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Transfert terminé avec succès", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Log.e("transferFirebaseData", "Failed to transfer data", e)

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Échec du transfert: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
