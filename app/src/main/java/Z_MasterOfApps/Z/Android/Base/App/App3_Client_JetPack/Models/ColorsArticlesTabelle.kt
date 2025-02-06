package Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ColorsArticlesTabelle(
    @PrimaryKey var idColore: Long = 0,
    val nameColore: String = "",
    val iconColore: String = "",
    var classementColore: Int = 0,
    var rankingTmpToDisplaye: Int = 0,
){
    // No-argument constructor for Firebase
    constructor() : this(0)
}
