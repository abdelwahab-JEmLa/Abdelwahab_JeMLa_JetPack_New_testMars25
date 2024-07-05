package b_Edite_Base_Donne

import a_RoomDB.BaseDonne
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.withNullability


fun calculateurPArRelationsEntreColumes(article: BaseDonne) {
    article.monPrixAchatUniter = article.monPrixVent / article.nmbrUnite
    article.prixDeVentTotaleChezClient = article.nmbrUnite * article.clienPrixVentUnite
    article.benficeTotaleEntreMoiEtClien = article.prixDeVentTotaleChezClient - article.monPrixAchat
    article.benificeTotaleEn2 = article.benficeTotaleEntreMoiEtClien / 2
    article.monBenfice = article.monPrixVent - article.monPrixAchat
    article.monPrixVent = article.monBenfice + article.monPrixAchat

}
@Composable
fun <T : Any> OutlinedTextFieldDynamique(
    article: BaseDonne,
    nomColum: KMutableProperty1<BaseDonne, T>,
    modifier: Modifier = Modifier.height(63.dp),
    textColore: Color = Color.Red,
    abdergNomColum: String? = nomColum.name
) {
    var valeurText by remember { mutableStateOf(nomColum.get(article).toString()) }

    LaunchedEffect(article) {
        valeurText = nomColum.get(article).toString()
    }

    OutlinedTextField(
        value = valeurText,
        onValueChange = { newText ->
            valeurText = newText
            val newValue: T? = parseValue(newText, nomColum.returnType)
            if (newValue != null) {
                nomColum.set(article, newValue)
                calculateurPArRelationsEntreColumes(article, )
            }
        },
        label = {
            AutoResizedText(
                text = "$abdergNomColum: ${nomColum.get(article)}",
                color = textColore,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        textStyle = TextStyle(color = textColore, textAlign = TextAlign.Center),
        modifier = modifier.fillMaxWidth()
    )
}

fun <T : Any> parseValue(value: String?, type: KType): T? {
    return try {
        when (type.withNullability(false)) {
            Int::class.createType() -> value?.toInt()
            Float::class.createType() -> value?.toFloat()
            Double::class.createType() -> value?.toDouble()
            Long::class.createType() -> value?.toLong()
            Boolean::class.createType() -> value?.toBoolean()
            String::class.createType() -> value
            else -> null
        } as? T
    } catch (e: Exception) {
        null
    }
}
fun <T : Any> calculateurParRelationsEntreColonnes2(
    newValue: String?,
    article: BaseDonne,
    nomColonne: KMutableProperty1<BaseDonne, T>,
    type: (String) -> T?,
) {
    val newValueTyped = newValue?.let(type)
    if (newValueTyped != null) {
        nomColonne.set(article, newValueTyped)
    }

    val monPrixAchat = article.monPrixAchat.toDouble()
    when (nomColonne) {
        BaseDonne::monPrixVent -> {
            val newBenfice = (newValueTyped as? Number)?.toDouble()?.minus(monPrixAchat)
            if (newBenfice != null) {
                article.monBenfice = newBenfice
            }
        }
        BaseDonne::monBenfice -> {
            val newPrixVent = (newValueTyped as? Number)?.toDouble()?.plus(monPrixAchat)
            if (newPrixVent != null) {
                article.monPrixVent = newPrixVent
            }
        }
    }

}
@Composable
fun DisplayArticleInformations2(
    article: BaseDonne,
    modifier: Modifier = Modifier,
) {
    // Using state to hold the values that will be shown in the OutlinedTextFields
    var valeurTextmonBenfice by remember { mutableStateOf(article.monBenfice.toString()) }
    var valeurTextmonPrixVent by remember { mutableStateOf(article.monPrixVent.toString()) }
    var valeurNmbrUnite by remember { mutableStateOf(article.nmbrUnite.toString()) }

    Column(
        modifier = modifier.padding(16.dp)
    ) {
        OutlinedTextField(
            value = valeurTextmonBenfice,
            onValueChange = { newText ->
                valeurTextmonBenfice = newText
                calculateurParRelationsEntreColonnes2(newText, article, BaseDonne::monBenfice, { it.toDoubleOrNull() }, )
            },
            label = { Text("m.B${article.monBenfice}") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = valeurNmbrUnite,
            onValueChange = { newText ->
                valeurNmbrUnite = newText
                calculateurParRelationsEntreColonnes2(newText, article, BaseDonne::nmbrUnite, { it.toIntOrNull() }, )
            },
            label = { Text("n.u${article.nmbrUnite}") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, textAlign = TextAlign.Center)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = valeurTextmonPrixVent,
            onValueChange = { newText ->
                valeurTextmonPrixVent = newText
                calculateurParRelationsEntreColonnes2(newText, article, BaseDonne::monPrixVent, { it.toDoubleOrNull() }, )
            },
            label = { Text("mpv${article.monPrixVent}") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, textAlign = TextAlign.Center)
        )
    }
}


