package A_Learn.Edite_Base_Donne

import A_Learn.A_Main_Ui.MainAppViewModel
import a_RoomDB.BaseDonne
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import java.io.File
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KType
import kotlin.reflect.full.createType

@Composable
fun A_Edite_Base_Screen(
    modifier: Modifier = Modifier,
    mainAppViewModel: MainAppViewModel,
) {
    Column(modifier = modifier.fillMaxSize()) {
        ArticlesScreenList(
            articlesList = mainAppViewModel.articlesBaseDonne,
            mainAppViewModel = mainAppViewModel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreenList(articlesList: List<BaseDonne>, mainAppViewModel: MainAppViewModel) {
    var selectedArticle by remember { mutableStateOf<BaseDonne?>(null) }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LazyVerticalGrid Sample") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(paddingValues)
        ) {
            items(items = articlesList.chunked(2)) { pairOfArticles ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        pairOfArticles.forEach { article ->
                            TestCard(article) { updatedArticle ->
                                selectedArticle = updatedArticle
                                focusManager.clearFocus() // Clear the focus from the text field
                            }
                        }
                    }
                    selectedArticle?.let { article ->
                        if (pairOfArticles.contains(article)) {
                            DisplayClickedArticle(
                                article,
                                mainAppViewModel,
                                onClose = { selectedArticle = null } // Reset selected article when close button is clicked
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TestCard(article: BaseDonne, onClick: (BaseDonne) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(170.dp)
            .clickable { onClick(article) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Column {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(230.dp)
            ) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                LoadImageFromPath(imagePath = imagePath)
            }
            Text(
                text = article.nomArticleFinale,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun DisplayClickedArticle(
    article: BaseDonne,
    mainAppViewModel: MainAppViewModel,
    onClose: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .wrapContentSize()
            .padding(25.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(Icons.Filled.Close, contentDescription = "Close")
            }
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(300.dp)
                    .wrapContentSize()
            ) {
                val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                LoadImageFromPath(imagePath = imagePath)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.nomArticleFinale,
                modifier = Modifier.padding(8.dp)
            )

            // Assuming monPrixVent and nmbrUnite are properties of BaseDonne
            val nomColumesList = listOf(
                BaseDonne::monPrixVent,
                BaseDonne::nmbrUnite,
                BaseDonne::nmbrCaron,
            )

            nomColumesList.forEach { nomColume ->
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextFieldDynamique(
                    article = article,
                    nomColum = nomColume,
                    mainAppViewModel = mainAppViewModel
                )
            }
        }
    }
}

@Composable
fun <T : Any> OutlinedTextFieldDynamique(
    article: BaseDonne,
    nomColum: KMutableProperty1<BaseDonne, T>,
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier
) {
    var valeurText by remember { mutableStateOf("") }
    var initialLabel by remember { mutableStateOf(nomColum.get(article).toString()) }

    // Reset valeurText and initialLabel whenever the article changes
    LaunchedEffect(article) {
        valeurText = ""
        initialLabel = nomColum.get(article).toString()
    }

    OutlinedTextField(
        value = valeurText,
        onValueChange = { newText ->
            valeurText = newText
            val newValue: T? = parseValue(newText, nomColum.returnType)
            if (newValue != null) {
                nomColum.set(article, newValue)
                mainAppViewModel.updateOrDelete(article)
            }
        },
        label = { Text("${nomColum.name} -> $initialLabel") },
        textStyle = TextStyle(textAlign = TextAlign.Center),
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    )
}

fun <T : Any> parseValue(value: String, type: KType): T? {
    return try {
        when (type) {
            Int::class.createType() -> value.toInt() as? T
            Float::class.createType() -> value.toFloat() as? T
            Double::class.createType() -> value.toDouble() as? T
            Long::class.createType() -> value.toLong() as? T
            Boolean::class.createType() -> value.toBoolean() as? T
            String::class.createType() -> value as? T
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun LoadImageFromPath(imagePath: String, modifier: Modifier = Modifier) {
    val defaultDrawable = R.drawable.neaveau
    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }

    val painter = rememberAsyncImagePainter(imageExist ?: defaultDrawable)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .wrapContentSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.wrapContentSize(Alignment.Center)
        )
    }
}
