package g_BoardStatistiques

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CardBoardStatistiques(viewModel: BoardStatistiquesStatViewModel) {
    val statistics by viewModel.statistics.collectAsState()
    var isUpdating by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F51B5))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            BoardHeader(
                isUpdating = isUpdating,
                onUpdateClick = {
                    viewModel.viewModelScope.launch {
                        isUpdating = true
                        viewModel.checkAndUpdateStatistics(updateDirectly = true)
                        isUpdating = false
                    }
                }
            )

            statistics.lastOrNull()?.let { stat ->
                StatisticsContent(stat, viewModel)
                TotalCalculations(stat)
            } ?: Text("No statistics available", color = Color.White)
        }
    }
}

@Composable
fun BoardHeader(isUpdating: Boolean, onUpdateClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "Board Statistics",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onUpdateClick, enabled = !isUpdating) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Update Statistics",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun StatisticsContent(stat: Statistiques, viewModel: BoardStatistiquesStatViewModel) {
    Text("Date: ${stat.date}", color = Color.White)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF009688))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            StatisticItem(
                label = "Total Don La Caisse",
                value = stat.totaleDonsLacaisse,
                onValueChange = { viewModel.updateTotaleCaisse(it) }
            )
            StatisticItem(
                label = "Total Produit Blocke",
                value = stat.totaleProduitBlocke,
                onValueChange = { viewModel.updateTotaleProduitBlocke(it) }
            )
            StatisticItem(
                label = "Total Credits Clients",
                value = stat.totaleCreditsClients * -1
            )
            StatisticItem(
                label = "Total Credits للقادم",
                value = stat.totaleCreditsSuppliers * -1
            )
            if (stat.creditsSuppDemiLongTerm != 0.0) {
                StatisticItem(
                    label = "Total Credits Long Terme",
                    value = stat.creditsSuppDemiLongTerm * -1,
                    onValueChange = { viewModel.sendFromShortToLongSuppCredits(it) }
                )
            }
        }
    }
}

@Composable
fun TotalCalculations(stat: Statistiques) {
    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
    val totalShortTerm = stat.totaleCreditsSuppliers * -1 +
            stat.totaleCreditsClients * -1 +
            stat.totaleProduitBlocke +
            stat.totaleDonsLacaisse
    Text(
        "الفائدة الكلية للقادم: $${String.format("%.2f", totalShortTerm)}",
        color = Color.White
    )
    if (stat.creditsSuppDemiLongTerm != 0.0) {
        val totalLongTerm = totalShortTerm + stat.creditsSuppDemiLongTerm * -1
        Text(
            "الفائدة الكلية للبعيد: $${String.format("%.2f", totalLongTerm)}",
            color = Color.White
        )
    }
}

@Composable
fun StatisticItem(label: String, value: Double, onValueChange: ((Double) -> Unit)? = null) {
    var isEditing by remember { mutableStateOf(false) }
    var editedValue by remember { mutableStateOf(value.toString()) }

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
    private val statistiquesRef = Firebase.database.getReference("G_Statistiques")
    private val suppliersRef = Firebase.database.getReference("F_Suppliers")
    private val firestore = Firebase.firestore

    private val _statistics = MutableStateFlow<List<Statistiques>>(emptyList())
    val statistics: StateFlow<List<Statistiques>> = _statistics.asStateFlow()

    private val currentDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    init {
        initStatFromFirebase()
        checkAndUpdateStatistics()
    }

    private fun initStatFromFirebase() {
        statistiquesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _statistics.value = dataSnapshot.children.mapNotNull { it.getValue(Statistiques::class.java) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun checkAndUpdateStatistics(updateDirectly: Boolean = false) {
        viewModelScope.launch {
            try {
                val snapshot = statistiquesRef.child(currentDate).get().await()
                if (!snapshot.exists() || updateDirectly) {
                    val (totalSuppliers, totalClients, totalLong) = calculateTotalCredits()
                    updateOrCreateStatistics(currentDate, totalSuppliers, totalClients, totalLong, snapshot)
                }
            } catch (exception: Exception) {
                // Handle any errors here
            }
        }
    }

    private suspend fun calculateTotalCredits(): Triple<Double, Double, Double> = coroutineScope {
        val suppliersDeferred = async { calculateCreditForCollection("F_SupplierArticlesFireS", false) }
        val clientsDeferred = async { calculateCreditForCollection("F_ClientsArticlesFireS", true) }
        val suppliersLongDeferred = async { calculateCreditForCollection("F_SupplierArticlesFireS", true) }

        Triple(suppliersDeferred.await(), clientsDeferred.await(), suppliersLongDeferred.await())
    }

    private suspend fun calculateCreditForCollection(collection: String, isLongTerm: Boolean): Double {
        return (1..20).sumOf { id ->
            val isLongTermSupplier = suppliersRef.child(id.toString()).child("longTermCredit").get().await().getValue(Boolean::class.java) ?: false
            if (isLongTerm == isLongTermSupplier || collection == "F_ClientsArticlesFireS") {
                firestore.collection(collection)
                    .document(id.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()
                    .getDouble("ancienCredits") ?: 0.0
            } else {
                0.0
            }
        }
    }

    private suspend fun updateOrCreateStatistics(
        date: String,
        totalSuppliers: Double,
        totalClients: Double,
        totalLong: Double,
        snapshot: DataSnapshot
    ) {
        val newStat = Statistiques(
            date = date,
            totaleCreditsSuppliers = totalSuppliers,
            totaleCreditsClients = totalClients,
            creditsSuppDemiLongTerm = totalLong,
            totaleDonsLacaisse = snapshot.child("totaleDonsLacaisse").getValue(Double::class.java) ?: 0.0,
            totaleProduitBlocke = snapshot.child("totaleProduitBlocke").getValue(Double::class.java) ?: 0.0
        )
        statistiquesRef.child(date).setValue(newStat).await()
        _statistics.value = listOf(newStat)
    }

    fun updateTotaleCaisse(newValue: Double) {
        updateStatistic { it.copy(totaleDonsLacaisse = newValue) }
    }

    fun updateTotaleProduitBlocke(newValue: Double) {
        updateStatistic { it.copy(totaleProduitBlocke = newValue) }
    }

    fun sendFromShortToLongSuppCredits(amountSent: Double) {
        updateStatistic {
            it.copy(
                creditsSuppDemiLongTerm = it.creditsSuppDemiLongTerm + amountSent,
                totaleCreditsSuppliers = it.totaleCreditsSuppliers - amountSent
            )
        }
    }
    fun updateTotaleCreditsClients(clientsPaymentActuelle: Double? = null, clientTotal: Double? = null, enleveDeLaCaisse: Double? = null) {
        updateStatistic { stat ->
            stat.copy(
                totaleCreditsClients = stat.totaleCreditsClients + (clientsPaymentActuelle ?: 0.0),
                totaleDonsLacaisse = stat.totaleDonsLacaisse + (clientsPaymentActuelle ?: clientTotal ?: enleveDeLaCaisse ?: 0.0)
            )
        }
    }

    private fun updateStatistic(update: (Statistiques) -> Statistiques) {
        viewModelScope.launch {
            _statistics.update { stats ->
                stats.map { stat ->
                    if (stat.date == currentDate) update(stat) else stat
                }
            }

            // Explicitly update Firebase
            _statistics.value.find { it.date == currentDate }?.let { updatedStat ->
                try {
                    statistiquesRef.child(currentDate).setValue(updatedStat).await()
                    Log.d("Firebase", "Successfully updated statistics in Firebase")
                } catch (e: Exception) {
                    Log.e("Firebase", "Error updating statistics in Firebase", e)
                    // You might want to handle this error, e.g., by showing a message to the user
                }
            }
        }
    }
}

data class Statistiques(
    val vid: Int = 0,
    var date: String = "",
    var totaleCreditsSuppliers: Double = 0.0,
    var totaleCreditsClients: Double = 0.0,
    var totaleProduitBlocke: Double = 0.0,
    var totaleDonsLacaisse: Double = 0.0,
    var creditsSuppDemiLongTerm: Double = 0.0
)
