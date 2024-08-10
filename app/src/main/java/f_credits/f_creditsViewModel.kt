package f_credits

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CreditsViewModel : ViewModel() {
    private val _supplierList = MutableStateFlow<List<SupplierTabelle>>(emptyList())
    val supplierList: StateFlow<List<SupplierTabelle>> = _supplierList.asStateFlow()

    private val database = FirebaseDatabase.getInstance()
    private val suppliersRef = database.getReference("suppliers")

    init {
        loadSuppliers()
    }

    private fun loadSuppliers() {
        suppliersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newList = snapshot.children.mapNotNull { it.getValue(SupplierTabelle::class.java) }
                _supplierList.value = newList
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                println("Error loading suppliers: ${error.message}")
            }
        })
    }


    data class SupplierTabelle(
        val vidSu: Long = 0,
        var idSupplierSu: Long = 0,
        var nomSupplierSu: String = ""
    ) {
        // No-argument constructor for Firebase
        constructor() : this(0)
    }
}