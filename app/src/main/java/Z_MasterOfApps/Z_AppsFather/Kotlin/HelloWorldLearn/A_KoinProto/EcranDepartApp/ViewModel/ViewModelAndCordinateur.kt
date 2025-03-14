package Z_MasterOfApps.Z_AppsFather.Kotlin.HelloWorldLearn.A_KoinProto.EcranDepartApp.ViewModel

import Z_CodePartageEntreApps.Model.I_CategoriesProduits
import Z_CodePartageEntreApps.Model.I_CategoriesRepository
import Z_CodePartageEntreApps.Model.A_ProduitModelRepository
import Z_CodePartageEntreApps.Model.H_GroupeCategories
import Z_CodePartageEntreApps.Model.H_GroupesCategoriesRepository
import Z_MasterOfApps.Z_AppsFather.Kotlin.HelloWorldLearn.A_KoinProto.EcranDepartApp.Navigator
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Coordinator(
    val viewModel: FragmentViewModel,
    private val navigator: Navigator
) {
    val stateFlow = viewModel.state
}

data class UiState(
    val categories: List<I_CategoriesProduits> = emptyList(),
    var groupesCategories: SnapshotStateList<H_GroupeCategories> =
        emptyList<H_GroupeCategories>().toMutableStateList(),
    var ModeAuClickButton: ModeAuClickButton = Z_MasterOfApps.Z_AppsFather.Kotlin.HelloWorldLearn.A_KoinProto.EcranDepartApp.ViewModel.ModeAuClickButton.ITS_ONE_CATE_IN_HOLD, // Add this line
    var holdedCategoryID: Long = 0, // Add this line
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null
)

enum class ModeAuClickButton(val holdedCategorysID: List<Long> ? =emptyList()) {
    NO_HOLDED,
    MULTI_CATEGORYS,
    ITS_ONE_CATE_IN_HOLD
}

class FragmentViewModel(
    val a_ProduitModelRepository: A_ProduitModelRepository,
    val i_CategoriesRepository: I_CategoriesRepository,
    private val groupesCategoriesRepository: H_GroupesCategoriesRepository
) : ViewModel() {
     val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {

    }

    private fun lenceCollecte() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, progress = 0f) }
            try {
                val (categories, progressFlow) = i_CategoriesRepository.onDataBaseChangeListnerAndLoad()
                val (groupesCategories, groupesProgressFlow) = groupesCategoriesRepository.onDataBaseChangeListnerAndLoad()

                // Launch a separate coroutine to collect progress updates
                viewModelScope.launch {
                    progressFlow.collectLatest { progress ->
                        _state.update { it.copy(progress = progress) }
                    }
                }

                // Collect groupesCategories updates
                viewModelScope.launch {
                    groupesProgressFlow.collectLatest { progress ->
                        _state.update { it.copy(progress = progress) }
                    }
                }

                // Update state with categories and groupesCategories
                _state.update {
                    it.copy(
                        categories = categories,
                        groupesCategories = groupesCategories.toMutableStateList(),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false, progress = 0f) }
            }
        }
    }

    // Function to update the first category ID of the group when a category is clicked
    fun updateFirstCategoryId(groupId: Long, categoryId: Long) {
        viewModelScope.launch {
            val updatedGroups = state.value.groupesCategories.map { group ->
                if (group.id == groupId) {
                    group.statuesMutable.idPremierCategorieDeCetteGroupe = categoryId
                }
                group
            }

            groupesCategoriesRepository.updateDatas(updatedGroups.toMutableStateList())
        }
    }

    fun handelClick(categoryId: Long) {
        viewModelScope.launch {
            _state.update {
                if (it.holdedCategoryID == 0L) {
                    // First selection of category
                    it.copy(holdedCategoryID = categoryId)
                } else {
                    // Trigger index change and update of all categories
                    indexCategorieEtUpdateIndexDeTouTLaListCategorie(it.holdedCategoryID, categoryId)
                    it.copy(holdedCategoryID = 0L) // Reset holded category
                }
            }
        }
    }

    private fun indexCategorieEtUpdateIndexDeTouTLaListCategorie(
        currentCategoryId: Long,
        newCategoryId: Long
    ) {
        // Update the index for moved category and adjust other category indices
        val updatedCategories = state.value.categories.toMutableList()

        val currentIndex = updatedCategories.indexOfFirst { it.id == currentCategoryId }
        val newIndex = updatedCategories.indexOfFirst { it.id == newCategoryId }

        if (currentIndex != -1 && newIndex != -1) {
            // Swap indices in the list
            val currentCategory = updatedCategories[currentIndex]
            val newCategory = updatedCategories[newIndex]

            val tempIndex = currentCategory.statuesMutable.indexDonsParentList
            currentCategory.statuesMutable.indexDonsParentList = newCategory.statuesMutable.indexDonsParentList
            newCategory.statuesMutable.indexDonsParentList = tempIndex

            // Sort the list based on the new indices
            updatedCategories.sortBy { it.statuesMutable.indexDonsParentList }

            // Update the repository with new list
            viewModelScope.launch {
                i_CategoriesRepository.updateDatas(updatedCategories.toMutableStateList())
            }

            _state.value.holdedCategoryID = 0
        }
    }
}
