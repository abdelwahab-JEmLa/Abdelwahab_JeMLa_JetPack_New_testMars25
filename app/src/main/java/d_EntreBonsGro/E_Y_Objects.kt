package d_EntreBonsGro

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

data class ImageZoomState(
    var scale: Float = 1f,
    var offsetX: Float = 0f,
    var offsetY: Float = 0f
)

@Composable
fun ZoomableImage(
    imagePath: String,
    supplierId: Int?,
    modifier: Modifier = Modifier,
    founisseurIdNowIs: Long?,
    articles: List<EntreBonsGrosTabele>,
) {
    val filteredAndSortedArticles = articles
        .filter { it.supplierIdBG == founisseurIdNowIs }
        .sortedBy { it.idArticleInSectionsOfImageBG }

    val zoomStateSaver = Saver<ImageZoomState, List<Float>>(
        save = { listOf(it.scale, it.offsetX, it.offsetY) },
        restore = { ImageZoomState(it[0], it[1], it[2]) }
    )

    val zoomState = rememberSaveable(saver = zoomStateSaver) {
        ImageZoomState()
    }

    var scale by remember { mutableStateOf(zoomState.scale) }
    var offsetX by remember { mutableStateOf(zoomState.offsetX) }
    var offsetY by remember { mutableStateOf(zoomState.offsetY) }
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
                contentDescription = "Zoomable image for supplier $supplierId",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { imageSize = it }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 3f)
                            val maxX = (size.width * (scale - 1)) / 2
                            val minX = -maxX
                            offsetX = (offsetX + pan.x).coerceIn(minX, maxX)
                            val maxY = (size.height * (scale - 1)) / 2
                            val minY = -maxY
                            offsetY = (offsetY + pan.y).coerceIn(minY, maxY)

                            zoomState.scale = scale
                            zoomState.offsetX = offsetX
                            zoomState.offsetY = offsetY
                        }
                    }
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

            // Add clickable boxes for each section
            Column(modifier = Modifier.fillMaxSize()) {
                filteredAndSortedArticles.forEachIndexed { index, article ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .wrapContentSize(),
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
                        deleteTheNewArticleIZ(filteredAndSortedArticles.last().vidBG.toString())
                    }
                }
            }) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                treeCount++
                createNewArticle(filteredAndSortedArticles, founisseurIdNowIs, supplierId?.toLong())
            }) {
                Text("+")
            }
        }
    }
}

fun createNewArticle(articles: List<EntreBonsGrosTabele>, founisseurNowIs: Long?, supplierId: Long?) {
    val newVid = (articles.maxOfOrNull { it.vidBG } ?: 0) + 1
    val currentDate = LocalDate.now().toString()
    val maxIdDivider = articles.maxOfOrNull { it.idArticleInSectionsOfImageBG.split("-").lastOrNull()?.toIntOrNull() ?: 0 } ?: 0
    val newIdDivider = "$supplierId-$currentDate-${maxIdDivider + 1}"

    val newArticle = EntreBonsGrosTabele(
        vidBG = newVid,
        idArticleInSectionsOfImageBG = newIdDivider,
        idArticleBG = 0,
        nomArticleBG = "",
        ancienPrixBG = 0.0,
        newPrixAchatBG = 0.0,
        quantityAcheteBG = 0,
        quantityUniterBG = 1,
        subTotaleBG = 0.0,
        grossisstBonN = founisseurNowIs?.toInt() ?: 0,
        supplierIdBG = supplierId ?: 0,
        supplierNameBG = "",
        uniterCLePlusUtilise = false,
        erreurCommentaireBG = "",
        passeToEndStateBG = true,
        dateCreationBG = currentDate
    )

    // Insert the new article into Firebase
    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("articles")

    newArticle.let {
        articlesRef.child(newVid.toString()).setValue(it)
            .addOnSuccessListener {
                println("New article inserted successfully")
            }
            .addOnFailureListener { e ->
                println("Error inserting new article: ${e.message}")
            }
    }
}

fun deleteTheNewArticleIZ(vidBG: Int) {
    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("articles")

    articlesRef.child(vidBG.toString()).removeValue()
        .addOnSuccessListener {
            println("Article deleted successfully")
        }
        .addOnFailureListener { e ->
            println("Error deleting article: ${e.message}")
        }
}