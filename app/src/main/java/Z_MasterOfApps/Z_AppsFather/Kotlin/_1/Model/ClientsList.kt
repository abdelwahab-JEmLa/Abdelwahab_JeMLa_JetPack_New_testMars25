package Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model

data class ClientsList(
    val vidSu: Long = 0,
    var idClientsSu: Long = 0,
    var nomClientsSu: String = "",
    var bonDuClientsSu: String = "",
    val couleurSu: String = "#FFFFFF", // Default color
    var currentCreditBalance: Double = 0.0, // New field for current credit balance
) {
    constructor() : this(0)
}
