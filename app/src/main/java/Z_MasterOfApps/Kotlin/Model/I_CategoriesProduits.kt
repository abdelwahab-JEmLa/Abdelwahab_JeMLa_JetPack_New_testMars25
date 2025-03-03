package Z_MasterOfApps.Kotlin.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class I_CategoriesProduits(
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
            .child("I_CategoriesProduits")

        // Private property to store categories for each _ModelAppsFather instance
        private val modelAppsCategoriesMap = mutableMapOf<_ModelAppsFather, SnapshotStateList<I_CategoriesProduits>>()

        // Public state flow to maintain list of categories
        private val _categoriesList = MutableStateFlow<List<I_CategoriesProduits>>(emptyList())
        private val categoriesList: StateFlow<List<I_CategoriesProduits>> = _categoriesList

        fun implimentSelfInViewModel(viewModelInitApp: ViewModelInitApp) {
            viewModelInitApp.viewModelScope.launch {
                val categoriesFlow = onDataBaseChangeListnerAndLoad()
                categoriesFlow.collectLatest { categories ->
                    // Fix: Get the correct reference to the SnapshotStateList
                    val categoriesList = viewModelInitApp._modelAppsFather.i_CategoriesProduits
                    categoriesList.clear()
                    categoriesList.addAll(categories)

                    // Update the map with the latest reference
                    modelAppsCategoriesMap[viewModelInitApp._modelAppsFather] = categoriesList
                }
            }
        }

        // Get or create categories list for a specific _ModelAppsFather instance
        fun getCategoriesList(modelAppsFather: _ModelAppsFather): SnapshotStateList<I_CategoriesProduits> {
            return modelAppsCategoriesMap.getOrPut(modelAppsFather) {
                emptyList<I_CategoriesProduits>().toMutableStateList()
            }
        }

        private fun onDataBaseChangeListnerAndLoad(
        ): StateFlow<List<I_CategoriesProduits>> {
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val categories = mutableListOf<I_CategoriesProduits>()

                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(I_CategoriesProduits::class.java)
                            category?.let {
                                categories.add(it)
                            }
                        }

                        // Sort categories by position (classmentDonsParentList)
                        categories.sortBy { it.statuesMutable.classmentDonsParentList }

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
