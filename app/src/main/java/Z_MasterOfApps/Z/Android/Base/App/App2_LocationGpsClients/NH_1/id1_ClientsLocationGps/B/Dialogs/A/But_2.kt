package Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.A

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.ViewModel.Extension.ViewModelExtension_App2_F1
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.ViewModel.Extension.VisbleClientsNow
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun But_2(
    viewModel: ViewModelInitApp,
    showLabels: Boolean,
    onClick: () -> Unit,
    extensionVM: ViewModelExtension_App2_F1,
    currentFilterMode: VisbleClientsNow,
) {
    ControlButton(
        onClick = onClick,
        icon = currentFilterMode.icon,
        contentDescription = "onFilterMarkers",
        showLabels = showLabels,
        labelText = "onFilterMarkers",
        containerColor = Color(0xFF2196F3)
    )
}
