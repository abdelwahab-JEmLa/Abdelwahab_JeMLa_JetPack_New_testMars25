package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.A_MainOptionsControlle

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.abdelwahabjemlajetpack.R

@Composable
fun B_7(
    viewModel: ViewModelInitApp,
    showLabels: Boolean,
) {
    ControlButton(
        onClick = {
            viewModel.extentionStartup.showCategorySelection = true
        },
        icon = R.raw.categ,
        contentDescription = "DialogeOptions",
        showLabels = showLabels,
        labelText = "DialogeOptions",
        containerColor = Color(0xFF2196F3)
    )
}
