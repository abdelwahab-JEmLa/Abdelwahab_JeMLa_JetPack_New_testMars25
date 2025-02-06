package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_5.id3_AfficheurDesProduitsPourLeColecteur.ViewModel

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase
import Z_MasterOfApps.Kotlin.Model.B_ClientsDataBase.Companion.updateClientsDataBase
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ExtensionVMApp1FragmentId_3(
    val viewModelInitApp: ViewModelInitApp,
) {
    private val clientDataBaseSnapList = viewModelInitApp._modelAppsFather.clientDataBase
    var iDClientAuFilter by mutableStateOf<Long?>(0)
    var clientFocused by mutableStateOf<Pair<B_ClientsDataBase, List<A_ProduitModel>>?>(null)

    fun upButton(index: Int) {
        // Ensure index is valid and there's a previous element
        if (index <= 0 || index >= clientDataBaseSnapList.size) {
            return
        }

        // Get the current and previous clients
        val currentClient = clientDataBaseSnapList[index]
        val prevClient = clientDataBaseSnapList[index - 1]

        // Swap their positions
        val currentPosition = currentClient.statueDeBase.positionDonClientsList
        val prevPosition = prevClient.statueDeBase.positionDonClientsList

        // Update positions
        currentClient.statueDeBase.positionDonClientsList = prevPosition
        prevClient.statueDeBase.positionDonClientsList = currentPosition

        // Update the list order
        clientDataBaseSnapList[index] = prevClient
        clientDataBaseSnapList[index - 1] = currentClient

        // Update both clients in the database
        currentClient.updateClientsDataBase(viewModelInitApp)
        prevClient.updateClientsDataBase(viewModelInitApp)
    }
}
