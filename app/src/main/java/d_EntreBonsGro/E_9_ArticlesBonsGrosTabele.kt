package d_EntreBonsGro

data class EntreBonsGrosTabele(
    val vid: Long = 0,
    var idArticle: Long = 0,
    var nomArticleBG: String = "",
    var ancienPrixBG: Double = 0.0,
    var newPrixAchatBG: Double = 0.0,
    var quantityAcheteBG: Int = 0,
    var quantityUniterBG: Int = 0,
    var subTotaleBG: Double = 0.0,
    var grossisstBonN: Int = 0,
    var uniterCLePlusUtilise: Boolean = false,
    var erreurCommentaireBG: String = ""
) {
    // No-argument constructor for Firebase
    constructor() : this(0)

    fun getColumnValue(columnName: String): Any = when (columnName) {
        "nomArticleBG" -> nomArticleBG
        "ancienPrixBG" -> ancienPrixBG
        "newPrixAchatBG" -> newPrixAchatBG
        "quantityAcheteBG" -> quantityAcheteBG
        "quantityUniterBG" -> quantityUniterBG
        "subTotaleBG" -> subTotaleBG
        "grossisstBonN" -> grossisstBonN
        "uniterCLePlusUtilise" -> uniterCLePlusUtilise
        "erreurCommentaireBG" -> erreurCommentaireBG
        else -> ""
    }
}