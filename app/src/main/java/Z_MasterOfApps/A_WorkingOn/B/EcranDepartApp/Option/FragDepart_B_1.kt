package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.Option

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.FragmentViewModel
import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun FragDepart_B_1(
    viewModel: FragmentViewModel,
    showLabels: Boolean,
) {
    var activeButton by remember { mutableStateOf(false) }

    ControlButton(
        onClick = {
                //-->
                //TODO(1): fait que si le button active de change
            //-->
            //TODO(1): viewModel.
        },
        icon = if ( activeButton) {
            LottieJsonGetterR_Raw_Icons.atay
        } else {
            LottieJsonGetterR_Raw_Icons.alimentation
        },
        contentDescription = "DialogeOptions",
        showLabels = showLabels,
        labelText = "DialogeOptions",
        containerColor = Color(0xFF2196F3)
    )
}
