package b_Edite_Base_Donne

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OutlineTextEditeBaseDonne(
    columnToChangeInString: String,
    abbreviation: String,
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    modifier: Modifier = Modifier,
) {
    var currentChangingField by remember { mutableStateOf("") }
    var textFieldValue by remember { mutableStateOf(article.getColumnValue(columnToChangeInString)?.toString() ?: "") }

    val textValue = if (currentChangingField == columnToChangeInString) {
        textFieldValue
    } else ""

    val labelValue = article.getColumnValue(columnToChangeInString)?.toString() ?: ""

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(3.dp)
    ) {
        OutlinedTextField(
            value = textValue,
            onValueChange = { newValue ->
                textFieldValue = removeTrailingZero(newValue)
                viewModel.updateBaseDonneStatTabel(columnToChangeInString, article, removeTrailingZero(newValue))
                currentChangingField = columnToChangeInString
            },
            label = {
                Text(
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
            visualTransformation = VisualTransformation.None // Ensuring no transformation
        )
    }
}

fun removeTrailingZero(value: String): String {
    return if (value.contains(".")) {
        value.replace(Regex("0*$"), "").replace(Regex("\\.$"), "")
    } else {
        value
    }
}
