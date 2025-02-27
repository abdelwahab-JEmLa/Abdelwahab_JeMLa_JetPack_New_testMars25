package Z_MasterOfApps.Kotlin.Model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

class F_BonsAchats (
    val vid: Long = 0,
)  {
    @IgnoreExtraProperties
    class ClientBonVentModel(
        vid: Long = 0,
        var clientIdChoisi: Long = 0,
        var produitStatueDeBaseDeChezCeClient: StatueDeBase = StatueDeBase(),
        init_colours_achete: List<ColorAchatModel> = emptyList(),
    ) {
        // Basic information
        var bonStatueDeBase by mutableStateOf(BonStatueDeBase())
        @IgnoreExtraProperties
        data class StatueDeBase(
            var positionDonClientsList: Int = 0,
        )
        // Status management
        @IgnoreExtraProperties
        class BonStatueDeBase {
            var lastUpdateTimestamp: Long by mutableStateOf(System.currentTimeMillis())
        }

        @get:Exclude
        var colours_Achete: SnapshotStateList<ColorAchatModel> =
            init_colours_achete.toMutableStateList()

        var coloursAcheteList: List<ColorAchatModel>
            get() = colours_Achete.toList()
            set(value) {
                colours_Achete.clear()
                colours_Achete.addAll(value)
            }
        @IgnoreExtraProperties
        class ColorAchatModel(
            var vidPosition: Long = 0,
            var couleurId: Long = 0,
            var nom: String = "",
            var quantity_Achete: Int = 0,
            var imogi: String = ""
        )
    }


}
