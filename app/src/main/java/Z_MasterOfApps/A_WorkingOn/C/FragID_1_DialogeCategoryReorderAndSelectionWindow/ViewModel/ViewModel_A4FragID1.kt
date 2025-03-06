package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel

import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepository
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategoriesRepository
import android.annotation.SuppressLint
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@SuppressLint("MutableCollectionMutableState")
class ViewModel_A4FragID1(
    private val a_ProduitModelRepository: A_ProduitModelRepository,
    private val i_CategoriesRepository: I_CategoriesRepository,
    val h_GroupesCategoriesRepository: H_GroupesCategoriesRepository
) : ViewModel() {

    // Initialize product model data with safer access
    private val a_ProduitModel by lazy {
        try {
            a_ProduitModelRepository.modelDatas
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    val i_CategoriesProduits by lazy {
        try {
            i_CategoriesRepository.modelDatas
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    fun addNewCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                // Create new category at the beginning of the list
                val newCategory = createNewCategory(categoryName)

                // Update existing categories' positions
                val updatedCategories = updateExistingCategoriesPositions()
                // Update UI state
                i_CategoriesRepository.updateDatas((listOf(newCategory) + updatedCategories).toMutableStateList())
            } catch (e: Exception) {
                // Silent exception handling
            }
        }
    }

    private fun createNewCategory(categoryName: String): I_CategoriesProduits {
        val maxId = i_CategoriesProduits
            .maxOfOrNull { it.id }
            ?: 0

        return I_CategoriesProduits(
            id = maxId + 1,
        ).apply {
            statuesMutable.indexDonsParentList = 0
            infosDeBase.nom = categoryName
        }
    }

    private fun updateExistingCategoriesPositions(): List<I_CategoriesProduits> {
        return i_CategoriesProduits.map { category ->
            // Create a copy of the category
            val updatedCategory = I_CategoriesProduits(
                id = category.id,
                infosDeBase = category.infosDeBase,
                statuesMutable = category.statuesMutable
            )
            // Update the position
            updatedCategory.statuesMutable.indexDonsParentList += 1
            // Return the updated category
            updatedCategory
        }
    }

    fun moveArticlesBetweenCategories(
        fromCategoryId: Long,
        toCategoryId: Long
    ) {
        viewModelScope.launch {
            try {
                // Find maximum classification ID in destination category
                val maxClassificationId = a_ProduitModel
                    .filter { it.parentCategoryId == toCategoryId }
                    .maxOfOrNull { it.indexInParentCategorie } ?: 0

                // Update articles from source category with new category ID and incremented classification
                val updatedArticles = a_ProduitModel.map { article ->
                    when (article.parentCategoryId) {
                        fromCategoryId -> article.apply {
                            parentCategoryId = toCategoryId
                            indexInParentCategorie = maxClassificationId + 1
                        }

                        else -> article
                    }
                }

                a_ProduitModelRepository.updateModelDatas(updatedArticles.toMutableStateList())

                deleteCategorie(fromCategoryId)

            } catch (e: Exception) {
                // Silent exception handling
            }
        }
    }

    private fun deleteCategorie(fromCategoryId: Long) {
        viewModelScope.launch {
            try {
                val updatedCategories = i_CategoriesProduits
                    .filter { it.statuesMutable.indexDonsParentList != fromCategoryId }

                updateClassmentsCategories(updatedCategories)
            } catch (e: Exception) {
                // Silent exception handling
            }
        }
    }

    private fun updateClassmentsCategories(updatedCategories: List<I_CategoriesProduits>) {
        viewModelScope.launch {
            try {
                // Update positions based on current order
                val updatedClassmentCategories = updatedCategories.mapIndexed { index, category ->
                    // Create a copy of the category
                    val updatedCategory = I_CategoriesProduits(
                        id = category.id,
                        infosDeBase = category.infosDeBase,
                        statuesMutable = category.statuesMutable
                    )
                    // Update the position
                    updatedCategory.statuesMutable.indexDonsParentList = (index + 1).toLong()
                    // Return the updated category
                    updatedCategory
                }

                i_CategoriesRepository.updateDatas(updatedClassmentCategories.toMutableStateList())
            } catch (e: Exception) {
                // Silent exception handling
            }
        }
    }

    fun handleCategoryMove(
        holdedIdCate: Long,
        clickedCategoryId: Long,
        onComplete: () -> Unit = {}  // Default empty function for onComplete
    ) {
        viewModelScope.launch {
            try {
                val categories = i_CategoriesProduits.toMutableList()

                val fromIndex = categories.indexOfFirst { it.id == holdedIdCate }
                val toIndex = categories.indexOfFirst { it.id == clickedCategoryId }

                if (fromIndex != -1 && toIndex != -1) {
                    val movedCategory = categories[fromIndex]

                    // Remove and insert at new position
                    categories.removeAt(fromIndex)
                    categories.add(toIndex, movedCategory)

                    // Update positions in database
                    categories.forEachIndexed { index, category ->
                        category.statuesMutable.indexDonsParentList = (index + 1).toLong()
                    }

                    i_CategoriesRepository.updateDatas(categories.toMutableStateList())
                    onComplete()
                }
            } catch (e: Exception) {
                // Silent exception handling
            }
        }
    }

    fun movePlusieurCategories(selectedCategories: List<I_CategoriesProduits>) {
        viewModelScope.launch {
            try {
                if (selectedCategories.isEmpty()) return@launch

                // Get all categories
                val allCategories = i_CategoriesProduits.toMutableList()

                // Sort selected categories by their current position to maintain relative order
                val sortedSelectedCategories = selectedCategories.sortedBy {
                    allCategories.indexOfFirst { cat -> cat.id == it.id }
                }

                // Create a new list without the selected categories
                val remainingCategories = allCategories.filter { category ->
                    !selectedCategories.any { it.id == category.id }
                }.toMutableList()

                // Find the insertion point - if not specified, add at the end
                val targetIndex = remainingCategories.size

                // Insert all selected categories at the target point
                remainingCategories.addAll(targetIndex, sortedSelectedCategories)

                // Update indices for all categories
                remainingCategories.forEachIndexed { index, category ->
                    category.statuesMutable.indexDonsParentList = (index + 1).toLong()
                }

                // Update the repository with the new order
                i_CategoriesRepository.updateDatas(remainingCategories.toMutableStateList())
            } catch (e: Exception) {
                // Silent exception handling
            }
        }
    }
}
