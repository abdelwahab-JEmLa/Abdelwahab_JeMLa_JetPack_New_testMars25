package c_ManageBonsClients

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
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
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.Firebase
import com.google.firebase.database.database

@Composable
fun DisplayDetailleArticle(
    article: ArticlesAcheteModele,
    currentChangingField: String,
    onValueOutlineChange: (String) -> Unit,
    focusRequester: FocusRequester,
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
            )

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
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val cardHeight = remember(article) {
            val shouldDisplayTotalProfitFireBase = article.monBenificeBM != article.monBenificeBM * article.totalQuantity
            val shouldDisplayTotalProfitFireStor = article.monBenificeFireStoreBM != article.monBenificeFireStoreBM * article.totalQuantity
            if (shouldDisplayTotalProfitFireBase || shouldDisplayTotalProfitFireStor) 180.dp else 160.dp
        }

        CombinedCard(
            article = article,
            onValueChange = onValueChange,
            currentChangingField = currentChangingField,
            cardType = "CardFireBase",
            onCardFocused = {
                updateChoisirePrixDepuitFireStoreOuBaseBM(article, "CardFireBase")
            },
            modifier = Modifier.height(cardHeight)
        )
        CombinedCard(
            article = article,
            onValueChange = onValueChange,
            currentChangingField = currentChangingField,
            cardType = "CardFireStor",
            onCardFocused = {
                updateChoisirePrixDepuitFireStoreOuBaseBM(article, "CardFireStor")
            },
            focusRequester = focusRequester,
            modifier = Modifier.height(cardHeight)
        )
        RowAutresInfo(article, onValueChange, currentChangingField)
    }
}
fun updateChoisirePrixDepuitFireStoreOuBaseBM(article: ArticlesAcheteModele, newType: String) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.vid.toString())
    articleRef.child("choisirePrixDepuitFireStoreOuBaseBM").setValue(newType)

}
@Composable
fun CombinedCard(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    cardType: String,
    onCardFocused: () -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    val isFireStor = cardType == "CardFireStor"
    var isCardFocused by remember { mutableStateOf(false) }
    var wasEverFocused by remember { mutableStateOf(false) }

    data class FieldInfo(
        val columnToChange: String,
        val abbreviation: String? = "",
        val weight: Float,
        val useFocusRequester: Boolean = false
    )

    val fields = when (cardType) {
        "CardFireBase" -> listOf(
            FieldInfo("clientBenificeBM", "cB", 0.4f),
            FieldInfo("monBenificeUniterBM", weight = 0.2f),
            FieldInfo("monBenificeBM", "mB", 0.4f),
            FieldInfo("monPrixVentUniterBM", weight = 0.4f),
            FieldInfo("monPrixVentBM", "mpV", 0.6f)
        )
        "CardFireStor" -> listOf(
            FieldInfo("clientBenificeFireStoreBM", "cBF", 0.4f),
            FieldInfo("monBenificeUniterFireStoreBM", weight = 0.2f),
            FieldInfo("monBenificeFireStoreBM", "mBF", 0.4f),
            FieldInfo("monPrixVentUniterFireStoreBM", weight = 0.4f),
            FieldInfo("monPrixVentFireStoreBM", "mpVF", 0.6f, true)
        )
        else -> emptyList()
    }

    val isChosenCard = article.choisirePrixDepuitFireStoreOuBaseBM == cardType
    val cardColor = when {
        isChosenCard -> Color.Red
        else -> Color.White
    }

    val textColor = if (isChosenCard) Color.Black else Color.Blue

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (isChosenCard) 2.dp else 0.dp,
                color = if (isChosenCard) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Rotated text inside the Card
            Box(
                modifier = Modifier
                    .width(45.dp)
                    .fillMaxHeight()
            ) {
                Text(
                    text = if (isFireStor) "Historique" else "App",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier
                        .rotate(-90f)
                        .align(Alignment.Center),
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }

            // Content column
            Column(modifier = Modifier.weight(1f).padding(3.dp)) {
                // Calculate total profit
                val totalProfit = if (isFireStor) {
                    article.monBenificeFireStoreBM * article.totalQuantity
                } else {
                    article.monBenificeBM * article.totalQuantity
                }

                // Check if monBenificeBM is not equal to total benefit
                val shouldDisplayTotalProfit = if (isFireStor) {
                    article.monBenificeFireStoreBM != totalProfit
                } else {
                    article.monBenificeBM != totalProfit
                }

                if (shouldDisplayTotalProfit) {
                    Text(
                        text = "Total Profit: ${String.format("%.2f", totalProfit)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }

                fields.chunked(3).forEachIndexed { rowIndex, rowFields ->
                    if (rowIndex > 0) {
                        Spacer(modifier = Modifier.height(3.dp))
                    }
                    Row(modifier = Modifier.weight(1f)) {
                        rowFields.forEach { field ->
                            OutlineTextEditeRegle(
                                columnToChange = field.columnToChange,
                                abbreviation = field.abbreviation,
                                calculateOthersRelated = { columnChanged, newValue ->
                                    onCardFocused()
                                    onValueChange(columnChanged)
                                    updateRelatedFields(article, columnChanged, newValue)
                                },
                                currentChangingField = currentChangingField,
                                article = article,
                                modifier = Modifier
                                    .weight(field.weight)
                                    .onFocusChanged { focusState ->
                                        if (isFireStor) {
                                            if (focusState.isFocused && !isCardFocused) {
                                                isCardFocused = true
                                                if (wasEverFocused) {
                                                    onCardFocused()
                                                }
                                                wasEverFocused = true
                                            } else if (!focusState.isFocused && isCardFocused) {
                                                isCardFocused = false
                                            }
                                        } else {
                                            if (focusState.isFocused) {
                                                onCardFocused()
                                            }
                                        }
                                    },
                                focusRequester = if (field.useFocusRequester) focusRequester else null,
                                textColor = textColor,
                                isChosenCard = isChosenCard
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun RowAutresInfo(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
    data class FieldInfo(
        val columnToChange: String,
        val abbreviation: String,
        val weight: Float,
        val updateRelated: Boolean
    )

    val fields = listOf(
        FieldInfo("clientPrixVentUnite", "cVU", 0.20f, true),
        FieldInfo("nmbrunitBC", "nu", 0.20f, true),
        FieldInfo("monPrixAchatUniterBC", "", 0.20f, true),
        FieldInfo("prixAchat", "pA", 0.40f, true)
    )

    Column(modifier = modifier) {
        Row {
            fields.forEach { field ->
                OutlineTextEditeRegle(
                    columnToChange = field.columnToChange,
                    abbreviation = field.abbreviation,
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        if (field.updateRelated) {
                            updateRelatedFields(article, columnChanged, newValue)
                        }
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(field.weight)
                        .height(67.dp)
                )
            }
        }

        OutlineTextEditeRegle(
            columnToChange = "nomArticleFinale",
            abbreviation = "",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueChange(columnChanged)
                updateNomArticleFinale(article, columnChanged, newValue)
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .fillMaxWidth()
                .height(67.dp),
            colore = Color.Red ,
            isText = true,
        )
    }
}

fun updateNomArticleFinale(article: ArticlesAcheteModele, columnChanged: String, newValue: String) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.vid.toString())
    articleRef.child(columnChanged).setValue(newValue)

    // Check if total quantity is 0 and update verifieState accordingly
    if (article.totalQuantity == 0) {
        articleRef.child("verifieState").setValue(true)
    }
}

fun updateRelatedFields(ar: ArticlesAcheteModele, columnChanged: String, newValue: String) {
    val newValueDouble = newValue.toDoubleOrNull() ?: return

    up(columnChanged, newValueDouble.toString(), ar.vid)

    when (columnChanged) {
        "clientBenificeBM" -> {
            up("benificeDivise", ((newValueDouble / ar.nmbrunitBC) - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.vid)
            up("monBenificeUniterBM", (((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.vid)
            up("monBenificeBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat).toString(), ar.vid)
            up("monPrixVentUniterBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble) / ar.nmbrunitBC).toString(), ar.vid)
            up("monPrixVentBM", (ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble).toString(), ar.vid)
        }

        "monBenificeUniterBM" -> {
            up("monBenificeBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.vid)
            up("monPrixVentUniterBM", (newValueDouble + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.vid)
            up("monPrixVentBM", (newValueDouble * ar.nmbrunitBC + ar.prixAchat).toString(), ar.vid)
        }

        "monBenificeBM" -> {
            up("monBenificeUniterBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.vid)
            up("monPrixVentUniterBM", ((newValueDouble / ar.nmbrunitBC) + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.vid)
            up("monPrixVentBM", (newValueDouble + ar.prixAchat).toString(), ar.vid)
            up("clientBenificeBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC)-(newValueDouble + ar.prixAchat)).toString(), ar.vid)
        }

        "monPrixAchatUniterBC" -> {
            up("prixAchat", (newValueDouble * ar.nmbrunitBC).toString(), ar.vid)
            up("monPrixVentBM", (newValueDouble * ar.nmbrunitBC + ar.monBenificeBM).toString(), ar.vid)
            up("monPrixVentFireStoreBM", (newValueDouble * ar.nmbrunitBC + ar.monBenificeFireStoreBM).toString(), ar.vid)
        }

        "prixAchat" -> {
            up("monPrixVentBM", (newValueDouble + ar.monBenificeBM).toString(), ar.vid)
            up("monPrixVentFireStoreBM", (newValueDouble + ar.monBenificeFireStoreBM).toString(), ar.vid)
        }

        "nmbrunitBC" -> {
            up("clientBenificeBM", ((ar.clientPrixVentUnite * newValueDouble) - (ar.monPrixVentUniterBM * newValueDouble)).toString(), ar.vid)

            up("clientBenificeFireStoreBM", ((ar.clientPrixVentUnite * newValueDouble) - (ar.monPrixVentUniterFireStoreBM * newValueDouble)).toString(), ar.vid)
        }

        "clientPrixVentUnite" -> {
            up("clientBenificeBM", ((newValueDouble * ar.nmbrunitBC) - ar.monPrixVentBM).toString(), ar.vid)

            up("clientBenificeFireStoreBM", ((newValueDouble * ar.nmbrunitBC) - ar.monPrixVentFireStoreBM).toString(), ar.vid)
        }

        "monPrixVentUniterBM" -> {
            up("monPrixVentBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.vid)
            up("monBenificeBM", (newValueDouble * ar.nmbrunitBC - ar.prixAchat).toString(), ar.vid)
            up("monBenificeUniterBM", (newValueDouble - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.vid)
        }

        "monPrixVentBM" -> {
            up("monPrixVentUniterBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.vid)
            up("monBenificeBM", (newValueDouble - ar.prixAchat).toString(), ar.vid)
            up("clientBenificeBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble).toString(), ar.vid)
            up("monBenificeUniterBM", ((newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.vid)
        }

        "monPrixVentFireStoreBM" -> {
            up("monPrixVentUniterFireStoreBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.vid)
            up("monBenificeFireStoreBM", (newValueDouble - ar.prixAchat).toString(), ar.vid)
            up("monBenificeUniterFireStoreBM", ((newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.vid)
            up("clientBenificeFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble).toString(), ar.vid)
        }

        "monPrixVentUniterFireStoreBM" -> {
            up("monPrixVentFireStoreBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.vid)
            up("monBenificeFireStoreBM", (newValueDouble * ar.nmbrunitBC - ar.prixAchat).toString(), ar.vid)
            up("monBenificeUniterFireStoreBM", (newValueDouble - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.vid)
        }

        "monBenificeFireStoreBM" -> {
            up("monBenificeUniterFireStoreBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.vid)
            up("monPrixVentUniterFireStoreBM", ((newValueDouble / ar.nmbrunitBC) + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.vid)
            up("monPrixVentFireStoreBM", (newValueDouble + ar.prixAchat).toString(), ar.vid)
            up("clientBenificeFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - (newValueDouble + ar.prixAchat)).toString(), ar.vid)
        }

        "monBenificeUniterFireStoreBM" -> {
            up("monBenificeFireStoreBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.vid)
            up("monPrixVentUniterFireStoreBM", (newValueDouble + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.vid)
            up("monPrixVentFireStoreBM", (newValueDouble * ar.nmbrunitBC + ar.prixAchat).toString(), ar.vid)
        }

        "clientBenificeFireStoreBM" -> {
            up("monBenificeUniterFireStoreBM", (((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.vid)
            up("monBenificeFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat).toString(), ar.vid)
            up("monPrixVentUniterFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble) / ar.nmbrunitBC).toString(), ar.vid)
            up("monPrixVentFireStoreBM", (ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble).toString(), ar.vid)
        }
    }
}

fun up(columnChanged: String, newValue: String, articleId: Long) {
    val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(articleId.toString())
    val articleUpdate = articleFromFireBase.child(columnChanged)

    val doubleValue = newValue.toDoubleOrNull()
    if (doubleValue != null && doubleValue.isFinite()) {
        articleUpdate.setValue(doubleValue)
    } else {
        // Handle invalid value (e.g., set to 0 or log an error)
        articleUpdate.setValue(0.0)
        println("Warning: Attempted to write invalid value ($newValue) to Firebase for column $columnChanged")
    }
}

// Update Firebase functions
fun updateNonTrouveState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.vid.toString())

    articleRef.child("nonTrouveState").setValue(!article.nonTrouveState)
}

fun updateVerifieState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.vid.toString())
    articleRef.child("verifieState").setValue(!article.verifieState)
}


@Composable
fun OutlineTextEditeRegle(
    columnToChange: String,
    abbreviation: String? = "",
    labelCalculated: String = "",
    currentChangingField: String,
    article: ArticlesAcheteModele,
    modifier: Modifier = Modifier,
    calculateOthersRelated: (String, String) -> Unit,
    focusRequester: FocusRequester? = null,
    colore: Color? = null,
    isText: Boolean = false,
    textColor: Color = Color.Unspecified,
    isChosenCard: Boolean = false
) {

    val initialValue = article.getColumnValue(columnToChange)
    var textFieldValue by remember {
        mutableStateOf(
            when {
                !isText && initialValue is Number -> initialValue.toString()
                isText && initialValue is String -> initialValue
                else -> ""
            }
        )
    }

    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    val labelValue = labelCalculated.ifEmpty {
        when {
            !isText && initialValue is Number -> initialValue.toString()
            isText && initialValue is String -> initialValue
            else -> ""
        }
    }

    val displayValue = if (!isText) {
        labelValue.toDoubleOrNull()?.let { doubleValue ->
            if (doubleValue % 1 == 0.0) {
                doubleValue.toInt().toString()
            } else {
                String.format("%.1f", doubleValue)
            }
        } ?: labelValue
    } else {
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
                val validatedValue = if (!isText) {
                    newValue.filter { it.isDigit() || it == '.' || it == '-' }
                } else {
                    newValue
                }
                textFieldValue = validatedValue
                calculateOthersRelated(columnToChange, validatedValue)
            },
            label = {
                AutoResizedTextBC(
                    text = "$abbreviation$displayValue",
                    color = if (isChosenCard) Color.Black else (colore ?: Color.Red),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            textStyle = TextStyle(
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = 14.sp
            ),
            modifier = modifier
                .fillMaxWidth()
                .height(65.dp)
                .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (!isText) KeyboardType.Number else KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            )
        )
    }
}
@Composable
fun AutoResizedTextBC(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center,
    bodyLarge: Boolean = false
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var readyToDraw by remember { mutableStateOf(false) }

    val defaultFontSize = if (bodyLarge) MaterialTheme.typography.bodyLarge.fontSize else MaterialTheme.typography.bodyMedium.fontSize
    val minFontSize = 7.sp

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,  // Utiliser la couleur passée en paramètre
            modifier = Modifier.drawWithContent {
                if (readyToDraw) drawContent()
            },
            softWrap = false,
            style = resizedTextStyle.copy(color = color),  // Appliquer la couleur au style
            textAlign = textAlign,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    if (resizedTextStyle.fontSize > minFontSize) {
                        resizedTextStyle = resizedTextStyle.copy(
                            fontSize = (resizedTextStyle.fontSize.value * 0.7f).sp
                        )
                    } else {
                        readyToDraw = true
                    }
                } else {
                    readyToDraw = true
                }
            }
        )
    }
}