package a_MainAppCompnents.Models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BuyBonModel(
    @PrimaryKey(autoGenerate = true)
    var vid: Long = 0,
    val date: String = "",
    val idSupplier: Long = 0,
    val nameSupplier: String = "",
    val total: Double = 0.0,
    val payed: Double = 0.0,
) {
    // No-argument constructor for Firebase
    constructor() : this(0)
}
