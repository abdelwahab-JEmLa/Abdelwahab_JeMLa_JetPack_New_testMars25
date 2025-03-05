package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.Option

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.FragmentViewModel
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.ModeAuClickButton
import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.update

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun FragDepart_B_1(
    viewModel: FragmentViewModel,
    showLabels: Boolean,
) {
    ControlButton(
        onClick = {
            // Change modes based on the current state
            val currentMode = viewModel.state.value.modeAuClickButton
            val newMode = when (currentMode) {
                ModeAuClickButton.NO_HOLDED -> ModeAuClickButton.HOLDED_GROUPE
                ModeAuClickButton.HOLDED_GROUPE -> ModeAuClickButton.ITS_ONE_CATE_IN_HOLD
                ModeAuClickButton.ITS_ONE_CATE_IN_HOLD -> ModeAuClickButton.NO_HOLDED
            }
            viewModel._state.update { it.copy(modeAuClickButton = newMode) }
        },
        icon = if (viewModel.state.value.modeAuClickButton == ModeAuClickButton.NO_HOLDED) {
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
