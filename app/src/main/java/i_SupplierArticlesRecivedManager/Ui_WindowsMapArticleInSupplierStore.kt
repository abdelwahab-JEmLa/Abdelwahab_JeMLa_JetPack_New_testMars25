package i_SupplierArticlesRecivedManager

import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.MapArticleInSupplierStore
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

//Title:WindowsMapArticleInSupplierStore
@Composable
fun WindowsMapArticleInSupplierStore(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    idSupplierOfFloatingButtonClicked: Long?,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showNonPlacedArticles by remember { mutableStateOf<MapArticleInSupplierStore?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val Places = uiState.mapArticleInSupplierStore .filter {
                            it.idSupplierOfStore ==   idSupplierOfFloatingButtonClicked
                        }

                        items(Places) { placeItem ->
                            CardDisplayerOfPlace(
                                uiState = uiState,
                                placeItem=placeItem,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(4.dp)
                                ,
                                viewModel = viewModel,
                                onDismiss = { showNonPlacedArticles = null }   ,
                                onClickToDisplayNonPlaced = {showNonPlacedArticles=it}
                            )
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Place")
                }
            }
        }
    }
    showNonPlacedArticles?.let { place ->
        WindowsOfNonPlacedArticles(
            uiState = uiState,
            onDismiss = { showNonPlacedArticles = null },
            modifier = Modifier,
            gridColumns = 2,
            place = place,
            viewModel = viewModel
        )
    }
    if (showAddDialog) {
        AddPlaceDialog(
            onDismiss = { showAddDialog = false },
            onAddPlace = { name ->
                viewModel.addNewPlace(name, idSupplierOfFloatingButtonClicked)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CardDisplayerOfPlace(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    modifier: Modifier = Modifier,
    viewModel: HeadOfViewModels,
    onDismiss: () -> Unit,
    placeItem: MapArticleInSupplierStore,
    onClickToDisplayNonPlaced: (MapArticleInSupplierStore) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClickToDisplayNonPlaced(placeItem) },
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = placeItem.namePlace,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (placeItem.inRightOfPlace) "Right" else "Left",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                val articlesForThisPlace = articleFilter(uiState, placeItem)

                items(articlesForThisPlace) { article ->
                    ArticleItemOfPlace(
                        article = article,
                        viewModel = viewModel,
                        onDismiss = onDismiss
                    )
                    HorizontalDivider(
                        modifier = Modifier,
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

private fun articleFilter(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    placeItem: MapArticleInSupplierStore
) = uiState.tabelleSupplierArticlesRecived.filter { article ->
    uiState.placesOfArticelsInEacheSupplierSrore.any { place ->
        place.idCombinedIdArticleIdSupplier == "${article.a_c_idarticle_c}_${article.idSupplierTSA}" &&
                place.idPlace == placeItem.idPlace
    }
}


@Composable
fun ArticleItemOfPlace(
    article: TabelleSupplierArticlesRecived,
    viewModel: HeadOfViewModels,
    onDismiss: () -> Unit
) {
    var showArticleDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { showArticleDetails = true },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = article.a_d_nomarticlefinale_c, style = MaterialTheme.typography.bodyLarge)
            Text(text = "ID: ${article.a_c_idarticle_c}", style = MaterialTheme.typography.bodySmall)
        }
    }

    if (showArticleDetails) {
        WindowArticleDetail(
            article = article,
            onDismissWithUpdate = {showArticleDetails=false
                onDismiss()},
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
    }
}

@Composable
fun WindowsOfNonPlacedArticles(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    onDismiss: () -> Unit,
    modifier: Modifier,
    gridColumns: Int,
    viewModel: HeadOfViewModels,
    place: MapArticleInSupplierStore,
) {
    val gridState = rememberLazyGridState()
    var searchText by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = { Text("Search Articles") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            state = gridState,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val articlesSupplier = if (searchText.isEmpty()) {
                                uiState.tabelleSupplierArticlesRecived.filter { article ->
                                    article.idSupplierTSA.toLong() == place.idSupplierOfStore &&
                                            !uiState.placesOfArticelsInEacheSupplierSrore.any { placedArticle ->
                                                placedArticle.idCombinedIdArticleIdSupplier == "${article.a_c_idarticle_c}_${article.idSupplierTSA}"
                                            }
                                }
                            } else {
                                uiState.tabelleSupplierArticlesRecived.filter { article ->
                                    article.a_d_nomarticlefinale_c.contains(searchText, ignoreCase = true)
                                }
                            }

                            items(articlesSupplier) { article ->
                                ArticleItem(
                                    article = article,
                                    onDismissWithUpdate = { clickedArticle ->
                                        val idCombinedIdArticleIdSupplier = "${clickedArticle.a_c_idarticle_c}_${place.idSupplierOfStore}"
                                        viewModel.addOrUpdatePlacesOfArticelsInEacheSupplierSrore(
                                            idCombinedIdArticleIdSupplier = idCombinedIdArticleIdSupplier,
                                            placeId = place.idPlace,
                                            idArticle = clickedArticle.a_c_idarticle_c,
                                            idSupp = place.idSupplierOfStore
                                        )
                                        viewModel.moveArticleNonFindToSupplier(
                                            listOf(clickedArticle),  // Wrap the single article in a list
                                            place.idSupplierOfStore
                                        )
                                        onDismiss()
                                    },
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun ArticleItem(
    article: TabelleSupplierArticlesRecived,
    onDismissWithUpdate: (TabelleSupplierArticlesRecived) -> Unit,
    viewModel: HeadOfViewModels,
) {
    var showNonPlacedAricles by remember { mutableStateOf<TabelleSupplierArticlesRecived?>(null)  }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .clickable { showNonPlacedAricles = article },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = article.a_d_nomarticlefinale_c, style = MaterialTheme.typography.bodyLarge)
            Text(text = "ID: ${article.a_c_idarticle_c}", style = MaterialTheme.typography.bodySmall)
        }
    }
    showNonPlacedAricles?.let { articleDisplaye ->
        WindowArticleDetail(
            article = articleDisplaye,
            onDismissWithUpdate = {
                onDismissWithUpdate(articleDisplaye)
                showNonPlacedAricles=null
            },
            viewModel = viewModel,
            modifier = Modifier.padding(horizontal = 3.dp),
        )
    }
}


@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onAddPlace: (String) -> Unit
) {
    var newPlaceName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Place") },
        text = {
            Column {
                OutlinedTextField(
                    value = newPlaceName,
                    onValueChange = { newPlaceName = it },
                    label = { Text("Place Name") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAddPlace(newPlaceName) }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

