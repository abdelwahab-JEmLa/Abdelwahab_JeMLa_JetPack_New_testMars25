package Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.ViewModel.Extension

import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase.Companion.updateClientsDataBase
import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

class ViewModelExtension_App2_F1(
    val viewModelScope: CoroutineScope,
    val produitsMainDataBase: MutableList<A_ProduitModel>,
    private val clientDataBaseSnapList: SnapshotStateList<B_ClientsDataBase>,
    val viewModel: ViewModelInitApp,
) {
    fun onClickAddMarkerButton(
        mapView: MapView,
    ) {
        val center = mapView.mapCenter
        require(center.latitude != 0.0) { "Invalid latitude value" }

        val newID = if (clientDataBaseSnapList.isEmpty()) {
            1L // Start with 1 if the list is empty
        } else {
            clientDataBaseSnapList.maxOf { it.id } + 1
        }
        val newnom = "Nouveau client #$newID"

        val newClient =
            B_ClientsDataBase(
                id = newID,
                nom = newnom
            ).apply {
                statueDeBase.cUnClientTemporaire = true
                gpsLocation.apply {
                    latitude = center.latitude
                    longitude = center.longitude
                    title = newnom
                    snippet = "Client temporaire"
                }
            }

        viewModel._modelAppsFather.clientDataBase.add(newClient)

        B_ClientsDataBase.refClientsDataBase
            .child(newClient.id.toString())
            .setValue(newClient)
    }

    // ViewModelExtensionMapsHandler.kt
    fun updateStatueClient(
        selectedMarker: Marker?,
        statueVente: B_ClientsDataBase.GpsLocation.DernierEtatAAffiche
    ) {
        clientDataBaseSnapList.toMutableList().forEach { client ->
            if (client.id == selectedMarker?.id?.toLong()) {
                // Now works because gpsLocation is part of the data class
                val updatedClient = client.copy(
                    gpsLocation = client.gpsLocation.copy(
                        actuelleEtat = statueVente
                    )
                )
                updatedClient.updateClientsDataBase( viewModel)
            }
        }
    }
}


