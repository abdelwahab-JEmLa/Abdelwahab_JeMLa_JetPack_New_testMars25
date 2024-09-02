package d_EntreBonsGro

import a_RoomDB.BaseDonne
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import coil.compose.AsyncImagePainter
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import java.util.Locale
    @Composable
fun ImageDisplayer(
    painter: AsyncImagePainter,
    heightOfImageAndRelated: Dp,
    imageOffset: Offset, // Explicitly declare the type as Offset
    onImageSizeChanged: (IntSize) -> Unit,
    sectionsDonsChaqueImage: Int,
    modifier: Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .height(heightOfImageAndRelated)
            .clip(RectangleShape)
            .offset { IntOffset(imageOffset.x.toInt(), imageOffset.y.toInt()) } // Use imageOffset parameter
    ) {
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) } // State for frame offset

        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            scale = (scale * zoomChange).coerceIn(1f, 3f)
            val maxX = (constraints.maxWidth * (scale - 1)) / 2
            val maxY = (constraints.maxHeight * (scale - 1)) / 2
            offset = Offset(
                x = (offset.x + offsetChange.x).coerceIn(-maxX, maxX),
                y = (offset.y + offsetChange.y).coerceIn(-maxY, maxY)
            )
        }

        Image(
            painter = painter,
            contentDescription = "Image for supplier section",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
                .transformable(state = state)
                .onSizeChanged(onImageSizeChanged)
                .drawWithContent {
                    drawContent()
                    val redColor = Color.Red.copy(alpha = 0.3f)
                    val blueColor = Color.Blue.copy(alpha = 0.3f)
                    for (i in 0 until sectionsDonsChaqueImage) {
                        val top = size.height * i.toFloat() / sectionsDonsChaqueImage
                        val bottom = size.height * (i + 1).toFloat() / sectionsDonsChaqueImage
                        val color = if (i % 2 == 0) redColor else blueColor
                        drawRect(
                            color = color,
                            topLeft = Offset(0f, top),
                            size = Size(size.width, bottom - top)
                        )
                    }
                }
        )
    }
}


@Composable
fun AutoResizedTextDI(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    modifier: Modifier = Modifier,
    color: Color = style.color,
    textAlign: TextAlign = TextAlign.Center,
    bodyLarge: Boolean = false
) {
    var resizedTextStyle by remember { mutableStateOf(style) }
    var shouldDraw by remember { mutableStateOf(false) }

    val defaultFontSize =
        if (bodyLarge) MaterialTheme.typography.bodyLarge.fontSize else MaterialTheme.typography.bodyMedium.fontSize

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
                    resizedTextStyle =
                        resizedTextStyle.copy(fontSize = resizedTextStyle.fontSize * 0.95)
                } else {
                    shouldDraw = true
                }
            }
        )
    }
}

@Composable
fun OutlineInputDI(
    inputText: String,
    articlesList: List<EntreBonsGrosTabele>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    suggestionsList: List<String>,
    articlesRef: DatabaseReference,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    selectedArticle: Long
) {
    var showDropdown by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var textFieldFocused by remember { mutableStateOf(false) }
    var currentInputText by remember { mutableStateOf(inputText) }

    val selectedArticleData = articlesList.find { it.vidBG == selectedArticle }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = currentInputText,
                onValueChange = { newValue ->
                    currentInputText = newValue
                    if (newValue.length >= 3) {
                        val cleanInput = newValue.replace(".", "").lowercase(Locale.getDefault())
                        filteredSuggestions = suggestionsList.asSequence().filter { suggestion ->
                            val cleanSuggestion =
                                suggestion.replace(".", "").lowercase(Locale.ROOT)
                            if (isArabicDI(cleanInput)) {
                                cleanSuggestion.contains(cleanInput.take(3))
                            } else {
                                cleanSuggestion.contains(cleanInput)
                            }
                        }.take(10).toList()
                        showDropdown = filteredSuggestions.isNotEmpty() && textFieldFocused
                    } else {
                        filteredSuggestions = emptyList()
                        showDropdown = false
                    }
                },
                label = {
                    Text(
                        when {
                            currentInputText.isEmpty() && selectedArticleData != null -> {
                                val baseDonneArticle =
                                    articlesBaseDonne.find { it.idArticle.toLong() == selectedArticleData.idArticleBG }
                                val nomArabe = baseDonneArticle?.nomArab ?: ""
                                "Quantity: ${selectedArticleData.quantityAcheteBG} x ${selectedArticleData.newPrixAchatBG} (${selectedArticleData.nomArticleBG}) $nomArabe"
                            }

                            currentInputText.isEmpty() -> "Entrer quantité et prix"
                            else -> currentInputText
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        textFieldFocused = focusState.isFocused
                        showDropdown = filteredSuggestions.isNotEmpty() && textFieldFocused
                    },
                trailingIcon = {
                    if (currentInputText.isNotEmpty()) {
                        IconButton(onClick = { currentInputText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear input")
                        }
                    }
                }
            )

            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            updateArticleIdFromSuggestionDI(
                                suggestion = suggestion,
                                selectedArticle = selectedArticle,
                                articlesRef = articlesRef,
                                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                                articlesBaseDonne = articlesBaseDonne,
                                onNameInputComplete = {
                                    currentInputText = ""
                                    showDropdown = false
                                },
                                editionPassedMode = false,
                                articlesEntreBonsGrosTabele = articlesList,
                                coroutineScope = coroutineScope
                            )
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (selectedArticleData != null) {
                    updateQuantuPrixArticleDI(
                        currentInputText,
                        selectedArticleData,
                        articlesRef,
                        coroutineScope
                    )
                    currentInputText = ""
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Update")
        }
    }
}

// Helper function to check if a string contains Arabic characters
fun isArabicDI(text: String): Boolean {
    return text.any { it.code in 0x0600..0x06FF || it.code in 0x0750..0x077F || it.code in 0x08A0..0x08FF }
}
