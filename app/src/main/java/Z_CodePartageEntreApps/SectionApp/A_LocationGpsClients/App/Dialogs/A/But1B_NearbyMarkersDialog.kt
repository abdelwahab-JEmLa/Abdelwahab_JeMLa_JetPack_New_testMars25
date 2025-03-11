package Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.Dialogs.A

import Z_CodePartageEntreApps.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun But1B_NearbyMarkersDialog(
    onDismiss: () -> Unit,
    markers: List<Marker>,
    currentLocation: Location?,
    proxim: Double,
    mapView: MapView,
    viewModelInitApp: ViewModelInitApp
) {
    // Get reference location with proper fallback handling
    val referenceLocation = remember(currentLocation, mapView.mapCenter) {
        currentLocation ?: Location("map_center").apply {
            val center = mapView.mapCenter
            latitude = center.latitude
            longitude = center.longitude
        }
    }

    // Filter markers based on distance, with null safety
    val nearbyMarkers = remember(markers, referenceLocation, proxim) {
        markers.filter { marker ->
            try {
                val markerLocation = Location("marker").apply {
                    latitude = marker.position.latitude
                    longitude = marker.position.longitude
                }
                referenceLocation.distanceTo(markerLocation) <= proxim
            } catch (e: Exception) {
                false // Skip markers with invalid positions
            }
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMarker by remember { mutableStateOf<Marker?>(null) }
    var editedName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Markers Within $proxim Meters of ${
                    if (currentLocation != null) "Current Location"
                    else "Map Center"
                }"
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(nearbyMarkers) { marker ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = marker.title ?: "Unnamed Location",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Distance: ${
                                        try {
                                            val markerLocation = Location("marker").apply {
                                                latitude = marker.position.latitude
                                                longitude = marker.position.longitude
                                            }
                                            "${
                                                referenceLocation.distanceTo(markerLocation)
                                                    .toInt()
                                            }m"
                                        } catch (e: Exception) {
                                            "Unknown"
                                        }
                                    }",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            IconButton(
                                onClick = {
                                    selectedMarker = marker
                                    editedName = marker.title ?: ""
                                    showEditDialog = true
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit name")
                            }
                        }
                    }
                }
                if (nearbyMarkers.isEmpty()) {
                    item {
                        Text(
                            "No markers found within $proxim meters of ${
                                if (currentLocation != null) "your location"
                                else "the map center"
                            }"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )

    if (showEditDialog && selectedMarker != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    label = { Text("Location Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedMarker?.let { marker ->
                            // Update marker title
                            marker.title = editedName

                            // Find and update the corresponding client in the database
                            val client = viewModelInitApp._modelAppsFather.clientDataBase
                                .find {
                                    it.gpsLocation.latitude == marker.position.latitude
                                            && it.gpsLocation.longitude == marker.position.longitude
                                }

                            client?.let { foundClient ->
                                // Update client name
                                foundClient.nom = editedName

                                val product =
                                    viewModelInitApp.produitsMainDataBase.find { produit ->
                                        produit.bonsVentDeCetteCota.any { bonVent ->
                                            bonVent.clientIdChoisi == foundClient.id
                                        }
                                    }

                                product?.let { foundProduct ->
                                    // Update the product in the database
                                    _ModelAppsFather.updateProduit(
                                        foundProduct,
                                        viewModelInitApp
                                    )
                                }
                            }

                            mapView.invalidate()
                        }
                        showEditDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
