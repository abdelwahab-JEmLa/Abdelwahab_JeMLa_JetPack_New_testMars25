package d_EntreBonsGro

import a_RoomDB.BaseDonne
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import c_ManageBonsClients.ArticlesAcheteModele
import coil.compose.AsyncImage
import com.google.firebase.database.DatabaseReference
import java.io.File
import kotlin.math.abs

@Composable
fun OutlineInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    nowItsNameInputeTime: Boolean,
    onNameInputComplete: () -> Unit,
    articlesList: List<EntreBonsGrosTabele>,
    suggestionsList: List<String>,
    vidOfLastQuantityInputted: Long?,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    editionPassedMode: Boolean,
    modifier: Modifier = Modifier
) {
    val lastArticle = if (editionPassedMode) {
        articlesList.filter { it.passeToEndState }.maxByOrNull { it.vid }
    } else {
        articlesList.maxByOrNull { it.vid }
    }
    var showDropdown by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = inputText,
            onValueChange = { newValue ->
                onInputChange(newValue)
                val cleanInput = newValue.replace(".", "").toLowerCase()
                filteredSuggestions = if (cleanInput.length >= 3) {
                    suggestionsList.filter {
                        it.replace(".", "").toLowerCase().contains(cleanInput)
                    }
                } else {
                    emptyList()
                }
                showDropdown = filteredSuggestions.isNotEmpty() && newValue.isNotEmpty()
            },
            label = {
                Text(
                    when {
                        inputText.isEmpty() && nowItsNameInputeTime && lastArticle != null ->
                            "Quantity: ${lastArticle.quantityAcheteBG} x ${lastArticle.newPrixAchatBG}"
                        inputText.isEmpty() && !nowItsNameInputeTime && vidOfLastQuantityInputted != null -> {
                            val lastInputtedArticle = articlesList.find { it.vid == vidOfLastQuantityInputted }
                            lastInputtedArticle?.let {
                                "last: ${it.quantityAcheteBG} x ${it.newPrixAchatBG} (${it.nomArticleBG})"
                            } ?: "Entrer quantité et prix"
                        }
                        inputText.isEmpty() -> "Entrer quantité et prix"
                        else -> inputText
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        updateArticleIdFromSuggestion(
                            suggestion,
                            vidOfLastQuantityInputted,
                            articlesRef,
                            articlesArticlesAcheteModele,
                            articlesBaseDonne,
                            onNameInputComplete,
                            editionPassedMode,
                            articlesList
                        )
                        onInputChange("")
                        showDropdown = false
                    }
                )
            }
        }
    }
}


@Composable
fun AfficheEntreBonsGro(
    articlesEntreBonsGro: List<EntreBonsGrosTabele>,
    onDeleteArticle: (EntreBonsGrosTabele) -> Unit,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(articlesEntreBonsGro) { article ->
            ArticleItem(
                article = article,
                onDelete = onDeleteArticle,
                articlesRef = articlesRef,
                articlesArticlesAcheteModele = articlesArticlesAcheteModele
            )
        }
    }
}
@Composable
fun ArticleItem(
    article: EntreBonsGrosTabele,
    onDelete: (EntreBonsGrosTabele) -> Unit,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>
) {
    var lastLaunchTime by remember { mutableStateOf(0L) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                updateSpecificArticle(it, article, articlesRef)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .height(150.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // First row
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
                        .padding(top = 4.dp)
                        .wrapContentSize(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${article.quantityAcheteBG}",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            " X ${article.newPrixAchatBG}",
                            color = if ((article.newPrixAchatBG - article.ancienPrixBG).toFloat() == 0f) Color.Red else Color.Unspecified,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            " =(${article.subTotaleBG})",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Second row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image section
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val matchingArticle = articlesArticlesAcheteModele.find { it.idArticle == article.idArticle }
                        if (matchingArticle != null) {
                            SingleColorImage(matchingArticle, articlesArticlesAcheteModele)
                        } else {
                            Box(modifier = Modifier.background(Color.Gray))
                        }
                    }

                    // Delete button
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                    ) {
                        IconButton(
                            onClick = { onDelete(article) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete article",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Article details section
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    val priceDifference = article.newPrixAchatBG - article.ancienPrixBG
                    if (priceDifference.toFloat() != 0f) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "aP>${article.ancienPrixBG} (${abs(priceDifference)})",
                                color = if (priceDifference > 0) Color.Red else Color.Green
                            )
                            Icon(
                                imageVector = if (priceDifference > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (priceDifference > 0) "Price increased" else "Price decreased",
                                tint = if (priceDifference > 0) Color.Red else Color.Green
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = article.nomArticleBG,
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (article.quantityUniterBG != 1) {
                            Text("nU>${article.quantityUniterBG}")
                        }
                    }
                }
            }
        }
    }
}
@Composable
 fun SingleColorImage(
    article: ArticlesAcheteModele,
    allArticles: List<ArticlesAcheteModele>
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val imagePathWithoutExt = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
            val imagePathWebp = "$imagePathWithoutExt.webp"
            val imagePathJpg = "$imagePathWithoutExt.jpg"
            val webpExists = File(imagePathWebp).exists()
            val jpgExists = File(imagePathJpg).exists()

            when {
                webpExists || jpgExists -> {
                    val imagePath = if (webpExists) imagePathWebp else imagePathJpg
                    AsyncImage(
                        model = imagePath,
                        contentDescription = "Article image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray)
                    )
                }
            }

            val totalQuantity = allArticles
                .filter { it.idArticle == article.idArticle }
                .sumOf { it.totalQuantity }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.White.copy(alpha = 0.6f))
            ) {
                Text(
                    text = "$totalQuantity",
                    color = Color.Red,
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}