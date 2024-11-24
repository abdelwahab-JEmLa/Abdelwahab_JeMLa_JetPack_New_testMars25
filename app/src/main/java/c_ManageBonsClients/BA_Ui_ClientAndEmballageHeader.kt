package c_ManageBonsClients

import a_MainAppCompnents.ArticlesAcheteModele
import a_MainAppCompnents.HeadOfViewModels
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.ClientsTabelle
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.addNewClient
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.calculateClientProfit
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.createEmptyArticle
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.generateClientColor
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import g_BoardStatistiques.BoardStatistiquesStatViewModel
import g_BoardStatistiques.f_2_CreditsClients.documentIdClientFireStoreClientCreditCB
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.round

const val TAG = "ClientManagement"

@SuppressLint("DefaultLocale")
@Composable
fun ClientAndEmballageHeader(
    nomClient: String,
    typeEmballage: Long,
    onToggleActive: () -> Unit,
    isActive: Boolean,
    allArticles: List<ArticlesAcheteModele>,
    clientTotal: Double,
    boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel,
    headOfViewModels: HeadOfViewModels,
    ) {
    val uiState by headOfViewModels.uiState.collectAsState()
    val placesOfArticelsInCamionette  =uiState.placesOfArticelsInCamionette
    val context = LocalContext.current

    var showPrintDialog by remember { mutableStateOf(false) }
    var showClientsBonUpdateDialog by remember { mutableStateOf(false) }
    var clientId by remember { mutableStateOf<Long?>(null) }
    var ancienCredits by remember { mutableDoubleStateOf(0.0) }
    val verifiedCount = allArticles.count { it.nomClient == nomClient && it.verifieState }
    val clientColor = remember(nomClient) { generateClientColor(nomClient) }
    val clientProfit = calculateClientProfit(allArticles, nomClient)
    val coroutineScope = rememberCoroutineScope()


    fun fetchAncienCredits(clientId: Long?, onCreditsFetched: (Double) -> Unit) {
        if (clientId != null) {
            val firestore = Firebase.firestore
            firestore.collection("F_ClientsArticlesFireS")
                .document(clientId.toString())
                .collection("latest Totale et Credit Des Bons")
                .document("latest")
                .get()
                .addOnSuccessListener { document ->
                    val credits = document.getDouble("ancienCredits") ?: 0.0
                    onCreditsFetched(credits)
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error fetching ancienCredits: ", e)
                    onCreditsFetched(0.0)
                }
        } else {
            onCreditsFetched(0.0)
        }
    }

    LaunchedEffect(nomClient) {
        val clientsTableRef = Firebase.database.getReference("G_Clients")
        clientsTableRef.orderByChild("nomClientsSu").equalTo(nomClient).limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val clientData = snapshot.children.first().getValue(ClientsTabelle::class.java)
                        clientId = clientData?.idClientsSu
                        fetchAncienCredits(clientId) { credits ->
                            ancienCredits = credits
                        }
                    } else {
                        // Client doesn't exist, add new client
                        addNewClient(nomClient) { newClientId ->
                            clientId = newClientId
                            fetchAncienCredits(clientId) { credits ->
                                ancienCredits = credits
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error fetching client ID: ${error.message}")
                }
            })
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(clientColor)
            .padding(4.dp)
    ) {
        val namePlace = placesOfArticelsInCamionette.find { it.idPlace == typeEmballage }?.namePlace ?: "غير محدد"
        Text(
            text = "$nomClient - $namePlace",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showPrintDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Print,
                        contentDescription = "Print",
                        tint = Color.Black
                    )
                }
                Text(
                    text = "A.C:${String.format("%.2f", ancienCredits)}Da",
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            IconButton(onClick = onToggleActive) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Check else Icons.Default.FilterList,
                    contentDescription = "Toggle Verification and Filter",
                    tint = Color.Black
                )
            }
            IconButton(onClick = { createEmptyArticle(nomClient) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Empty Article",
                    tint = Color.Black
                )
            }  // New IconButton for updating DaySoldBons without printing
            IconButton(
                onClick = {
                    if (clientId != null) {
                        coroutineScope.launch {
                            headOfViewModels.updateDaySoldBons(
                                clientId = clientId!!,
                                clientName = nomClient,
                                total = clientTotal,
                                payed = clientTotal
                            )
                            // Show a toast to confirm the update
                            Toast.makeText(
                                context,
                                "Bon ajouté sans impression",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "ID client non trouvé",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Save,  // or Icons.Default.Upload
                    contentDescription = "Update DaySoldBons",
                    tint = Color.Black
                )
            }
            IconButton(
                onClick = {
                    if (clientId != null) {
                        showClientsBonUpdateDialog = true
                    } else {
                        Log.e("ClientAndEmballageHeader", "Client ID is null for $nomClient")
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Update Client Credit",
                    tint = Color.Black
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${String.format("%.2f", clientProfit)}Da",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Total: ${String.format("%.2f", clientTotal)}Da",
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (showPrintDialog) {
        AlertDialog(
            onDismissRequest = { showPrintDialog = false },
            title = { Text("Confirm Printing") },
            text = {
                Column {
                    Text("There are $verifiedCount verified articles. Do you want to proceed with printing?")
                    if (ancienCredits < 0) {
                        BlinkingText(text = "Attention: Credit ancien de ${String.format("%.2f", ancienCredits)}Da!")
                    }
                }
            },
// In the print dialog confirmation button click handler
            confirmButton = {
                TextButton(
                    onClick = {
                        val verifiedClientArticles = allArticles.filter { it.nomClient == nomClient && it.verifieState }

                        coroutineScope.launch {
                            updateNonVerifieANonTrouveState(allArticles, nomClient)
                            processClientData(context, nomClient, verifiedClientArticles, ancienCredits = ancienCredits)
                            if (clientId != null) {
                                updateClientsCreditFromHeader(
                                    clientId!!.toInt(),
                                    clientsTotalDeCeBon = clientTotal,
                                    clientsPaymentActuelle = clientTotal,
                                    restCreditDeCetteBon = 0.0,
                                    newBalenceOfCredits = ancienCredits,
                                )

                                // Add this new call to update DaySoldBons
                                headOfViewModels.updateDaySoldBons(
                                    clientId = clientId!!,
                                    clientName = nomClient,
                                    total = clientTotal,
                                    payed = clientTotal
                                )
                            }
                            boardStatistiquesStatViewModel.updateTotaleCreditsClients(clientTotal = clientTotal)
                        }
                        showPrintDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClientsBonUpdateDialog && clientId != null) {
        ClientsCreditDialog(
            showDialog = showClientsBonUpdateDialog,
            onDismiss = { showClientsBonUpdateDialog = false },
            clientsId = clientId,
            clientsName = nomClient,
            clientsTotal = clientTotal,
            coroutineScope = coroutineScope,
            context = context, boardStatistiquesStatViewModel = boardStatistiquesStatViewModel,
        )
    }
}
fun updateNonVerifieANonTrouveState(allArticles: List<ArticlesAcheteModele>, nomClient: String) {
    val nonVerifiedClientArticles = allArticles.filter { it.nomClient == nomClient && !it.verifieState }

    nonVerifiedClientArticles.forEach { article ->
        val articleRef = Firebase.database.getReference("ArticlesAcheteModeleAdapted").child(article.vid.toString())
        articleRef.child("nonTrouveState").setValue(true)
            .addOnSuccessListener {
                Log.d("Firebase", "Article ${article.vid} mis à jour avec succès pour nonTrouveState = true.")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Erreur lors de la mise à jour de l'article ${article.vid}: ", e)
            }
    }
}

@Composable
fun BlinkingText(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1000
                0.7f at 500
            },
            repeatMode = RepeatMode.Reverse
        ), label = "blinking"
    )

    Text(
        text = text,
        color = Color.Red,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier
            .padding(4.dp)
            .alpha(alpha)
    )
}

suspend fun processClientData(context: Context, nomClient: String, clientArticles: List<ArticlesAcheteModele>, ancienCredits: Double) {
    val fireStore = com.google.firebase.ktx.Firebase.firestore
    try {
        // Filter articles for the specific client and with verified state
        val verifiedClientArticles = clientArticles.filter { it.nomClient == nomClient && it.verifieState }

        // Get the date from the first article (assuming all articles have the same date)
        val firstArticle = verifiedClientArticles.firstOrNull()
        val dateString = firstArticle?.dateDachate ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(
            Date()
        )

        val (texteImprimable, totaleBon) = prepareTexteToPrint(nomClient, dateString, verifiedClientArticles, ancienCredits)

        imprimerDonnees(context, texteImprimable.toString(), totaleBon)

        exportToFirestore(fireStore, verifiedClientArticles, nomClient, dateString)

        updateClientsList(fireStore, nomClient)


        Log.d(TAG, "Données imprimées:\n$texteImprimable")

    } catch (e: Exception) {
        Log.e(TAG, "Erreur lors du traitement des données client", e)
    }
}

private fun prepareTexteToPrint(nomClient: String, dateString: String, clientArticles: List<ArticlesAcheteModele>, ancienCredits: Double): Pair<StringBuilder, Double> {
    val texteImprimable = StringBuilder()
    var totaleBon = 0.0
    var pageCounter = 0

    texteImprimable.apply {
        append("<BIG><CENTER>Abdelwahab<BR>")
        append("<BIG><CENTER>JeMla.Com<BR>")
        append("<SMALL><CENTER>0553885037<BR>")
        append("<SMALL><CENTER>Facture<BR>")
        append("<BR>")
        append("<SMALL><CENTER>$nomClient                        $dateString<BR>")
        append("<BR>")
        append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
        append("<SMALL><BOLD>    Quantité      Prix         <NORMAL>Sous-total<BR>")
        append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
    }

    clientArticles.forEachIndexed { index, article ->
        val monPrixVentDetermineBM = if (article.choisirePrixDepuitFireStoreOuBaseBM != "CardFireStor") article.monPrixVentBM else article.monPrixVentFireStoreBM
        val arrondi = round(monPrixVentDetermineBM * 10) / 10
        val subtotal = arrondi * article.totalQuantity
        if (subtotal != 0.0) {
            texteImprimable.apply {
                append("<MEDIUM1><LEFT>${article.nomArticleFinale}<BR>")
                append("    <MEDIUM1><LEFT>${article.totalQuantity}   ")
                append("<MEDIUM1><LEFT>${arrondi}Da   ")
                append("<SMALL>$subtotal<BR>")
                append("<LEFT><NORMAL><MEDIUM1>---------------------<BR>")
            }

            totaleBon += subtotal
            if ((index + 1) % 15 == 0) {
                pageCounter++
                texteImprimable.append("<BR><CENTER>PAGE $pageCounter<BR><BR><BR>")
            }
        }
    }

    texteImprimable.apply {
        append("<LEFT><NORMAL><MEDIUM1>=====================<BR>")
        append("<BR><BR>")
        append("<MEDIUM1><CENTER>Total<BR>")
        append("<MEDIUM3><CENTER>${round(totaleBon * 10) / 10}Da<BR>")
        if (ancienCredits < 0) {
            append("<MEDIUM1><CENTER>Credit Du Compte actuel<BR>")
            append("<MEDIUM2><CENTER>${round(ancienCredits * 10) / 10}Da<BR>")
        }
        append("<CENTER>---------------------<BR>")
        append("<BR><BR><BR>>")
    }

    return Pair(texteImprimable, totaleBon)
}
fun updateClientsCreditFromHeader(
    clientId: Int,
    clientsTotalDeCeBon: Double,
    clientsPaymentActuelle: Double,
    restCreditDeCetteBon: Double,
    newBalenceOfCredits: Double
) {
    val firestore = Firebase.firestore
    val currentDateTime = LocalDateTime.now()
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = currentDateTime.format(dateTimeFormatter)

    val data = hashMapOf(
        "date" to formattedDateTime,
        "totaleDeCeBon" to clientsTotalDeCeBon,
        "payeCetteFoit" to clientsPaymentActuelle,
        "creditFaitDonCeBon" to restCreditDeCetteBon,
        "ancienCredits" to newBalenceOfCredits
    )

    try {
        val documentId = documentIdClientFireStoreClientCreditCB()
        firestore.collection("F_ClientsArticlesFireS")
            .document(clientId.toString())
            .collection("Totale et Credit Des Bons")
            .document(documentId)
            .set(data)
            .addOnSuccessListener {
                Log.d("Firestore", "Document successfully written!")
                // Update the latest document
                firestore.collection("F_ClientsArticlesFireS")
                    .document(clientId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Latest document successfully updated!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating latest document", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error writing document", e)
            }
    } catch (e: Exception) {
        Log.e("Firestore", "Error updating clients credit: ", e)
    }
}
