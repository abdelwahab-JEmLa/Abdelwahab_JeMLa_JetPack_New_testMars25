package d_EntreBonsGro

import a_MainAppCompnents.ArticlesAcheteModele
import a_RoomDB.BaseDonne
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DatabaseReference
import f_credits.SupplierTabelle
import kotlinx.coroutines.CoroutineScope

@Composable
fun DialogsController(
    showDiviseurDesSections: Boolean,
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    founisseurIdNowIs: Long?,
    showDialogeNbrIMGs: Boolean,
    onDissmiss: () -> Unit,
    selectedArticle: EntreBonsGrosTabele?,
    articlesRef: DatabaseReference,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    coroutineScope: CoroutineScope,
    showOutlineDialog: Boolean,
    suggestionsList: List<String>,
    onSectionCountChange: (Int) -> Unit,
    onImageCountChange: (Int) -> Unit,
    onOutlineDialogClose: () -> Unit,
    supplierList: List<SupplierTabelle>,
    suggestionsListFromAutreNom: List<String>, voiceFrancais: Boolean
) {
    if (showDiviseurDesSections) {
        TreeCountControl(
            sectionsDonsChaqueImage = sectionsDonsChaqueImage,
            filteredAndSortedArticles = filteredAndSortedArticles,
            founisseurIdNowIs = founisseurIdNowIs,
            onCountChange = onSectionCountChange,
            supplierList = supplierList,
            articlesEntreBonsGrosTabele

        )
    }

    if (showDialogeNbrIMGs) {
        ImageCountDialog(
            onDismiss = onDissmiss,
            onSelectCount = onImageCountChange
        )
    }

    if (showOutlineDialog) {
        OutlineDialog(
            selectedArticle = selectedArticle,
            articlesEntreBonsGrosTabele = articlesEntreBonsGrosTabele,
            articlesArticlesAcheteModele = articlesArticlesAcheteModele,
            articlesBaseDonne = articlesBaseDonne,
            suggestionsList = suggestionsList,
            articlesRef = articlesRef,
            coroutineScope = coroutineScope,
            onDismiss = onOutlineDialogClose ,
            suggestionsListFromAutreNom=suggestionsListFromAutreNom, voiceFrancais = voiceFrancais,
        )
    }
}@Composable
fun HeightAndWidthAdjustmentControls(
    heightOfImageAndRelated: Dp,
    widthOfImageAndRelated: Dp,
    onHeightAdjustment: (Int) -> Unit,
    onWidthAdjustment: (Int) -> Unit,
    onOffsetAdjustment: (Float) -> Unit  // New parameter for offset adjustment
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Height: ${heightOfImageAndRelated.value.toInt()}dp")
                Row {
                    IconButton(onClick = { onHeightAdjustment(-10) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Height")
                    }
                    IconButton(onClick = { onHeightAdjustment(10) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Height")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Width: ${widthOfImageAndRelated.value.toInt()}dp")
                Row {
                    IconButton(onClick = { onWidthAdjustment(-20) }) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease Width")
                    }
                    IconButton(onClick = { onWidthAdjustment(20) }) {
                        Icon(Icons.Default.Add, contentDescription = "Increase Width")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Offset Adjustment")
                Row {
                    IconButton(onClick = { onOffsetAdjustment(-10f) }) {  // Button to move image left
                        Icon(Icons.Default.ArrowBack, contentDescription = "Move Left")
                    }
                    IconButton(onClick = { onOffsetAdjustment(10f) }) {  // Button to move image right
                        Icon(Icons.Default.ArrowForward, contentDescription = "Move Right")
                    }
                }
            }
        }
    }
}

@Composable
fun ImageCountDialog(
    onDismiss: () -> Unit,
    onSelectCount: (Int) -> Unit
) {
    var selectedCount by remember { mutableStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Number of Images") },
        text = {
            Column {
                Text("Selected count: $selectedCount")
                Slider(
                    value = selectedCount.toFloat(),
                    onValueChange = { selectedCount = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSelectCount(selectedCount)
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SuggestionsDialog(
    filteredSuggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                            onClick = { onSuggestionSelected(suggestion) },
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
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun OutlineDialog(
    selectedArticle: EntreBonsGrosTabele?,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>,
    articlesArticlesAcheteModele: List<ArticlesAcheteModele>,
    articlesBaseDonne: List<BaseDonne>,
    suggestionsList: List<String>,
    articlesRef: DatabaseReference,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit,
    suggestionsListFromAutreNom: List<String>, voiceFrancais: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    selectedArticle = article.vidBG,
                    suggestionsListFromAutreNom=suggestionsListFromAutreNom,
                    voiceFrancais=voiceFrancais,

                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}
@Composable
fun TreeCountControl(
    sectionsDonsChaqueImage: Int,
    filteredAndSortedArticles: List<EntreBonsGrosTabele>,
    founisseurIdNowIs: Long?,
    onCountChange: (Int) -> Unit,
    supplierList: List<SupplierTabelle>,
    articlesEntreBonsGrosTabele: List<EntreBonsGrosTabele>
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
            createNewArticle(
                filteredAndSortedArticles,
                founisseurIdNowIs,
                sectionsDonsChaqueImage + 1,
                supplierList,
                articlesEntreBonsGrosTabele=articlesEntreBonsGrosTabele
                )
        }) {
            Text("+")
        }
    }
}
