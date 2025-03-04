package Z_MasterOfApps.A_WorkingOn.A.App.ViewModel

import Z_MasterOfApps.Kotlin.Model.CategoriesRepository
import Z_MasterOfApps.Kotlin.Model.GroupesCategoriesRepository
import Z_MasterOfApps.Kotlin.Model.H_GroupeCategories
import Z_MasterOfApps.Kotlin.Model.I_CategoriesProduits
import Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules.Navigator
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

    fun onCategorieChoisi(categorieId: Long) {
        //-->
        //TODO(1): fai que au click de
    }
}

data class UiState(
    val categories: List<I_CategoriesProduits> = emptyList(),
    val groupeCategories: List<H_GroupeCategories> = emptyList(),
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null
)

class FragmentViewModel(
    private val categoriesRepository: CategoriesRepository,
    private val groupesCategoriesRepository: GroupesCategoriesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        lenceCollecte()
    }

    private fun lenceCollecte() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, progress = 0f) }
            try {
                val (categories, progressFlow) = categoriesRepository.onDataBaseChangeListnerAndLoad()

                // Launch a separate coroutine to collect progress updates
                viewModelScope.launch {
                    progressFlow.collectLatest { progress ->
                        _state.update { it.copy(progress = progress) }
                    }
                }

                // Update state with categories and keep isLoading until progress reaches 100%
                _state.update { it.copy(categories = categories, isLoading = false) }     //-->
                //TODO(1): ajou un load  H_GroupeCategories
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false, progress = 0f) }
            }
        }
    }
    //-->
    //TODO(1): cree une fun au click update idPremierCategorieDeCetteGroupe par l id categorie click
}
