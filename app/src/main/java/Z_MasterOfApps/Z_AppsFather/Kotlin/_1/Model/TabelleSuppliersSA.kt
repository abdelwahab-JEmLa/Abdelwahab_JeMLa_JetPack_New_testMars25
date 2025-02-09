package Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model

data class TabelleSuppliersSA(
    var idSupplierSu: Long = 0,
    var nomSupplierSu: String = "",
    var nomVocaleArabeDuSupplier: String = "",
    var nameInFrenche: String = "",
    var bonDuSupplierSu: String = "",
    val couleurSu: String = "#FFFFFF", // Default color
    var currentCreditBalance: Double = 0.0, // New field for current credit balance
    var longTermCredit : Boolean = false,
    var ignoreItProdects: Boolean = false,
    var classmentSupplier: Double = 0.0,
    ) {
    constructor() : this(0)
}
