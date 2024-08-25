package d_EntreBonsGro


import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.ArticleDao
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ArticlesAcheteModele
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun DessinableImage(
    modifier: Modifier = Modifier,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    founisseurIdNowIs: Long?,
    soquetteBonNowIs: Int?,
    showDiviseurDesSections: Boolean,
    articlesRef: DatabaseReference,
    baseDonneRef: DatabaseReference,
    suggestionsList: List<String>,
    articleDao: ArticleDao,
    coroutineScope: CoroutineScope,
    showOutline: Boolean
) {
    val filteredAndSortedArticles = articlesEntreBonsGrosTabele
        .filter { it.supplierIdBG == founisseurIdNowIs }
        .sortedBy { it.idArticleInSectionsOfImageBG }

    var nmbrImagesDuBon by remember { mutableIntStateOf(1) }
    var sectionsDonsChaqueImage by remember { mutableIntStateOf(10) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var showDialog by remember { mutableStateOf(false) }
    var showOutlineDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val configuration = LocalConfiguration.current
    val isPortraitLandscap = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    // Add a rotation counter
    var rotationCounter by remember { mutableIntStateOf(0) }

    // Use DisposableEffect to increment the rotation counter when the configuration changes
    DisposableEffect(configuration) {
        rotationCounter++
        onDispose { }
    }

    // Modify the LaunchedEffect to use the supplier ID and rotation counter
    LaunchedEffect(founisseurIdNowIs, rotationCounter) {
        if (rotationCounter == 1 || founisseurIdNowIs != null) {
            showDialog = true
        }
    }

    var selectedArticle by remember { mutableStateOf<EntreBonsGrosTabele?>(null) }
    var lastLaunchTime by remember { mutableStateOf(0L) }
    var showSuggestions by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }

    var isRecognizing by remember { mutableStateOf(false) }

    fun processVoiceInput(input: String) {
        if (input.firstOrNull()?.isDigit() == true || input.contains("+") || input.startsWith("-")) {
            selectedArticle?.let {
                updateQuantuPrixArticleDI(input, it, articlesRef, coroutineScope)
            }
        } else if (input.contains("تغيير")) {
            val newArabName = input.substringAfter("تغيير").trim()
            coroutineScope.launch {
                selectedArticle?.let { article ->
                    baseDonneRef.child(article.idArticleBG.toString()).child("nomArab").setValue(newArabName)
                    articleDao.updateArticleArabName(article.idArticleBG, newArabName)
                }
            }
        } else {
            val cleanInput = input.replace(".", "").toLowerCase()
            filteredSuggestions = suggestionsList.filter { it.replace(".", "").toLowerCase().contains(cleanInput) }

            when {
                filteredSuggestions.size == 1 -> {
                    updateArticleIdFromSuggestionDI(
                        suggestion = filteredSuggestions[0],
                        selectedArticle = selectedArticle?.vidBG,
                        articlesRef = articlesRef,
                        articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                        articlesBaseDonne = articlesBaseDonne,
                        onNameInputComplete = { /* Implement if needed */ },
                        editionPassedMode = false,
                        articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                        coroutineScope = coroutineScope
                    )
                }
                filteredSuggestions.isEmpty() -> {
                    val filteredSuggestions3Sentence = suggestionsList.filter {
                        it.replace(".", "").toLowerCase().contains(cleanInput.take(3))
                    }
                    showSuggestions = true
                    filteredSuggestions = filteredSuggestions3Sentence
                }
                else -> {
                    showSuggestions = true
                }
            }
        }
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isRecognizing = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                processVoiceInput(it)
            }
        } else {
            Toast.makeText(context, "La reconnaissance vocale a échoué. Veuillez réessayer.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(modifier = modifier.verticalScroll(scrollState)) {
        for (imageIndex in 0 until nmbrImagesDuBon) {
            val imagePath = "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${soquetteBonNowIs ?: 1}.${imageIndex + 1}).jpg"
            val imageUri = Uri.parse(imagePath)
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(context).data(imageUri).build()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // L Column: Quantity, Price, Subtotal
                    Box(
                        modifier = Modifier
                            .weight(0.2f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            for (sectionIndex in 0 until sectionsDonsChaqueImage) {
                                val articleIndex = imageIndex * sectionsDonsChaqueImage + sectionIndex
                                val article = filteredAndSortedArticles.getOrNull(articleIndex)

                                article?.let {
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clickable {
                                                if (showOutline) {
                                                    selectedArticle = it
                                                    showOutlineDialog = true
                                                } else {
                                                    selectedArticle = it
                                                    val currentTime = System.currentTimeMillis()
                                                    if (currentTime - lastLaunchTime > 1000) {
                                                        lastLaunchTime = currentTime
                                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour cet article...")
                                                        }
                                                        speechRecognizerLauncher.launch(intent)
                                                    }
                                                }
                                            },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AutoResizeText(
                                                text = "${it.quantityAcheteBG} X ${it.newPrixAchatBG} = ${it.subTotaleBG}",
                                                color = if ((it.newPrixAchatBG - it.ancienPrixBG) == 0.0) Color.Red else Color.Unspecified,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Center: Image
                    Image(
                        painter = painter,
                        contentDescription = "Image for supplier section ${imageIndex + 1}",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .weight(0.6f)
                            .height(400.dp)
                            .onSizeChanged { if (imageIndex == 0) imageSize = it }
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
                                        size = androidx.compose.ui.geometry.Size(size.width, bottom - top)
                                    )

                                }
                            }
                    )
                    // R Column: Article Names
                    Box(
                        modifier = Modifier
                            .weight(0.2f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(4.dp)
                        ) {
                            for (sectionIndex in 0 until sectionsDonsChaqueImage) {
                                val articleIndex = imageIndex * sectionsDonsChaqueImage + sectionIndex
                                val article = filteredAndSortedArticles.getOrNull(articleIndex)

                                article?.let {
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedArticle = it
                                                val currentTime = System.currentTimeMillis()
                                                if (currentTime - lastLaunchTime > 1000) {
                                                    lastLaunchTime = currentTime
                                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour cet article...")
                                                    }
                                                    speechRecognizerLauncher.launch(intent)
                                                }
                                            },
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AutoResizeText(
                                                text = it.nomArticleBG,
                                                color = Color.Black,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

                when (painter.state) {
                    is AsyncImagePainter.State.Loading -> {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    is AsyncImagePainter.State.Error -> {
                        Text(
                            text = "Error loading image",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {}
                }
            }
        }
    }

    if (showDiviseurDesSections) {
        TreeCountControl(
            sectionsDonsChaqueImage = sectionsDonsChaqueImage,
            filteredAndSortedArticles = filteredAndSortedArticles,
            founisseurIdNowIs = founisseurIdNowIs,
            onCountChange = { newCount ->
                sectionsDonsChaqueImage = newCount
            }
        )
    }

    if (showDialog) {
        ImageCountDialog(
            onDismiss = { showDialog = false },
            onSelectCount = { count ->
                nmbrImagesDuBon = count
                showDialog = false
            }
        )
    }

    if (showSuggestions) {
        AlertDialog(
            onDismissRequest = { showSuggestions = false },
            title = { Text("Suggestions") },
            text = {
                LazyColumn {
                    items(filteredSuggestions) { suggestion ->
                        val randomColor = Color(
                            red = (0..255).random(),
                            green = (0..255).random(),
                            blue = (0..255).random()
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = randomColor
                            )
                        ) {
                            TextButton(
                                onClick = {
                                    updateArticleIdFromSuggestionDI(
                                        suggestion = suggestion,
                                        selectedArticle = selectedArticle?.vidBG,
                                        articlesRef = articlesRef,
                                        articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                                        articlesBaseDonne = articlesBaseDonne,
                                        onNameInputComplete = { /* Implement if needed */ },
                                        editionPassedMode = false,
                                        articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
                                        coroutineScope = coroutineScope
                                    )
                                    showSuggestions = false
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color.White
                                )
                            ) {
                                Text(suggestion)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSuggestions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showOutlineDialog) {
        AlertDialog(
            onDismissRequest = { showOutlineDialog = false },
            title = { Text("Modifier l'article") },
            text = {
                selectedArticle?.let { article ->
                    OutlineInputDI(
                        inputText = "",
                        articlesList = articlesEntreBonsGrosTabele,
                        articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                        articlesBaseDonne = articlesBaseDonne,
                        suggestionsList = suggestionsList,
                        articlesRef = articlesRef,
                        coroutineScope = coroutineScope,
                        selectedArticle = article.vidBG
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Fermer")
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
                        val cleanInput = newValue.replace(".", "").toLowerCase()
                        filteredSuggestions = suggestionsList.asSequence().filter { suggestion ->
                            val cleanSuggestion = suggestion.replace(".", "").toLowerCase(Locale.ROOT)
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
                                val baseDonneArticle = articlesBaseDonne.find { it.idArticle.toLong() == selectedArticleData.idArticleBG }
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
                    updateQuantuPrixArticleDI(currentInputText, selectedArticleData, articlesRef, coroutineScope)
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
@Composable
fun AutoResizeText(
    text: String,
    color: Color,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
    maxLines: Int = 1
) {
    val initialTextStyle = MaterialTheme.typography.bodyLarge
    var scaledTextStyle by remember { mutableStateOf(initialTextStyle) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        textAlign = textAlign,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        maxLines = maxLines,
        style = scaledTextStyle,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight) {
                scaledTextStyle = scaledTextStyle.copy(fontSize = scaledTextStyle.fontSize * 0.9f)
            } else {
                readyToDraw = true
            }
        }
    )
}
@Composable
private fun TreeCountControl(
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    founisseurIdNowIs: Long?,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("sections count: $sectionsDonsChaqueImage")
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            if (sectionsDonsChaqueImage > 1) {
                onCountChange(sectionsDonsChaqueImage - 1)
                if (filteredAndSortedArticles.isNotEmpty()) {
                    deleteTheNewArticleIZ(filteredAndSortedArticles.last().vidBG)
                }
            }
        }) {
            Text("-")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = {
            onCountChange(sectionsDonsChaqueImage + 1)
            createNewArticle(filteredAndSortedArticles, founisseurIdNowIs, sectionsDonsChaqueImage + 1)
        }) {
            Text("+")
        }
    }
}

@Composable
fun ImageCountDialog(
    onDismiss: () -> Unit,
    onSelectCount: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Number of Images") },
        text = {
            Column {
                for (count in 1..5) {
                    Button(
                        onClick = { onSelectCount(count) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text("$count")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
