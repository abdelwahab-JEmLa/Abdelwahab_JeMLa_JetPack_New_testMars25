package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.ViewModel

import Z_MasterOfApps.Kotlin.Model.E_AppsOptionsStates
import Z_MasterOfApps.Kotlin.Model.E_AppsOptionsStates.ApplicationEstInstalleDonTelephone.Companion.metricsWidthPixels
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.updateProduit
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch

class Startup_Extension(
    val viewModelInitApp: ViewModelInitApp,
) {
    val produitsMainDataBase = viewModelInitApp.produitsMainDataBase
    val applicationEstInstalleDonTelephone =
        viewModelInitApp._modelAppsFather.applicationEstInstalleDonTelephone
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val refDBJetPackExport = firebaseDatabase.getReference("e_DBJetPackExport")

    var dialogeOptions by mutableStateOf(false)


    init {
        val manufacturer = android.os.Build.MANUFACTURER
        val model = android.os.Build.MODEL
        val phoneName = "$manufacturer $model"

        // Verify and add the phone
        verifyAndAddPhone(phoneName, metricsWidthPixels)
    }

    fun verifyAndAddPhone(phoneName: String, screenWidth: Int) {
        // Get reference to the phones in Firebase
        E_AppsOptionsStates.caReference.get().addOnSuccessListener { snapshot ->
            var phoneExists = false
            var maxId = 0

            // Check if phone exists and find max ID
            snapshot.children.forEach { snap ->
                try {
                    val phone = E_AppsOptionsStates.ApplicationEstInstalleDonTelephone().apply {
                        id = snap.child("id").getValue(Int::class.java) ?: 0
                        nom = snap.child("nom").getValue(String::class.java) ?: ""
                        widthScreen = snap.child("widthScreen").getValue(Int::class.java) ?: 0
                        itsReciverTelephone = snap.child("itsReciverTelephone").getValue(Boolean::class.java) ?: false
                    }

                    // Update max ID
                    if (phone.id > maxId) {
                        maxId = phone.id
                    }

                    // Check if phone exists
                    if (phone.nom == phoneName) {
                        phoneExists = true
                        // Update local state
                        applicationEstInstalleDonTelephone.add(phone)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // If phone doesn't exist, add it
            if (!phoneExists) {
                val newId = maxId + 1

                val newPhone = E_AppsOptionsStates.ApplicationEstInstalleDonTelephone().apply {
                    id = newId
                    nom = phoneName
                    widthScreen = screenWidth
                    itsReciverTelephone = false
                }

                // Add to local state
                applicationEstInstalleDonTelephone.add(newPhone)

                // Add to Firebase
                E_AppsOptionsStates.caReference
                    .child(newId.toString())
                    .setValue(newPhone)
            }
        }.addOnFailureListener { exception ->
            // Handle any errors
            exception.printStackTrace()
        }
    }


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

    /**
     * Checks each product in the database and updates the idcolor1 field to 1
     * if it's currently set to 0
     */
    fun updateProductsIdColor1() {
        viewModelInitApp.viewModelScope.launch {
            refDBJetPackExport.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { articleSnapshot ->
                        val idArticle = articleSnapshot.key

                        // Check if the idcolor1 field exists and equals 0
                        val idcolor1Value = articleSnapshot.child("idcolor1").getValue(Long::class.java)
                        if (idcolor1Value == 0L) {
                            refDBJetPackExport.child(idArticle ?: "").child("idcolor1").setValue(1L)
                        }

                        // Check couleur2 field and remove idcolor2 if empty or null
                        val couleur2Value = articleSnapshot.child("couleur2").getValue(String::class.java)
                        if (couleur2Value.isNullOrEmpty()) {
                            refDBJetPackExport.child(idArticle ?: "").child("idcolor2").removeValue()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle possible errors
                    println("Database error when updating idcolor1: ${error.message}")
                }
            })
        }
    }

    /**
     * Checks all products and removes bonCommendDeCetteCota if:
     * - The product has no bonsVentDeCetteCota entries, or
     * - Any bonVent has a null coloursAcheteList
     * Updates Firebase after removing the orders.
     */
    /**
     * Checks all products and removes bonCommendDeCetteCota and bonsVentDeCetteCota if:
     * - The product has no bonsVentDeCetteCota entries, or
     * - Any bonVent has empty coloursAcheteList or all quantities are 0
     * Special logging is implemented for product ID 3739
     */
    fun suppBonCommendSiNaPasDeBonVent() {
        viewModelInitApp.viewModelScope.launch {
            try {
                // Create a snapshot of products to avoid concurrent modification
                val productsToCheck = viewModelInitApp._modelAppsFather.produitsMainDataBase.toList()

                // Process each product
                productsToCheck.forEach { produit ->
                    // Special logging for product ID 3739
                    if (produit.id == 3739L) {
                        println("Analyzing product 3739:")
                        produit.bonsVentDeCetteCota.forEach { bonVent ->
                            println("- BonVent colors count: ${bonVent.coloursAcheteList.size}")
                            println("- Colors with quantities:")
                            bonVent.coloursAcheteList.forEach { color ->
                                println("  * ${color.nom}: ${color.quantity_Achete}")
                            }
                        }
                    }

                    // Check if product has an order and either:
                    // - Has no sales entries (empty bonsVentDeCetteCota)
                    // - Has any bonVent with null coloursAcheteList
                    if (produit.bonCommendDeCetteCota != null &&
                        (produit.bonsVentDeCetteCota.isEmpty() ||
                                produit.bonsVentDeCetteCota.any { bonVent ->
                                    bonVent.coloursAcheteList.isEmpty() ||
                                            bonVent.coloursAcheteList.all { it.quantity_Achete == 0 }
                                })) {

                        // Log if this is product 3739
                        if (produit.id == 3739L) {
                            println("Product 3739 is being cleared because:")
                            if (produit.bonsVentDeCetteCota.isEmpty()) {
                                println("- No sales entries found")
                            } else {
                                println("- Found sales entries with no quantities:")
                                produit.bonsVentDeCetteCota.forEach { bonVent ->
                                    if (bonVent.coloursAcheteList.isEmpty()) {
                                        println("  * Empty colors list")
                                    } else if (bonVent.coloursAcheteList.all { it.quantity_Achete == 0 }) {
                                        println("  * All quantities are 0")
                                    }
                                }
                            }
                        }

                        // Move the order to history before removing it
                        produit.historiqueBonsCommend.add(produit.bonCommendDeCetteCota!!)

                        // Remove the order and clear sales
                        produit.bonCommendDeCetteCota = null
                        produit.bonsVentDeCetteCota.clear()

                        // Update the product in Firebase
                        updateProduit(produit, viewModelInitApp)
                    }
                }
            } catch (e: Exception) {
                // Log any errors that occur during the process
                println("Error in suppBonCommendSiNaPasDeBonVent: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun addPrototype(newPrototype: E_AppsOptionsStates.F_PrototypseDeProgramationInfos) {
        // Add to local state first
        viewModelInitApp._modelAppsFather.f_PrototypseDeProgramationInfos.add(newPrototype)

        // Then update Firebase
        E_AppsOptionsStates.F_PrototypseDeProgramationInfos.caReference
            .child(newPrototype.vid.toString())
            .setValue(newPrototype)
    }

    fun updatePrototype(prototype: E_AppsOptionsStates.F_PrototypseDeProgramationInfos) {
        // Update local state first
        val index = viewModelInitApp._modelAppsFather.f_PrototypseDeProgramationInfos
            .indexOfFirst { it.vid == prototype.vid }
        if (index != -1) {
            viewModelInitApp._modelAppsFather.f_PrototypseDeProgramationInfos[index] = prototype
        }

        // Then update Firebase
        E_AppsOptionsStates.F_PrototypseDeProgramationInfos.caReference
            .child(prototype.vid.toString())
            .setValue(prototype)
    }
    fun activedialogeOptions () {
        dialogeOptions = true
    }
}
