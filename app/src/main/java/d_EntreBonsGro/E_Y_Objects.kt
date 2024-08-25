package d_EntreBonsGro

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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

@Composable
fun ZoomableImage(
    modifier: Modifier = Modifier,
    founisseurIdNowIs: Long?,
    articles: List<EntreBonsGrosTabele>,
    soquetteBonNowIs: Int?,
) {
    var imagePath by remember { mutableStateOf("file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${soquetteBonNowIs ?: 1}).jpg") }

    val filteredAndSortedArticles = articles
        .filter { it.supplierIdBG == founisseurIdNowIs }
        .sortedBy { it.idArticleInSectionsOfImageBG }

    var treeCount by remember { mutableStateOf(filteredAndSortedArticles.size) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val context = LocalContext.current
    val imageUri = remember(imagePath) {
        try {
            Uri.parse(imagePath)
        } catch (e: Exception) {
            null
        }
    }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(imageUri).build()
    )

    var lastLaunchTime by remember { mutableStateOf(0L) }
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle speech recognition result here
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clipToBounds()
        ) {
            Image(
                painter = painter,
                contentDescription = "Image for supplier",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { imageSize = it }
                    .drawWithContent {
                        drawContent()
                        if (treeCount > 1) {
                            val dividerColor = Color.Red
                            val dividerStrokeWidth = 2f
                            for (i in 1 until treeCount) {
                                val y = size.height * i.toFloat() / treeCount
                                drawLine(
                                    color = dividerColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = dividerStrokeWidth
                                )
                            }
                        }
                    }
            )

            Column(modifier = Modifier.fillMaxSize()) {
                filteredAndSortedArticles.forEachIndexed { index, article ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(end = 8.dp, bottom = 2.dp)
                                .wrapContentSize()
                                .clickable {
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastLaunchTime > 1000) {
                                        lastLaunchTime = currentTime
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                                            putExtra(
                                                RecognizerIntent.EXTRA_PROMPT,
                                                "Parlez maintenant pour mettre à jour cet article..."
                                            )
                                        }
                                        speechRecognizerLauncher.launch(intent)
                                    }
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "${article.quantityAcheteBG}",
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    " X ${article.newPrixAchatBG}",
                                    color = if ((article.newPrixAchatBG - article.ancienPrixBG) == 0.0) Color.Red else Color.Unspecified,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    " =(${article.subTotaleBG})",
                                    textAlign = TextAlign.Center
                                )
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
                else -> {} // Do nothing for success state
            }
        }

        // Tree count control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Tree count: $treeCount")
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (treeCount > 0) {
                    treeCount--
                    if (filteredAndSortedArticles.isNotEmpty()) {
                        deleteTheNewArticleIZ(filteredAndSortedArticles.last().vidBG)
                    }
                }
            }) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                treeCount++
                createNewArticle(filteredAndSortedArticles, founisseurIdNowIs)
            }) {
                Text("+")
            }
        }
    }
}
