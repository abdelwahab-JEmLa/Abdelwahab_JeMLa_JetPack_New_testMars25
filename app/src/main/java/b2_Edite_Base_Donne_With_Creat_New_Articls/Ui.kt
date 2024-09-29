package b2_Edite_Base_Donne_With_Creat_New_Articls


import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import b_Edite_Base_Donne.AutoResizedText
import b_Edite_Base_Donne.LoadImageFromPath

enum class FieldsDisplayer(val fields: List<Pair<String, String>>) {
    TOP_ROW(listOf("clienPrixVentUnite" to "c.pU", "nmbrCaron" to "n.c", "nmbrUnite" to "n.u")),
    PrixAchats(listOf("monPrixAchatUniter" to "U/", "monPrixAchat" to "m.pA>")),
    BenficesEntre(listOf("benificeTotaleEn2" to "b.E2", "benficeTotaleEntreMoiEtClien" to "b.EN")),
    Benfices(listOf("benificeClient" to "b.c")),
    MonPrixVent(listOf("monPrixVentUniter" to "u/", "monPrixVent" to "M.P.V"))
}

@Composable
fun ArticleDetailWindow(
    article: BaseDonneECBTabelle,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    modifier: Modifier
) {
    var displayeInOutlines by remember { mutableStateOf(true) }
    var currentChangingField by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Card(
                modifier = modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = modifier.fillMaxWidth()) {
                    DisplayColorsCards(article)

                    // TOP_ROW fields
                    Row(modifier = modifier.fillMaxWidth()) {
                        FieldsDisplayer.TOP_ROW.fields.forEach { (column, abbr) ->
                            DisplayField(
                                column, abbr, currentChangingField, article, viewModel, displayeInOutlines,
                                modifier
                                    .weight(1f)
                                    .height(67.dp)
                            ) { currentChangingField = column }
                        }
                    }

                    // Remaining FieldsDisplayer groups
                    FieldsDisplayer.values().drop(1).forEach { fieldsGroup ->
                        Row(modifier = modifier.fillMaxWidth()) {
                            fieldsGroup.fields.forEach { (column, abbr) ->
                                when (fieldsGroup) {
                                    FieldsDisplayer.BenficesEntre -> {
                                        if (article.clienPrixVentUnite > 0) {
                                            InfoBoxWhithVoiceInpute(
                                                "$abbr -> ${article.getColumnValue(column)}",
                                                modifier.weight(1f).padding(top = 6.dp).height(67.dp)
                                            )
                                        }
                                    }
                                    else -> {
                                        DisplayField(
                                            column, abbr, currentChangingField, article, viewModel, displayeInOutlines,
                                            modifier
                                                .weight(1f)
                                                .height(67.dp)
                                        ) { currentChangingField = column }
                                    }
                                }
                            }
                        }
                    }

                    CalculationButtons(article, viewModel, modifier)
                    ArticleToggleButton(article, viewModel, modifier)

                    // Article name
                    AutoResizedTextECB(
                        text = article.nomArticleFinale.capitalize(Locale.current),
                        fontSize = 25.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = modifier.fillMaxWidth()
                    )
                    // Display in Outlines switch
                    Row(
                        modifier = modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Display in Outlines")
                        Spacer(modifier.weight(1f))
                        Switch(checked = displayeInOutlines, onCheckedChange = { displayeInOutlines = it })
                    }
                }
            }
        }
    }
}

@Composable
fun AutoResizedTextECB(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize
) {
    var currentFontSize by remember { mutableStateOf(fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.capitalize(Locale.current),
            color = color,
            fontSize = currentFontSize,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.drawWithContent { if (readyToDraw) drawContent() },
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.didOverflowHeight) {
                    currentFontSize *= 0.9f
                } else {
                    readyToDraw = true
                }
            }
        )
    }
}

@Composable
fun InfoBoxWhithVoiceInpute(
    text: String,
    modifier: Modifier = Modifier
) {
    val parts = text.split("->")
    val abbreviation = parts.getOrNull(0)?.trim() ?: ""
    val value = parts.getOrNull(1)?.trim() ?: ""

    val roundedValue = try {
        value.toDouble().let { if (it % 1 == 0.0) it.toInt().toString() else String.format("%.1f", it) }
    } catch (e: NumberFormatException) {
        value
    }

    Box(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.extraSmall
            ),
        contentAlignment = Alignment.Center
    ) {
        AutoResizedTextECB(
            text = "$abbreviation -> $roundedValue",
            color = MaterialTheme.colorScheme.error
        )
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
            .fillMaxWidth()
    ) {
        itemsIndexed(couleursList) { index, couleur ->
            if (couleur != null) {
                Card(modifier = Modifier
                    .height(200.dp)
                   ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
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
                        AutoResizedTextECB(text = couleur)
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
fun CalculationButtons(
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            ,
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
fun ArticleToggleButton(
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
    modifier: Modifier
) {
    Button(
        onClick = { viewModel },
        colors = ButtonDefaults.buttonColors(containerColor = if (article.affichageUniteState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(text = if (article.affichageUniteState) "Cacher les Unités" else "Afficher les Unités")
    }
}

