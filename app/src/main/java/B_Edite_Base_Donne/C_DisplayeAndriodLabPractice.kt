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
import kotlin.reflect.KMutableProperty1

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
            StatetsController(article = article, mainAppViewModel = mainAppViewModel, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun StatetsController(
    article: BaseDonne,
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier
) {
    var articleState by remember { mutableStateOf(article.copy()) }

    DisplayeArticle(
        articleState = articleState,
        onValueChangeDemonPrixVent = { newText ->
            articleState = calculeNewValues(
                newText, articleState, BaseDonne::monPrixVent, String::toDoubleOrNull, mainAppViewModel
            )
        },
        onValueChangeDemonBenfice = { newText ->
            articleState = calculeNewValues(
                newText, articleState, BaseDonne::monBenfice, String::toDoubleOrNull, mainAppViewModel
            )
        },
        modifier = modifier
    )
}

fun <T : Any> calculeNewValues(
    newValue: String?,
    article: BaseDonne,
    nomColonne: KMutableProperty1<BaseDonne, T>,
    type: (String) -> T?,
    mainAppViewModel: MainAppViewModel
): BaseDonne {
    val newArticle = article.copy()
    val newValueTyped = newValue?.let(type)
    if (newValueTyped != null) {
        nomColonne.set(newArticle, newValueTyped)
    }

    val monPrixAchat = newArticle.monPrixAchat.toDouble()
    when (nomColonne) {
        BaseDonne::monPrixVent -> {
            val newBenfice = (newValueTyped as? Number)?.toDouble()?.minus(monPrixAchat)
            if (newBenfice != null) {
                newArticle.monBenfice = newBenfice
            }
        }
        BaseDonne::monBenfice -> {
            val newPrixVent = (newValueTyped as? Number)?.toDouble()?.plus(monPrixAchat)
            if (newPrixVent != null) {
                newArticle.monPrixVent = newPrixVent
            }
        }
    }
    mainAppViewModel.updateArticleAncienMetode(newArticle)
    return newArticle
}

@Composable
fun DisplayeArticle(
    articleState: BaseDonne,
    onValueChangeDemonPrixVent: (String) -> Unit,
    onValueChangeDemonBenfice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Card(modifier = modifier.padding(10.dp)) {
        Column {
            val monPrixVentToStringe = articleState.monPrixVent.toString()
            OutlinedTextField(
                value = monPrixVentToStringe,
                onValueChange = onValueChangeDemonPrixVent,
                label = { Text("mpv>$monPrixVentToStringe") },
                modifier = modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(15.dp))
            val monBenficeToStringe = articleState.monBenfice.toString()
            OutlinedTextField(
                value = monBenficeToStringe,
                onValueChange = onValueChangeDemonBenfice,
                label = { Text("mBe>$monBenficeToStringe") },
                modifier = modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
            )
        }
    }
}
