package b2_Edite_Base_Donne_With_Creat_New_Articls

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import b_Edite_Base_Donne.AutoResizedText
import b_Edite_Base_Donne.LoadImageFromPath
import b_Edite_Base_Donne.capitalizeFirstLetter

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
fun ArticleDetailDialog(
    article: BaseDonneECBTabelle,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize(0.95f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    DisplayDetailleArticle(
                        article=article,
                        viewModel=viewModel,
                        )
                }
                Spacer(modifier = Modifier.height(16.dp))
                AutoResizedTextECB(
                    text = article.nomArticleFinale,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
@Composable
fun DisplayDetailleArticle(
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
) {
    var displayeInOutlines by remember { mutableStateOf(false) }
    var currentChangingField by remember { mutableStateOf("") }

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
                        viewModel = viewModel,
                        displayeInOutlines = displayeInOutlines,
                        modifier = Modifier
                            .weight(1f)
                            .height(67.dp),
                        onValueChanged = {
                            currentChangingField=columnToChange
                        }
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
                            viewModel = viewModel,
                            displayeInOutlines = displayeInOutlines,
                            modifier = Modifier.fillMaxWidth(),
                            onValueChanged = {
                                currentChangingField=columnToChange
                            }
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

                    CalculationButtons(article, viewModel)
                    ArticleToggleButton(article, viewModel)
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
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
    displayeInOutlines: Boolean,
    modifier: Modifier = Modifier ,
    onValueChanged: (String) -> Unit,
    ) {
    if (displayeInOutlines) {
        OutlineTextECB(
            columnToChange = columnToChange,
            abbreviation = abbreviation,
            onValueChanged = onValueChanged,
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        val columnValue = article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
        BeneInfoBox(
            text = "$abbreviation: $columnValue",
            modifier = modifier
        )
    }
}

@Composable
fun OutlineTextECB(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: "") }

    // Déterminer la valeur du champ texte
    val textValue = if (currentChangingField == columnToChange) {
        textFieldValue
    } else ""

    // Déterminer la valeur de l'étiquette
    val labelValue = article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
    val roundedValue = try {
        val doubleValue = labelValue.toDouble()
        if (doubleValue % 1 == 0.0) {
            doubleValue.toInt().toString()
        } else {
            String.format("%.1f", doubleValue)
        }
    } catch (e: NumberFormatException) {
        labelValue // Retourner la valeur initiale en cas d'exception
    }

    // Get the keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 3.dp)
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textFieldValue = newValue.replace(',', '.')
                viewModel.updateAndCalculateAuthersField(textFieldValue, columnToChange, article)
                onValueChanged(columnToChange)
            },
            label = {
                AutoResizedText(
                    text = "$abbreviation$roundedValue",
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            textStyle = TextStyle(
                color = Color.Blue,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(65.dp),
            visualTransformation = VisualTransformation.None, // Aucune transformation
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            )
        )
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
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
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
                viewModel.updateAndCalculateAuthersField(newPrice.toString(), "monPrixAchat", article)
            },
            text = "/"
        )
        CalculationButton(
            onClick = {
                val newPrice2 = article.monPrixAchat * article.nmbrUnite
                viewModel.updateAndCalculateAuthersField(newPrice2.toString(), "monPrixAchat", article)
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
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
) {
    Button(
        onClick = { viewModel },
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

@Composable
fun DisplayColorsCards(
    article: BaseDonneECBTabelle,
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
fun ColorCard(article: BaseDonneECBTabelle, index: Int, couleur: String) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(300.dp)
            .padding(end = 8.dp)
    ) {
        val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticleECB}_${index + 1}"
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




