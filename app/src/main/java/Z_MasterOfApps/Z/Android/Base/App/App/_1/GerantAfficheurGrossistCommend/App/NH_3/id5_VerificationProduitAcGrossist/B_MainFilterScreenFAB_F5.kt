package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.ViewModel.Extension.ViewModelExtension_App1_F5
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun B_MainScreenFilterFAB_F5(
    extensionVM: ViewModelExtension_App1_F5,
    modifier: Modifier = Modifier,
    viewModelProduits: ViewModelInitApp,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var showButtons by remember { mutableStateOf(false) }

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
            horizontalAlignment = Alignment.End
        ) {
            FloatingActionButton(
                onClick = { showButtons = !showButtons },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (showButtons) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            AnimatedVisibility(visible = showButtons) {
                Column(horizontalAlignment = Alignment.End) {
                    viewModelProduits._modelAppsFather.groupedProductsParGrossist
                        .filter { (_, products) -> products.isNotEmpty() }
                        .forEachIndexed { index, (grossist, produits) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${grossist.nom} (${produits.size})",
                                    modifier = Modifier
                                        .background(
                                            if (viewModelProduits._paramatersAppsViewModelModel
                                                    .telephoneClientParamaters.selectedGrossistForServeur == grossist.id
                                            ) Color(0xFF2196F3) else Color.Transparent
                                        )
                                        .padding(4.dp)
                                )

                                FloatingActionButton(
                                    onClick = {
                                        extensionVM.produitsVerifie.clear()
                                        extensionVM.produitsVerifie.addAll(produits)
                                    },
                                    modifier = Modifier.size(48.dp),
                                    containerColor = try {
                                        val couleur = grossist.statueDeBase.couleur
                                        Color(
                                            android.graphics.Color.parseColor(
                                                if (couleur.startsWith("#")) couleur
                                                else "#${couleur}"
                                            )
                                        )
                                    } catch (e: Exception) {
                                        Color(0xFFFF0000)
                                    }
                                ) {
                                    Text(produits.size.toString())
                                }
                            }
                        }
                }
            }
        }
    }
}
