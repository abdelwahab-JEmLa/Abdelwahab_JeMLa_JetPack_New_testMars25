package Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.B.Dialogs.A

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.ViewModel.Extension.ViewModelExtension_App2_F1
import Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.ViewModel.Extension.VisbleClientsNow
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
        labelText = currentFilterMode.name,
        containerColor = Color(0xFF2196F3)
    )
}
