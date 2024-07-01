package B_Edit_Base_Donne

import B_Edite_Base_Donne.MainAppViewModel
import a_RoomDB.BaseDonne
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DisplayAndroidLabPractice(
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier,
) {
    val articlesBaseDonne by mainAppViewModel.articlesBaseDonne.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        ListsController(
            articles = articlesBaseDonne,
            mainAppViewModel = mainAppViewModel
        )
    }
}

@Composable
fun ListsController(
    articles: List<BaseDonne>,
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(articles, key = { it.idArticle }) { article ->
            StatesController(article = article, mainAppViewModel = mainAppViewModel, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun StatesController(
    article: BaseDonne,
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier
) {
    var articleState by remember { mutableStateOf(article.copy()) }

    DisplayArticle(
        articleState = articleState,
        onValueChangePrixDeVent = { newText ->
            articleState = calculateNewValues(
                newText, articleState, "monPrixVent",  mainAppViewModel
            )
        },
        onValueChangeBenfice = { newText ->
            articleState = calculateNewValues(
                newText, articleState, "monBenfice", mainAppViewModel
            )
        },
        modifier = modifier
    )
}

fun calculateNewValues(
    newValue: String?,
    article: BaseDonne,
    nomColonne: String,
    mainAppViewModel: MainAppViewModel
): BaseDonne {
    val newArticle = article.copy()
    val value = newValue?.toDoubleOrNull() ?: 0.0

    when (nomColonne) {
        "monBenfice" -> {
            newArticle.monBenfice = value
        }
        "prixDeVentTotaleChezClient" -> {
            newArticle.prixDeVentTotaleChezClient = value
        }
        "monPrixVent" -> {
            newArticle.monPrixVent = value
        }
    }

    if (nomColonne != "monPrixVent") {
        newArticle.monPrixVent = newArticle.monBenfice + article.monPrixAchat
    }
    if (nomColonne != "prixDeVentTotaleChezClient") {
        newArticle.prixDeVentTotaleChezClient = article.clienPrixVentUnite * article.nmbrUnite
    }
    if (nomColonne != "monBenfice") {
        newArticle.monBenfice = newArticle.monPrixVent - article.monPrixAchat
    }
    mainAppViewModel.updateArticle(newArticle)
    return newArticle
}

@Composable
fun DisplayArticle(
    articleState: BaseDonne,
    onValueChangePrixDeVent: (String) -> Unit,
    onValueChangeBenfice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Card(modifier = modifier.padding(10.dp)) {
        Column {
            val monPrixVentToString = articleState.monPrixVent.toString()
            OutlinedTextField(
                value = monPrixVentToString,
                onValueChange = onValueChangePrixDeVent,
                label = { Text("mpv>$monPrixVentToString") },
                modifier = modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(15.dp))
            val monBenficeToString = articleState.monBenfice.toString()
            OutlinedTextField(
                value = monBenficeToString,
                onValueChange = onValueChangeBenfice,
                label = { Text("mBe>$monBenficeToString") },
                modifier = modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
            )
        }
    }
}
