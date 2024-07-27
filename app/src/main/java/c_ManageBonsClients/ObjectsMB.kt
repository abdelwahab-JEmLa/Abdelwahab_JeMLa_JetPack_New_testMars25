package c_ManageBonsClients

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import java.io.File

@Composable
fun OutlineTextEditeRegle(
    columnToChange: String,
    abbreviation: String,
    labelCalculated: String = "",
    currentChangingField: String,
    article: ArticlesAcheteModele,
    modifier: Modifier = Modifier,
    calculateOthersRelated: (String, String) -> Unit
) {
    var textFieldValue by remember { mutableStateOf((article.getColumnValue(columnToChange) as? Double)?.toString() ?: "") }

    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    // Déterminer la valeur de l'étiquette
    val labelValue = labelCalculated.ifEmpty { (article.getColumnValue(columnToChange) as? Double)?.toString() ?: "" }
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
                .height(65.dp),
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
@Composable
fun LoadImageFromPathBC(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.blanc
    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }

    val painter = rememberAsyncImagePainter(imageExist ?: defaultDrawable)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        )
    }
}
@Composable
fun KeyboardAwareLayout(content: @Composable () -> Unit) {
    val density = LocalDensity.current
    val windowInsets = WindowInsets.ime

    val imeHeight by remember {
        derivedStateOf {
            windowInsets.getBottom(density)
        }
    }

    val imeVisible by remember {
        derivedStateOf {
            imeHeight > 0
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(bottom = with(density) { imeHeight.toDp() })
    ) {
        content()
    }
}

