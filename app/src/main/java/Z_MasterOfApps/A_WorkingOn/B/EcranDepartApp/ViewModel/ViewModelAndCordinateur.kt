package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel

import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.FragmentViewModel
import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.UiState
import Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules.Navigator
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class Coordinator_Frag_Depart(
    val viewModel: FragmentViewModel,
    private val navigator: Navigator
) {
    val stateFlow = viewModel.state

}

data class UiState_Frag_Depart(
    val error: String? = null
)

class ViewModel_Frag_Depart(
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    init {
    }


}
