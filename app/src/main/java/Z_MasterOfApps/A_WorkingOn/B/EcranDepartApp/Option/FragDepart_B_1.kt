package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.Option

import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun FragDepart_B_1(
    showLabels: Boolean,
) {
    ControlButton(
        onClick = {

        },
        icon = LottieJsonGetterR_Raw_Icons.reacticonanimatedjsonurl,
        contentDescription = "DialogeOptions",
        showLabels = showLabels,
        labelText = "DialogeOptions",
        containerColor = Color(0xFF2196F3)
    )
}
