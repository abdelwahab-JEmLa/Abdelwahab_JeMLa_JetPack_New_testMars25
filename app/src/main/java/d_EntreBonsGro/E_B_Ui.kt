package d_EntreBonsGro

import a_MainAppCompnents.ArticlesAcheteModele
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.database.DatabaseReference
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
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
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope
) {
    var showDropdown by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }
    var textFieldFocused by remember { mutableStateOf(false) }

    val lastArticle by remember(articlesList, editionPassedMode) {
        mutableStateOf(
            if (editionPassedMode) {
                articlesList.filter { it.passeToEndStateBG }.maxByOrNull { it.vidBG }
            } else {
                articlesList.maxByOrNull { it.vidBG }
            }
        )
    }

    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                value = inputText,
                onValueChange = { newValue ->
                    onInputChange(newValue)
                    if (newValue.length >= 3) {
                        val cleanInput = newValue.replace(".", "").toLowerCase()
                        filteredSuggestions = suggestionsList.asSequence().filter { suggestion ->
                            val cleanSuggestion = suggestion.replace(".", "").toLowerCase(Locale.ROOT)
                            if (isArabic(cleanInput)) {
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
                            inputText.isEmpty() && nowItsNameInputeTime && lastArticle != null -> {
                                val baseDonneArticle = articlesBaseDonne.find { it.idArticle.toLong() == lastArticle!!.idArticleBG }
                                val nomArabe = baseDonneArticle?.nomArab ?: ""
                                "Quantity: ${lastArticle!!.quantityAcheteBG} x ${lastArticle!!.newPrixAchatBG} (${lastArticle!!.nomArticleBG}) $nomArabe"
                            }
                            inputText.isEmpty() && !nowItsNameInputeTime && vidOfLastQuantityInputted != null -> {
                                articlesList.find { it.vidBG == vidOfLastQuantityInputted }?.let { article ->
                                    val baseDonneArticle = articlesBaseDonne.find { it.idArticle.toLong() == article.idArticleBG }
                                    val nomArabe = baseDonneArticle?.nomArab ?: ""
                                    "last: ${article.quantityAcheteBG} x ${article.newPrixAchatBG} (${article.nomArticleBG}) $nomArabe"
                                } ?: "Entrer quantité et prix"
                            }
                            inputText.isEmpty() -> "Entrer quantité et prix"
                            else -> inputText
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
                    if (inputText.isNotEmpty()) {
                        IconButton(onClick = { onInputChange("") }) {
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
                            updateArticleIdFromSuggestion(
                                suggestion,
                                vidOfLastQuantityInputted,
                                articlesRef,
                                articlesArticlesAcheteModele,
                                articlesBaseDonne,
                                onNameInputComplete,
                                editionPassedMode,
                                articlesList,
                                coroutineScope = coroutineScope,
                            )
                            onInputChange("")
                            showDropdown = false
                        }
                    )
                }
            }
        }
    }
}

// Helper function to check if a string contains Arabic characters
fun isArabic(text: String): Boolean {
    return text.any { it.code in 0x0600..0x06FF || it.code in 0x0750..0x077F || it.code in 0x08A0..0x08FF }
}

@Composable
fun AfficheEntreBonsGro(
    articlesEntreBonsGro: List<EntreBonsGrosTabele>,
    onDeleteArticle: (EntreBonsGrosTabele) -> Unit,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope,
    onDeleteFromFirestore: (EntreBonsGrosTabele) -> Unit,
    suppliersList: List<SupplierTabelle>,
    onSupplierChanged: (Long, Int) -> Unit,
    suppliersRef: DatabaseReference  // Add this parameter
) {
    LazyColumn(modifier = modifier) {
        items(articlesEntreBonsGro) { article ->
            ArticleItem(
                article = article,
                onDelete = onDeleteArticle,
                articlesRef = articlesRef,
                articlesArticlesAcheteModele = articlesArticlesAcheteModele,
                coroutineScope = coroutineScope,
                onDeleteFromFirestore = onDeleteFromFirestore,
                suppliersList = suppliersList,
                onSupplierChanged = onSupplierChanged,
                suppliersRef = suppliersRef  // Pass the suppliersRef here
            )
        }
    }
}
@Composable
fun ArticleItem(
    article: EntreBonsGrosTabele,
    onDelete: (EntreBonsGrosTabele) -> Unit,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    coroutineScope: CoroutineScope,
    onDeleteFromFirestore: (EntreBonsGrosTabele) -> Unit,
    suppliersList: List<SupplierTabelle>,
    onSupplierChanged: (Long, Int) -> Unit,
    suppliersRef: DatabaseReference  // Add this parameter
) {
    var lastLaunchTime by remember { mutableStateOf(0L) }
    var showSupplierDialog by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            spokenText?.let {
                updateSpecificArticle(it, article, articlesRef, coroutineScope)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp)
            .height(190.dp),
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
                    .padding(2.dp),
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
                        val matchingArticle = articlesArticlesAcheteModele.find { it.idArticle == article.idArticleBG }
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
                            .padding(2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                onDelete(article)
                                onDeleteFromFirestore(article)
                            },
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
                var quantityUniterBG by remember { mutableStateOf("") }

                // Article details section
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(2.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        val priceDifference = if (article.uniterCLePlusUtilise) {
                            article.newPrixAchatBG - article.ancienPrixOnUniterBG
                        } else {
                            article.newPrixAchatBG - article.ancienPrixBG
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newUniterCLePlusUtilise = !article.uniterCLePlusUtilise
                                    coroutineScope.launch {
                                        articlesRef.child(article.vidBG.toString()).apply {
                                            child("uniterCLePlusUtilise").setValue(newUniterCLePlusUtilise)
                                        }
                                    }
                                }
                        )  {
                            if (article.quantityUniterBG != 1) {
                                OutlinedTextField(
                                    value = quantityUniterBG,
                                    onValueChange = { newValue ->
                                        if (newValue.isNotEmpty() && newValue.toDoubleOrNull() != null) {
                                            quantityUniterBG = newValue
                                            coroutineScope.launch {
                                                articlesRef.child(article.vidBG.toString()).apply {
                                                    child("quantityUniterBG").setValue(newValue.toDouble())
                                                }
                                            }
                                        }
                                    },
                                    label = { Text(article.quantityUniterBG.toString()) },
                                    textStyle = LocalTextStyle.current.copy(
                                        color = if (article.uniterCLePlusUtilise) Color.White else Color.Unspecified
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = if (article.uniterCLePlusUtilise) Color.Red else Color.Transparent,
                                        unfocusedContainerColor = if (article.uniterCLePlusUtilise) Color.Red else Color.Transparent,
                                        focusedLabelColor = if (article.uniterCLePlusUtilise) Color.White else Color.Unspecified,
                                        unfocusedLabelColor = if (article.uniterCLePlusUtilise) Color.White else Color.Unspecified
                                    ),
                                    modifier = Modifier.width(80.dp)
                                )
                                Text("X")
                            }
                            Text(
                                text = if (article.uniterCLePlusUtilise) {
                                    "${article.ancienPrixOnUniterBG} (${if (priceDifference > 0) "-" else "+"}${String.format("%.2f", abs(priceDifference))})"
                                } else {
                                    "${article.ancienPrixBG} (${if (priceDifference > 0) "-" else "+"}${String.format("%.2f", abs(priceDifference))})"
                                },
                                color = if (priceDifference > 0) Color.Red else Color.Green
                            )
                            Icon(
                                imageVector = if (priceDifference > 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = if (priceDifference > 0) "Price increased" else "Price decreased",
                                tint = if (priceDifference > 0) Color.Red else Color.Green
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = article.nomArticleBG,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = Color(0xFF8E24AA) // Purple color
                            )
                        }
                        Text(
                            text = article.supplierNameBG,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showSupplierDialog = true },
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }

    SupplierSelectionDialog(
        showDialog = showSupplierDialog,
        onDismiss = { showSupplierDialog = false },
        onSupplierSelected = { selectedSupplierBon,_ ->
            val selectedSupplier = suppliersList.find { it.bonDuSupplierSu == selectedSupplierBon.toString() }
            if (selectedSupplier != null) {
                coroutineScope.launch {
                    articlesRef.child(article.vidBG.toString()).apply {
                        child("supplierNameBG").setValue(selectedSupplier.nomSupplierSu)
                        child("supplierIdBG").setValue(selectedSupplier.idSupplierSu)
                        child("grossisstBonN").setValue(selectedSupplierBon)
                    }
                }
                onSupplierChanged(article.vidBG, selectedSupplierBon)
            }
        },
        suppliersList = suppliersList,
        suppliersRef = suppliersRef  // Pass the suppliersRef here
    )
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

