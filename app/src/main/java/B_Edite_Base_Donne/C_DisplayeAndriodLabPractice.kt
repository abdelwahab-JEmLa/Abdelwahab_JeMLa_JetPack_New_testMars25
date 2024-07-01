package B_Edite_Base_Donne

import a_RoomDB.BaseDonne
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun DisplayeAndriodLabPractice(
    mainAppViewModel: MainAppViewModel,
    modifier: Modifier = Modifier,
) {
    // Collect the state as a Compose State
    val articlesBaseDonne by mainAppViewModel.articlesBaseDonne.collectAsState()

    Column(modifier = modifier.fillMaxSize()) {
        AndriodLabPracticeArtList(
            list = articlesBaseDonne,
            onValueChanged = { article, value ->
                mainAppViewModel.changeColumeValue(article, value)
            },
            onValueChangedmonBenfice = { article, value ->
                mainAppViewModel.changeColumemonBenficeValue(article, value)
            },
        )
    }
}

@Composable
fun AndriodLabPracticeArtList(
    list: List<BaseDonne>,
    onValueChanged: (BaseDonne, String) -> Unit,
    onValueChangedmonBenfice: (BaseDonne, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(
            items = list,
            key = { article -> article.idArticle }
        ) { article ->
            AndriodLabPracticeArt(
                monPrixVent = article.monPrixVent.toString(),
                onValueChange = { newText -> onValueChanged(article, newText) },
                monBenfice = article.monBenfice.toString(),
                onValueChangemonBenfice = { newText -> onValueChangedmonBenfice(article, newText) },
            )
        }
    }
}

@Composable
fun AndriodLabPracticeArt(
    monPrixVent: String,
    onValueChange: (String) -> Unit,
    monBenfice: String,
    onValueChangemonBenfice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        OutlinedTextField(
            value = monPrixVent,
            onValueChange = onValueChange,
            label = { Text("mpv>$monPrixVent") },
            modifier = modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(15.dp))
        OutlinedTextField(
            value = monBenfice,
            onValueChange = onValueChangemonBenfice,
            label = { Text("mBe>$monBenfice") },
            modifier = modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Red, textAlign = TextAlign.Center)
        )
    }
}