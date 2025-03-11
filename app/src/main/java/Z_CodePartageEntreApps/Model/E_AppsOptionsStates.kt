package Z_CodePartageEntreApps.Model

import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.firebaseDatabase
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.database.IgnoreExtraProperties

class E_AppsOptionsStates {

    @IgnoreExtraProperties
    class ApplicationEstInstalleDonTelephone {
        var id by mutableStateOf(0)
        var nom by mutableStateOf("")
        var widthScreen by mutableStateOf(0)
        var itsReciverTelephone by mutableStateOf(false)

        companion object {
            val metricsWidthPixels = Resources.getSystem().displayMetrics.widthPixels
            val caReference = E_AppsOptionsStates.caReference
                .child("ApplicationEstInstalleDonTelephone")
        }
    }

    @IgnoreExtraProperties
    class F_PrototypseDeProgramationInfos {
        var vid by mutableLongStateOf(0L)
        var titre by mutableStateOf<String?>(null)
        var dateInString by mutableStateOf("2025-01-01")
        var commentairesDesChangements by mutableStateOf(mutableListOf<String>())
        var idPremierProduitOuCesChangementEstAplique by mutableLongStateOf(0L)

        companion object {
            val caReference = E_AppsOptionsStates.caReference
                .child("F_PrototypseDeProgramationInfos")
        }
    }

    companion object {
        val caReference = firebaseDatabase
            .getReference("0_UiState_3_Host_Package_3_Prototype11Dec")
            .child("E_AppsOptionsStates")
    }
}
