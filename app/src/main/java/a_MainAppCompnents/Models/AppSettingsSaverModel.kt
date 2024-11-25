package a_MainAppCompnents.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

// AppSettingsSaverModel.kt
@Entity
data class AppSettingsSaverModel(
    @PrimaryKey var id: Long = 0,
    val name: String = "",
    val valueBoolean: Boolean = false,
    val valueLong: Long = 0,
    val dateForNewEntries: String = "", //yyyy-mm-dd
) {
    // No-argument constructor for Firebase
    constructor() : this(0)
}
