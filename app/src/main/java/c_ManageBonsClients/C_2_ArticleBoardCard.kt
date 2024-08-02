package c_ManageBonsClients

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.AutoResizedText
import b_Edite_Base_Donne.capitalizeFirstLetter
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.io.File

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
                Card(
                    modifier = Modifier
                        .height(190.dp)
                        .clickable { onArticleSelect(article) }
                ) {
                    Column {

                        PriceOverlay(article.monPrixVentBM)

                        if (article.quantityAcheteCouleur2 + article.quantityAcheteCouleur3 + article.quantityAcheteCouleur4 == 0) {
                            SingleColorImage(article)
                        } else {
                            MultiColorGrid(article)
                        }

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


@Composable
private fun ArticleName(
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
            text = capitalizeFirstLetter(name),
            modifier = Modifier.padding(vertical = 4.dp),
            textAlign = TextAlign.Center,
            color = color
        )
    }
}

@Composable
private fun SingleColorImage(article: ArticlesAcheteModele) {
    Box(
    ) {
        val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
        LoadImageFromPathBC(imagePath = imagePath)
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
                        .height(60.dp)
                    ,
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
        modifier = Modifier
            .padding(4.dp),
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

