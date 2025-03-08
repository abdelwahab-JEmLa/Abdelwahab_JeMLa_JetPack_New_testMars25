package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.A_MainOptionsControlle

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phonelink
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun B_1_SwitchGerantOuAfficheurPhone(
    viewModelInitApp: ViewModelInitApp,
    showLabels: Boolean
) {
    ControlButton(
        onClick = {
            viewModelInitApp
                ._paramatersAppsViewModelModel
                .cLeTelephoneDuGerant = true
        },
        icon = Icons.Default.Phonelink,
        contentDescription = "",
        showLabels = showLabels,
        labelText =  "",
        containerColor =  Color(0xFF2196F3)
    )
}
