package Z_MasterOfApps.Kotlin.Model

import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.IgnoreExtraProperties
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    ) {
        var premierCategorieDeCetteGroupe by mutableLongStateOf(0L)
    }

    companion object {
        val caReference = firebaseDatabase
            .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")
            .child("F_CataloguesCategories")

        // Public state flow to maintain list of categories
        private val _categoriesList = MutableStateFlow<List<H_GroupesCategories>>(emptyList())
        private val categoriesList: StateFlow<List<H_GroupesCategories>> = _categoriesList

        fun implimentVMH_GroupesCategories(viewModelInitApp: ViewModelInitApp) {
            viewModelInitApp.viewModelScope.launch {
                val categoriesFlow = onDataBaseChangeListnerAndLoad()
                categoriesFlow.collectLatest { categories ->
                    // Convert List to SnapshotStateList and update the property
                    viewModelInitApp.modelAppsFather.h_GroupesCategories.clear()
                    viewModelInitApp.modelAppsFather.h_GroupesCategories.addAll(categories)
                }
            }
        }

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
