// DialogInfosProduit.kt
package Z_MasterOfApps.Z_AppsFather.Kotlin.Partage.Views

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Packages._1.GerantAfficheurGrossistCommend.App.NH_3.id2_TravaillieurListProduitAchercheChezLeGrossist.E.Dialogs.QuantitySelectionDialog
import Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.GlideDisplayImageBykeyId
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogInfosProduit(
    viewModelInitApp: ViewModelInitApp,
    mainItem: A_ProduitModel,
    modifier: Modifier = Modifier,
    onCLickOnMain: () -> Unit = {},
) {
    val produitsAChoisireLeurClient =
        viewModelInitApp._paramatersAppsViewModelModel.produitsAChoisireLeurClient
    var showDialog by remember { mutableStateOf(false) }
    var selectedColor by remember { mutableStateOf<A_ProduitModel.ClientBonVentModel.ColorAchatModel?>(null) }
    var selectedBonVent by remember { mutableStateOf<A_ProduitModel.ClientBonVentModel?>(null) }

    Box(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onCLickOnMain() }
            .padding(8.dp)
    ) {
        IconButton(
            onClick = {
                produitsAChoisireLeurClient.remove(mainItem)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Remove product",
                tint = MaterialTheme.colorScheme.error
            )
        }

        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GlideDisplayImageBykeyId(
                    modifier = Modifier
                        .width(350.dp)
                        .height(350.dp),
                    imageGlidReloadTigger = 0,
                    mainItem = mainItem,
                    size = 350.dp
                )

                Column(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = mainItem.nom,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "ID: ${mainItem.id}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            mainItem.bonsVentDeCetteCota.forEach { bonVent ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp)
                        .heightIn(max = 150.dp)
                ) {
                    Text(
                        text = viewModelInitApp._modelAppsFather.clientDataBase.find {
                            it.id == bonVent.clientIdChoisi
                        }?.nom ?: "Unknown Client",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(bonVent.colours_Achete.filter { it.quantity_Achete > 0 }) { color ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModelInitApp
                                                .functionsPartageEntreFragment.changeColours_AcheteQuantity_Achete(
                                                    bonVent,
                                                    mainItem,
                                                    color,
                                                    0
                                                )
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,contentDescription = "Clear quantity",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedBonVent = bonVent
                                            selectedColor = color
                                            showDialog = true
                                        }
                                ) {
                                    Text(
                                        text = color.imogi.ifEmpty { color.nom.take(2) },
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        text = "${color.quantity_Achete}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && selectedColor != null) {
        QuantitySelectionDialog(
            onQuantitySelected = { quantity ->
                viewModelInitApp
                    .functionsPartageEntreFragment.changeColours_AcheteQuantity_Achete(
                        selectedBonVent,
                        mainItem,
                        selectedColor!!,
                        quantity
                    )
            },
            onDismiss = { showDialog = false }
        )
    }
}
