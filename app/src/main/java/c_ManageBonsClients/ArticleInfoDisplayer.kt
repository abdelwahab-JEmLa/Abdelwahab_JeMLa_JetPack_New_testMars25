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
import androidx.compose.ui.draw.drawWithContent
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
        CombinedCard(
            article = article,
            onValueChange = onValueChange,
            currentChangingField = currentChangingField,
            cardType = "CardFireBase",
            onCardFocused = {
                updateChoisirePrixDepuitFireStoreOuBaseBM(article, "CardFireBase")
            },
            modifier = Modifier.height(140.dp)
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
            modifier = Modifier.height(140.dp)
        )
        RowAutresInfo(article, onValueChange, currentChangingField)

    }
}
fun updateChoisirePrixDepuitFireStoreOuBaseBM(article: ArticlesAcheteModele, newType: String) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
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

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (article.choisirePrixDepuitFireStoreOuBaseBM == cardType) 2.dp else 0.dp,
                color = if (article.choisirePrixDepuitFireStoreOuBaseBM == cardType) Color.Red else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column(modifier = Modifier.padding(3.dp)) {
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
                            focusRequester = if (field.useFocusRequester) focusRequester else null
                        )
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
        FieldInfo("clientPrixVentUnite", "cVU", 0.20f, false),
        FieldInfo("nmbrunitBC", "nu", 0.20f, false),
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
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child(columnChanged).setValue(newValue)
}

fun updateRelatedFields(ar: ArticlesAcheteModele, columnChanged: String, newValue: String) {
    val newValueDouble = newValue.toDoubleOrNull() ?: return

    up(columnChanged, newValueDouble.toString(), ar.idArticle)

    when (columnChanged) {
        "clientBenificeBM" -> {
            up("benificeDivise", ((newValueDouble / ar.nmbrunitBC) - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monBenificeUniterBM", (((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("monPrixVentUniterBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentBM", (ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble).toString(), ar.idArticle)
        }

        "monBenificeUniterBM" -> {
            up("monBenificeBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterBM", (newValueDouble + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentBM", (newValueDouble * ar.nmbrunitBC + ar.prixAchat).toString(), ar.idArticle)
        }

        "monBenificeBM" -> {
            up("monBenificeUniterBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterBM", ((newValueDouble / ar.nmbrunitBC) + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentBM", (newValueDouble + ar.prixAchat).toString(), ar.idArticle)
            up("clientBenificeBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC)-(newValueDouble + ar.prixAchat)).toString(), ar.idArticle)
        }

        "monPrixAchatUniterBC" -> {
            up("prixAchat", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentBM", (newValueDouble * ar.nmbrunitBC + ar.monBenificeBM).toString(), ar.idArticle)
            up("monPrixVentFireStoreBM", (newValueDouble * ar.nmbrunitBC + ar.monBenificeFireStoreBM).toString(), ar.idArticle)
        }

        "prixAchat" -> {
            up("monPrixVentBM", (newValueDouble + ar.monBenificeBM).toString(), ar.idArticle)
            up("monPrixVentFireStoreBM", (newValueDouble + ar.monBenificeFireStoreBM).toString(), ar.idArticle)
        }

        "nmbrunitBC" -> {
            up("clientBenificeBM", ((ar.clientPrixVentUnite * newValueDouble) - (ar.monPrixVentUniterBM * newValueDouble)).toString(), ar.idArticle)

            up("clientBenificeFireStoreBM", ((ar.clientPrixVentUnite * newValueDouble) - (ar.monPrixVentUniterFireStoreBM * newValueDouble)).toString(), ar.idArticle)
        }

        "clientPrixVentUnite" -> {
            up("clientBenificeBM", ((newValueDouble * ar.nmbrunitBC) - ar.monPrixVentBM).toString(), ar.idArticle)

            up("clientBenificeFireStoreBM", ((newValueDouble * ar.nmbrunitBC) - ar.monPrixVentFireStoreBM).toString(), ar.idArticle)
        }

        "monPrixVentUniterBM" -> {
            up("monPrixVentBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBM", (newValueDouble * ar.nmbrunitBC - ar.prixAchat).toString(), ar.idArticle)
            up("monBenificeUniterBM", (newValueDouble - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
        }

        "monPrixVentBM" -> {
            up("monPrixVentUniterBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeBM", (newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("clientBenificeBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble).toString(), ar.idArticle)
            up("monBenificeUniterBM", ((newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.idArticle)
        }

        "monPrixVentFireStoreBM" -> {
            up("monPrixVentUniterFireStoreBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeFireStoreBM", (newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("monBenificeUniterFireStoreBM", ((newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("clientBenificeFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble).toString(), ar.idArticle)
        }

        "monPrixVentUniterFireStoreBM" -> {
            up("monPrixVentFireStoreBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeFireStoreBM", (newValueDouble * ar.nmbrunitBC - ar.prixAchat).toString(), ar.idArticle)
            up("monBenificeUniterFireStoreBM", (newValueDouble - (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
        }

        "monBenificeFireStoreBM" -> {
            up("monBenificeUniterFireStoreBM", (newValueDouble / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterFireStoreBM", ((newValueDouble / ar.nmbrunitBC) + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentFireStoreBM", (newValueDouble + ar.prixAchat).toString(), ar.idArticle)
            up("clientBenificeFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - (newValueDouble + ar.prixAchat)).toString(), ar.idArticle)
        }

        "monBenificeUniterFireStoreBM" -> {
            up("monBenificeFireStoreBM", (newValueDouble * ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentUniterFireStoreBM", (newValueDouble + (ar.prixAchat / ar.nmbrunitBC)).toString(), ar.idArticle)
            up("monPrixVentFireStoreBM", (newValueDouble * ar.nmbrunitBC + ar.prixAchat).toString(), ar.idArticle)
        }

        "clientBenificeFireStoreBM" -> {
            up("monBenificeUniterFireStoreBM", (((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monBenificeFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC) - newValueDouble - ar.prixAchat).toString(), ar.idArticle)
            up("monPrixVentUniterFireStoreBM", ((ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble) / ar.nmbrunitBC).toString(), ar.idArticle)
            up("monPrixVentFireStoreBM", (ar.clientPrixVentUnite * ar.nmbrunitBC - newValueDouble).toString(), ar.idArticle)
        }
    }
}

//updateFireBase
fun up(columnChanged: String, newValue: String, articleId: Long) {
    val articleFromFireBase = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(articleId.toString())
    val articleUpdate = articleFromFireBase.child(columnChanged)
    articleUpdate.setValue(newValue.toDoubleOrNull() ?: 0.0)
}

// Update Firebase functions
fun updateNonTrouveState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())

    articleRef.child("nonTrouveState").setValue(!article.nonTrouveState)
}

fun updateVerifieState(article: ArticlesAcheteModele) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child("verifieState").setValue(!article.verifieState)
}

fun updateTypeEmballage(article: ArticlesAcheteModele, newType: String) {
    val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.idArticle.toString())
    articleRef.child("typeEmballage").setValue(newType)
    val baseDoneRef = Firebase.database.getReference("e_DBJetPackExport").child(article.idArticle.toString())
    baseDoneRef.child("cartonState").setValue(newType)
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
    isText: Boolean = false  // New parameter to determine if the field is numeric
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
                    color = colore ?: Color.Red,
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
            color = color,
            modifier = Modifier.drawWithContent {
                if (readyToDraw) drawContent()
            },
            softWrap = false,
            style = resizedTextStyle,
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
