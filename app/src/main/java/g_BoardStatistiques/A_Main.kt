package g_BoardStatistiques

import android.util.Log
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardBoardStatistiques(viewModel: BoardStatistiquesStatViewModel) {
    val statistics by viewModel.statistics.collectAsState()

    LaunchedEffect(key1 = viewModel) {
        viewModel.checkAndUpdateStatistics()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Board Statistics",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        statistics.lastOrNull()?.let { latestStat ->
            Text(
                text = "Date: ${latestStat.date}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = "Total Credits: $${String.format("%.2f", latestStat.totaleCredits)}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
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
            val statisticsRef = firestore.collection("G_Statistiques").document(currentDate)

            try {
                val docSnapshot = statisticsRef.get().await()
                if (!docSnapshot.exists()) {
                    val totalCredits = calculateTotalCredits()
                    updateOrCreateStatistics(totalCredits)
                } else {
                    fetchLatestStatistics()
                }
            } catch (e: Exception) {
                Log.e("Firestore", "Error checking statistics: ", e)
            }
        }
    }

    private suspend fun calculateTotalCredits(): Double {
        return withContext(Dispatchers.IO) {
            (1..9).sumOf { supplierId ->
                getCurrentCreditBalance(supplierId.toLong())
            }
        }
    }

    private suspend fun getCurrentCreditBalance(supplierId: Long): Double {
        return withContext(Dispatchers.IO) {
            try {
                val latestDoc = firestore.collection("F_SupplierArticlesFireS")
                    .document(supplierId.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()

                latestDoc.getDouble("ancienCredits") ?: 0.0
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching current credit balance: ", e)
                0.0
            }
        }
    }

    private fun updateOrCreateStatistics(totalCredits: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val statisticsRef = firestore.collection("G_Statistiques").document(currentDate)

            try {
                val newStatistics = Statistiques(date = currentDate, totaleCredits = totalCredits)
                statisticsRef.set(newStatistics).await()
                fetchLatestStatistics() // Refresh the statistics after updating
            } catch (e: Exception) {
                Log.e("Firestore", "Error updating or creating statistics: ", e)
            }
        }
    }

    private fun fetchLatestStatistics() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val latestStats = firestore.collection("G_Statistiques")
                    .orderBy("date", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()
                    .toObjects(Statistiques::class.java)

                _statistics.value = latestStats
            } catch (e: Exception) {
                Log.e("Firestore", "Error fetching latest statistics: ", e)
            }
        }
    }
}

data class Statistiques(
    val vid: Int = 0,
    var date: String = "",
    var totaleCredits: Double = 0.0,
) {
    // No-argument constructor for Firebase
    constructor() : this(0)
}
