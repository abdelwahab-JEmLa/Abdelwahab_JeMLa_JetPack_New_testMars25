package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.Modules.SearchDialog_F1
import Z_MasterOfApps.Z.Android.Packages._1.GerantAfficheurGrossistCommend.App.NH_5.ID5_VerificationProduitAcGrossist.D_MainItem_F5
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.ViewModel.Extension.ViewModelExtension_App1_F5
import Z_MasterOfApps.Z.Android.Packages._1.GerantAfficheurGrossistCommend.App.NH_5.ID5_VerificationProduitAcGrossist.ViewModel.Extension.Z_OnClick.MainItem.Actions.excludeProduit
import Z_MasterOfApps.Z.Android.Packages._1.GerantAfficheurGrossistCommend.App.NH_5.ID5_VerificationProduitAcGrossist.ViewModel.Extension.Z_OnClick.MainItem.Actions.includeProduit
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun C_MainList_F5(
    extensionVM: ViewModelExtension_App1_F5,
    viewModel: ViewModelInitApp,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    var showSearchDialog by remember { mutableStateOf(false) }

    // Use animateFloatAsState for smooth blinking animation
    val blinkingAlpha by animateFloatAsState(
        targetValue = if (extensionVM.prochenClickIncludeProduit != null) 0.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blinking"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Produits Excluded (${extensionVM.excludedProduits.size})",
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.titleMedium
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xE3C85858).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentPadding = paddingValues,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(extensionVM.excludedProduits) { product ->
                D_MainItem_F5(
                    mainItem = product,
                    onCLickOnMain = {
                        extensionVM.prochenClickIncludeProduit =
                            if (extensionVM.prochenClickIncludeProduit != product) product
                            else null
                    },
                    modifier = Modifier
                        .animateItem(
                            fadeInSpec = tween(500),
                            fadeOutSpec = tween(500)
                        )
                        .then(
                            if (extensionVM.prochenClickIncludeProduit?.id == product.id) {
                                Modifier.alpha(blinkingAlpha)
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }

        // Section Produits Verifie
        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xE3C85858).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentPadding = paddingValues,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(5) }) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Rechercher",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        "Produits Verifie (${extensionVM.produitsVerifie.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(extensionVM.produitsVerifie) { product ->
                D_MainItem_F5(
                    mainItem = product,
                    onCLickOnMain = {
                        if (extensionVM.prochenClickIncludeProduit != null) {
                            extensionVM.includeProduit(product)
                        } else {
                            extensionVM.excludeProduit(product)
                        }
                    },
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(500),
                        fadeOutSpec = tween(500)
                    )
                )
            }
        }
    }

    SearchDialog_F1(
        unpositionedItems = extensionVM.excludedProduits,
        viewModelProduits = viewModel,
        showDialog = showSearchDialog,
        onDismiss = { showSearchDialog = false }
    )
}
