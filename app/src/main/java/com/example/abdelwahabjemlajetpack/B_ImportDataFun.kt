package com.example.abdelwahabjemlajetpack

import a_RoomDB.BaseDonne
import android.util.Log
import b_Edite_Base_Donne.ArticleDao
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
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
        val sortedArticles = articlesFromFirebase.sortedWith(compareBy<BaseDonne>{ it.classementCate }.thenBy  { it.idCategorie })

        articleDao.deleteAll()
        articleDao.insertAll(sortedArticles)
        viewModel.initBaseDonneStatTabel()
        viewModel.initDataBaseDonneForNewByStatInCompos()

    } catch (e: Exception) {
        Log.e("MainAppViewModel", "Failed to import data from Firebase", e)
    }
}


suspend fun exportToFireBase(articleDao: ArticleDao, refFireBase: String) {
    // Récupérer les articles depuis Room
    val articlesFromRoom = withContext(Dispatchers.IO) {
        articleDao.getAllArticlesOrder()
    }

    // Référence Firebase
    val refFirebase = FirebaseDatabase.getInstance().getReference(refFireBase)

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

suspend fun transferFirebaseData(refSource: DatabaseReference, refDestination: DatabaseReference) {

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
                monPrixAchatUniter =  monPrixVent / nmbrUnite,
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

