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

        // Function to update category position
        fun updateCategoryPosition(category: H_GroupesCategories, newPosition: Long) {
            // Update in Firebase
            caReference.child(category.id.toString()).child("statuesMutable")
                .child("classmentDonsParentList").setValue(newPosition)
        }

        // Function to reorder all categories after moving one to top
        fun reorderCategoriesAfterPromotion(promotedCategory: H_GroupesCategories) {
            val currentCategories = _categoriesList.value.toMutableList()

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
    }
}
