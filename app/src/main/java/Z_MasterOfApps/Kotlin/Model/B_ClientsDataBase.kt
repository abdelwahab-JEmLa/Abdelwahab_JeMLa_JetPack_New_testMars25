package Z_MasterOfApps.Kotlin.Model

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class B_ClientsDataBase(
    var id: Long = 1,
    var nom: String = "Non Defini",
    var statueDeBase: StatueDeBase = StatueDeBase(),
    var gpsLocation: GpsLocation = GpsLocation(),
) {
    @IgnoreExtraProperties
    data class StatueDeBase(
        var numTelephone: String = "",
        var couleur: String = "#FFFFFF",
        var positionDonClientsList: Int = 0,
        var caRefDonAncienDataBase: String = "G_Clients",
        var cUnClientTemporaire: Boolean = true,
        var auFilterFAB: Boolean = false
    )

    @IgnoreExtraProperties
    data class GpsLocation(
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var title: String = "",
        var snippet: String = "",
        var actuelleEtat: DernierEtatAAffiche? = null
    ) {
        @IgnoreExtraProperties
        enum class DernierEtatAAffiche(val color: Int, val nomArabe: String) {
            ON_MODE_COMMEND_ACTUELLEMENT(android.R.color.holo_green_light, "نشط / متصل"),
            VENDU_A_LUI(android.R.color.holo_purple, ""),
            Cible(android.R.color.holo_red_light, "Cible"),
            CLIENT_ABSENT(android.R.color.darker_gray, "غائب الشاري"),
            AVEC_MARCHANDISE(android.R.color.holo_blue_light, "عندو سلعة"),
            FERME(android.R.color.darker_gray, "مغلق")
        }
    }

    // B_ClientsDataBase.kt - Updated companion object
    companion object {
        val refClientsDataBase = Firebase.database
            .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")
            .child("B_ClientsDataBase")

        fun B_ClientsDataBase.updateClientsDataBase(
            viewModel: ViewModelInitApp
        ) {
            viewModel.viewModelScope.launch {
                try {
                    // Create a snapshot of the current state
                    val currentState = this@updateClientsDataBase.copy()

                    // Update local state using clear and addAll
                    val clientsList = viewModel._modelAppsFather.clientDataBase
                    val updatedList = clientsList.toMutableList()
                    val index = updatedList.indexOfFirst { it.id == currentState.id }

                    if (index != -1) {
                        updatedList[index] = currentState
                    } else {
                        // If client doesn't exist, add them
                        updatedList.add(currentState)
                    }

                    // Replace entire list
                    clientsList.clear()
                    clientsList.addAll(updatedList)

                    // Update Firebase with error handling
                    try {
                        refClientsDataBase.child(currentState.id.toString())
                            .setValue(currentState)
                            .await()
                    } catch (e: Exception) {
                        // Revert local state if Firebase update fails
                        clientsList.clear()
                        clientsList.addAll(
                            if (index != -1) updatedList.toMutableList().apply { this[index] = this@updateClientsDataBase }
                            else updatedList.dropLast(1)
                        )
                        throw e
                    }

                } catch (e: Exception) {
                    Log.e("B_ClientsDataBase", "Failed to update client", e)
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is B_ClientsDataBase) return false

        return id == other.id &&
                nom == other.nom &&
                statueDeBase == other.statueDeBase &&
                gpsLocation == other.gpsLocation
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + nom.hashCode()
        result = 31 * result + statueDeBase.hashCode()
        result = 31 * result + gpsLocation.hashCode()
        return result
    }
}
