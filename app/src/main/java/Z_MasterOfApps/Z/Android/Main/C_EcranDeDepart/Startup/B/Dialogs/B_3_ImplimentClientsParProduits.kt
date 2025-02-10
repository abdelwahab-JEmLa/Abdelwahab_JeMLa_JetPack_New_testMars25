package Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.B.Dialogs

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreTime
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun B_3_ImplimentClientsParProduits(
    viewModelInitApp: ViewModelInitApp,
    showLabels: Boolean
) {
    ControlButton(
        onClick = {
            viewModelInitApp.extentionStartup.implimentClientsParProduits()
        },
        icon = Icons.Default.MoreTime,
        contentDescription = "",
        showLabels = showLabels,
        labelText =  "",
        containerColor =  Color(0xFF9C27B0)
    )
}
