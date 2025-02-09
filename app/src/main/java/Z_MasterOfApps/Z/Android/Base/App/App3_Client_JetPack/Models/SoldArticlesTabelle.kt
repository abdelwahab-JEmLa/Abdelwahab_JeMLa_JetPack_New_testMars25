package Z_MasterOfApps.Z.Android.Base.App.App3_Client_JetPack.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SoldArticlesTabelle(
    @PrimaryKey(autoGenerate = true) val vid: Long = 0,
    val idArticle: Long = 0,
    val nameArticle: String = "",
    val clientSoldToItId: Long = 0,
    val date: String = "",
    val color1IdPicked: Long = 0,
    val color1SoldQuantity: Int = 0,
    val color2IdPicked: Long = 0,
    val color2SoldQuantity: Int = 0,
    val color3IdPicked: Long = 0,
    val color3SoldQuantity: Int = 0,
    val color4IdPicked: Long = 0,
    val color4SoldQuantity: Int = 0,
    val confimed: Boolean = false,

    ) {
    constructor() : this(0)
}
