package Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.ViewModel.Extension

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_CodePartageEntreApps.Model.B_ClientsDataBase
import Z_CodePartageEntreApps.Model.B_ClientsDataBase.Companion.updateClientsDataBase
import Z_CodePartageEntreApps.Model.B_ClientsDataBase.StatueDeBase.TypeDeSonMagasine
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Main.Utils.LottieJsonGetterR_Raw_Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    var auClickeCaUpdateClientPar by mutableStateOf(TypeDeSonMagasine.ATAYAT_MOUKASSARAT)
    // Add this property to ViewModelInitApp

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
                statueDeBase.apply {
                    cUnClientTemporaire = true
                    typeDeSonMagasine = auClickeCaUpdateClientPar
                }
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
                updatedClient.updateClientsDataBase(viewModel)
            }
        }
    }
}

enum class VisbleClientsNow(val icon: Any) {  // Changed from LottieJsonGetterR_Raw_Icons to Any
    showNonAbsentClientsOnly(LottieJsonGetterR_Raw_Icons.reacticonanimatedjsonurl),
    showCibleClientsOnly(LottieJsonGetterR_Raw_Icons.afficheFenetre),
    showAtayClients(LottieJsonGetterR_Raw_Icons.atay),
    showClientsOnlyAcEtateCIBLE_POUR_2(Icons.Default.CheckCircleOutline),
    showAlimentionlients(LottieJsonGetterR_Raw_Icons.alimentation),
    showAll(LottieJsonGetterR_Raw_Icons.reacticonanimatedjsonurl);
}
