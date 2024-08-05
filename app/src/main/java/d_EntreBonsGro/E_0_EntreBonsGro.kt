package d_EntreBonsGro

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

@Composable
fun FragmentEntreBonsGro() {
    var articlesEntreBonsGro by remember { mutableStateOf<List<ArticlesBonsGrosTabele>>(emptyList()) }
    val articlesEntreBonsGroModeleRef = Firebase.database.getReference("ArticlesBonsGrosTabele")

    LaunchedEffect(Unit) {
        // Set up Firebase listener
        articlesEntreBonsGroModeleRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newArticles = dataSnapshot.children.mapNotNull { it.getValue(ArticlesBonsGrosTabele::class.java) }
                articlesEntreBonsGro = newArticles
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("Firebase data fetch cancelled: ${databaseError.message}")
            }
        })
    }

    Column {
        // Custom app bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "EntreBonsGro",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            AfficheEntreBonsGro(articlesEntreBonsGro)
        }
    }
}

@Composable
fun AfficheEntreBonsGro(articlesEntreBonsGro: List<ArticlesBonsGrosTabele>) {
    LazyColumn {
        items(articlesEntreBonsGro) { article ->
            ArticleItem(article)
        }
    }
}

@Composable
fun ArticleItem(article: ArticlesBonsGrosTabele) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = article.nomArticleBG,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Ancien prix: ${article.ancienPrixBG}")
                    Text("Nouveau prix: ${article.newPrixAchatBG}")
                }
                Column {
                    Text("Quantité: ${article.quantityAcheteBG}")
                    Text("Unités: ${article.quantityUniterBG}")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sous-total: ${article.subTotaleBG}")
            if (article.erreurCommentaireBG.isNotBlank()) {
                Text(
                    text = "Erreur: ${article.erreurCommentaireBG}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            //TODO ajout un floting button qui lence startVoiceInput
        }
    }
}
private val REQUEST_CODE_SPEECH_INPUT = 100

private fun startVoiceInput() {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-DZ")
    intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Parlez maintenant...")
    try {
        startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "Votre appareil ne supporte pas la reconnaissance vocale.", Toast.LENGTH_SHORT).show()
    }
}

override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
        val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        result?.let {
            entreQantityEtPrix.setText(it[0])
        }
    }
}


// Fonction pour insérer des données dans la table EntreBonsGros
suspend fun insertDataIntoEntreBonsGrosTable(idArticle: Int? = null) {


    val text = entreQantityEtPrix.text.toString()
    // Expression régulière pour capturer la quantité et le prix
    val regex = """(\d+)\s*[x+]\s*(\d+(\.\d+)?)""".toRegex()
    val matchResult = regex.find(text)

    // Extraction des quantités et prix à partir du texte
    val (quantity, prix) = matchResult?.destructured?.let {
        Pair(it.component1().toIntOrNull(), it.component2().toDoubleOrNull())
    } ?: Pair(null, null)

    // Trouver l'article correspondant dans la liste
    val articleIndex = articlesList.indexOfFirst {
        it.quantityAchete_C == 0 && it.grossisstBonN == spChoisirBon.selectedItemPosition
    }

    if (articleIndex != -1) {
        // Mise à jour de l'article existant
        val article = articlesList[articleIndex]
        article.quantityAchete_C = quantity!!
        article.new_prix_d_achat_c = prix!!
        article.subTotale =(quantity*prix).toDouble()
        database.EntreBonsGrosTabeleDao().update(article)
        addToAfficheAdapter(article)
    } else if (quantity != null && prix != null) {
        // Création d'un nouvel article
        val newVid = (articlesList.maxByOrNull { it.vid }?.vid ?: 0) + 1

        val newArticle = EntreBonsGrosTabele(
            vid = newVid,
            idArticle = newVid,
            a_d_nomarticlefinale_c = "",
            ancienPrix = 0.0,
            new_prix_d_achat_c = prix,
            quantityAchete_C = quantity,
            quantity_uniter_c = 1,
            subTotale = prix * quantity,
            grossisstBonN = spChoisirBon.selectedItemPosition,
            uniter_c_le_plus_utilise = false,
            erreur_commentaire = ""
        )

        articlesList.add(newArticle)
        database.EntreBonsGrosTabeleDao().insert(newArticle)
        addToAfficheAdapter(newArticle)
    } else {
        println("Erreur: Impossible d'extraire la quantité ou le prix de vente.")
    }

    // Réinitialisation du texte d'entrée
    entreQantityEtPrix.setText("")
    updateTotalAchats()
}