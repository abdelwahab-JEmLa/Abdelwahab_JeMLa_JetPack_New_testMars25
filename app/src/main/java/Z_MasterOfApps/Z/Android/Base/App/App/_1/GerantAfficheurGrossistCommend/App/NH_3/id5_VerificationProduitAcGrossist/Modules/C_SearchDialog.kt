package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.Modules

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.updateProduit
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.C_ItemMainFragment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun SearchDialog_F1(
    unpositionedItems: List<A_ProduitModel>,
    viewModelProduits: ViewModelInitApp,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current


    if (showDialog) {
        LaunchedEffect(Unit) {
            delay(100)
            focusRequester.requestFocus()
        }

        Dialog(
            onDismissRequest = {
                focusManager.clearFocus()
                onDismiss()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        "Rechercher un produit",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Nom du produit") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    val filteredItems = unpositionedItems.filter {
                        it.nom.contains(searchText, ignoreCase = true)
                    }

                    if (searchText.length >= 2) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filteredItems,
                                key = { it.id }
                            ) { product ->
                                C_ItemMainFragment(
                                    mainItem = product,
                                    onCLickOnMain = {
                                        val positionedProducts =
                                            viewModelProduits.produitsMainDataBase.filter {
                                                it.bonCommendDeCetteCota
                                                    ?.mutableBasesStates
                                                    ?.cPositionCheyCeGrossit == true
                                            }
                                        val newPosition = (positionedProducts.maxOfOrNull {
                                            it.bonCommendDeCetteCota
                                                ?.mutableBasesStates
                                                ?.positionProduitDonGrossistChoisiPourAcheterCeProduit
                                                ?: 0
                                        } ?: 0) + 1

                                        product.bonCommendDeCetteCota?.apply {
                                            mutableBasesStates
                                                ?.cPositionCheyCeGrossit = true
                                            mutableBasesStates
                                                ?.positionProduitDonGrossistChoisiPourAcheterCeProduit =
                                                newPosition
                                        }
                                        updateProduit(product, viewModelProduits)
                                        onDismiss()
                                    },
                                    modifier = Modifier.animateItem(
                                        fadeInSpec = null,
                                        fadeOutSpec = null
                                    ),
                                )
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            focusManager.clearFocus()
                            onDismiss()
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}
