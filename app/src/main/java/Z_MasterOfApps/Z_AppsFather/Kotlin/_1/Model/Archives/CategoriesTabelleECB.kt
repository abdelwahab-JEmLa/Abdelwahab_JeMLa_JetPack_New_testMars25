package Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Archives

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CategoriesTabelleECB")
data class CategoriesTabelleECB(
    @PrimaryKey(autoGenerate = true)
    val idCategorieInCategoriesTabele: Long = 0,
    val nomCategorieInCategoriesTabele: String = "",
    var idClassementCategorieInCategoriesTabele: Int = 0 ,
    val catalogueParentID: Long = 0,

    ) {
    constructor() : this(0, "", 0)
}
