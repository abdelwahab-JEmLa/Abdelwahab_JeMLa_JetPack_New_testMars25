package Z_MasterOfApps.Kotlin.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.firebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class H_GroupesCategories(
    var id: Long = 0,
    var infosDeBase: InfosDeBase = InfosDeBase(),
    var statuesMutable: StatuesMutable = StatuesMutable(),
) {
    @IgnoreExtraProperties
    class InfosDeBase(
        var nom: String = "Non Defini",
    )

    @IgnoreExtraProperties
    class StatuesMutable(
        var classmentDonsParentList: Long = 0,
    )

    companion object {
        private val caReference = firebaseDatabase
            .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")
            .child("F_CataloguesCategories")

        // Public state flow to maintain list of categories
        private val _categoriesList = MutableStateFlow<List<H_GroupesCategories>>(emptyList())
        private val categoriesList: StateFlow<List<H_GroupesCategories>> = _categoriesList

        fun onDataBaseChangeListnerAndLoad(
        ): StateFlow<List<H_GroupesCategories>> {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val categories = mutableListOf<H_GroupesCategories>()

                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(H_GroupesCategories::class.java)
                            category?.let {
                                categories.add(it)
                            }
                        }

                        // Update only our local StateFlow - removed viewModel update
                        _categoriesList.value = categories
                    } catch (e: Exception) {
                        // Handle errors in a way that doesn't depend on viewModel
                        println("Failed to load categories: ${e.message}")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Database error: ${error.message}")
                }
            }

            // Attach the listener to the reference
            caReference.addValueEventListener(listener)

            return categoriesList
        }
    }
}
