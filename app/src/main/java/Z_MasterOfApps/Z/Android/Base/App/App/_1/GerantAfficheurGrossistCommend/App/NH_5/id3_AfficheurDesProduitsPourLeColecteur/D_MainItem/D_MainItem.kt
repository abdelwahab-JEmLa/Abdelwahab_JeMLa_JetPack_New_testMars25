package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_5.id3_AfficheurDesProduitsPourLeColecteur.D_MainItem

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.GlideDisplayImageBykeyId
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainItem_F3(
    mainItem: A_ProduitModel,
    viewModelProduits: ViewModelInitApp,
    modifier: Modifier = Modifier,
    onCLickOnMain: () -> Unit = {},
    position: Int? = null,
) {
    val clientIdChoisi = viewModelProduits
        .frag_3A1_ExtVM
        .clientFocused?.first?.id ?: 0

    val height = 190.dp  // Matched with MainItem_F2
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)  // Updated height
            .background(
                color = if (position != null)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onCLickOnMain() },
        contentAlignment = Alignment.Center
    ) {
        GlideDisplayImageBykeyId(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            imageGlidReloadTigger = 0,
            mainItem = mainItem,  // Updated height
            size = 140.dp,  // Matched with MainItem_F2
            qualityImage = 80  // Added quality parameter
        )

        Text(
            text = "ID: ${mainItem.id}",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .background(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(4.dp),
            style = MaterialTheme.typography.bodySmall,
            fontSize = 8.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        val colorAchatModelList = mainItem.bonsVentDeCetteCota
            .filter { bonVent ->
                bonVent.clientIdChoisi == clientIdChoisi
            }
            .flatMap { it.colours_Achete }

        val totalQuantity = colorAchatModelList
            .sumOf { it.quantity_Achete }

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .width(270.dp)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = mainItem.nom,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        ),
                        color = Color.Black,  // Updated color
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = Color.White.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(4.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = totalQuantity.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            color = Color.Black,
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp)
                        )
                        Text(
                            text = "ك.الكلية",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            ),
                            color = Color.Black
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .padding(7.dp)  // Added padding
                ) {
                    val colorItems = colorAchatModelList
                        .filter { it.quantity_Achete > 0 }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(0.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        items(colorItems) { colorFlavor ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val displayText = when {
                                    colorFlavor.imogi.isNotEmpty() -> colorFlavor.imogi
                                    else -> colorFlavor.nom.take(2)
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "$displayText>",
                                        fontSize = 24.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "(${colorFlavor.quantity_Achete})",
                                        fontSize = 24.sp,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .background(
                                                color = Color.White.copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (position != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp)
            ) {
                Text(
                    text = position.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
