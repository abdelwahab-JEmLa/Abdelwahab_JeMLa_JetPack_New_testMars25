package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.Utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.A.ControlButton

@Composable
 fun LabelsButton(
    showLabels: Boolean,
    onShowLabelsChange: (Boolean) -> Unit
) {
    ControlButton(
        onClick = { onShowLabelsChange(!showLabels) },
        icon = Icons.Default.Info,
        contentDescription = if (showLabels) "Hide labels" else "Show labels",
        showLabels = showLabels,
        labelText = if (showLabels) "Hide labels" else "Show labels",
        containerColor = Color(0xFF3F51B5)
    )
}

@Composable
 fun MenuButton(
    showLabels: Boolean,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit
) {
    ControlButton(
        onClick = { onShowMenuChange(!showMenu) },
        icon = if (showMenu) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
        contentDescription = if (showMenu) "Hide menu" else "Show menu",
        showLabels = showLabels,
        labelText = if (showMenu) "Hide" else "Options",
        containerColor = Color(0xFF3F51B5)
    )
}
