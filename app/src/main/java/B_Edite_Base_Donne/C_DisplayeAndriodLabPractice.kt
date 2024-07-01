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
        AndroidLabPracticeArtList(
            list = articlesBaseDonne,
            onValueChanged = { article, value ->
                mainAppViewModel.updateViewModelWhithCalulationColumes(
                    value, article, BaseDonne::monPrixVent
                ) { it.toDoubleOrNull() }
            },
            onValueChangedmonBenfice = { article, value ->
                mainAppViewModel.updateViewModelWhithCalulationColumes(
                    value, article, BaseDonne::monBenfice
                ) { it.toDoubleOrNull() }
            },
        )
    }
}

@Composable
fun AndroidLabPracticeArtList(
    list: List<BaseDonne>,
    onValueChanged: (BaseDonne, String) -> Unit,
    onValueChangedmonBenfice: (BaseDonne, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(items = list, key = { article -> article.idArticle }) { article ->
            AndroidLabPracticeArt(
                article = article,
                onValueChange = { newText -> onValueChanged(article, newText) },
                onValueChangemonBenfice = { newText -> onValueChangedmonBenfice(article, newText) },
            )
        }
    }
}

@Composable
fun AndroidLabPracticeArt(
    article: BaseDonne,
    onValueChange: (String) -> Unit,
    onValueChangemonBenfice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = Modifier.height(8.dp))
    Card(modifier.padding(10.dp)) {
        Column {
            OutlinedTextField(
                value = article.monPrixVent.toString(),
                onValueChange = onValueChange,
                label = { Text("mpv>${article.monPrixVent}") },
                modifier = modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
            )
            Spacer(modifier = Modifier.height(15.dp))
            OutlinedTextField(
                value = article.monBenfice.toString(),
                onValueChange = onValueChangemonBenfice,
                label = { Text("mBe>${article.monBenfice}") },
                modifier = modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
            )
        }
    }
}
