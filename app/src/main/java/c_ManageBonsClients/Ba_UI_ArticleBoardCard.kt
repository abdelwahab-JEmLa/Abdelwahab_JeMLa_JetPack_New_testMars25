package c_ManageBonsClients

import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.PlacesOfArticelsInCamionette
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInbox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.AutoResizedText
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.LoadImageFromPathBC
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.roundToInt


@Composable
fun ArticleName(
    name: String,
    color: Color ,
    onNameClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNameClick)
    ) {
        AutoResizedText(
            text = name.capitalize(Locale.current),
            modifier = Modifier.padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
            color = color
        )
    }
}


@Composable
fun ArticleBoardCard(
    article: ArticlesAcheteModele,
    onClickNonTrouveState: (ArticlesAcheteModele) -> Unit,
    onArticleSelect: (ArticlesAcheteModele) -> Unit,
    isVerificationMode: Boolean,
    onClickVerificated: (ArticlesAcheteModele) -> Unit,
    modifier: Modifier = Modifier, headOfViewModels: HeadOfViewModels, // Add this line
) {
    val cardColor = when {
        article.nonTrouveState -> Color.Red
        article.verifieState -> Color.Yellow
        else -> Color.White
    }

    val textColor = if (!article.nonTrouveState ) Color.Black else Color.White
    var showPackagingDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.width(170.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = modifier.padding(2.dp)) {
            Column {
                PriceOverlay(
                    article= article,
                    price = roundToOneDecimal(article.monPrixVentBM),
                    monPrixVentFireStoreBM = roundToOneDecimal(article.monPrixVentFireStoreBM),
                    choisirePrixDepuitFireStoreOuBaseBM = article.choisirePrixDepuitFireStoreOuBaseBM,
                    monBenificeBM=roundToOneDecimal(article.monBenificeBM),
                    monBenificeFireStoreBM = roundToOneDecimal(article.monBenificeFireStoreBM ),
                    totalQuantity = article.totalQuantity,
                    achat = article.prixAchat,
                    modifier = modifier.fillMaxWidth()
                )

                // Image content
                Box(
                    modifier = modifier
                        .height(250.dp)
                        .clickable {
                            if (isVerificationMode) {
                                onClickVerificated(article)
                            } else {
                                onClickNonTrouveState(article)
                            }
                        }
                ){
                    if (article.quantityAcheteCouleur2 + article.quantityAcheteCouleur3 + article.quantityAcheteCouleur4 == 0) {
                        SingleColorImage(article)
                    } else {
                        MultiColorGrid(article)
                    }
                }

                Row {
                    var totalQuantityText by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = totalQuantityText,
                        onValueChange = { newValue ->
                            totalQuantityText = newValue
                            val newTotalQuantity = newValue.toIntOrNull() ?: 0
                            val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.vid.toString())
                            val articleUpdate = articleFromFireBase.child("totalQuantity")
                            articleUpdate.setValue(newTotalQuantity)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = article.totalQuantity.toString()) },
                        modifier = modifier.weight(0.3f)
                    )
                    ArticleName(
                        name = article.nomArticleFinale,
                        color = textColor,
                        onNameClick =  { onArticleSelect(article) },
                        modifier = modifier.weight(0.7f)
                    )
                }
            }
            Box(
                modifier = modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.Blue, CircleShape)
            ) {
                IconButton(
                    onClick = { showPackagingDialog = true },
                    modifier = modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInbox,
                        contentDescription = "Packaging Type",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    if (showPackagingDialog) {
        ShowPackagingDialog(
            headOfViewModels,
            article = article,
            onDismiss = { showPackagingDialog = false }
        )
    }
}

@Composable
fun PriceOverlay(
    price: Double,
    monPrixVentFireStoreBM: Double,
    choisirePrixDepuitFireStoreOuBaseBM: String,
    monBenificeBM: Double,
    monBenificeFireStoreBM: Double,
    totalQuantity: Int,
    modifier: Modifier = Modifier,
    achat: Double,
    article: ArticlesAcheteModele
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.7f))
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PriceWithProfit(
                article=article,
                label = "App",
                price = price,
                profit = monBenificeBM,
                totalQuantity = totalQuantity,
                isSelected = choisirePrixDepuitFireStoreOuBaseBM == "CardFireBase",
                achat=achat,
                modifier = Modifier.weight(1f)
            )

            if (monPrixVentFireStoreBM > 0) {
                PriceWithProfit(
                    label = "Hes",
                    price = monPrixVentFireStoreBM,
                    profit = monBenificeFireStoreBM,
                    totalQuantity = totalQuantity,
                    isSelected = choisirePrixDepuitFireStoreOuBaseBM == "CardFireStor",
                    modifier = Modifier.weight(1f),
                    achat = achat,
                    article = article
                )
            }
        }
    }
}


@Composable
fun PriceWithProfit(
    label: String,
    price: Double,
    profit: Double,
    totalQuantity: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    achat: Double,
    article: ArticlesAcheteModele
) {
    val textColor = when {
        article.warningRecentlyChanged -> Color.White
        isSelected -> Color.Red
        else -> Color.Black
    }
    val customPink = Color(0xFFFFC0CB) // Light pink color
    val backgroundColor = when {
        article.warningRecentlyChanged -> Color.Red
        isSelected && profit < 0 -> customPink.copy(alpha = 0.3f)
        isSelected -> Color.Yellow.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    val achatno = achat == 0.0
    val totalProfit = profit * totalQuantity

    Column(
        modifier = modifier
            .background(backgroundColor)
            .padding(2.dp)
    ) {
        AutoResizedText(
            text = label,
            textAlign = TextAlign.Start,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )

        AutoResizedText(
            text = "%.1f".format(price),
            textAlign = TextAlign.Start,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )
        if (!achatno) {
            AutoResizedText(
                text = "%.1f".format(profit),
                textAlign = TextAlign.Start,
                color = if (profit < 0 && !article.warningRecentlyChanged) Color.Red else textColor,
                modifier = Modifier.fillMaxWidth()
            )
            if (totalProfit != profit) {
                AutoResizedText(
                    text = "Total: %.1f".format(totalProfit),
                    textAlign = TextAlign.Start,
                    color = if (totalProfit < 0 && !article.warningRecentlyChanged) Color.Red else textColor,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
fun roundToOneDecimal(value: Double): Double {
    return (value * 10.0).roundToInt() / 10.0
}

@Composable
private fun SingleColorImage(article: ArticlesAcheteModele) {
    Card(
        modifier = Modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imagePathWithoutExt = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
            val imagePathWebp = "$imagePathWithoutExt.webp"
            val imagePathJpg = "$imagePathWithoutExt.jpg"
            val webpExists = File(imagePathWebp).exists()
            val jpgExists = File(imagePathJpg).exists()

            if (webpExists || jpgExists) {
                LoadImageFromPathBC(imagePath = imagePathWithoutExt, modifier = Modifier.fillMaxSize())
            } else {
                // Display rotated article name for empty articles
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = article.nomArticleFinale,
                        color = Color.Red,
                        modifier = Modifier
                            .rotate(45f)
                            .padding(4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!article.nomCouleur1.contains("Sta", ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = article.nomCouleur1,
                        color = Color.Red,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.6f))
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "${article.totalQuantity}",
                    color = Color.Red,
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun MultiColorGrid(article: ArticlesAcheteModele) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize()
    ) {
        val colorData = listOf(
            article.quantityAcheteCouleur1 to article.nomCouleur1,
            article.quantityAcheteCouleur2 to article.nomCouleur2,
            article.quantityAcheteCouleur3 to article.nomCouleur3,
            article.quantityAcheteCouleur4 to article.nomCouleur4
        )

        items(colorData.size) { index ->
            val (quantity, colorName) = colorData[index]
            if (quantity > 0) {
                ColorItemCard(article, index, quantity, colorName)
            }
        }
    }
}

@Composable
private fun ColorItemCard(article: ArticlesAcheteModele, index: Int, quantity: Int, colorName: String?) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxSize()
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF40E0D0) // Bleu turquoise
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val imagePathWithoutExt = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_${index + 1}"
            val imagePathWebp = "$imagePathWithoutExt.webp"
            val imagePathJpg = "$imagePathWithoutExt.jpg"
            val webpExists = File(imagePathWebp).exists()
            val jpgExists = File(imagePathJpg).exists()

            if (webpExists || jpgExists) {
                LoadImageFromPathBC(
                    imagePath = imagePathWithoutExt,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    text = colorName ?: "",
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(45f)
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(4.dp),
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = quantity.toString(),
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.6f))
                    .padding(4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
/**Places Dialoge*/
@Composable
fun ShowPackagingDialog(
    headOfViewModels: HeadOfViewModels,
    article: ArticlesAcheteModele,
    onDismiss: () -> Unit
) {
    val uiState by headOfViewModels.uiState.collectAsState()
    val placesOfArticelsInCamionette = uiState.placesOfArticelsInCamionette
    var showAddPlaceDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Packaging Type") },
        text = {
            Column {
                LazyColumn {
                    items(placesOfArticelsInCamionette) { place ->
                        PackagingToggleButton(
                            text = place.namePlace,
                            isSelected = place.idPlace == article.idArticlePlaceInCamionette,
                            onClick = {
                                coroutineScope.launch {
                                    headOfViewModels.updateArticlePackaging(article, place.idPlace)
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                FloatingActionButton(
                    onClick = { showAddPlaceDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("+")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )

    if (showAddPlaceDialog) {
        AddPlaceDialog(
            onDismiss = { showAddPlaceDialog = false },
            onAddPlace = { newPlace ->
                coroutineScope.launch {
                    headOfViewModels.addNewPlaceInCamionette(newPlace)
                }
                showAddPlaceDialog = false
            }
        )
    }
}

@Composable
fun PackagingToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}

@Composable
fun AddPlaceDialog(
    onDismiss: () -> Unit,
    onAddPlace: (PlacesOfArticelsInCamionette) -> Unit
) {
    var namePlace by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Place") },
        text = {
            OutlinedTextField(
                value = namePlace,
                onValueChange = { namePlace = it },
                label = { Text("Place Name") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (namePlace.isNotBlank()) {
                        onAddPlace(
                            PlacesOfArticelsInCamionette(
                            idPlace = 0, // This will be set by the database
                            namePlace = namePlace,
                            classement = 0 // This will be set by the ViewModel
                        )
                        )
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
fun updateTypeEmballage(article: ArticlesAcheteModele, newType: String) {
    val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.vid.toString())
    val articleUpdate = articleFromFireBase.child("typeEmballage")
    articleUpdate.setValue(newType)
}
