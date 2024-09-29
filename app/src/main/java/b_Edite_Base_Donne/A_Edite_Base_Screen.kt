package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.abdelwahabjemlajetpack.R
import com.example.abdelwahabjemlajetpack.ui.theme.DarkGreen
import com.example.abdelwahabjemlajetpack.ui.theme.Pink80
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import f_credits.SupplierTabelle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun A_Edite_Base_Screen(
    editeBaseDonneViewModel: EditeBaseDonneViewModel = viewModel(),
    articleDao: ArticleDao,
) {
    val articles by editeBaseDonneViewModel.baseDonneStatTabel.collectAsState()
    val articlesDataBaseDonne = editeBaseDonneViewModel.dataBaseDonne
    val isFilterApplied by editeBaseDonneViewModel.isFilterApplied.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var currentChangingField by remember { mutableStateOf("") }
    var selectedArticle by remember { mutableStateOf<BaseDonneStatTabel?>(null) }
    var articleDataBaseDonne by remember { mutableStateOf<BaseDonne?>(null) }
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var isSearchVisible by remember { mutableStateOf(false) }

    Ab_FilterManager(showDialog, isFilterApplied, editeBaseDonneViewModel, onOrderClick = {
        coroutineScope.launch {
            val orderedArticles = orderDateDao(articleDao)
            editeBaseDonneViewModel.updateArticles(orderedArticles)
        }
    }) { showDialog = false }

    Scaffold(
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Articles List")
                    IconButton(onClick = { showDialog = true }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Filter")
                    }
                }
                if (isSearchVisible) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            editeBaseDonneViewModel.updateSearchQuery(it)
                        },
                        label = { Text("Search Articles") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isSearchVisible = !isSearchVisible }
            ) {
                Icon(
                    imageVector = if (isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearchVisible) "Close Search" else "Open Search"
                )
            }
        },
        content = { paddingValues ->
            ArticlesScreenList(
                editeBaseDonneViewModel = editeBaseDonneViewModel,
                articlesDataBaseDonne = articlesDataBaseDonne,
                articlesBaseDonneStatTabel = articles,
                selectedArticle = selectedArticle,
                onArticleSelect = { article ->
                    val index = articles.indexOf(article)
                    focusManager.clearFocus()
                    selectedArticle = article
                    coroutineScope.launch {
                        if (index >= 0) {
                            listState.scrollToItem(index / 2)
                        }
                    }
                    currentChangingField = ""
                    articleDataBaseDonne =
                        articlesDataBaseDonne.find { it.idArticle == article.idArticle }
                },
                listState = listState,
                currentChangingField = currentChangingField,
                paddingValues = paddingValues,
                function = { currentChangingField = it },
                function1 = { articlesDataBaseDonne ->
                    if (articlesDataBaseDonne != null) {
                        articleDataBaseDonne = articlesDataBaseDonne.copy(affichageUniteState = !articlesDataBaseDonne.affichageUniteState)
                        editeBaseDonneViewModel.updateDataBaseDonne(articleDataBaseDonne)
                    }
                },
                onClickImageDimentionChangeur = { baseDonne ->
                    val updatedArticle = baseDonne.copy(funChangeImagsDimention = !baseDonne.funChangeImagsDimention)
                    editeBaseDonneViewModel.updateDataBaseDonne(updatedArticle)
                },
            )
        }
    )
}
suspend fun orderDateDao(articleDao: ArticleDao): List<BaseDonneStatTabel> {
    val baseDonneStatTabelListOrderDate = articleDao.getAllArticlesOrderDate()
    val filteredAndSortedArticles = baseDonneStatTabelListOrderDate.map {
        BaseDonneStatTabel(
            it.idArticle,
            it.nomArticleFinale,
            it.classementCate,
            it.nomArab,
            it.nmbrCat,
            it.couleur1,
            it.couleur2,
            it.couleur3,
            it.couleur4,
            it.nomCategorie2,
            it.nmbrUnite,
            it.nmbrCaron,
            it.affichageUniteState,
            it.commmentSeVent,
            it.afficheBoitSiUniter,
            it.monPrixAchat,
            it.clienPrixVentUnite,
            it.minQuan,
            it.monBenfice,
            it.monPrixVent,
            it.diponibilityState,
            it.neaon2,
            it.idCategorie,
            it.funChangeImagsDimention,
            it.nomCategorie,
            it.neaon1,
            it.lastUpdateState,
            it.cartonState,
            it.dateCreationCategorie,
            it.prixDeVentTotaleChezClient,
            it.benficeTotaleEntreMoiEtClien,
            it.benificeTotaleEn2,
            it.monPrixAchatUniter,
            it.monPrixVentUniter,
            it.benificeClient,
            it.monBeneficeUniter,
        )
    }
    return filteredAndSortedArticles.sortedByDescending { it.dateCreationCategorie } // Trier par idArticle dans l'ordre décroissant

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ArticlesScreenList(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articlesDataBaseDonne: List<BaseDonne>,
    articlesBaseDonneStatTabel: List<BaseDonneStatTabel>,
    selectedArticle: BaseDonneStatTabel?,
    onArticleSelect: (BaseDonneStatTabel) -> Unit,
    listState: LazyListState,
    currentChangingField: String,
    paddingValues: PaddingValues,
    function: (String) -> Unit,
    function1: (BaseDonne?) -> Unit,
    onClickImageDimentionChangeur: (BaseDonne) -> Unit,
) {

    LazyColumn(
        state = listState,
        modifier = Modifier.padding(paddingValues)
    ) {
        itemsIndexed(items = articlesBaseDonneStatTabel.chunked(2)) { _, pairOfArticles ->
            Column(modifier = Modifier.fillMaxWidth()) {
                if (selectedArticle != null && pairOfArticles.contains(selectedArticle)) {
                    val relatedBaseDonne = articlesDataBaseDonne.find { it.idArticle == selectedArticle.idArticle }
                    DisplayDetailleArticle(
                        article = selectedArticle,
                        articlesDataBaseDonne = relatedBaseDonne,
                        editeBaseDonneViewModel = editeBaseDonneViewModel,
                        currentChangingField = currentChangingField,
                        onValueChanged = function,
                        onUniteToggleClick = function1,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    pairOfArticles.forEach { article ->
                        val relatedBaseDonne = articlesDataBaseDonne.find { it.idArticle == article.idArticle }
                        ArticleBoardCard(
                            article = article,
                            articlesDataBaseDonne = relatedBaseDonne,
                            onClickImageDimentionChangeur = onClickImageDimentionChangeur,
                            onArticleSelect = onArticleSelect,
                            editeBaseDonneViewModel =editeBaseDonneViewModel

                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleBoardCard(
    article: BaseDonneStatTabel,
    articlesDataBaseDonne: BaseDonne?,
    onClickImageDimentionChangeur: (BaseDonne) -> Unit,
    onArticleSelect: (BaseDonneStatTabel) -> Unit,
    editeBaseDonneViewModel: EditeBaseDonneViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    val database = FirebaseDatabase.getInstance()
    val suppliersRef = database.getReference("F_Suppliers")
    var suppliers by remember { mutableStateOf<List<SupplierTabelle>>(emptyList()) }
    var selectedSupplier by remember { mutableStateOf<SupplierTabelle?>(null) }
    val firestore = Firebase.firestore

    val supplierArticlesRef = firestore.collection("F_SupplierArticlesFireS")
    var updateStatus by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    // Fetch suppliers when the component is first composed
    LaunchedEffect(Unit) {
        suppliersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val suppliersList =
                    snapshot.children.mapNotNull { it.getValue(SupplierTabelle::class.java) }
                suppliers = suppliersList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    // Function to update the supplier article and close the dialog
    fun updateSupplierArticle(supplier: SupplierTabelle) {
        showDialog = false  // Close the dialog immediately
        coroutineScope.launch {
            try {
                val batch = firestore.batch()
                val lineData = hashMapOf<String, Any>(
                    "idArticleBG" to (article.idArticle ?: ""),
                    "nomArticleFinale" to (article.nomArticleFinale ?: ""),
                    "diponibilityState" to (article.diponibilityState ?: ""),
                    "lastUpdateState" to (currentDate),

                    )
                val docId = "${article.idArticle}"
                val docRef = supplierArticlesRef
                    .document(supplier.idSupplierSu.toString())
                    .collection("historiquesAchats")
                    .document(docId)
                batch.set(docRef, lineData)

                batch.commit().await()
                updateStatus = "Update successful for ${supplier.nomSupplierSu}"
                selectedSupplier = supplier
            } catch (e: Exception) {
                updateStatus = "Error: ${e.message}"
            }
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.width(170.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.padding(2.dp)
        ) {
            Column {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.height(230.dp)
                ) {
                    val imagePath =
                        "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                    LoadImageFromPath(
                        imagePath = imagePath,
                        modifier = Modifier.clickable {
                            val newDisponibilityState = when (article.diponibilityState) {
                                "" -> "Non Dispo"
                                "Non Dispo" -> "NonForNewsClients"
                                "NonForNewsClients" -> ""
                                else -> ""
                            }
                            val updatedArticle = articlesDataBaseDonne?.copy(
                                diponibilityState = newDisponibilityState
                            )
                            if (updatedArticle != null) {
                                editeBaseDonneViewModel.updateDataBaseDonne(updatedArticle)
                            }
                            editeBaseDonneViewModel.updateBaseDonneStatTabel(
                                "diponibilityState",
                                article,
                                newDisponibilityState
                            )
                        }
                    )

                    // Check if the article is not available or not for new clients
                    when (articlesDataBaseDonne?.diponibilityState) {
                        "Non Dispo" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TextDecrease,
                                    contentDescription = "Not Available For all",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White
                                )
                            }
                        }

                        "NonForNewsClients" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Gray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Not Available For New Clients",
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(0.dp)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.3f)
                                .background(Color.White.copy(alpha = 0.7f))
                                .padding(0.dp)
                        ) {
                            val articlemonBenfice = article.monPrixVent - article.monPrixAchat
                            val monBeneficeUniter = articlemonBenfice / article.nmbrUnite
                            AutoResizedText(
                                text = "Be>${
                                    if (articlesDataBaseDonne?.affichageUniteState == false) String.format(
                                        "%.1f",
                                        articlemonBenfice
                                    ) else String.format("%.1f", monBeneficeUniter)
                                }",
                                textAlign = TextAlign.Center,
                                color = DarkGreen,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(0.7f)
                                .background(Color.White.copy(alpha = 0.7f))
                                .padding(0.dp)
                        ) {
                            AutoResizedText(
                                text = "Pv>${
                                    if (articlesDataBaseDonne?.affichageUniteState == false) String.format(
                                        "%.1f",
                                        article.monPrixVent
                                    ) else String.format("%.1f", article.monPrixVentUniter)
                                }",
                                textAlign = TextAlign.Center,
                                color = Color.Red,
                            )
                        }
                    }
                }

                AutoResizedText(
                    text = capitalizeFirstLetter(article.nomArticleFinale),
                    modifier = Modifier
                        .padding(vertical = 0.dp)
                        .clickable {
                            onArticleSelect(article)
                        },
                    textAlign = TextAlign.Center,
                    color = Color.Red
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {


                    IconButton(
                        onClick = { showDialog = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "More Info"
                        )
                    }
                    AutoResizedText(
                        text = capitalizeFirstLetter(article.nomCategorie),
                        modifier = Modifier
                            .padding(vertical = 0.dp)
                            .clickable {
                                onArticleSelect(article)
                            },
                        textAlign = TextAlign.Center,
                        color = Pink80
                    )
                }
            }
            if (articlesDataBaseDonne != null) {
                IconButton(
                    onClick = { onClickImageDimentionChangeur(articlesDataBaseDonne) },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ) {
                    Icon(
                        imageVector = if (articlesDataBaseDonne.funChangeImagsDimention) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle Favorite",
                        tint = if (articlesDataBaseDonne.funChangeImagsDimention) Color.Red else DarkGreen
                    )
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select a Supplier to Update") },
            text = {
                FlowRow(
                    maxItemsInEachRow = 3,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    suppliers.forEach { supplier ->
                        Button(
                            onClick = { updateSupplierArticle(supplier) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                supplier.nomSupplierSu,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {}  // No confirm button needed as dialog closes on selection
        )
    }

    // Show a snackbar or some other UI element to display the update status
    updateStatus?.let { status ->
        LaunchedEffect(status) {
            // You can replace this with a Snackbar or some other UI component to show the status
            println(status)
            delay(3000) // Show for 3 seconds
            updateStatus = null
        }
    }
}



@Composable
fun DisplayDetailleArticle(
    article: BaseDonneStatTabel,
    articlesDataBaseDonne: BaseDonne?,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    currentChangingField: String,
    onValueChanged: (String) -> Unit,
    onUniteToggleClick: (BaseDonne?) -> Unit,
) {
    var displayeInOutlines by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopRowQuantitys(
                article = article,
                articlesDataBaseDonne = articlesDataBaseDonne,
                viewModel = editeBaseDonneViewModel,
                currentChangingField = currentChangingField,
                onValueChanged = onValueChanged,
                displayeInOutlines = displayeInOutlines
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DisplayColorsCards(article, Modifier.weight(0.38f))
                DisplayArticleInformations(
                    editeBaseDonneViewModel = editeBaseDonneViewModel,
                    article = article,
                    articlesDataBaseDonne = articlesDataBaseDonne,
                    modifier = Modifier.weight(0.62f),
                    onValueOutlineChanged = onValueChanged,
                    currentChangingField = currentChangingField,
                    onClickUniteToggleButton = onUniteToggleClick,
                    displayeInOutlines = displayeInOutlines
                )
            }
            Text(
                text = capitalizeFirstLetter(article.nomArticleFinale),
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(7.dp)
            )
            // Add a switch to toggle displayeInOutlines
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Display in Outlines")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = displayeInOutlines,
                    onCheckedChange = { displayeInOutlines = it }
                )
            }
        }
    }
}

@Composable
fun TopRowQuantitys(
    article: BaseDonneStatTabel,
    articlesDataBaseDonne: BaseDonne?,
    viewModel: EditeBaseDonneViewModel,
    currentChangingField: String,
    onValueChanged: (String) -> Unit,
    displayeInOutlines: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        val fields = listOf(
            "clienPrixVentUnite" to "c.pU",
            "nmbrCaron" to "n.c",
            "nmbrUnite" to "n.u"
        )
        fields.forEach { (columnToChange, abbreviation) ->
            if (displayeInOutlines) {
                OutlineTextEditeBaseDonne(
                    columnToChange = columnToChange,
                    abbreviation = abbreviation,
                    onValueChanged = onValueChanged,
                    currentChangingField = currentChangingField,
                    article = article,
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .height(67.dp)
                )
            } else {
                BeneInfoBox(
                    text = "$abbreviation: ${getArticleValue(article, columnToChange)}",
                    modifier = Modifier
                        .weight(1f)
                        .height(67.dp)
                )
            }
        }
    }
}

@Composable
fun DisplayArticleInformations(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    article: BaseDonneStatTabel,
    articlesDataBaseDonne: BaseDonne?,
    modifier: Modifier = Modifier,
    onValueOutlineChanged: (String) -> Unit,
    currentChangingField: String,
    onClickUniteToggleButton: (BaseDonne?) -> Unit,
    displayeInOutlines: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        ArticlePriceInfo(article, editeBaseDonneViewModel, currentChangingField, onValueOutlineChanged, displayeInOutlines)
        if (article.clienPrixVentUnite > 0) {
            ArticleBenefitInfo(article, editeBaseDonneViewModel, currentChangingField, onValueOutlineChanged, displayeInOutlines)
        }
        ArticleSalePriceInfo(article, editeBaseDonneViewModel, currentChangingField, onValueOutlineChanged, displayeInOutlines)
        CalculationButtons(article, editeBaseDonneViewModel, onValueOutlineChanged)
        ArticleToggleButton(articlesDataBaseDonne, editeBaseDonneViewModel, onClickUniteToggleButton)
    }
}

@Composable
fun ArticlePriceInfo(
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    currentChangingField: String,
    onValueChanged: (String) -> Unit,
    displayeInOutlines: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        if (article.nmbrUnite > 1) {
            DisplayField(
                columnToChange = "monPrixAchatUniter",
                abbreviation = "U/",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = viewModel,
                onValueChanged = onValueChanged,
                displayeInOutlines = displayeInOutlines,
                modifier = Modifier
                    .weight(0.40f)
                    .height(63.dp)
            )
        }
        DisplayField(
            columnToChange = "monPrixAchat",
            abbreviation = "m.pA>",
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            onValueChanged = onValueChanged,
            displayeInOutlines = displayeInOutlines,
            modifier = Modifier
                .weight(0.60f)
                .height(63.dp)
        )
    }
}

@Composable
fun DisplayField(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    onValueChanged: (String) -> Unit,
    displayeInOutlines: Boolean,
    modifier: Modifier = Modifier
) {
    if (displayeInOutlines) {
        OutlineTextEditeBaseDonne(
            columnToChange = columnToChange,
            abbreviation = abbreviation,
            onValueChanged = onValueChanged,
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            modifier = modifier
        )
    } else {
        BeneInfoBox(
            text = "$abbreviation: ${getArticleValue(article, columnToChange)}",
            modifier = modifier
        )
    }
}


@Composable
fun ArticleBenefitInfo(
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    currentChangingField: String,
    onValueChanged: (String) -> Unit,
    displayeInOutlines: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .padding(top = 5.dp)
        ) {
            BeneInfoBox(
                text = "b.E2 -> ${article.benificeTotaleEn2}",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(5.dp))
            BeneInfoBox(
                text = "b.EN -> ${article.benficeTotaleEntreMoiEtClien}",
                modifier = Modifier.weight(1f)
            )
        }
        DisplayField(
            columnToChange = "benificeClient",
            abbreviation = "b.c",
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            onValueChanged = onValueChanged,
            displayeInOutlines = displayeInOutlines,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun ArticleSalePriceInfo(
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    currentChangingField: String,
    onValueChanged: (String) -> Unit,
    displayeInOutlines: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(63.dp)
    ) {
        if (article.nmbrUnite > 1) {
            DisplayField(
                columnToChange = "monPrixVentUniter",
                abbreviation = "u/",
                currentChangingField = currentChangingField,
                article = article,
                viewModel = viewModel,
                onValueChanged = onValueChanged,
                displayeInOutlines = displayeInOutlines,
                modifier = Modifier.weight(0.35f)
            )
        }
        DisplayField(
            columnToChange = "monPrixVent",
            abbreviation = "M.P.V",
            currentChangingField = currentChangingField,
            article = article,
            viewModel = viewModel,
            onValueChanged = onValueChanged,
            displayeInOutlines = displayeInOutlines,
            modifier = Modifier.weight(0.65f)
        )
    }
}

// Update the getArticleValue function to include the new fields
fun getArticleValue(article: BaseDonneStatTabel, columnName: String): String {
    return when (columnName) {
        "clienPrixVentUnite" -> article.clienPrixVentUnite.toString()
        "nmbrCaron" -> article.nmbrCaron.toString()
        "nmbrUnite" -> article.nmbrUnite.toString()
        "monPrixAchatUniter" -> article.monPrixAchatUniter.toString()
        "monPrixAchat" -> article.monPrixAchat.toString()
        "monPrixVentUniter" -> article.monPrixVentUniter.toString()
        "monPrixVent" -> article.monPrixVent.toString()
        "benificeClient" -> article.benificeClient.toString()
        // Add other fields as needed
        else -> ""
    }
}

// Make sure to include this composable if it's not already defined
@Composable
fun BeneInfoBox(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, shape = MaterialTheme.shapes.extraSmall)
            .padding(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }
}
@Composable
fun CalculationButtons(
    article: BaseDonneStatTabel,
    viewModel: EditeBaseDonneViewModel,
    onValueChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CalculationButton(
            onClick = {
                val newPrice = article.monPrixAchat / article.nmbrUnite
                onValueChanged("monPrixAchat")
                viewModel.updateCalculated(newPrice.toString(), "monPrixAchat", article)
            },
            text = "/"
        )
        CalculationButton(
            onClick = {
                val newPrice2 = article.monPrixAchat * article.nmbrUnite
                onValueChanged("monPrixAchat")
                viewModel.updateCalculated(newPrice2.toString(), "monPrixAchat", article)
            },
            text = "*"
        )
    }
}

@Composable
fun CalculationButton(onClick: () -> Unit, text: String) {
    Button(
        onClick = onClick,
    ) {
        Text(text)
    }
}

@Composable
fun ArticleToggleButton(
    article: BaseDonne?,
    viewModel: EditeBaseDonneViewModel,
    onClickUniteToggleButton: (BaseDonne?) -> Unit,
) {
    article?.let {
        Button(
            onClick = { onClickUniteToggleButton(article) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (article.affichageUniteState)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            ),
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = if (article.affichageUniteState)
                    "Cacher les Unités"
                else
                    "Afficher les Unités"
            )
        }
    }
}

@Composable
fun DisplayColorsCards(
    article: BaseDonneStatTabel,
    modifier: Modifier = Modifier
) {
    val couleursList = listOf(
        article.couleur1,
        article.couleur2,
        article.couleur3,
        article.couleur4,
    ).filterNot { it.isNullOrEmpty() }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .padding(3.dp)
            .fillMaxWidth()
    ) {
        itemsIndexed(couleursList) { index, couleur ->
            if (couleur != null) {
                ColorCard(article, index, couleur)
            }
        }
    }
}

@Composable
fun ColorCard(article: BaseDonneStatTabel, index: Int, couleur: String) {
    Card(
        modifier = Modifier
            .width(250.dp)
            .height(300.dp)
            .padding(end = 8.dp)
    ) {
        val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_${index + 1}"
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(250.dp)
                    .fillMaxWidth()
            ) {
                LoadImageFromPath(imagePath = imagePath)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = couleur)
        }
    }
}
//---------------------------------------------------------------


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LoadImageFromPath(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.blanc

    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = imageExist ?: defaultDrawable,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
        ) {
            it
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(1000) // Set a larger size
                .thumbnail(0.25f) // Start with 25% quality
                .fitCenter() // Ensure the image fits within the bounds
                .transition(DrawableTransitionOptions.withCrossFade()) // Smooth transition as quality improves
        }
    }
}

// Helper function
fun capitalizeFirstLetter(text: String): String {
    return text.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}




