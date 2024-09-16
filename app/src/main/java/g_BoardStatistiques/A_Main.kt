package g_BoardStatistiques
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
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

    LaunchedEffect(viewModel) {
        viewModel.checkAndUpdateStatistics()
    }

    Column(modifier = Modifier) {
        Text(
            "Board Statistics",
            style = MaterialTheme.typography.labelSmall // TODO center
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
        viewModelScope.launch(Dispatchers.IO) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val totalCreditsSuppliers = calculateTotalCreditsSuppliers()
            val totalCreditsClients = calculateTotalCreditsClients()
            updateOrCreateStatistics(currentDate, totalCreditsSuppliers, totalCreditsClients)
        }
    }

    private suspend fun calculateTotalCreditsSuppliers(): Double =
        (1..20).sumOf { getCurrentCreditBalance(it.toLong()) }

    private suspend fun getCurrentCreditBalance(supplierId: Long): Double =
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")
            .get()
            .await()
            .getDouble("ancienCredits") ?: 0.0

    private suspend fun calculateTotalCreditsClients(): Double =
        firestore.collection("F_ClientsArticlesFireS")
            .get()
            .await()
            .documents
            .sumOf { it.getDouble("ancienCredits") ?: 0.0 }

    private suspend fun updateOrCreateStatistics(date: String, totalCreditsSuppliers: Double, totalCreditsClients: Double) {
        firestore.collection("G_Statistiques").document(date)
            .set(Statistiques(
                date = date,
                totaleCreditsSuppliers = totalCreditsSuppliers,
                totaleCreditsClients = totalCreditsClients
            ))
            .await()
        fetchLatestStatistics()
    }

    private suspend fun fetchLatestStatistics() {
        _statistics.value = firestore.collection("G_Statistiques")
            .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
            .toObjects(Statistiques::class.java)
    }
}

data class Statistiques(
    val vid: Int = 0,
    var date: String = "",
    var totaleCreditsSuppliers: Double = 0.0,
    var totaleCreditsClients: Double = 0.0
)
