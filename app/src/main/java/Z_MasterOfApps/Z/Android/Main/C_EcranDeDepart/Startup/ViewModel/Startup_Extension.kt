package Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.ViewModel

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.updateProduit
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import com.google.firebase.Firebase
import com.google.firebase.database.database

class Startup_Extension(
    val viewModelInitApp: ViewModelInitApp,
) {
    val produitsMainDataBase = viewModelInitApp.produitsMainDataBase

    fun clearAchats() {
        // Create a snapshot of the products to avoid concurrent modification
        val productsToProcess = viewModelInitApp._modelAppsFather.produitsMainDataBase.toList()

        // Clear Firebase references first
        val database = Firebase.database
        database.getReference("K_GroupeurBonCommendToSupplierRef").removeValue()
        database.getReference("O_SoldArticlesTabelle").removeValue()

        // Process each product safely
        productsToProcess.forEach { produit ->
            // Safely add current data to history
            produit.bonCommendDeCetteCota?.let { currentBonCommend ->
                // Create a new list and add all items to avoid concurrent modification
                val updatedHistorique = ArrayList(produit.historiqueBonsCommend)
                updatedHistorique.add(currentBonCommend)
                produit.historiqueBonsCommend.clear()
                produit.historiqueBonsCommend.addAll(updatedHistorique)
            }

            if (produit.bonsVentDeCetteCotaList.isNotEmpty()) {
                // Create a new list and add all items to avoid concurrent modification
                val updatedHistoriqueVents = ArrayList(produit.historiqueBonsVents)
                updatedHistoriqueVents.addAll(produit.bonsVentDeCetteCotaList)
                produit.historiqueBonsVents.clear()
                produit.historiqueBonsVents.addAll(updatedHistoriqueVents)
            }

            // Clear current data
            produit.bonCommendDeCetteCota = null
            produit.bonsVentDeCetteCota.clear()

            // Update the product in Firebase
            updateProduit(produit, viewModelInitApp)
        }
    }

    fun implimentClientsParProduits() {
        // Implementation remains unchanged
    }
}
