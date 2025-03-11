package Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.Dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.osmdroid.views.overlay.Marker

//NavDialoge(showNavigationDialog, selectedMarker, context)

@Composable
fun NavigationDialog(
    onDismiss: () -> Unit,
    onConfirm: (Marker) -> Unit,
    marker: Marker
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Navigation") },
        text = { Text("Voulez-vous démarrer la navigation vers ce point ?") },
        confirmButton = {
            Button(onClick = { onConfirm(marker); onDismiss() }) {
                Text("Démarrer")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun NavDialoge(
    showNavigationDialog: Boolean,
    selectedMarker: Marker?,
    context: Context
) {
    var showNavigationDialog1 = showNavigationDialog
    if (showNavigationDialog1 && selectedMarker != null) {
        NavigationDialog(
            onDismiss = { showNavigationDialog1 = false },
            onConfirm = { marker ->
                val uri = Uri.parse(
                    "google.navigation:q=${marker.position.latitude},${marker.position.longitude}&mode=d"
                )
                val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                context.startActivity(mapIntent)
            },
            marker = selectedMarker
        )
    }
}
