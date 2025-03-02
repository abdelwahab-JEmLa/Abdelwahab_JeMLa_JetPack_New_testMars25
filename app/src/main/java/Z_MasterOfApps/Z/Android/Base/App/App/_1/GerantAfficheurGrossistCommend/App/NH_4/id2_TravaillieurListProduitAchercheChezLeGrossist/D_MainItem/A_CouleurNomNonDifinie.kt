package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.D_MainItem

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
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
import androidx.compose.foundation.layout.offset
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
fun A_CouleurNomNonDefinie(
    mainItem: A_ProduitModel,
    modifier: Modifier = Modifier,
    onCLickOnMain: () -> Unit = {},
    position: Int? = null,
) {
    val height = 190.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                color = if (position != null)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onCLickOnMain() },
        contentAlignment = Alignment.Center
    ) {
        val colorAchatModelList = mainItem.bonCommendDeCetteCota
            ?.coloursEtGoutsCommendee
            ?.toList() ?: emptyList()

        val totalQuantity = colorAchatModelList
            .sumOf { it.quantityAchete }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp)
        ) {
            val colorItems = colorAchatModelList
                .filter { it.quantityAchete > 0 }

            // Determine layout based on item count
            when (colorItems.size) {
                1 -> {
                    // Single item takes full width
                    colorItems.firstOrNull()?.let { colorFlavor ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ColorItemContent(
                                colorFlavor = colorFlavor,
                                mainItem = mainItem,
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }
                    }
                }
                else -> {
                    // Multiple items use grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.Center
                    ) {
                        items(colorItems) { colorFlavor ->
                            ColorItemContent(
                                colorFlavor = colorFlavor,
                                mainItem = mainItem,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Rest of the UI remains unchanged
        val position = mainItem.bonCommendDeCetteCota
            ?.mutableBasesStates
            ?.positionProduitDonGrossistChoisiPourAcheterCeProduit

        Text(
            text = "$position>ID: ${mainItem.id}",
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

        Row(
            modifier = Modifier.fillMaxWidth()
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
                        color = Color.Black,
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

@Composable
private fun ColorItemContent(
    colorFlavor: A_ProduitModel.GrossistBonCommandes.ColoursGoutsCommendee,
    mainItem: A_ProduitModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Quantity chip floating above
            Box(
                modifier = Modifier
                    .offset(y = (-8).dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = colorFlavor.quantityAchete.toString(),
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            }

            // Image or fallback
            val colorIndex = (colorFlavor.id.toInt() - 1)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (colorIndex >= 0 &&
                    colorIndex < mainItem.statuesBase.coloursEtGoutsIds.size &&
                    !mainItem.statuesBase.naAucunImage
                ) {
                    GlideDisplayImageBykeyId(
                        modifier = Modifier.fillMaxSize(),
                        imageGlidReloadTigger = mainItem.statuesBase.imageGlidReloadTigger,
                        mainItem = mainItem,
                        size = 60.dp,
                        qualityImage = 80,
                        colorIndex = colorIndex
                    )
                }
            }

            // Name/emoji below
            Text(
                text = when {
                    colorFlavor.emogi.isNotEmpty() -> colorFlavor.emogi
                    else -> colorFlavor.nom.take(2)
                },
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
