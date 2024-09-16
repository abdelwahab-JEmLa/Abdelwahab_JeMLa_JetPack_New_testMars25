package g_BoardStatistiques
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.google.firebase.firestore.FirebaseFirestore
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

    val scope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }

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

            Button(    //TODO fait que ca soit un petit icon button
                onClick = {
                    scope.launch {
                        isUpdating = true
                        viewModel.checkAndUpdateStatistics(updateDirectly=true)
                        isUpdating = false
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                enabled = !isUpdating
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Update Statistics")
                }
            }
            statistics.lastOrNull()?.let { stat ->
                Text("Date: ${stat.date}", color = Color.White)

                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF009688)) // Turquoise card
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatisticItem(
                            label = "Total Don La Caisse",
                            value = stat.totaleDonsLacaisse,
                            onValueChange = { viewModel.updateTotaleCaisse(it) },
                        )
                        StatisticItem(
                            label = "Total Produit Blocke",
                            value = stat.totaleProduitBlocke,
                            onValueChange = { viewModel.updateTotaleProduitBlocke(it) },
                        )
                        StatisticItem(
                            label = "Total Credits Clients",
                            value = stat.totaleCreditsClients,
                            onValueChange = { viewModel.updateTotaleProduitBlocke(it) },
                            )
                        StatisticItem(
                            label = "Total Credits Long Terme",
                            value = stat.creditsSuppDemiLongTerm  ,
                            onValueChange = { viewModel.sendFromShortToLongSuppCredits(it) }

                        )
                        StatisticItem(
                            label = "Total Credits Suppliers",
                            value = stat.totaleCreditsSuppliers
                        )

                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
                val calculeToTale = (stat.totaleCreditsSuppliers*-1)
                + (stat.totaleCreditsClients*-1)
                + stat.totaleProduitBlocke
                + stat.totaleDonsLacaisse

                Text(
                    "الفائدة الكلية: $${String.format("%.2f",calculeToTale )}",
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
    private val G_StatistiquesRef = Firebase.database.getReference("G_Statistiques")
    private val suppliersRef = Firebase.database.getReference("F_Suppliers")

    private val _statistics = MutableStateFlow<List<Statistiques>>(emptyList())
    val statistics: StateFlow<List<Statistiques>> = _statistics.asStateFlow()

    private val currentDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val firestore = Firebase.firestore

    init {
        intiaStatFromFireBase()
        checkAndUpdateStatistics()
    }

    private fun intiaStatFromFireBase() {
        G_StatistiquesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                _statistics.value = dataSnapshot.children.mapNotNull { it.getValue(Statistiques::class.java) }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    fun checkAndUpdateStatistics(updateDirectly: Boolean=false) {
        viewModelScope.launch {
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val snapshot = G_StatistiquesRef.child(currentDate).get().await()

                if (!snapshot.exists()||updateDirectly) {
                    val (totalSuppliers, totalClients,totaleLong) = calculateTotalCredits()
                    updateOrCreateStatistics(currentDate, totalSuppliers, totalClients,totaleLong,snapshot)
                }
            } catch (exception: Exception) {
                // Handle any errors here
                Log.e("Firebase", "Error checking or updating statistics", exception)
            }
        }
    }

    private fun updateFireBaseRef(stat: Statistiques) {
        viewModelScope.launch {
            G_StatistiquesRef.child(stat.date).setValue(stat)
        }
    }
    fun updateTotaleCreditsClients(clientsPaymentActuelle: Double) {
        _statistics.update { currentStats ->
            currentStats.map { stat ->
                if (stat.date == currentDate) {
                    stat.copy(
                        totaleCreditsClients = stat.totaleCreditsClients - clientsPaymentActuelle,
                        totaleDonsLacaisse =stat.totaleDonsLacaisse + clientsPaymentActuelle
                    )

                } else {
                    stat
                }
            }
        }
        // Update Firebase with the new statistic
        viewModelScope.launch {
            val updatedStat = _statistics.value.find { it.date == currentDate }
            updatedStat?.let { updateFireBaseRef(it) }
        }
    }
    fun sendFromShortToLongSuppCredits(amontSended: Double) {
        _statistics.update { currentStats ->
            currentStats.map { stat ->
                if (stat.date == currentDate) {
                    stat.copy(
                        creditsSuppDemiLongTerm = stat.creditsSuppDemiLongTerm + amontSended,
                        totaleCreditsSuppliers =stat.totaleDonsLacaisse - amontSended
                    )

                } else {
                    stat
                }
            }
        }
        // Update Firebase with the new statistic
        viewModelScope.launch {
            val updatedStat = _statistics.value.find { it.date == currentDate }
            updatedStat?.let { updateFireBaseRef(it) }
        }
    }

    fun updateTotaleProduitBlocke(newValue: Double) {
        viewModelScope.launch {
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val updatedStat = _statistics.value.lastOrNull()?.copy(totaleProduitBlocke = newValue)
                ?: Statistiques(date = currentDate, totaleProduitBlocke = newValue)

            G_StatistiquesRef.child(currentDate).setValue(updatedStat)
        }
    }

    fun updateTotaleCaisse(newValue: Double) {
        _statistics.update { currentStats ->
            currentStats.map { stat ->
                if (stat.date == currentDate) {
                    stat.copy(totaleDonsLacaisse = newValue)
                } else {
                    stat
                }
            }
        }

        // Update Firebase with the new statistic
        viewModelScope.launch {
            val updatedStat = _statistics.value.find { it.date == currentDate }
            updatedStat?.let { updateFireBaseRef(it) }
        }
    }


    private suspend fun calculateTotalCredits(): Triple<Double, Double, Double> {
        return coroutineScope {
            val suppliersDeferred = async { calculateCreditForCollection(firestore, "F_SupplierArticlesFireS") }
            val clientsDeferred = async { calculateCreditClients(firestore, "F_ClientsArticlesFireS") }
            val suppliersLongDeferred = async { calculateCreditLongForCollection(firestore, "F_SupplierArticlesFireS") }

            Triple(suppliersDeferred.await(), clientsDeferred.await(), suppliersLongDeferred.await())
        }
    }
    private suspend fun calculateCreditLongForCollection(firestore: FirebaseFirestore, collection: String): Double {
        return (1..20).sumOf { id ->
            val supplierRef = suppliersRef.child(id.toString())
            val isLongTermSupplier = supplierRef.child("longTermCredit").get().await().getValue(Boolean::class.java) ?: false

            if ( isLongTermSupplier) {
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
    private suspend fun calculateCreditClients(firestore: FirebaseFirestore, collection: String): Double {
        return (1..20).sumOf { id ->

                firestore.collection(collection)
                    .document(id.toString())
                    .collection("latest Totale et Credit Des Bons")
                    .document("latest")
                    .get()
                    .await()
                    .getDouble("ancienCredits") ?: 0.0

        }
    }

    private suspend fun calculateCreditForCollection(firestore: FirebaseFirestore, collection: String): Double {
        return (1..20).sumOf { id ->
            val supplierRef = suppliersRef.child(id.toString())
            val isLongTermSupplier = supplierRef.child("longTermCredit").get().await().getValue(Boolean::class.java) ?: false

            if (!isLongTermSupplier ) {
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
    suspend fun updateOrCreateStatistics(
        date: String,
        totalSuppliers: Double,
        totalClients: Double,
        totaleLong: Double,
        snapshot: DataSnapshot
    ) {
        val newStat = Statistiques(
            date = date,
            totaleCreditsSuppliers = totalSuppliers,
            totaleCreditsClients = totalClients,
            creditsSuppDemiLongTerm = totaleLong,
            totaleDonsLacaisse = snapshot.child("totaleDonsLacaisse").getValue(Double::class.java) ?: 0.0,
            totaleProduitBlocke = snapshot.child("totaleProduitBlocke").getValue(Double::class.java) ?: 0.0
        )
        G_StatistiquesRef.child(date).setValue(newStat).await()
        _statistics.value = listOf(newStat)
    }
}

data class Statistiques(
    val vid: Int = 0,
    var date: String = "",
    var totaleCreditsSuppliers: Double = 0.0,
    var totaleCreditsClients: Double = 0.0    ,
    var totaleProduitBlocke: Double = 0.0,
    var totaleDonsLacaisse: Double = 0.0,
    var creditsSuppDemiLongTerm: Double = 0.0,

    )
