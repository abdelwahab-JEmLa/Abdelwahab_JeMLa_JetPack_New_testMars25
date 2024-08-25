package d_EntreBonsGro

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
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

    var treeCount by remember { mutableStateOf(filteredAndSortedArticles.size) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Handle speech recognition result here
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Column {
            // Main content (images and cards)
            Row(modifier = Modifier.weight(1f)) {
                // Image section (70% of screen width)
                Box(modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                ) {
                    LazyColumn {
                        items(5) { index ->
                            val imagePath = "file:///storage/emulated/0/Abdelwahab_jeMla.com/Programation/1_BonsGrossisst/(${soquetteBonNowIs ?: 1}.${index + 1}).jpg"
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

                            Image(
                                painter = painter,
                                contentDescription = "Image ${index + 1} for supplier $founisseurIdNowIs",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(screenHeight / 5)
                            )

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
                    }
                }

                // Card section (30% of screen width)
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                ) {
                    filteredAndSortedArticles.forEach { article ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
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

            // Tree count control (only visible in portrait mode)
            if (isPortrait) {
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