package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.Dialog

import Z_CodePartageEntreApps.Model._ModelAppsFather
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z_AppsFather.Kotlin.Partage.Views.B_1_CameraFAB
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.ParamatersAppsModel.DeviceMode
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun A_OptionsControlsButtons_F1(
    appsHeadModel: _ModelAppsFather,
    modifier: Modifier = Modifier,
    viewModel: ViewModelInitApp,
) {
    // State management
    var showOptions by remember { mutableStateOf(false) }
    var deviceMode by remember { mutableStateOf(DeviceMode.SERVER) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Constants for colors
    val fabColors = object {
        val primary = Color(0xFF3F51B5)
        val secondary = Color(0xFF4CAF50)
        val accent = Color(0xFFFF5722)
    }

    // Main container
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Main FAB
            FloatingActionButton(
                onClick = { showOptions = !showOptions },
                modifier = Modifier.size(48.dp),
                containerColor = fabColors.primary
            ) {
                Icon(
                    imageVector = if (showOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (showOptions) "Masquer les options" else "Afficher les options"
                )
            }

            // Options menu
            AnimatedVisibility(
                visible = showOptions,
                enter = fadeIn(tween(300)) + expandVertically(
                    animationSpec = tween(300)
                ),
                exit = fadeOut(tween(300)) + shrinkVertically(
                    animationSpec = tween(300)
                )
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Delete FAB
                    FloatingActionButton(
                        onClick = { /* Delete functionality */ },
                        modifier = Modifier.size(48.dp),
                        containerColor = fabColors.secondary
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer"
                        )
                    }

                    // Camera FAB
                    B_1_CameraFAB(
                        viewModel = viewModel,
                        size = 48.dp,
                        containerColor = fabColors.secondary
                    )

                    // Position Edit FAB
                    FloatingActionButton(
                        onClick = {
                            viewModel
                                ._paramatersAppsViewModelModel
                                .visibilityClientEditePositionDialog = true
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = fabColors.accent
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowUp,
                            contentDescription = "Éditer la position"
                        )
                    }

                    // Mode Toggle FAB
                    FloatingActionButton(
                        onClick = {
                            deviceMode = when (deviceMode) {
                                DeviceMode.SERVER -> DeviceMode.DISPLAY
                                DeviceMode.DISPLAY -> DeviceMode.SERVER
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = fabColors.accent
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = if (deviceMode == DeviceMode.SERVER)
                                "Passer en mode Affichage" else "Passer en mode Serveur"
                        )
                    }
                }
            }
        }
    }
}
