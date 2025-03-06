package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel

import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepository
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategoriesRepository
import android.util.Log
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ViewModel_A4FragID1(
    private val a_ProduitModelRepository: A_ProduitModelRepository,
    private val i_CategoriesRepository: I_CategoriesRepository,
    val h_GroupesCategoriesRepository: H_GroupesCategoriesRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ViewModel_A4FragID1"
    }

    // Initialize product model data with safer access
    private val a_ProduitModel by lazy {
        try {
            a_ProduitModelRepository.modelDatas
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing product data: ${e.message}", e)
            mutableListOf()
        }
    }

    val i_CategoriesProduits by lazy {
        try {
            i_CategoriesRepository.modelDatas
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing categories data: ${e.message}", e)
            mutableListOf()
        }
    }

    // Log initialization for debugging
    init {
        try {
            Log.d(TAG, "ViewModel_A4FragID1 initializing...")
            Log.d(TAG, "Repositories initialized successfully")
            Log.d(TAG, "ViewModel_A4FragID1 initialized with ${i_CategoriesProduits.size} categories")
        } catch (e: Exception) {
            Log.e(TAG, "Error during ViewModel initialization: ${e.message}", e)
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
                Log.d(TAG, "New category added: $categoryName")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding new category: ${e.message}", e)
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
                Log.d(TAG, "Moving articles from category $fromCategoryId to $toCategoryId")

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
                Log.d(TAG, "Articles moved successfully")

                deleteCategorie(fromCategoryId)

            } catch (e: Exception) {
                Log.e(TAG, "Failed to move articles between categories: ${e.message}", e)
            }
        }
    }

    private fun deleteCategorie(fromCategoryId: Long) {
        viewModelScope.launch {
            try {
                val updatedCategories = i_CategoriesProduits
                    .filter { it.statuesMutable.indexDonsParentList != fromCategoryId }

                Log.d(TAG, "Deleting category $fromCategoryId, remaining categories: ${updatedCategories.size}")
                updateClassmentsCategories(updatedCategories)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting category $fromCategoryId: ${e.message}", e)
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
                Log.d(TAG, "Categories reordered successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating category positions: ${e.message}", e)
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
                Log.d(TAG, "Handling category move from $holdedIdCate to $clickedCategoryId")
                val categories = i_CategoriesProduits.toMutableList()

                val fromIndex = categories.indexOfFirst { it.id == holdedIdCate }
                val toIndex = categories.indexOfFirst { it.id == clickedCategoryId }

                if (fromIndex != -1 && toIndex != -1) {
                    val movedCategory = categories[fromIndex]

                    // Remove and insert at new position
                    categories.removeAt(fromIndex)
                    categories.add(toIndex, movedCategory)

                    i_CategoriesRepository.updateDatas(categories.toMutableStateList())

                    // Update positions in database
                    categories.forEachIndexed { index, category ->
                        category.statuesMutable.indexDonsParentList = (index + 1).toLong()
                    }

                    Log.d(TAG, "Category moved successfully")
                    onComplete()
                } else {
                    Log.w(TAG, "Category move failed: fromIndex=$fromIndex, toIndex=$toIndex")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during category move: ${e.message}", e)
            }
        }
    }
}
