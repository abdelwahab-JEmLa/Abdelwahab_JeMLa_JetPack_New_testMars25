package Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.B.Dialogs

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.B.Dialogs.Utils.LabelsButton
import Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.B.Dialogs.Utils.MenuButton
import Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.ViewModel.Startup_Extension
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun A_OptionsControlsButtons(
    viewModelInitApp: ViewModelInitApp,
    paddingValues: PaddingValues,
    extensionVM: Startup_Extension,
) {
    var showMenu by remember { mutableStateOf(true) }
    var showLabels by remember { mutableStateOf(true) }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (showMenu) {

                    B_3_ImplimentClientsParProduits(viewModelInitApp ,showLabels)

                    B_1_SwitchGerantOuAfficheurPhone(
                        showLabels = showLabels,
                        viewModelInitApp = viewModelInitApp
                    )
                    B_2_ClearAchatsEtCommendsEtSauvgardHistoriques(
                        extensionVM=extensionVM,
                        showLabels = showLabels,
                        viewModelInitApp = viewModelInitApp
                    )
                    B_4_creeDepuitAncienDataBases(
                        showLabels = showLabels,
                        viewModel = viewModelInitApp
                    )
                }

                LabelsButton(
                    showLabels = showLabels,
                    onShowLabelsChange = { showLabels = it }
                )

                MenuButton(
                    showLabels = showLabels,
                    showMenu = showMenu,
                    onShowMenuChange = { showMenu = it }
                )
            }
        }
    }
}

@Composable
fun ControlButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    showLabels: Boolean,
    labelText: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.size(40.dp),
            containerColor = containerColor
        ) {
            Icon(icon, contentDescription)
        }
        if (showLabels) {
            Text(
                labelText,
                modifier = Modifier
                    .background(containerColor)
                    .padding(4.dp),
                color = Color.White
            )
        }
    }
}
