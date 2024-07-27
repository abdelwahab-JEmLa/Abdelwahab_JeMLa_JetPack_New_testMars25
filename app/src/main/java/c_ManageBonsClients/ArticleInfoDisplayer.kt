package c_ManageBonsClients

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import b_Edite_Base_Donne.capitalizeFirstLetter


@Composable
fun DisplayDetailleArticle(
    article: ArticlesAcheteModele,
    currentChangingField: String,
    onValueOutlineChange: (String) -> Unit
) {
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
                onValueChange = onValueOutlineChange
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
    onValueChange: (String, ) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            ColumnBenifices(article, onValueChange, currentChangingField,modifier = Modifier.weight(1f))
            ColumnPVetPa(article, onValueChange, currentChangingField,modifier = Modifier.weight(1f))
        }
        RowAutresInfo(article, onValueChange, currentChangingField,)
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
    }

}

@Composable
private fun ColumnPVetPa(
    article: ArticlesAcheteModele,
    onValueChange: (String, ) -> Unit,
    currentChangingField: String,
    modifier: Modifier = Modifier

) {
    Column (modifier = modifier.fillMaxWidth()){
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
    }

}