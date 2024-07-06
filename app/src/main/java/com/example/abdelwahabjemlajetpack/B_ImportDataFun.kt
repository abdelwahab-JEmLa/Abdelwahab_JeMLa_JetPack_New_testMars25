package com.example.abdelwahabjemlajetpack

import a_RoomDB.BaseDonne
import android.util.Log
import b_Edite_Base_Donne.ArticleDao
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private val refFirebase = Firebase.database.getReference("d_db_jetPack")



suspend fun importFromFirebase(articleDao: ArticleDao) {
    try {
        val dataSnapshot = Firebase.database.getReference("d_db_jetPack").get().await()
        val articlesFromFirebase = parseDataSnapshot(dataSnapshot)
        val sortedArticles = articlesFromFirebase.sortedWith(compareBy<BaseDonne> { it.idCategorie }.thenBy { it.classementCate })

        articleDao.deleteAll()
        articleDao.insertAll(sortedArticles)

    } catch (e: Exception) {
        Log.e("MainAppViewModel", "Failed to import data from Firebase", e)
    }
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
                monPrixVentUniter = monPrixVent / 2
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
