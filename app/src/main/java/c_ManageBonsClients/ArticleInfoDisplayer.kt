package c_ManageBonsClients

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import b_Edite_Base_Donne.capitalizeFirstLetter

@Composable
fun DisplayDetailleArticle(
    article: ArticlesAcheteModele,
    currentChangingField: String,
    onValueOutlineChange: (String) -> Unit,
    focusRequester: FocusRequester,
    firebaseArticle: ArticlesAcheteModele?,
) {

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .wrapContentSize()
            .padding(4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            InformationsChanger(
                article = article,
                currentChangingField = currentChangingField,
                onValueChange = onValueOutlineChange,
                focusRequester = focusRequester,
                firebaseArticle = firebaseArticle
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = capitalizeFirstLetter(article.nomArticleFinale),
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun InformationsChanger(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    firebaseArticle: ArticlesAcheteModele?,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            ColumnBenifices(article, onValueChange, currentChangingField, modifier = Modifier.weight(1f))
            ColumnPVetPa(article, onValueChange, currentChangingField, modifier = Modifier.weight(1f), focusRequester = focusRequester,
                firebaseArticle = firebaseArticle)
        }
        CardFireStor(article, onValueChange, currentChangingField, modifier = Modifier.height(140.dp))

        RowAutresInfo(article, onValueChange, currentChangingField)
    }
}



@Composable
private fun CardFireStor(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier,
) {
    var isAnyChildFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isAnyChildFocused) 2.dp else 0.dp,
                color = if (isAnyChildFocused) Color.Red else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(modifier = Modifier.padding(3.dp)) {
            Row(modifier = Modifier.weight(1f)) {
                OutlineTextEditeRegle(
                    columnToChange = "monBenificeUniterFireStoreBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )

                OutlineTextEditeRegle(
                    columnToChange = "clientBenificeFireStoreBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.2f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )

                OutlineTextEditeRegle(
                    columnToChange = "monBenificeFireStoreBM",
                    abbreviation = "mBF",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(modifier = Modifier.weight(1f)) {
                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentUniterFireStoreBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.5f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )

                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentFireStoreBM",
                    abbreviation = "mpVF",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.5f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )
            }
        }
    }
}

@Composable
private fun CardFireBase(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier,
) {
    var isAnyChildFocused by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isAnyChildFocused) 2.dp else 0.dp,
                color = if (isAnyChildFocused) Color.Red else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(modifier = Modifier.padding(3.dp)) {
            Row(modifier = Modifier.weight(1f)) {
                OutlineTextEditeRegle(
                    columnToChange = "monBenificeUniterFireStoreBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )

                OutlineTextEditeRegle(
                    columnToChange = "clientBenificeFireStoreBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.2f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )

                OutlineTextEditeRegle(
                    columnToChange = "monBenificeFireStoreBM",
                    abbreviation = "mBF",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(modifier = Modifier.weight(1f)) {
                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentUniterFireStoreBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.5f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )

                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentFireStoreBM",
                    abbreviation = "mpVF",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.5f)
                        .onFocusChanged { isAnyChildFocused = it.isFocused }
                )
            }
        }
    }
}


@Composable
private fun ColumnPVetPa(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    firebaseArticle: ArticlesAcheteModele?,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            OutlineTextEditeRegle(
                columnToChange = "monPrixVentUniterBC",
                abbreviation = "/U",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged)
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.4f)
                    .height(67.dp)
            )

            if (firebaseArticle != null) {
                OutlineTextEditeFromFireStore(
                    columnToChange = "monPrixVentBons",
                    abbreviation = "mpV",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = firebaseArticle,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(67.dp),
                    focusRequester = focusRequester,
                )
            }
        }
        Row {
            OutlineTextEditeRegle(
                columnToChange = "monPrixVentUniterBC",
                abbreviation = "/U",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged, )
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.4f)
                    .height(67.dp)
            )

            OutlineTextEditeRegle(
                columnToChange = "monPrixVentBons",
                abbreviation = "mpV",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged, )
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.6f)
                    .height(67.dp)
            )
        }
        Row {
            OutlineTextEditeRegle(
                columnToChange = "monPrixAchatUniterBC",
                abbreviation = "/U",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged, )
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.4f)
                    .height(67.dp)
            )

            OutlineTextEditeRegle(
                columnToChange = "prixAchat",
                abbreviation = "mpA",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged, )
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.71f)
                    .height(67.dp)
            )
        }
    }
}

@Composable
fun OutlineTextEditeFromFireStore(
    columnToChange: String,
    abbreviation: String,
    labelCalculated: String = "",
    currentChangingField: String,
    article: ArticlesAcheteModele,
    modifier: Modifier = Modifier,
    calculateOthersRelated: (String, String) -> Unit,
    focusRequester: FocusRequester? = null,
) {
    var textFieldValue by remember { mutableStateOf((article.getColumnValue(columnToChange) as? Double)?.toString() ?: "") }


    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    val labelValue = when {
        labelCalculated.isNotEmpty() -> labelCalculated
        else -> (article.getColumnValue(columnToChange) as? Double)?.toString() ?: ""
    }
    val roundedValue = try {
        val doubleValue = labelValue.toDouble()
        if (doubleValue % 1 == 0.0) {
            doubleValue.toInt().toString()
        } else {
            String.format("%.1f", doubleValue)
        }
    } catch (e: NumberFormatException) {
        labelValue
    }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 3.dp)
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                calculateOthersRelated(columnToChange, newValue)
            },
            label = {
                AutoResizedTextBC(
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
                .height(65.dp)
                .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            )
        )
    }
}

@Composable
private fun RowAutresInfo(
    article: ArticlesAcheteModele,
    onValueChange: (String,) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
    Row {
        OutlineTextEditeRegle(
            columnToChange = "clientPrixVentUnite",
            abbreviation = "cVU",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueChange(columnChanged)
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)

        )
        OutlineTextEditeRegle(
            columnToChange = "nmbrunitBC",
            abbreviation = "nu",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueChange(columnChanged)
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(1f)
                .height(67.dp)

        )

    }

}


/**
 * contien outlines de benifice
 */
@Composable
private fun ColumnBenifices(
    article: ArticlesAcheteModele,
    onValueChange: (String,) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
    Column (modifier = modifier.fillMaxWidth()){
        Row {
            OutlineTextEditeRegle(
                columnToChange = "benificeDivise",
                abbreviation = "b/2",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.4f)
                    .height(67.dp)
            )

            OutlineTextEditeRegle(
                columnToChange = "benificeClient",
                abbreviation = "bC",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged, )
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.6f)
                    .height(67.dp)
            )
        }
        Row {
            OutlineTextEditeRegle(
                columnToChange = "monBenificeUniterBC",
                abbreviation = "/U",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged, )
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.4f)
                    .height(67.dp)
            )

            OutlineTextEditeRegle(
                columnToChange = "monBenificeBC",
                abbreviation = "mB",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged, )
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.6f)
                    .height(67.dp)
            )
        }
        Row {
            OutlineTextEditeRegle(
                columnToChange = "monPrixAchatUniterBC",
                abbreviation = "/U",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged)
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.4f)
                    .height(67.dp)
            )

            OutlineTextEditeRegle(
                columnToChange = "prixAchat",
                abbreviation = "mpA",
                calculateOthersRelated = { columnChanged, newValue ->
                    onValueChange(columnChanged)
                    updateRelatedFields(article, columnChanged, newValue)
                },
                currentChangingField = currentChangingField,
                article = article,
                modifier = Modifier
                    .weight(0.71f)
                    .height(67.dp)
            )
        }

    }
}

