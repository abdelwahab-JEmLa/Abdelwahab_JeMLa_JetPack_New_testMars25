package Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.ViewModel

import Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules.Navigator
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.CategoriesRepository
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.I_CategoriesProduits
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class Coordinator(
    val viewModel: FragmentViewModel,
    private val navigator: Navigator
) {
    val stateFlow = viewModel.state

    fun onProductClick(categorieId: String) {
        navigator.navigate("detail/$categorieId")
    }
}

data class UiState(
    val categories: List<I_CategoriesProduits> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class FragmentViewModel(private val categoriesRepository: CategoriesRepository) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
        lenceCollecte()
    }

    private fun lenceCollecte() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val categories = categoriesRepository.onDataBaseChangeListnerAndLoad()
                _state.update { it.copy(categories = categories, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
