package Z_MasterOfApps.Kotlin.ViewModel.Init.C_Compare

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_CodePartageEntreApps.Model.B_ClientsDataBase
import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.ClientsList
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent.ProduitsAncienDataBaseMain
import android.util.Log
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.tasks.await

private const val TAG = "CompareUpdate"

object CompareUpdate {
    suspend fun setupeCompareUpdateAncienModels() {
        try {
            updateAncienDataBase()
            updateClientsDatabase()
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupeCompareUpdateAncienModels", e)
            throw e
        }
    }

    private suspend fun updateAncienDataBase() {
        val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")
        val produitsFireBaseRef = firebaseDatabase.getReference("0_UiState_3_Host_Package_3_Prototype11Dec/produits")

        try {
            // Get both database snapshots
            val existingProducts = refDBJetPackExport.get().await().children.mapNotNull { it.key }
            val currentProducts = produitsFireBaseRef.get().await()

            // Process each product
            currentProducts.children.forEach { snap ->
                try {
                    processProduct(snap, existingProducts, refDBJetPackExport)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing product ${snap.key}", e)
                    // Continue with next product instead of failing completely
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateAncienDataBase", e)
            throw e
        }
    }

    private suspend fun processProduct(
        snap: DataSnapshot,
        existingProducts: List<String>,
        refDBJetPackExport: com.google.firebase.database.DatabaseReference
    ) {
        val productId = snap.key ?: return

        // Skip if product already exists
        if (productId in existingProducts) {
            Log.d(TAG, "Product $productId already exists, skipping")
            return
        }

        // Safely convert snapshot to product
        val product = try {
            snap.getValue(A_ProduitModel::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to deserialize product $productId", e)
            null
        } ?: return

        // Convert and save new product
        try {
            val convertedProduct = convertToAncienProduct(product)
            refDBJetPackExport.child(productId).setValue(convertedProduct).await()
            Log.d(TAG, "Successfully converted and saved product $productId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save converted product $productId", e)
            throw e
        }
    }

    private fun convertToAncienProduct(product: A_ProduitModel): ProduitsAncienDataBaseMain {
        val colors = product.coloursEtGoutsList.sortedBy { it.position_Du_Couleur_Au_Produit }

        return ProduitsAncienDataBaseMain().apply {
            idArticle = product.id.toInt().toLong()
            nomArticleFinale = product.nom.takeIf { it.isNotBlank() } ?: "No Name"
            monPrixAchat = product.statuesBase.infosCoutes.monPrixAchat.takeIf { it > 0.0 } ?: 0.0
            monPrixVent = product.statuesBase.infosCoutes.monPrixVent.takeIf { it > 0.0 } ?: 0.0
            articleHaveUniteImages = !product.statuesBase.naAucunImage
            cartonState = if (product.statuesBase.characterProduit.emballageCartone) "CARTON" else "UNITE"

            // Safely assign colors with null checks
            colors.getOrNull(0)?.let {
                couleur1 = it.nom.takeIf { name -> name != "Non Defini" }
                idcolor1 = it.id
            }
            colors.getOrNull(1)?.let {
                couleur2 = it.nom.takeIf { name -> name != "Non Defini" }
                idcolor2 = it.id
            }
            colors.getOrNull(2)?.let {
                couleur3 = it.nom.takeIf { name -> name != "Non Defini" }
                idcolor3 = it.id
            }
            colors.getOrNull(3)?.let {
                couleur4 = it.nom.takeIf { name -> name != "Non Defini" }
                idcolor4 = it.id
            }
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
    }
}
