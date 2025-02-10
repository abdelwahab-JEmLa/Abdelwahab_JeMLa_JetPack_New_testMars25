package Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.B.Dialogs

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z_AppsFather.Kotlin._3.Init.creeDepuitAncienDataBases
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun B_4_creeDepuitAncienDataBases(
    viewModel: ViewModelInitApp,
    showLabels: Boolean,
) {
    var clearDataClickCount by remember { mutableIntStateOf(0) }

    ControlButton(
        onClick = {
            if (clearDataClickCount == 0) {
                clearDataClickCount++
            } else {
                creeDepuitAncienDataBases(viewModel)
                clearDataClickCount = 0
            }
        },
        icon = if (clearDataClickCount == 0) Icons.Default.Warning else Icons.Default.Done,
        contentDescription = "creeDepuitAncienDataBases",
        showLabels = showLabels,
        labelText = if (clearDataClickCount == 0) "creeDepuitAncienDataBases ??" else "Click again to confirm",
        containerColor = if (clearDataClickCount == 0) Color(0xFFF44336) else Color(0xFF7D7426)
    )
}
