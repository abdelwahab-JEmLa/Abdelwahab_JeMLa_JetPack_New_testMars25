package Z_MasterOfApps.Z_AppsFather.Kotlin.Learn

import Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules.Navigator
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.ProductRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

class DetailViewModel(
    private val productId: String,
    private val repository: ProductRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    init {
        loadProductDetails()
    }

    private fun loadProductDetails() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val product = repository.getProductById(productId)
                _state.update { it.copy(product = product, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun retry() {
        loadProductDetails()
    }
}

interface UserRepository {
    fun getCurrentUser(): User?
}

class UserRepositoryImpl(private val productRepository: ProductRepository) : UserRepository {
    override fun getCurrentUser(): User = User("1", "John Doe")
}

class DetailCoordinator(
    private val productId: String,
    private val navigator: Navigator
) {
    // Get ViewModel with the productId parameter
    private val viewModel: DetailViewModel by lazy {
        GlobalContext.get().get { parametersOf(productId) }
    }

    val stateFlow = viewModel.state

    fun onBackClick() {
        navigator.navigate("main")
    }

    fun onRetry() {
        viewModel.retry()
    }
}
