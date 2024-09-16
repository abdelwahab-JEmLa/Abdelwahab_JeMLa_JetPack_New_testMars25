package g_BoardStatistiques
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
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

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Board Statistics", style = MaterialTheme.typography.titleSmall)
        Divider(modifier = Modifier.padding(vertical = 8.dp))

        statistics.lastOrNull()?.let { stat ->
            Text("Date: ${stat.date}")
            Text("Total Credits: $${String.format("%.2f", stat.totaleCredits)}")
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
            val totalCredits = calculateTotalCredits()
            updateOrCreateStatistics(currentDate, totalCredits)
        }
    }

    private suspend fun calculateTotalCredits(): Double =
        (1..9).sumOf { getCurrentCreditBalance(it.toLong()) }

    private suspend fun getCurrentCreditBalance(supplierId: Long): Double =
        firestore.collection("F_SupplierArticlesFireS")
            .document(supplierId.toString())
            .collection("latest Totale et Credit Des Bons")
            .document("latest")
            .get()
            .await()
            .getDouble("ancienCredits") ?: 0.0

    private suspend fun updateOrCreateStatistics(date: String, totalCredits: Double) {
        firestore.collection("G_Statistiques").document(date)
            .set(Statistiques(date = date, totaleCredits = totalCredits))
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
    var totaleCredits: Double = 0.0
)
