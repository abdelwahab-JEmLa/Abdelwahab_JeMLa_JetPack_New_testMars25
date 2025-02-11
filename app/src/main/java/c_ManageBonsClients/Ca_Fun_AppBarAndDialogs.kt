package c_ManageBonsClients

import a_MainAppCompnents.ArticlesAcheteModele
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun ClientSelectionDialog(
    numberedClients: List<Pair<String, String>>,
    onClientSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    onClearFilter: () -> Unit,
    calculateClientProfit: (String) -> Double,
    articles: List<ArticlesAcheteModele>  // Ajout de ce paramètre
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Client") },
        text = {
            LazyColumn {
                item {
                    TextButton(
                        onClick = onClearFilter,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear Filter")
                    }
                }
                items(numberedClients.size) { index ->
                    val (numberedClient, clientName) = numberedClients[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = generateClientColor(clientName)
                        )
                    ) {
                        TextButton(
                            onClick = { onClientSelected(clientName) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(numberedClient, color = Color.Black)
                                Text(
                                    "Bénéfice: ${String.format("%.2f", calculateClientProfit(clientName))}Da",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                // Titre: Calcul et affichage du total client
                                val clientTotal = calculateClientTotal(articles.filter { it.nomClient == clientName })
                                Text(
                                    "Total: ${String.format("%.2f", clientTotal)}Da",
                                    color = Color.Black,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
