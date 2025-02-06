package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.D_MainItem.ExpandedMainItem_F2
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.D_MainItem.MainItem_F2
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MainList_F2(
    initVisibleProducts: List<A_ProduitModel>,
    viewModelInitApp: ViewModelInitApp,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val produitsAChoisireLeurClient =
        viewModelInitApp._paramatersAppsViewModelModel.produitsAChoisireLeurClient

    val afficheProduitsPourRegleConflites =
        viewModelInitApp.frag2_A1_ExtVM.afficheProduitsPourRegleConflites

    val visibleProducts = if (afficheProduitsPourRegleConflites)
        produitsAChoisireLeurClient else initVisibleProducts

    var expandedItemId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xE3C85858).copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = visibleProducts.sortedBy { product ->
                product.bonCommendDeCetteCota
                    ?.mutableBasesStates
                    ?.positionProduitDonGrossistChoisiPourAcheterCeProduit
                    ?: Int.MAX_VALUE
            },
        ) { product ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Always show the collapsed item
                MainItem_F2(
                    mainItem = product,
                    modifier = Modifier.fillMaxWidth(),
                    onCLickOnMain = {
                        expandedItemId = if (expandedItemId == product.id)
                            null else product.id
                    }
                )

                // Animated expanded content
                AnimatedVisibility(
                    visible = expandedItemId == product.id,
                    enter = expandVertically(
                        animationSpec = spring(
                            dampingRatio = 0.9f,
                            stiffness = 300f
                        )
                    ),
                    exit = shrinkVertically(
                        animationSpec = spring(
                            dampingRatio = 0.9f,
                            stiffness = 300f
                        )
                    )
                ) {
                    ExpandedMainItem_F2(
                        viewModelInitApp = viewModelInitApp,
                        mainItem = product,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        onCLickOnMain = { expandedItemId = null }
                    )
                }
            }
        }
    }
}
