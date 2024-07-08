package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

@Composable
fun OutlineTextEditeBaseDonne(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    modifier: Modifier = Modifier,
    function: (String) -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(article.getColumnValue(columnToChange)?.toString() ?: "") }

    // Déterminer la valeur du champ texte
    val textValue = if (currentChangingField == columnToChange) {
        textFieldValue
    } else ""

    // Déterminer la valeur de l'étiquette
    val labelValue = article.getColumnValue(columnToChange)?.toString() ?: ""

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(3.dp)
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textFieldValue = newValue
                viewModel.updateCalculated(textFieldValue, columnToChange, article, viewModel)
                function(columnToChange)
            },
            label = {
                AutoResizedText(
                    text = "$abbreviation: $labelValue",
                    color = Color.Blue,
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
            visualTransformation = VisualTransformation.None // Aucune transformation
        )
    }
}


@Composable
fun AutoResizedText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize = MaterialTheme.typography.bodyMedium.fontSize

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            modifier = Modifier.drawWithContent {
                if (shouldDraw) drawContent()
            },
            softWrap = false,
            style = resizedTextStyle,
            textAlign = textAlign,
            onTextLayout = { result ->
                if (result.didOverflowWidth) {
                    if (style.fontSize.isUnspecified) {
                        resizedTextStyle = resizedTextStyle.copy(fontSize = defaultFontSize)
                    }
                    resizedTextStyle = resizedTextStyle.copy(fontSize = resizedTextStyle.fontSize * 0.95)
                } else {
                    shouldDraw = true
                }
            }
        )
    }
}







////////////////////////////////////////////////////////////////////
/////////                        cOMMENT                     ///////
////////////////////////////////////////////////////////////////////
fun calculateNewValues(
    columnName: String,
    newValue: String?,
    article: BaseDonne,
): Any {
    val value = newValue?.toDoubleOrNull() ?: 0.0
    val columeAchange = article.copy()

    when (columnName) {
        "monPrixVent" -> columeAchange.monPrixVent = value
        "monBenefice" -> columeAchange.monBenfice = value
        "prixDeVentTotaleChezClient" -> columeAchange.prixDeVentTotaleChezClient = value
        "monPrixAchatUniter" -> columeAchange.monPrixAchatUniter = value
    }

    columeAchange.apply {
        if (columnName != "monPrixVent") {
            monPrixVent = monBenfice + article.monPrixAchat
        }
        if (columnName != "prixDeVentTotaleChezClient") {
            prixDeVentTotaleChezClient = article.clienPrixVentUnite * article.nmbrUnite
        }
        if (columnName != "monBenefice") {
            monBenfice = monPrixVent - article.monPrixAchat
        }
        if (columnName != "monPrixAchatUniter") {
            monPrixAchatUniter = monPrixVent / article.nmbrUnite
        }
    }

    return columeAchange
}
