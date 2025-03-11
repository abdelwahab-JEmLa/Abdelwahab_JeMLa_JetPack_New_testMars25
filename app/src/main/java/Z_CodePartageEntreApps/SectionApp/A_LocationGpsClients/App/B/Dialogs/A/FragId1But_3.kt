package Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.B.Dialogs.A

import Z_CodePartageEntreApps.Model.B_ClientsDataBase.StatueDeBase.TypeDeSonMagasine
import Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.ViewModel.Extension.ViewModelExtension_App2_F1
import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun FragId1But_3(
    showLabels: Boolean,
    extensionVM: ViewModelExtension_App2_F1,
) {
    ControlButton(
        onClick = {
            // Toggle between the two TypeDeSonMagasine values
            extensionVM.auClickeCaUpdateClientPar = if ( extensionVM.auClickeCaUpdateClientPar == TypeDeSonMagasine.ATAYAT_MOUKASSARAT) {
                TypeDeSonMagasine.AlIMENTATION_GENERALE
            } else {
                TypeDeSonMagasine.ATAYAT_MOUKASSARAT
            }
        },
        // Choose icon based on current state
        icon = if ( extensionVM.auClickeCaUpdateClientPar == TypeDeSonMagasine.ATAYAT_MOUKASSARAT) {
            LottieJsonGetterR_Raw_Icons.atay
        } else {
            LottieJsonGetterR_Raw_Icons.alimentation
        },
        contentDescription = "auClickeCaUpdateClientPar",
        showLabels = showLabels,
        labelText = "auClickeCaUpdateClientPar",
        containerColor = Color(0xFF2196F3)
    )
}
