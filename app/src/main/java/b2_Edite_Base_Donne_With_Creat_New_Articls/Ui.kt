package b2_Edite_Base_Donne_With_Creat_New_Articls

import a_RoomDB.BaseDonne
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import b_Edite_Base_Donne.BaseDonneStatTabel
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import b_Edite_Base_Donne.LoadImageFromPath
import b_Edite_Base_Donne.OutlineTextEditeBaseDonne
import b_Edite_Base_Donne.capitalizeFirstLetter
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.abdelwahabjemlajetpack.R
import java.io.File

@Composable
fun CategoryHeaderECB(
    category: CategoriesTabelleECB,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Text(
            text = category.nomCategorieInCategoriesTabele,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}


@Composable  fun OverlayContentECB(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White)
    }
}
@Composable fun DisponibilityOverlayECB(state: String) {
    when (state) {
        "Non Dispo" -> OverlayContentECB(color = Color.Black, icon = Icons.Default.TextDecrease)
        "NonForNewsClients" -> OverlayContentECB(color = Color.Gray, icon = Icons.Default.Person)
    }
}
@Composable
fun DisplayDetailleArticle(
    article: BaseDonneStatTabel,
    articlesDataBaseDonne: BaseDonne?,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    currentChangingField: String,
    onValueChanged: (String) -> Unit,
    onUniteToggleClick: (BaseDonne?) -> Unit,
) {
    var displayeInOutlines by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val allFields = listOf(
                "clienPrixVentUnite" to "c.pU",
                "nmbrCaron" to "n.c",
                "nmbrUnite" to "n.u",
                "monPrixAchatUniter" to "U/",
                "monPrixAchat" to "m.pA>",
                "benificeClient" to "b.c",
                "monPrixVentUniter" to "u/",
                "monPrixVent" to "M.P.V"
            )

            // Display top row fields
            Row(modifier = Modifier.fillMaxWidth()) {
                allFields.take(3).forEach { (columnToChange, abbreviation) ->
                    DisplayField(
                        columnToChange = columnToChange,
                        abbreviation = abbreviation,
                        currentChangingField = currentChangingField,
                        article = article,
                        viewModel = editeBaseDonneViewModel,
                        onValueChanged = onValueChanged,
                        displayeInOutlines = displayeInOutlines,
                        modifier = Modifier
                            .weight(1f)
                            .height(67.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DisplayColorsCards(article, Modifier.weight(0.38f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.62f)
                ) {
                    // Display remaining fields
                    allFields.drop(3).forEach { (columnToChange, abbreviation) ->
                        DisplayField(
                            columnToChange = columnToChange,
                            abbreviation = abbreviation,
                            currentChangingField = currentChangingField,
                            article = article,
                            viewModel = editeBaseDonneViewModel,
                            onValueChanged = onValueChanged,
                            displayeInOutlines = displayeInOutlines,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Display additional information
                    if (article.clienPrixVentUnite > 0) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .padding(top = 5.dp)
                        ) {
                            BeneInfoBox(
                                text = "b.E2 -> ${article.benificeTotaleEn2}",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            BeneInfoBox(
                                text = "b.EN -> ${article.benficeTotaleEntreMoiEtClien}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    CalculationButtons(article, editeBaseDonneViewModel, onValueChanged)
                    ArticleToggleButton(articlesDataBaseDonne, editeBaseDonneViewModel, onUniteToggleClick)
                }
            }

            Text(
                text = capitalizeFirstLetter(article.nomArticleFinale),
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp)
            )

            // Add a switch to toggle displayeInOutlines
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Display in Outlines")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = displayeInOutlines,
                    onCheckedChange = { displayeInOutlines = it }
                )
            }
        }
    }
}
@Composable
fun DisplayField(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    onValueChanged: (String) -> Unit,
    displayeInOutlines: Boolean,
    modifier: Modifier = Modifier
) {
    if (displayeInOutlines) {
        OutlineTextEditeBaseDonne(
            columnToChange = columnToChange,
            abbreviation = abbreviation,
            onValueChanged = onValueChanged,
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        BeneInfoBox(
            text = "$abbreviation: ${getArticleValue(article, columnToChange)}",
            modifier = modifier
        )
    }
}




// Update the getArticleValue function to include the new fields
fun getArticleValue(article: BaseDonneStatTabel, columnName: String): String {
    return when (columnName) {
        "clienPrixVentUnite" -> article.clienPrixVentUnite.toString()
        "nmbrCaron" -> article.nmbrCaron.toString()
        "nmbrUnite" -> article.nmbrUnite.toString()
        "monPrixAchatUniter" -> article.monPrixAchatUniter.toString()
        "monPrixAchat" -> article.monPrixAchat.toString()
        "monPrixVentUniter" -> article.monPrixVentUniter.toString()
        "monPrixVent" -> article.monPrixVent.toString()
        "benificeClient" -> article.benificeClient.toString()
        // Add other fields as needed
        else -> ""
    }
}

// Make sure to include this composable if it's not already defined
@Composable
fun BeneInfoBox(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.extraSmall)
            .padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun CalculationButtons(
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    onValueChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CalculationButton(
            onClick = {
                val newPrice = article.monPrixAchat / article.nmbrUnite
                onValueChanged("monPrixAchat")
                viewModel.updateCalculated(newPrice.toString(), "monPrixAchat", article)
            },
            text = "/"
        )
        CalculationButton(
            onClick = {
                val newPrice2 = article.monPrixAchat * article.nmbrUnite
                onValueChanged("monPrixAchat")
                viewModel.updateCalculated(newPrice2.toString(), "monPrixAchat", article)
            },
            text = "*"
        )
    }
}

@Composable
fun CalculationButton(onClick: () -> Unit, text: String) {
    Button(
        onClick = onClick,
    ) {
        Text(text)
    }
}

@Composable
fun ArticleToggleButton(
    article: BaseDonne?,
    viewModel: EditeBaseDonneViewModel,
    onClickUniteToggleButton: (BaseDonne?) -> Unit,
) {
    article?.let {
        Button(
            onClick = { onClickUniteToggleButton(article) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (article.affichageUniteState)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (article.affichageUniteState)
                    "Cacher les Unités"
                else
                    "Afficher les Unités"
            )
        }
    }
}

@Composable
fun DisplayColorsCards(
    article: BaseDonneStatTabel,
    modifier: Modifier = Modifier
) {
    val couleursList = listOf(
        article.couleur1,
        article.couleur2,
        article.couleur3,
        article.couleur4,
    ).filterNot { it.isNullOrEmpty() }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(3.dp)
            .fillMaxWidth()
    ) {
        itemsIndexed(couleursList) { index, couleur ->
            if (couleur != null) {
                ColorCard(article, index, couleur)
            }
        }
    }
}

@Composable
fun ColorCard(article: BaseDonneStatTabel, index: Int, couleur: String) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(300.dp)
            .padding(end = 8.dp)
    ) {
        val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_${index + 1}"
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            ) {
                LoadImageFromPath(imagePath = imagePath)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = couleur)
        }
    }
}
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoadImageFromPath(imagePath: String, modifier: Modifier = Modifier) {
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

// Helper function
fun capitalizeFirstLetter(text: String): String {
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}


