package Z_CodePartageEntreApps.Model

import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.firebaseDatabase
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class D_CouleursEtGoutesProduitsInfos(
    var id: Long = 1,
    var infosDeBase: InfosDeBase = InfosDeBase(),
    var statuesMutable: StatuesMutable = StatuesMutable(),
) {
    @IgnoreExtraProperties
    data class InfosDeBase(
        var nom: String = "Non Defini",
        var imogi: String = "🎨",
    )

    @IgnoreExtraProperties
    data class StatuesMutable(
        var classmentDonsParentList: Long = 0,
        var sonImageNeExistPas: Boolean = false,
        var caRefDonAncienDataBase: String = "H_ColorsArticles",
    )

    companion object {
         val caReference = firebaseDatabase
            .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")
            .child("D_CouleursEtGoutesProduitsInfos")

        fun D_CouleursEtGoutesProduitsInfos.update(
            viewModel: ViewModelInitApp
        ) {
            viewModel.viewModelScope.launch {
                try {
                    // Create a snapshot of the current state
                    val currentState = this@update.copy()

                    // Update local state using clear and addAll
                    val grossistsList = viewModel._modelAppsFather.couleursProduitsInfos
                    val updatedList = grossistsList.toMutableList()
                    val index = updatedList.indexOfFirst { it.id == currentState.id }

                    if (index != -1) {
                        updatedList[index] = currentState
                    } else {
                        // If grossist doesn't exist, add them
                        updatedList.add(currentState)
                    }

                    // Replace entire list
                    grossistsList.clear()
                    grossistsList.addAll(updatedList)

                    // Update Firebase with error handling
                    try {
                        caReference.child(currentState.id.toString())
                            .setValue(currentState)
                            .await()
                    } catch (e: Exception) {
                        // Revert local state if Firebase update fails
                        grossistsList.clear()
                        grossistsList.addAll(
                            if (index != -1) updatedList.toMutableList().apply { this[index] = this@update }
                            else updatedList.dropLast(1)
                        )
                        throw e
                    }

                } catch (e: Exception) {
                    Log.e("D_CouleursEtGoutesProduitsInfos", "Failed to update ", e)
                }
            }
        }
    }

}
