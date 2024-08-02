package c_ManageBonsClients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.abdelwahabjemlajetpack.R
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoadImageFromPathBC(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.blanc

    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = imageExist ?: defaultDrawable,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
        ) {
            it
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(1000) // Set a larger size
                .thumbnail(0.25f) // Start with 25% quality
                .fitCenter() // Ensure the image fits within the bounds
                .transition(DrawableTransitionOptions.withCrossFade()) // Smooth transition as quality improves
        }
    }
}




@Composable
fun ArticleName(
    name: String,
    color: Color,
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
            color = Color.Red
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
) {
    val cardColor = when {
        article.nonTrouveState -> Color.Red
        article.verifieState -> Color.Yellow
        else -> Color.White
    }
    val textColor = if (!article.nonTrouveState) Color.Black else Color.White
    var showPackagingDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(170.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.padding(2.dp)) {
            Column {
                // Price overlay at the top of the column
                PriceOverlay(
                    price = roundToOneDecimal(article.monPrixVentBM),
                    monPrixVentFireStoreBM = roundToOneDecimal(article.monPrixVentFireStoreBM),
                    choisirePrixDepuitFireStoreOuBaseBM = article.choisirePrixDepuitFireStoreOuBaseBM,
                    clientBenificeBM = roundToOneDecimal(article.monBenificeBM),
                    clientBenificeFireStoreBM = roundToOneDecimal(article.monBenificeUniterFireStoreBM),
                    modifier = Modifier.fillMaxWidth()
                )

                // Image content
                Box(
                    modifier = Modifier
                        .height(250.dp)
                        .clickable { onArticleSelect(article) }
                ) {
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
                            val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
                            val articleUpdate = articleFromFireBase.child("totalQuantity")
                            articleUpdate.setValue(newTotalQuantity)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text(text = article.totalQuantity.toString()) },
                        modifier = Modifier.weight(0.3f)
                    )
                    ArticleName(
                        name = article.nomArticleFinale,
                        color = textColor,
                        onNameClick = {
                            if (isVerificationMode) {
                                onClickVerificated(article)
                            } else {
                                onClickNonTrouveState(article)
                            }
                        },
                        modifier = Modifier.weight(0.7f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.BottomEnd)
                    .background(Color.Blue, CircleShape)
            ) {
                IconButton(
                    onClick = { showPackagingDialog = true },
                    modifier = Modifier.fillMaxSize()
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
    clientBenificeBM: Double,
    clientBenificeFireStoreBM: Double,
    modifier: Modifier = Modifier
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
                label = "App",
                price = price,
                profit = clientBenificeBM,
                isSelected = choisirePrixDepuitFireStoreOuBaseBM == "CardFireBase",
                modifier = Modifier.weight(1f)
            )

            if (monPrixVentFireStoreBM > 0) {
                PriceWithProfit(
                    label = "Hes",
                    price = monPrixVentFireStoreBM,
                    profit = clientBenificeFireStoreBM,
                    isSelected = choisirePrixDepuitFireStoreOuBaseBM == "CardFireStor",
                    modifier = Modifier.weight(1f)
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
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val textColor = if (isSelected) Color.Red else Color.Black
    val backgroundColor = if (isSelected) Color.Yellow.copy(alpha = 0.3f) else Color.Transparent

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
        AutoResizedText(
            text = "%.1f".format(profit),
            textAlign = TextAlign.Start,
            color = textColor,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun roundToOneDecimal(value: Double): Double {
    return (value * 10.0).roundToInt() / 10.0
}

@Composable
private fun SingleColorImage(article: ArticlesAcheteModele) {
    Box(modifier = Modifier.fillMaxSize()) {
        val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
        LoadImageFromPathBC(imagePath = imagePath, modifier = Modifier.fillMaxSize())

        if (!article.nomCouleur1.contains("Sta", ignoreCase = true)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp), // Add some padding at the bottom
                contentAlignment = Alignment.BottomCenter // Align content to bottom center
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
        }
    }
}

@Composable
private fun PriceOverlay(price: Double) {
    Box(
        modifier = Modifier.padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.7f))
                .padding(4.dp)
        ) {
            AutoResizedText(
                text = "Pv>$price",
                textAlign = TextAlign.Center,
                color = Color.Red,
            )
        }
    }
}


// The ShowPackagingDialog and updateTypeEmballage functions remain the same
@Composable
fun ShowPackagingDialog(
    article: ArticlesAcheteModele,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Packaging Type") },
        text = {
            Column {
                PackagingToggleButton("Carton", article.typeEmballage == "Carton") {
                    updateTypeEmballage(article, "Carton")
                    onDismiss()
                }
                PackagingToggleButton("Boit", article.typeEmballage == "Boit") {
                    updateTypeEmballage(article, "Boit")
                    onDismiss()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PackagingToggleButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Red else Color.Green
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(text)
    }
}

fun updateTypeEmballage(article: ArticlesAcheteModele, newType: String) {
    val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    val articleUpdate = articleFromFireBase.child("typeEmballage")
    articleUpdate.setValue(newType)
}