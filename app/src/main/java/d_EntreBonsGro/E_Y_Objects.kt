package d_EntreBonsGro

import android.content.Intent
import android.content.res.Configuration
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

@Composable
fun ZoomableImage(
    soquetteBonNowIs: Int?,
    modifier: Modifier = Modifier,
    founisseurIdNowIs: Long?,
    articles: List<EntreBonsGrosTabele>,
) {
    val context = LocalContext.current
    var lastLaunchTime by remember { mutableStateOf(0L) }
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val filteredAndSortedArticles = articles
        .filter { it.supplierIdBG == founisseurIdNowIs }
        .sortedBy { it.idArticleInSectionsOfImageBG }

    var sectionsDonsChaqueImage by remember { mutableStateOf(filteredAndSortedArticles.size) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }
    var showDialog by remember { mutableStateOf(false) }

    // Trigger dialog when founisseurIdNowIs changes
    LaunchedEffect(founisseurIdNowIs) {
        showDialog = true
    }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle speech recognition result here
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Limit the maximum height of the content
        val maxContentHeight = remember { 10000.dp } // Adjust this value as needed

        Column(modifier = Modifier.heightIn(max = maxContentHeight)) {
            // Main content (images and cards)
            Row(modifier = Modifier.weight(1f, fill = false)) {
                // Image section (70% of screen width)
                Box(modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = rememberLazyListState()
                    ) {
                        itemsIndexed(filteredAndSortedArticles.take(sectionsDonsChaqueImage)) { index, article ->
                            val imagePath = "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${soquetteBonNowIs ?: 1}.${index + 1}).jpg"
                            Log.d("ZoomableImage", "Attempting to load image: $imagePath")

                            Box {
                                SubcomposeAsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(imagePath)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Image ${index + 1} for supplier $founisseurIdNowIs",
                                    contentScale = ContentScale.FillWidth,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 1000.dp) // Limit maximum height of each image
                                        .onSizeChanged { size ->
                                            imageSize = size.takeIf { it.height <= 10000 } ?: IntSize(size.width, 10000)
                                        }
                                ) {
                                    when (painter.state) {
                                        is AsyncImagePainter.State.Loading -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .background(Color.LightGray)
                                            ) {
                                                CircularProgressIndicator(Modifier.align(Alignment.Center))
                                            }
                                        }
                                        is AsyncImagePainter.State.Error -> {
                                            val error = (painter.state as AsyncImagePainter.State.Error).result.throwable
                                            Log.e("ZoomableImage", "Error loading image: ${error.message}", error)
                                            Text("Error loading image", color = Color.Red)
                                        }
                                        is AsyncImagePainter.State.Success -> {
                                            Log.d("ZoomableImage", "Image loaded successfully: $imagePath")
                                            Image(
                                                painter = painter,
                                                contentDescription = null,
                                                contentScale = ContentScale.FillWidth,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        else -> {}
                                    }
                                }

                                // Add clickable section to the image
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((imageSize.height / filteredAndSortedArticles.size).dp.coerceAtMost(1000.dp))
                                        .offset(y = (index * imageSize.height / filteredAndSortedArticles.size).dp.coerceAtMost(1000.dp))
                                        .clickable {
                                            val currentTime = System.currentTimeMillis()
                                            if (currentTime - lastLaunchTime > 1000) {
                                                lastLaunchTime = currentTime
                                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
                                                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant pour mettre à jour cette section...")
                                                }
                                                speechRecognizerLauncher.launch(intent)
                                            }
                                        }
                                )
                            }
                        }
                    }
                }

                // Information boxes section (30% of screen width)
                LazyColumn(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                ) {
                    itemsIndexed(filteredAndSortedArticles.take(sectionsDonsChaqueImage)) { index, article ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height((imageSize.height / filteredAndSortedArticles.size).dp.coerceAtMost(1000.dp))
                                .padding(4.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxSize(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 2.dp, vertical = 4.dp),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        "${article.quantityAcheteBG}",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        " X ${article.newPrixAchatBG}",
                                        color = if ((article.newPrixAchatBG - article.ancienPrixBG) == 0.0) Color.Red else Color.Unspecified,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        " =(${article.subTotaleBG})",
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tree count control (only visible in portrait mode)
            TreeCountControl(
                isPortrait,
                sectionsDonsChaqueImage,
                filteredAndSortedArticles,
                founisseurIdNowIs
            ) { newCount ->
                sectionsDonsChaqueImage = newCount
            }
        }

        // Dialog for selecting the number of images
        if (showDialog) {
            ImageCountDialog(
                onDismiss = { showDialog = false },
                onSelectCount = { count ->
                    sectionsDonsChaqueImage = count
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun ImageCountDialog(
    onDismiss: () -> Unit,
    onSelectCount: (Int) -> Unit
) {
    androidx.compose.material3.AlertDialog(
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

@Composable
private fun TreeCountControl(
    isPortrait: Boolean,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    founisseurIdNowIs: Long?,
    onCountChange: (Int) -> Unit
) {
    if (isPortrait) {
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
                createNewArticle(filteredAndSortedArticles, founisseurIdNowIs)
            }) {
                Text("+")
            }
        }
    }
}
fun createNewArticle(articles: List<EntreBonsGrosTabele>, founisseurIdNowIs: Long?) {
    val newVid = (articles.maxOfOrNull { it.vidBG } ?: 0) + 1
    val currentDate = LocalDate.now().toString()
    val maxIdDivider = articles.maxOfOrNull { it.idArticleInSectionsOfImageBG.split("-").lastOrNull()?.toIntOrNull() ?: 0 } ?: 0
    val newIdDivider = "$founisseurIdNowIs-$currentDate-${maxIdDivider + 1}"

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
        grossisstBonN =  0,
        supplierIdBG = founisseurIdNowIs ?: 0,
        supplierNameBG = "",
        uniterCLePlusUtilise = false,
        erreurCommentaireBG = "",
        passeToEndStateBG = true,
        dateCreationBG = currentDate
    )

    // Insert the new article into Firebase
    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")

    articlesRef.child(newVid.toString()).setValue(newArticle)
        .addOnSuccessListener {
            println("New article inserted successfully")
        }
        .addOnFailureListener { e ->
            println("Error inserting new article: ${e.message}")
        }
}

fun deleteTheNewArticleIZ(vidBG: Long) {
    val database = FirebaseDatabase.getInstance()
    val articlesRef = database.getReference("ArticlesBonsGrosTabele")

    articlesRef.child(vidBG.toString()).removeValue()
        .addOnSuccessListener {
            println("Article deleted successfully")
        }
        .addOnFailureListener { e ->
            println("Error deleting article: ${e.message}")
        }
}