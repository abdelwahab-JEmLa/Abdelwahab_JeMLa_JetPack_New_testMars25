package d_EntreBonsGro


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.CoroutineScope

@Composable
fun DessinableImage(
    modifier: Modifier = Modifier,
    founisseurIdNowIs: Long?,
    articles: List<EntreBonsGrosTabele>,
    soquetteBonNowIs: Int?,
    isPortraitLandscap: Boolean,
    showDivider: Boolean,
    articlesRef: DatabaseReference,
    coroutineScope: CoroutineScope,
) {

    val filteredAndSortedArticles = articles
        .filter { it.supplierIdBG == founisseurIdNowIs }
        .sortedBy { it.idArticleInSectionsOfImageBG }

    var nmbrImagesDuBon by remember { mutableIntStateOf(1) }
    var sectionsDonsChaqueImage by remember { mutableIntStateOf(10) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(founisseurIdNowIs, isPortraitLandscap) {
        showDialog = true
    }

    var lastLaunchTime by remember { mutableStateOf(0L) }
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                updateSpecificArticleDI(it, article, articlesRef, coroutineScope)//TODo regle l article pour qui soit l article click
            }
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
                    Image(
                        painter = painter,
                        contentDescription = "Image for supplier section ${imageIndex + 1}",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .weight(0.8f)
                            .height(400.dp)
                            .onSizeChanged { if (imageIndex == 0) imageSize = it }
                            .drawWithContent {
                                drawContent()
                                val dividerColor = Color.Red
                                val dividerStrokeWidth = 2f
                                for (i in 1 until sectionsDonsChaqueImage) {
                                    val y = size.height * i.toFloat() / sectionsDonsChaqueImage
                                    drawLine(
                                        color = dividerColor,
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y),
                                        strokeWidth = dividerStrokeWidth
                                    )
                                }
                            }
                    )

                    // Information des articles
                    Box(modifier = Modifier
                        .weight(0.2f)
                        .fillMaxHeight()
                        .clickable {

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
                                            .fillMaxWidth(),
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
    if (showDivider) {
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
