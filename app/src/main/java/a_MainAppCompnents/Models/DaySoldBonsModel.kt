package a_MainAppCompnents.Models

    data class DaySoldBonsModel(
        var id: Long = 0,
        val idClient: Long = 0,
        val nameClient: String = "",
        val total: Double = 0.0,
        val payed: Double = 0.0,
        val date: String = "",
    ) {
        // No-argument constructor for Firebase
        constructor() : this(0)
    }
