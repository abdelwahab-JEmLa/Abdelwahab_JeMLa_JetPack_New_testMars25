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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
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
) {
    var whatCardIsFocused by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxWidth()) {
        CardFireBase(
            article,
            onValueChange,
            currentChangingField,
            modifier = Modifier.height(140.dp),
            whatCardIsFocused = whatCardIsFocused,
            thisCardIsFocused = { whatCardIsFocused = "CardFireBase" }
        )
        CardFireStor(
            article,
            onValueChange,
            currentChangingField,
            modifier = Modifier.height(140.dp),
            whatCardIsFocused = whatCardIsFocused,
            thisCardIsFocused = { whatCardIsFocused = "CardFireStor" },
                    focusRequester=focusRequester,
        )

        RowAutresInfo(article, onValueChange, currentChangingField)
    }
}

@Composable
private fun CardFireBase(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier,
    whatCardIsFocused: String,
    thisCardIsFocused: (String) -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (whatCardIsFocused == "CardFireBase") 2.dp else 0.dp,
                color = if (whatCardIsFocused == "CardFireBase") Color.Red else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Column(modifier = Modifier.padding(3.dp)) {
            Row(modifier = Modifier.weight(1f)) {
                OutlineTextEditeRegle(
                    columnToChange = "monBenificeUniterBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireBase") }
                )

                OutlineTextEditeRegle(
                    columnToChange = "clientBenificeBM",
                    abbreviation = "cB",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.2f)
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireBase") }
                )

                OutlineTextEditeRegle(
                    columnToChange = "monBenificeBM",
                    abbreviation = "mB",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.4f)
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireBase") }
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Row(modifier = Modifier.weight(1f)) {
                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentUniterBM",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.5f)
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireBase") }
                )

                OutlineTextEditeRegle(
                    columnToChange = "monPrixVentBM",
                    abbreviation = "mpV",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.5f)
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireBase") }
                )
            }
        }
    }
}

@Composable
private fun CardFireStor(
    article: ArticlesAcheteModele,
    onValueChange: (String) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier,
    whatCardIsFocused: String,
    thisCardIsFocused: (String) -> Unit,
    focusRequester: FocusRequester
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (whatCardIsFocused == "CardFireStor") 2.dp else 0.dp,
                color = if (whatCardIsFocused == "CardFireStor") Color.Red else Color.Transparent,
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
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireStor") }
                )

                OutlineTextEditeRegle(
                    columnToChange = "clientBenificeFireStoreBM",
                    abbreviation = "cBF",
                    calculateOthersRelated = { columnChanged, newValue ->
                        onValueChange(columnChanged)
                        updateRelatedFields(article, columnChanged, newValue)
                    },
                    currentChangingField = currentChangingField,
                    article = article,
                    modifier = Modifier
                        .weight(0.2f)
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireStor") }
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
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireStor") }
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
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireStor") }
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
                        .onFocusChanged { if (it.isFocused) thisCardIsFocused("CardFireStor") },
                    focusRequester=focusRequester,
                )
            }
        }
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
                .weight(0.10f)
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
                .weight(0.10f)
                .height(67.dp)

        )

        OutlineTextEditeRegle(
            columnToChange = "monPrixAchatUniterBC",
            abbreviation = "mpVF",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueChange(columnChanged)
                updateRelatedFields(article, columnChanged, newValue)
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(0.15f)
                .height(67.dp)

        )
        OutlineTextEditeRegle(
            columnToChange = "prixAchat",
            abbreviation = "pA",
            calculateOthersRelated = { columnChanged, newValue ->
                onValueChange(columnChanged)
                updateRelatedFields(article, columnChanged, newValue)
            },
            currentChangingField = currentChangingField,
            article = article,
            modifier = Modifier
                .weight(0.65f)
                .height(67.dp)

        )
    }
}


