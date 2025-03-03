package Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.ViewModel

import Z_MasterOfApps.Kotlin.Model.H_GroupesCategories
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategories.Companion.caReference
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExtensionVM_A4FragID_1(
    val viewModelInitApp: ViewModelInitApp,
) {
    var h_GroupesCategories = viewModelInitApp._modelAppsFather.h_GroupesCategories

    var afficheDialoge by mutableStateOf(false)

    init {
        // Use collectLatest to convert StateFlow to SnapshotStateList
        viewModelInitApp.viewModelScope.launch {
            val categoriesFlow = H_GroupesCategories.onDataBaseChangeListnerAndLoad()
            categoriesFlow.collectLatest { categories ->
                // Convert List to SnapshotStateList and update the property
                viewModelInitApp._modelAppsFather.h_GroupesCategories.clear()
                viewModelInitApp._modelAppsFather.h_GroupesCategories.addAll(categories)
            }
        }
    }

    // Function to reorder all categories after moving one to top
    fun reorderCategoriesAfterPromotion(promotedCategory: H_GroupesCategories) {
        val currentCategories = h_GroupesCategories

        // Remove the promoted category
        currentCategories.removeIf { it.id == promotedCategory.id }

        // Update positions of all categories
        currentCategories.forEachIndexed { index, category ->
            // Add 1 to index because position 0 is reserved for promoted category
            val newPosition = index + 1L
            if (category.statuesMutable.classmentDonsParentList != newPosition) {
                updateCategoryPosition(category, newPosition)
            }
        }

        // Update the promoted category to position 0
        updateCategoryPosition(promotedCategory, 0)
    }

    // Function to update category position
    private fun updateCategoryPosition(category: H_GroupesCategories, newPosition: Long) {
        // Update in Firebase
        caReference.child(category.id.toString()).child("statuesMutable")
            .child("classmentDonsParentList").setValue(newPosition)
    }
}
