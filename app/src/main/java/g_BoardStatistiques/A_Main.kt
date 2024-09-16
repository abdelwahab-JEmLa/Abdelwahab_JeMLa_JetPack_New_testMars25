package g_BoardStatistiques
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardBoardStatistiques(viewModel: BoardStatistiquesStatViewModel) {
    val statistics by viewModel.statistics.collectAsState()

    LaunchedEffect(viewModel) { viewModel.checkAndUpdateStatistics() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5)) // Blue card
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Board Statistics",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            statistics.lastOrNull()?.let { stat ->
                Text("Date: ${stat.date}", color = Color.White)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF009688)) // Turquoise card
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatisticItem(
                            label = "Total Produit Blocke",
                            value = stat.totaleProduitBlocke,
                            onValueChange = { viewModel.updateTotaleProduitBlocke(it) },
                        )
                        StatisticItem(
                            label = "Total Credits Suppliers",
                            value = stat.totaleCreditsSuppliers
                        )
                        StatisticItem(
                            label = "Total Credits Clients",
                            value = stat.totaleCreditsClients
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                Text(
                    "الفائدة الكلية: $${String.format("%.2f", stat.totaleCreditsSuppliers + stat.totaleCreditsClients- stat.totaleProduitBlocke)}",
                    color = Color.White
                )
            } ?: Text("No statistics available", color = Color.White)
        }
    }
}

@Composable
fun StatisticItem(label: String, value: Double, onValueChange: ((Double) -> Unit)? = null) {
    var isEditing by remember { mutableStateOf(false) }
    var editedValue by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, modifier = Modifier.weight(1f))
        if (isEditing && onValueChange != null) {
            OutlinedTextField(
                value = editedValue,
                onValueChange = { editedValue = it },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                )
            )
            IconButton(onClick = {
                onValueChange(editedValue.toDoubleOrNull() ?: value)
                isEditing = false
            }) {
                Icon(Icons.Default.Check, contentDescription = "Confirm", tint = Color.White)
            }
        } else {
            Text("$${String.format("%.2f", value)}", color = Color.White)
            if (onValueChange != null) {
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
                }
            }
        }
    }
}
class BoardStatistiquesStatViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val _statistics = MutableStateFlow<List<Statistiques>>(emptyList())
    val statistics: StateFlow<List<Statistiques>> = _statistics.asStateFlow()

    fun checkAndUpdateStatistics() {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val latestStat = _statistics.value.lastOrNull()
            if (latestStat == null || latestStat.date != currentDate) {
                val (totalSuppliers, totalClients) = calculateTotalCredits()
                updateOrCreateStatistics(currentDate, totalSuppliers, totalClients)
            }
        }
    }

    fun updateTotaleProduitBlocke(newValue: Double) {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val updatedStat = _statistics.value.lastOrNull()?.copy(totaleProduitBlocke = newValue)
                ?: Statistiques(date = currentDate, totaleProduitBlocke = newValue)

            firestore.collection("G_Statistiques").document(currentDate)
                .set(updatedStat)
                .await()

            _statistics.value = listOf(updatedStat)
        }
    }
    private suspend fun calculateTotalCredits(): Pair<Double, Double> {
        var totalSuppliers = 0.0
        var totalClients = 0.0
        for (i in 1..20) {
            totalSuppliers += getCredit("F_SupplierArticlesFireS", i.toLong())
            totalClients += getCredit("F_ClientsArticlesFireS", i.toLong())
        }
        return Pair(totalSuppliers, totalClients)
    }

    private suspend fun getCredit(collection: String, id: Long): Double =
        firestore.collection(collection)
            .document(id.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")
            .get()
            .await()
            .getDouble("ancienCredits") ?: 0.0

    private suspend fun updateOrCreateStatistics(date: String, totalSuppliers: Double, totalClients: Double) {
        firestore.collection("G_Statistiques").document(date)
            .set(Statistiques(date = date, totaleCreditsSuppliers = totalSuppliers, totaleCreditsClients = totalClients))
            .await()
        _statistics.value = listOf(Statistiques(date = date, totaleCreditsSuppliers = totalSuppliers, totaleCreditsClients = totalClients))
    }
}

data class Statistiques(
    val vid: Int = 0,
    var date: String = "",
    var totaleCreditsSuppliers: Double = 0.0,
    var totaleCreditsClients: Double = 0.0    ,
    var totaleProduitBlocke: Double = 0.0,
)
