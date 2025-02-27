// B_ListMain.kt
package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.updateProduit
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.Modules.MoveProductsDialog
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.Modules.SearchDialog_F1
import Z_MasterOfApps.Z.Android.Main.Utils.LottieJsonGetterR_Raw_Icons
import Z_MasterOfApps.Z_AppsFather.Kotlin.Partage.Views.AnimatedIconLottieJsonFile
import Z_MasterOfApps.Z_AppsFather.Kotlin.Partage.Views.DialogInfosProduit
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Moving
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun B_ListMainFragment(
    viewModel: ViewModelInitApp,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    visibleProducts: List<A_ProduitModel>
) {
    var showMoveDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showProductInfoDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<A_ProduitModel?>(null) }

    val (positionedProducts, unpositionedProducts) = visibleProducts.partition {
        it.bonCommendDeCetteCota?.mutableBasesStates?.cPositionCheyCeGrossit == true
    }

    val produitsAChoisireLeurClient = viewModel
        ._paramatersAppsViewModelModel.produitsAChoisireLeurClient

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE3C85858).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentPadding = paddingValues,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (positionedProducts.isNotEmpty()) {
            val positionedProductsSorted = positionedProducts.sortedBy {
                it.bonCommendDeCetteCota?.mutableBasesStates?.positionProduitDonGrossistChoisiPourAcheterCeProduit
            }

            item(span = { GridItemSpan(5) }) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Produits avec position (${positionedProducts.size})",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )

                    AnimatedIconLottieJsonFile(
                        LottieJsonGetterR_Raw_Icons.reacticonanimatedjsonurl,
                        onClick = {
                            positionedProductsSorted.forEach { product ->
                                product.bonCommendDeCetteCota?.mutableBasesStates?.cPositionCheyCeGrossit = false
                                updateProduit(product, viewModel)
                            }
                        }
                    )

                    Box {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "ChoisireClient",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "${produitsAChoisireLeurClient.size}",
                            modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    viewModel.frag1_A1_ExtVM.addToproduitsAChoisireLeurClient(
                                        positionedProductsSorted.last()
                                    )
                                },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    AnimatedIconLottieJsonFile(
                        LottieJsonGetterR_Raw_Icons.afficheFenetre,
                        onClick = {
                            selectedProduct = positionedProductsSorted.lastOrNull()
                            if (selectedProduct != null) {
                                showProductInfoDialog = true
                            }
                        }
                    )
                }
            }

            items(
                items = positionedProductsSorted,
            ) { product ->
                C_ItemMainFragment(
                    mainItem = product,
                    onCLickOnMain = {
                        product.bonCommendDeCetteCota?.mutableBasesStates?.cPositionCheyCeGrossit = false
                        updateProduit(product, viewModel)
                    },
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                )
            }
        }

        if (unpositionedProducts.isNotEmpty()) {
            item(span = { GridItemSpan(5) }) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { showMoveDialog = true }) {
                        Icon(
                            Icons.Default.Moving,
                            contentDescription = "Déplacer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        "Produits sans position (${unpositionedProducts.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(
                items = unpositionedProducts.sortedWith(compareBy(
                    { product ->
                        val position = product.bonCommendDeCetteCota?.mutableBasesStates?.positionProduitDonGrossistChoisiPourAcheterCeProduit
                        if (position == null || position == 0) null else position
                    },
                    { it.nom.lowercase() }
                )),
            ) { product ->
                C_ItemMainFragment(
                    mainItem = product,
                    onCLickOnMain = {
                        val newPosition = (positionedProducts.maxOfOrNull {
                            it.bonCommendDeCetteCota?.mutableBasesStates?.positionProduitDonGrossistChoisiPourAcheterCeProduit ?: 0
                        } ?: 0) + 1

                        product.bonCommendDeCetteCota?.apply {
                            mutableBasesStates?.cPositionCheyCeGrossit = true
                            mutableBasesStates?.positionProduitDonGrossistChoisiPourAcheterCeProduit = newPosition
                        }
                        if (product.itsTempProduit) {
                            product.statuesBase.prePourCameraCapture = true
                        }
                        updateProduit(product, viewModel)
                    },
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                )
            }
        }
    }

    if (showMoveDialog) {
        MoveProductsDialog(
            selectedProducts = unpositionedProducts,
            viewModelProduits = viewModel,
            onDismiss = { showMoveDialog = false },
            onProductsMoved = { showMoveDialog = false }
        )
    }

    if (showSearchDialog) {
        SearchDialog_F1(
            unpositionedItems = unpositionedProducts,
            viewModelProduits = viewModel,
            showDialog = showSearchDialog,
            onDismiss = { showSearchDialog = false }
        )
    }

    if (showProductInfoDialog && selectedProduct != null) {
        Dialog(onDismissRequest = { showProductInfoDialog = false }) {
            DialogInfosProduit(
                viewModelInitApp = viewModel,
                mainItem = selectedProduct!!,
                onCLickOnMain = { showProductInfoDialog = false }
            )
        }
    }
}
