package d_EntreBonsGro

data class ArticlesBonsGrosTabele(
    val vid: Long = 0,
    var idArticleBG: Long = 0,
    var nomArticleBG: String = "",
    var ancienPrixBG: Double = 0.0,
    var newPrixAchatBG: Double = 0.0,
    var quantityAcheteBG: Double = 0.0,
    var quantityUniterBG: Double = 0.0,
    var subTotaleBG: Double = 0.0,
    var grossisstBonNumBG: Int = 0,
    var uniterLePlusUtiliseStateBG: Boolean = false,
    var erreurCommentaireBG: String = ""
) {
    // Constructeur sans argument nécessaire pour Firebase
    constructor() : this(0)

    fun getColumnValue(columnName: String): Any = when (columnName) {
        "nomArticleBG" -> nomArticleBG
        // ajoute les autres colonnes ici
        else -> ""
    }
}