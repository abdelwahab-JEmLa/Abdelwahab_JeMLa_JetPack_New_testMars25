package Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils

import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.A.ControlButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ShowLocationsInfoBubble(
    showLabels: Boolean,
    showMarkerDetails: Boolean,
    onShowMarkerDetailsChange: (Boolean) -> Unit
) {
    ControlButton(
        onClick = { onShowMarkerDetailsChange(!showMarkerDetails) },
        icon = Icons.Default.Info,
        contentDescription = "Details",
        showLabels = showLabels,
        labelText = if (showMarkerDetails) "Hide details" else "Show details",
        containerColor = Color(0xFF009688)
    )
}
