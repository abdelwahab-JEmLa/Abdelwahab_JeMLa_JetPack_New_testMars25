// ParamatersAppsViewModel.kt
package Z_MasterOfApps.Z_AppsFather.Kotlin._2.ViewModel

import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.ParamatersAppsModel
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ParamatersAppsViewModel : ViewModel() {
    var _paramatersAppsViewModelModel by mutableStateOf(ParamatersAppsModel())
    val paramatersAppsViewModelModel: ParamatersAppsModel get() = _paramatersAppsViewModelModel

    var initializationProgress by mutableFloatStateOf(0f)
    var isInitializing by mutableStateOf(false)
    var initializationComplete by mutableStateOf(false)
    private var createStart by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
    var loadingProgress by mutableFloatStateOf(0f)
    private val _grossistVisibleMntChangeFlow = MutableSharedFlow<Pair<Long, Int>>()
    val grossistVisibleMntChangeFlow = _grossistVisibleMntChangeFlow.asSharedFlow()

    private val grossistVisibleMnt = mutableMapOf<Long, ValueEventListener>()

    private companion object {
        const val TAG = "ViewModelInitApp"
    }

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                isInitializing = true
                initializationProgress = 0f

              //  setupListeners()
                initializationComplete = true
                initializationProgress = 1f
            } catch (e: Exception) {
                Log.e(TAG, "Initialization failed", e)
                handleInitializationError(e)
            } finally {
                isInitializing = false
            }
        }
    }

    /*private fun setupListeners() {
        // Start observing the grossistVisibleMnt value
        observeGrossistVisibleMnt()
    }

    private fun observeGrossistVisibleMnt() {
        val ref = ParamatersAppsModel.refSelfFireBase
            .child("telephoneClientParamaters/grossistVisibleMnt")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newValue = snapshot.getValue(Long::class.java) ?: 0L
                _paramatersAppsViewModelModel.telephoneClientParamaters.selectedGrossist = newValue

                viewModelScope.launch {
                    _grossistVisibleMntChangeFlow.emit(Pair(newValue, 0))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Failed to read grossistVisibleMnt", error.toException())
            }
        }

        ref.addValueEventListener(listener)
        grossistVisibleMnt[0L] = listener // Store listener for cleanup
    }  */

    private fun handleInitializationError(error: Exception) {
        Log.e(TAG, "Initialization error", error)
        initializationProgress = 0f
        initializationComplete = false
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up listeners
        grossistVisibleMnt.forEach { (_, listener) ->
            ParamatersAppsModel.refSelfFireBase
                .child("telephoneClientParamaters/grossistVisibleMnt")
                .removeEventListener(listener)
        }
        grossistVisibleMnt.clear()
    }
}
