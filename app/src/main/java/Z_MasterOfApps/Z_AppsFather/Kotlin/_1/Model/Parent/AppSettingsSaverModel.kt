package Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

// AppSettingsSaverModel.kt
@Entity
data class AppSettingsSaverModel(
    @PrimaryKey var id: Long = 0,
    val name: String = "",
    val valueBoolean: Boolean = false,
    val valueLong: Long = 0,
    val date: Date = Date(),
    ) {
    // No-argument constructor for Firebase
    constructor() : this(0)
}
