package Z_MasterOfApps.Z_AppsFather.Kotlin._3.Init.Z.GetAncienDataBasesMain

import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.AncienResourcesDataBaseMain
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.Ancien_ClientsDataBase_Main
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.Ancien_ColorArticle_Main
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.Ancien_SoldArticlesTabelle_Main
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.ProduitsAncienDataBaseMain
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.tasks.await

internal suspend fun GetAncienDataBasesMain(): AncienResourcesDataBaseMain {
    try {
        Log.d("GetAncienData", "Starting to fetch data from Firebase")

        val produitsSnapshot = Firebase.database
            .getReference("e_DBJetPackExport")
            .get()
            .await()
        Log.d("GetAncienData", "Retrieved ${produitsSnapshot.childrenCount} products")

        val soldArticlesSnapshot = Firebase.database
            .getReference("O_SoldArticlesTabelle")
            .get()
            .await()
        Log.d("GetAncienData", "Retrieved ${soldArticlesSnapshot.childrenCount} sold articles")

        val couleurs_Snapshot = Firebase.database
            .getReference("H_ColorsArticles")
            .get()
            .await()
        Log.d("GetAncienData", "Retrieved ${couleurs_Snapshot.childrenCount} colors")

        val clients_Snapshot = Firebase.database
            .getReference("G_Clients")
            .get()
            .await()
        Log.d("GetAncienData", "Retrieved ${clients_Snapshot.childrenCount} clients")

        val produitsList = produitsSnapshot.children.mapNotNull {
            it.getValue(ProduitsAncienDataBaseMain::class.java)
        }
        val soldArticlesList = soldArticlesSnapshot.children.mapNotNull {
            it.getValue(Ancien_SoldArticlesTabelle_Main::class.java)
        }
        val couleurs_List = couleurs_Snapshot.children.mapNotNull {
            it.getValue(Ancien_ColorArticle_Main::class.java)
        }
        val clients_List = clients_Snapshot.children.mapNotNull {
            it.getValue(Ancien_ClientsDataBase_Main::class.java)
        }

        Log.d("GetAncienData", "Successfully parsed: " +
                "${produitsList.size} products, " +
                "${soldArticlesList.size} sold articles, " +
                "${couleurs_List.size} colors, " +
                "${clients_List.size} clients")

        return AncienResourcesDataBaseMain(
            produitsList,
            soldArticlesList,
            couleurs_List,
            clients_List
        )
    } catch (e: Exception) {
        Log.e("GetAncienData", "Error fetching data from Firebase", e)
        throw e
    }
}
