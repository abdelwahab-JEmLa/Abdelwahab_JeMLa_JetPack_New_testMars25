package g_BoardStatistiques
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "Board Statistics",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 3.dp))
        statistics.lastOrNull()?.let { stat ->
            Text("Date: ${stat.date}")
            Text("Total Credits Suppliers: $${String.format("%.2f", stat.totaleCreditsSuppliers)}")
            Text("Total Credits Clients: $${String.format("%.2f", stat.totaleCreditsClients)}")
        } ?: Text("No statistics available")
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
    var totaleCreditsClients: Double = 0.0
)
