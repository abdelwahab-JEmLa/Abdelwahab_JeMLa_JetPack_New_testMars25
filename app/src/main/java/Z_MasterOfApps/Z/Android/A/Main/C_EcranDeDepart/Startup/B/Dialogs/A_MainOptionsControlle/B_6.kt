package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.A_MainOptionsControlle

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Usage in B_6.kt
@Composable
fun B_6(
    viewModel: ViewModelInitApp,
    showLabels: Boolean,
) {
    Log.d(TAG, "B_6 composable called")
    ControlButton(
        onClick = {
            Log.d(TAG, "B_6 onClick triggered")
            viewModel.extentionStartup.activedialogeOptions()

        },
        icon = LottieJsonGetterR_Raw_Icons.reacticonanimatedjsonurl,
        contentDescription = "DialogeOptions",
        showLabels = showLabels,
        labelText = "DialogeOptions",
        containerColor = Color(0xFF2196F3)
    )
}
