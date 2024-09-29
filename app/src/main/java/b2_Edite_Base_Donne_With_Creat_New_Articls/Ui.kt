package b2_Edite_Base_Donne_With_Creat_New_Articls

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import b_Edite_Base_Donne.AutoResizedText
import b_Edite_Base_Donne.BeneInfoBox
import b_Edite_Base_Donne.LoadImageFromPath
import b_Edite_Base_Donne.capitalizeFirstLetter

@Composable
fun ArticleDetailWindos(
    article: BaseDonneECBTabelle,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels
) {
    var displayeInOutlines by remember { mutableStateOf(true) }
    var currentChangingField by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Close button
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Main content
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        val allFields = listOf(
                            "clienPrixVentUnite" to "c.pU",
                            "nmbrCaron" to "n.c",
                            "nmbrUnite" to "n.u",
                            "monPrixAchatUniter" to "U/", "monPrixAchat" to "m.pA>", "benificeClient" to "b.c",
                            "monPrixVentUniter" to "u/", "monPrixVent" to "M.P.V"
                        )

                        // Top row fields
                        Row(modifier = Modifier.fillMaxWidth()) {
                            allFields.take(3).forEach { (column, abbr) ->
                                DisplayField(
                                    column, abbr, currentChangingField, article, viewModel, displayeInOutlines,
                                    Modifier
                                        .weight(1f)
                                        .height(67.dp)
                                ) { currentChangingField = column }
                            }
                        }

                        // Main content row
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Color cards
                            DisplayColorsCards(
                                article,
                                Modifier
                                    .weight(0.4f)
                                    .fillMaxHeight()
                            )

                            // Other fields and buttons
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .weight(0.6f)
                                    .fillMaxHeight()
                            ) {
                                allFields.drop(3).forEach { (column, abbr) ->
                                    DisplayField(
                                        column,
                                        abbr,
                                        currentChangingField,
                                        article,
                                        viewModel,
                                        displayeInOutlines,
                                        Modifier.fillMaxWidth()
                                    ) { currentChangingField = column }
                                }

                                if (article.clienPrixVentUnite > 0) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(55.dp)
                                            .padding(top = 5.dp)
                                    ) {
                                        BeneInfoBox("b.E2 -> ${article.benificeTotaleEn2}", Modifier.weight(1f))
                                        Spacer(modifier = Modifier.width(5.dp))
                                        BeneInfoBox(
                                            "b.EN -> ${article.benficeTotaleEntreMoiEtClien}",
                                            Modifier.weight(1f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                CalculationButtons(article, viewModel)
                                ArticleToggleButton(article, viewModel)
                            }
                        }

                        // Article name
                        Text(
                            text = capitalizeFirstLetter(article.nomArticleFinale),
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )

                        // Display mode switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Display in Outlines")
                            Spacer(Modifier.weight(1f))
                            Switch(checked = displayeInOutlines, onCheckedChange = { displayeInOutlines = it })
                        }
                    }
                }
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
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit
) {
    if (displayeInOutlines) {
        OutlineTextECB(
            columnToChange,
            abbreviation,
            currentChangingField,
            article,
            viewModel,
            modifier,
            onValueChanged
        )
    } else {
        val columnValue = article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
        InfoBoxWhithVoiceInpute("$abbreviation: $columnValue", modifier)
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
    onValueChanged: (String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf(article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: "") }
    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    val labelValue = article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
    val roundedValue = try {
        labelValue.toDouble()
            .let { if (it % 1 == 0.0) it.toInt().toString() else String.format("%.1f", it) }
    } catch (e: NumberFormatException) {
        labelValue
    }

    val keyboardController = LocalSoftwareKeyboardController.current

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
                modifier = Modifier.fillMaxWidth()
            )
        },
        textStyle = TextStyle(color = Color.Blue, textAlign = TextAlign.Center, fontSize = 14.sp),
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp),
        keyboardOptions = KeyboardOptions(
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

@Composable
fun InfoBoxWhithVoiceInpute(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.extraSmall
            )
            .padding(4.dp)
    ) {
        Text(text = text, modifier = Modifier.align(Alignment.Center), textAlign = TextAlign.Center)
    }
}

@Composable
fun CalculationButtons(
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            viewModel.updateAndCalculateAuthersField(
                (article.monPrixAchat / article.nmbrUnite).toString(),
                "monPrixAchat",
                article
            )
        }) { Text("/") }
        Button(onClick = {
            viewModel.updateAndCalculateAuthersField(
                (article.monPrixAchat * article.nmbrUnite).toString(),
                "monPrixAchat",
                article
            )
        }) { Text("*") }
    }
}

@Composable
fun ArticleToggleButton(article: BaseDonneECBTabelle, viewModel: HeadOfViewModels) {
    Button(
        onClick = { viewModel },
        colors = ButtonDefaults.buttonColors(containerColor = if (article.affichageUniteState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error),
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth()
    ) {
        Text(text = if (article.affichageUniteState) "Cacher les Unités" else "Afficher les Unités")
    }
}

@Composable
fun DisplayColorsCards(article: BaseDonneECBTabelle, modifier: Modifier = Modifier) {
    val couleursList = listOf(
        article.couleur1,
        article.couleur2,
        article.couleur3,
        article.couleur4
    ).filterNot { it.isNullOrEmpty() }
    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(3.dp)
            .fillMaxWidth()
    ) {
        itemsIndexed(couleursList) { index, couleur ->
            if (couleur != null) {
                Card(modifier = Modifier
                    .width(250.dp)
                    .height(300.dp)
                    .padding(end = 8.dp)) {
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
                            LoadImageFromPath(imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticleECB}_${index + 1}")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = couleur)
                    }
                }
            }
        }
    }
}
