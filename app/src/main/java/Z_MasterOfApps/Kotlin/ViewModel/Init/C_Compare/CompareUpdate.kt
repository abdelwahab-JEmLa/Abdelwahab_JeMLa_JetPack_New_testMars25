package Z_MasterOfApps.Kotlin.ViewModel.Init.C_Compare

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.ClientsList
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.ProduitsAncienDataBaseMain
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

object CompareUpdate {
    suspend fun setupeCompareUpdateAncienModels(): Unit {
        updateAncienDataBase()
        updateClientsDatabase()
    }

    private suspend fun updateAncienDataBase() {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")
        val produitsFireBaseRef = firebaseDatabase.getReference("0_UiState_3_Host_Package_3_Prototype11Dec/produits")

        try {
            // Get both database snapshots
            val existingProducts = refDBJetPackExport.get().await().children.mapNotNull { it.key }
            val currentProducts = produitsFireBaseRef.get().await()

            // Process each product
            currentProducts.children.forEach { snap ->
                val productId = snap.key ?: return@forEach

                // Skip if product already exists
                if (productId in existingProducts) return@forEach

                // Convert and save new product
                val product = snap.getValue(A_ProduitModel::class.java) ?: return@forEach
                val colors = product.coloursEtGoutsList.sortedBy { it.position_Du_Couleur_Au_Produit }

                val convertedProduct = ProduitsAncienDataBaseMain().apply {
                    idArticle = product.id
                    nomArticleFinale = product.nom
                    monPrixAchat = product.statuesBase.infosCoutes.monPrixAchat
                    monPrixVent = product.statuesBase.infosCoutes.monPrixVent
                    articleHaveUniteImages = !product.statuesBase.naAucunImage
                    cartonState = if (product.statuesBase.characterProduit.emballageCartone) "CARTON" else "UNITE"
                    couleur1 = colors.getOrNull(0)?.nom
                    idcolor1 = colors.getOrNull(0)?.id ?: 0
                    couleur2 = colors.getOrNull(1)?.nom
                    idcolor2 = colors.getOrNull(1)?.id ?: 0
                    couleur3 = colors.getOrNull(2)?.nom
                    idcolor3 = colors.getOrNull(2)?.id ?: 0
                    couleur4 = colors.getOrNull(3)?.nom
                    idcolor4 = colors.getOrNull(3)?.id ?: 0
                }

                refDBJetPackExport.child(productId).setValue(convertedProduct).await()
            }
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun updateClientsDatabase() {
        val sourceClientsRef = firebaseDatabase.getReference("0_UiState_3_Host_Package_3_Prototype11Dec/B_ClientsDataBase")
        val refClientsList = firebaseDatabase.getReference("G_Clients")

        try {
            // Get both database snapshots
            val existingClients = refClientsList.get().await().children.mapNotNull { it.key }
            val currentClients = sourceClientsRef.get().await()

            // Process each client
            currentClients.children.forEach { snap ->
                val clientId = snap.key ?: return@forEach

                // Skip if client already exists
                if (clientId in existingClients) return@forEach

                // Convert and save new client
                val client = snap.getValue(B_ClientsDataBase::class.java) ?: return@forEach

                // Convert to ClientsList format
                val convertedClient = ClientsList(
                    vidSu = 0,
                    idClientsSu = client.id,
                    nomClientsSu = client.nom,
                    bonDuClientsSu = "",  // Default empty string as per ClientsList model
                    couleurSu = client.statueDeBase.couleur,
                    currentCreditBalance = 0.0  // Default to 0 as per ClientsList model
                )

                refClientsList.child(clientId).setValue(convertedClient).await()
            }
        } catch (e: Exception) {
            throw e
        }
    }   //-->
    //TODO(1): ajout une aut fun pour 
}
