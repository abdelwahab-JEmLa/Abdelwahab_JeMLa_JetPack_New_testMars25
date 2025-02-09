package Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.ControlButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun AlimentclientDataBaseSnapList(
    viewModelInitApp: ViewModelInitApp,
    showLabels: Boolean
) {
    var clearDataClickCount by remember { mutableIntStateOf(0) }

    ControlButton(
        onClick = {
            if (clearDataClickCount == 0) {
                clearDataClickCount++
            } else {
                //     viewModelInitApp.mapsHandler.alimentclientDBDepuitCalcule()
                clearDataClickCount = 0
            }
        },
        icon = Icons.Default.Delete,
        contentDescription = "alimentclientDBDepuitCalcule",
        showLabels = showLabels,
        labelText = if (clearDataClickCount == 0) "alimentclientDBDepuitCalcule" else "Click again to confirm",
        containerColor = if (clearDataClickCount == 0) Color(0xFFE91E63) else Color(0xFFF44336)
    )
}
