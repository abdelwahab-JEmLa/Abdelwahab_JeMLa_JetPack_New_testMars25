package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_5.id3_AfficheurDesProduitsPourLeColecteur

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.A_ProduitModel.Companion.ExtraiGrossistInfos
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.D_MainItem.ExpandedMainItem_F2
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_5.id3_AfficheurDesProduitsPourLeColecteur.D_MainItem.MainItem_F3
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun MainList_F3(
    viewModel: ViewModelInitApp,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val frag_3A1_ExtVM = viewModel.frag_3A1_ExtVM
    val visibleProducts = frag_3A1_ExtVM.clientFocused?.second?.sortedWith(
        compareBy<A_ProduitModel> { product ->
            product.bonCommendDeCetteCota?.let { bon ->
                viewModel._modelAppsFather.grossistsDataBase.find { it.id == bon.idGrossistChoisi }
                    ?.statueDeBase?.itIndexInParentList
            } ?: Int.MAX_VALUE
        }.thenBy { product ->
            product.bonCommendDeCetteCota?.mutableBasesStates?.positionProduitDonGrossistChoisiPourAcheterCeProduit ?: Int.MAX_VALUE
        }
    )

    var expandedItemId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xE3C85858).copy(alpha = 0.1f)),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visibleProducts?.let { products ->
            val etagerProducts = products.filter { !it.statuesBase.characterProduit.emballageCartone }
            val cartonsProducts = products.filter { it.statuesBase.characterProduit.emballageCartone }

            if (etagerProducts.isNotEmpty()) {
                item {
                    Text(
                        text = "Unite",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                ProductsList(
                    products = etagerProducts,
                    viewModel = viewModel,
                    expandedItemId = expandedItemId,
                    onExpandedItemIdChange = { expandedItemId = it },
                    groupByGrossist = true
                )
            }

            if (cartonsProducts.isNotEmpty()) {
                item {
                    Text(
                        text = "Cartons",
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray)
                            .padding(8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                ProductsList(
                    products = cartonsProducts,
                    viewModel = viewModel,
                    expandedItemId = expandedItemId,
                    onExpandedItemIdChange = { expandedItemId = it },
                    groupByGrossist = false // Don't group Cartons products by grossist
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.ProductsList(
    products: List<A_ProduitModel>,
    viewModel: ViewModelInitApp,
    expandedItemId: Long?,
    onExpandedItemIdChange: (Long?) -> Unit,
    groupByGrossist: Boolean = true
) {
    if (groupByGrossist) {
        // Existing grouping logic for non-carton products
        val groupedProducts = products.groupBy { product ->
            viewModel._modelAppsFather.grossistsDataBase.find {
                it.id == product.bonCommendDeCetteCota?.idGrossistChoisi
            }
        }

        val sortedGrossists = groupedProducts.keys.filterNotNull().sortedBy {
            it.statueDeBase.itIndexInParentList
        }

        sortedGrossists.forEach { grossist ->
            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(android.graphics.Color.parseColor(grossist.statueDeBase.couleur)))
                        .padding(16.dp)
                ) {
                    Text(
                        text = grossist.nom,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (grossist.statueDeBase.couleur == "#FFFFFF") Color.Black else Color.White
                    )
                }
            }

            val grossistProducts = groupedProducts[grossist].orEmpty().sortedBy {
                it.bonCommendDeCetteCota?.mutableBasesStates?.positionProduitDonGrossistChoisiPourAcheterCeProduit
            }

            items(grossistProducts) { product ->
                ProductItem(
                    product = product,
                    viewModel = viewModel,
                    expandedItemId = expandedItemId,
                    onExpandedItemIdChange = onExpandedItemIdChange
                )
            }
        }
    } else {
        // Non-grouped logic for carton products with updated sorting
        val sortedProducts = products.sortedWith(
            compareBy<A_ProduitModel> { product ->
                // First sort by grossist position
                product.bonCommendDeCetteCota?.let {
                    product.ExtraiGrossistInfos(viewModel)
                        ?.statueDeBase?.itIndexInParentList
                } ?: Int.MAX_VALUE
            }.thenBy { product ->
                // Then sort by product position within grossist
                product.bonCommendDeCetteCota?.mutableBasesStates?.positionProduitDonGrossistChoisiPourAcheterCeProduit ?: Int.MAX_VALUE
            }
        )

        items(sortedProducts) { product ->
            ProductItem(
                product = product,
                viewModel = viewModel,
                expandedItemId = expandedItemId,
                onExpandedItemIdChange = onExpandedItemIdChange
            )
        }
    }
}


@Composable
private fun ProductItem(
    product: A_ProduitModel,
    viewModel: ViewModelInitApp,
    expandedItemId: Long?,
    onExpandedItemIdChange: (Long?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        MainItem_F3(
            viewModelProduits = viewModel,
            mainItem = product,
            modifier = Modifier.fillMaxWidth(),
            onCLickOnMain = {
                onExpandedItemIdChange(if (expandedItemId == product.id) null else product.id)
            }
        )

        AnimatedVisibility(
            visible = expandedItemId == product.id,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            ExpandedMainItem_F2(
                viewModelInitApp = viewModel,
                mainItem = product,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                onCLickOnMain = { onExpandedItemIdChange(null) }
            )
        }
    }
}
