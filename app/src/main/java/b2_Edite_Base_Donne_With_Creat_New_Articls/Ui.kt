package b2_Edite_Base_Donne_With_Creat_New_Articls


import a_MainAppCompnents.CategoriesTabelleECB
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.DataBaseArticles
import a_MainAppCompnents.HeadOfViewModels
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import b_Edite_Base_Donne.AutoResizedText
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.abdelwahabjemlajetpack.R
import kotlinx.coroutines.launch
import java.io.File

enum class FieldsDisplayer(val fields: List<Triple<String, String, Boolean>>) {
    TOP_ROW(listOf(
        Triple("clienPrixVentUnite", "c.pU", true),
        Triple("nmbrCaron", "n.c", true),
        Triple("nmbrUnite", "n.u", true),
    )),
    PrixAchats(listOf(
        Triple("monPrixAchatUniter", "U/", true),
        Triple("monPrixAchat", "m.pA>", true)
    )),
    BenficesEntre(listOf(
        Triple("benificeTotaleEn2", "b.E2", false),
        Triple("benficeTotaleEntreMoiEtClien", "b.EN", false)
    )),
    Benfices(listOf(Triple("benificeClient", "b.c", false))),
    MonPrixVent(listOf(
        Triple("monPrixVentUniter", "u/", false),
        Triple("monPrixVent", "M.P.V", false)
    )) ,
    NomArticle(listOf(Triple("nomArticleFinale", "", true))),
    Categorie(listOf(Triple("nomCategorie", "", true))),

}


@Composable
fun ArticleDetailWindow(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    article: DataBaseArticles,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    onReloadTrigger: () -> Unit,
    reloadTrigger: Int
) {
    var displayInOutlines by remember { mutableStateOf(true) }
    var currentChangingField by remember { mutableStateOf("") }
    val isNewArticle by remember(article) { derivedStateOf { article.monPrixAchat == 0.0 } }
    var localReloadTrigger by remember { mutableStateOf(0) }

    val isFirstArticle = viewModel.isFirstArticle(article.idArticle)
    val isLastArticle = viewModel.isLastArticle(article.idArticle)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.tempImageUri?.let { uri ->
                coroutineScope.launch {
                    try {
                        val category = viewModel.getCategoryByName(article.nomCategorie)
                        val newArticle = viewModel.addNewParentArticle(uri, category)
                        viewModel.updateCurrentEditedArticle(newArticle)
                        localReloadTrigger++
                        currentChangingField = ""
                        onReloadTrigger()
                    } catch (e: Exception) {
                        Log.e("ArticleDetailWindow", "Failed to add new article", e)
                        Toast.makeText(context, "Failed to add new article", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        DisplayColorsCards(
                            article = article,
                            viewModel = viewModel,
                            onDismiss = onDismiss,
                            onReloadTrigger = onReloadTrigger,
                            relodeTigger = reloadTrigger
                        )

                        // TOP_ROW fields
                        Row(modifier = Modifier.fillMaxWidth()) {
                            FieldsDisplayer.TOP_ROW.fields.forEach { (column, abbr, _) ->
                                DisplayField(
                                    uiState = uiState,
                                    columnToChange = column,
                                    abbreviation = abbr,
                                    currentChangingField = currentChangingField,
                                    article = article,
                                    viewModel = viewModel,
                                    displayeInOutlines = displayInOutlines,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(67.dp),
                                    onValueChanged = { currentChangingField = column },
                                )
                            }
                        }

                        // Remaining FieldsDisplayer groups
                        FieldsDisplayer.entries.drop(1).forEach { fieldsGroup ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                fieldsGroup.fields.forEach { (column, abbr, _) ->
                                    when (fieldsGroup) {
                                        FieldsDisplayer.Categorie -> {
                                            CategorySelector(
                                                startCategory = article.nomCategorie,
                                                categories = uiState.categoriesECB,
                                                onCategorySelected = { selectedCategory ->
                                                    viewModel.updateArticleCategory(
                                                        article.idArticle,
                                                        selectedCategory.idCategorieInCategoriesTabele,
                                                        selectedCategory.nomCategorieInCategoriesTabele
                                                    )
                                                    currentChangingField = "nomCategorie"
                                                },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(67.dp)
                                            )
                                        }
                                        FieldsDisplayer.BenficesEntre, FieldsDisplayer.Benfices, FieldsDisplayer.MonPrixVent -> {
                                            if (!isNewArticle) {
                                                DisplayField(
                                                    columnToChange = column,
                                                    abbreviation = abbr,
                                                    currentChangingField = currentChangingField,
                                                    article = article,
                                                    viewModel = viewModel,
                                                    displayeInOutlines = displayInOutlines,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(67.dp),
                                                    onValueChanged = { currentChangingField = column },
                                                    uiState = uiState
                                                )
                                            }
                                        }
                                        else -> {
                                            DisplayField(
                                                columnToChange = column,
                                                abbreviation = abbr,
                                                currentChangingField = currentChangingField,
                                                article = article,
                                                viewModel = viewModel,
                                                displayeInOutlines = displayInOutlines,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(67.dp),
                                                onValueChanged = { currentChangingField = column },
                                                uiState = uiState
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        ArticleToggleButton(article, viewModel, Modifier.fillMaxWidth())
                    }
                }

                // Floating switch at the top-right
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Display in outlines", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = displayInOutlines,
                        onCheckedChange = { displayInOutlines = it }
                    )
                }

                // Floating "Previous" button on the left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (!isFirstArticle) {
                                val previousId = viewModel.getPreviousArticleId(article.idArticle)
                                viewModel.updateCurrentEditedArticle(viewModel.getArticleById(previousId))
                                localReloadTrigger++
                                currentChangingField = ""
                                onReloadTrigger()
                            }
                        },
                        containerColor = if (isFirstArticle) Color.Gray else MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Previous",
                            tint = Color.White
                        )
                    }
                }

                // Floating "Next" or "Add" button on the right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = {
                            if (!isLastArticle) {
                                val nextId = viewModel.getNextArticleId(article.idArticle)
                                viewModel.updateCurrentEditedArticle(viewModel.getArticleById(nextId))
                                localReloadTrigger++
                                currentChangingField = ""
                                onReloadTrigger()
                            } else {
                                viewModel.tempImageUri = viewModel.createTempImageUri(context)
                                viewModel.tempImageUri?.let { cameraLauncher.launch(it) }
                            }
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            imageVector = if (isLastArticle) Icons.Default.Add else Icons.Default.ArrowForward,
                            contentDescription = if (isLastArticle) "Add New Article" else "Next",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun ArticleToggleButton(
    article: DataBaseArticles,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = { viewModel.toggleAffichageUniteState(article) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (article.affichageUniteState)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = if (article.affichageUniteState) "Cacher les Unités" else "Afficher les Unités"
        )
    }
}
@Composable
fun CategorySelector(
    startCategory: String,
    categories: List<CategoriesTabelleECB>,
    onCategorySelected: (CategoriesTabelleECB) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentCategory by remember { mutableStateOf(startCategory) }

    val filteredCategories = remember(searchQuery, categories) {
        if (searchQuery.length >= 3) {
            categories.filter { it.nomCategorieInCategoriesTabele.contains(searchQuery, ignoreCase = true) }
        } else {
            emptyList()
        }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                expanded = it.length >= 3
                currentCategory = searchQuery
            },
            label = { Text(currentCategory) },
            modifier = Modifier.fillMaxWidth()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredCategories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.nomCategorieInCategoriesTabele) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                        searchQuery = ""
                        currentCategory= category.nomCategorieInCategoriesTabele
                    }
                )
            }
        }
    }
}

@Composable
fun OutlineTextECB(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: DataBaseArticles,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit,
    uiState: CreatAndEditeInBaseDonnRepositeryModels
) {
   val categories = uiState.categoriesECB
    var textFieldValue by remember { mutableStateOf(
        article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
    ) }
    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    val labelValue = article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
    val roundedValue = when {
        columnToChange == "nomArticleFinale" -> labelValue
        else -> try {
            labelValue.toDouble()
                .let { if (it % 1 == 0.0) it.toInt().toString() else String.format("%.1f", it) }
        } catch (e: NumberFormatException) {
            labelValue
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            textFieldValue = newValue.replace(',', '.')
            viewModel.updateAndCalculateAuthersField(textFieldValue, columnToChange, article)

            onValueChanged(columnToChange)
        },
        label = {
            AutoResizedText(
                text = "$abbreviation$roundedValue",
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )
        },
        textStyle = TextStyle(color = Color.Blue, textAlign = TextAlign.Center, fontSize = 14.sp),
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = when (columnToChange) {
                "nomArticleFinale" -> KeyboardType.Text
                else -> KeyboardType.Number
            },
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        )
    )
}
@Composable
fun DisplayField(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: DataBaseArticles,
    viewModel: HeadOfViewModels,
    displayeInOutlines: Boolean,
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit,
    uiState: CreatAndEditeInBaseDonnRepositeryModels
) {
    if (displayeInOutlines) {
        OutlineTextECB(
            columnToChange,
            abbreviation,
            currentChangingField,
            article,
            viewModel,
            modifier,
            onValueChanged,
            uiState=uiState
        )
    } else {
        InfoBoxWhithVoiceInpute(
            columnToChange = columnToChange,
            abbreviation = abbreviation,
            article = article,
            displayeInOutlines = displayeInOutlines,
            modifier = modifier
        )
    }
}
@Composable
fun InfoBoxWhithVoiceInpute(
    columnToChange: String,
    abbreviation: String,
    article: DataBaseArticles,
    displayeInOutlines: Boolean,
    modifier: Modifier = Modifier
) {
    val displayValue = when (val columnValue = article.getColumnValue(columnToChange)) {
        is Double -> if (columnValue % 1 == 0.0) columnValue.toInt().toString() else String.format("%.1f", columnValue)
        is Int -> columnValue.toString()
        else -> columnValue?.toString() ?: ""
    }
    Box(
        modifier = modifier
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.extraSmall
            )
            .padding(
                top = if (displayeInOutlines) 10.dp else 15.dp,
                start = 4.dp,
                end = 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        AutoResizedTextECB(
            text = "$abbreviation -> $displayValue",
            color = MaterialTheme.colorScheme.error
        )
    }
}

 //CategoryHeaderECB
@Composable
fun CategoryHeaderECB(
    category: CategoriesTabelleECB,
    viewModel: HeadOfViewModels,
    isSelected: Boolean,
    onNewArticleAdded: (DataBaseArticles) -> Unit ,
    onCategoryClick: (CategoriesTabelleECB) -> Unit
 ) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)

    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category.nomCategorieInCategoriesTabele,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
                    .clickable { onCategoryClick(category) }
            )
            AddArticleButton(viewModel ,category, onNewArticleAdded)
        }
    }
}

@Composable
private fun rememberCameraLauncher(
    viewModel: HeadOfViewModels,
    category: CategoriesTabelleECB,
    onNewArticleAdded: (DataBaseArticles) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { success ->
    if (success) {
        viewModel.tempImageUri?.let { uri ->
            viewModel.viewModelScope.launch {
                try {
                    val newArticle = viewModel.addNewParentArticle(uri, category)
                    onNewArticleAdded(newArticle)
                } catch (e: Exception) {
                    Log.e("CategoryHeaderECB", "Failed to add new article", e)
                    // Show an error message to the user
                    // For example, you could use a SnackBar or a Toast
                }
            }
        }
    }
}


@Composable
fun AddArticleButton(
    viewModel: HeadOfViewModels,
    category: CategoriesTabelleECB,
    onNewArticleAdded: (DataBaseArticles) -> Unit,
    takeFloatingButton:Boolean =false
) {
    val context = LocalContext.current
    val cameraLauncher = rememberCameraLauncher(viewModel, category, onNewArticleAdded)
   if (takeFloatingButton) {
       FloatingActionButton(
           onClick = {
               viewModel.tempImageUri = viewModel.createTempImageUri(context)
               viewModel.tempImageUri?.let { cameraLauncher.launch(it) }
           },
           modifier = Modifier.padding(end = 16.dp)
       ) {
           Icon(Icons.Default.Add, contentDescription = "Add Article")
       }
   }  else {
       IconButton(
           onClick = {
               viewModel.tempImageUri = viewModel.createTempImageUri(context)
               viewModel.tempImageUri?.let { cameraLauncher.launch(it) }
           },
           modifier = Modifier.padding(end = 16.dp)
       ) {
           Icon(Icons.Default.Add, contentDescription = "Add Article")
       }
   }
}

@Composable
fun DisplayColorsCards(article: DataBaseArticles, viewModel: HeadOfViewModels, modifier: Modifier = Modifier,
                       onDismiss: () -> Unit,
                       onReloadTrigger: () -> Unit,
                       relodeTigger: Int
) {
    val context = LocalContext.current
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.tempImageUri?.let { uri ->
                viewModel.addColorToArticle(uri, article)
                onReloadTrigger()
            }
        }
    }

    val uniteImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.tempImageUri?.let { uri ->
                viewModel.addUniteImageToArticle(uri, article)
                onReloadTrigger()
            }
        }
    }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        if (article.articleHaveUniteImages) {
            item {
                ColorCard(
                    article = article,
                    index = -1,
                    couleur = "Unite",
                    viewModel = viewModel,
                    onDismiss = onDismiss,
                    onReloadTrigger = onReloadTrigger,
                    relodeTigger = relodeTigger
                )
            }
        }

        val couleursList = listOf(
            article.couleur1,
            article.couleur2,
            article.couleur3,
            article.couleur4
        ).filterNot { it.isNullOrEmpty() }

        itemsIndexed(couleursList) { index, couleur ->
            if (couleur != null) {
                ColorCard(
                    article = article,
                    index = index,
                    couleur = couleur,
                    viewModel = viewModel,
                    onDismiss = onDismiss,
                    onReloadTrigger = onReloadTrigger,
                    relodeTigger = relodeTigger
                )
            }
        }

        item {
            AddColorCard(
                onClickCreatUniteImages = {
                    viewModel.tempImageUri = viewModel.createTempImageUri(context)
                    viewModel.tempImageUri?.let { uniteImageLauncher.launch(it) }
                },
                onClick = {
                    viewModel.tempImageUri = viewModel.createTempImageUri(context)
                    viewModel.tempImageUri?.let { cameraLauncher.launch(it) }
                }
            )
        }
    }
}

@Composable
private fun AddColorCard(
    onClickCreatUniteImages: () -> Unit,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .height(200.dp)
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new color",
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = onClickCreatUniteImages) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Add unite image",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun UniteImageCard(
    article: DataBaseArticles,
    viewModel: HeadOfViewModels,
    onReloadTrigger: () -> Unit,
    relodeTigger: Int
) {
    Card(
        modifier = Modifier
            .size(200.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            DisplayeImageECB(
                article = article,
                viewModel = viewModel,
                index = -1, // Use a special index for unite image
                reloadKey = relodeTigger
            )
        }
    }
}


@Composable
private fun ColorCard(
    article: DataBaseArticles,
    index: Int,
    couleur: String,
    viewModel: HeadOfViewModels,
    onDismiss: () -> Unit,
    onReloadTrigger: () -> Unit,
    relodeTigger: Int,
) {
    val uiState by viewModel.uiState.collectAsState()
    val colorsList = uiState.colorsArticles
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var colorName by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.tempImageUri?.let { uri ->
                imageUri = uri
                viewModel.processNewImage(uri, article, index + 1)
                onReloadTrigger()
            }
        }
    }

    LaunchedEffect(imageUri) {
        if (imageUri != null) {
            viewModel.clearTempImage(context)
            imageUri = null
        }
    }

    Card(
        modifier = Modifier
            .size(200.dp)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Background Image
            DisplayeImageECB(
                article = article,
                viewModel = viewModel,
                index = index,
                reloadKey = relodeTigger,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay content
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row with buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Change photo button
                    IconButton(
                        onClick = {
                            viewModel.tempImageUri = viewModel.createTempImageUri(context)
                            launcher.launch(viewModel.tempImageUri!!)
                        }
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = "Change photo")
                    }

                    // Delete color button
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete color")
                    }
                }
                if (index!=-1) {
                    // AutoCompleteTextField at the bottom
                    AutoCompleteTextField(
                        value = colorName,
                        onValueChange = { newValue ->
                            colorName = newValue
                            viewModel.updateColorName(article, index, newValue)
                        },
                        options = colorsList.map { it.nameColore },
                        label = couleur,
                        onOptionSelected = { selectedColor ->
                            colorName = selectedColor
                            viewModel.updateColorName(
                                article,
                                index,
                                selectedColor,
                                ecraseLeDernie = true
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this color?") },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteColor(article, index + 1)
                    if (index == 0) onDismiss()
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DisplayeImageECB(
    article: DataBaseArticles,
    viewModel: HeadOfViewModels,
    index: Int = 0,
    reloadKey: Any = Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val baseImagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_${if (index == -1) "Unite" else (index + 1)}"
    val viewModelImagePath = "${viewModel.viewModelImagesPath}/${article.idArticle}_${if (index == -1) "Unite" else (index + 1)}"

    val imageExist by remember(article.idArticle, reloadKey) {
        mutableStateOf(
            listOf("jpg", "webp").firstNotNullOfOrNull { extension ->
                listOf(viewModelImagePath, baseImagePath).firstOrNull { path ->
                    File("$path.$extension").exists()
                }?.let { "$it.$extension" }
            }
        )
    }

    val imageSource = imageExist ?: R.drawable.blanc

    val requestKey = "${article.idArticle}_${if (index == -1) "Unite" else index}_$reloadKey"

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(imageSource)
            .size(Size(1000, 1000))
            .crossfade(true)
            .setParameter("key", requestKey, memoryCacheKey = requestKey)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    label: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var filteredOptions by remember { mutableStateOf(emptyList<String>()) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue)
                if (newValue.length >= 2) {
                    filteredOptions = options.filter { option ->
                        option.contains(newValue, ignoreCase = true)
                    }
                    expanded = filteredOptions.isNotEmpty()
                } else {
                    expanded = false
                    filteredOptions = emptyList()
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f),
                focusedContainerColor = Color.White.copy(alpha = 0.8f)
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}




