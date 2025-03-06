package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs

// A_OptionsControlsButtons.kt

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.Utils.LabelsButton
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.Utils.MenuButton
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.ViewModel.Startup_Extension
import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import Z_MasterOfApps.Z_AppsFather.Kotlin.Partage.Views.AnimatedIconLottieJsonFile
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
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

const val TAG = "ControlButton"

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
                    B_7(viewModelInitApp ,showLabels)

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
                    B_5(
                        showLabels = showLabels,
                        viewModel = viewModelInitApp
                    )

                    B_6(
                        showLabels = showLabels,
                        viewModel = viewModelInitApp ,
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

// Fix for A_OptionsControlsButtons.kt
@Composable
fun ControlButton(
    onClick: () -> Unit,
    icon: Any,
    contentDescription: String,
    showLabels: Boolean,
    labelText: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Log.d(TAG, "ControlButton called with icon type: ${icon.javaClass.simpleName}")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (icon) {
            is ImageVector -> {
                Log.d(TAG, "Rendering ImageVector icon")
                FloatingActionButton(
                    onClick = {
                        Log.d(TAG, "ImageVector FAB clicked")
                        onClick()
                    },
                    modifier = modifier.size(40.dp),
                    containerColor = containerColor
                ) {
                    Icon(icon, contentDescription)
                }
            }
            is LottieJsonGetterR_Raw_Icons -> {
                Log.d(TAG, "Rendering LottieJsonGetterR_Raw_Icons")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            Log.d(TAG, "LottieJson Box clicked")
                            onClick()
                        }
                        .background(
                            color = containerColor,
                            shape = CircleShape
                        )
                        .also {
                            Log.d(TAG, "Box modifiers applied successfully")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedIconLottieJsonFile(
                        ressourceXml = icon,
                        onClick = onClick
                    )
                }
            }
            is Int -> {
                // Support for direct resource IDs like R.raw.categ
                Log.d(TAG, "Rendering direct resource ID: $icon")
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            Log.d(TAG, "Resource ID Box clicked")
                            onClick()
                        }
                        .background(
                            color = containerColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedIconLottieJsonFile(
                        resourceId = icon,
                        onClick = onClick
                    )
                }
            }
            else -> {
                Log.e(TAG, "Unsupported icon type: ${icon.javaClass.simpleName}")
                throw IllegalArgumentException("Unsupported icon type")
            }
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
