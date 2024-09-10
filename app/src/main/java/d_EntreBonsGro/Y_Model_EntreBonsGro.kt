package d_EntreBonsGro

data class EntreBonsGrosTabele(
    val vidBG: Long = 0,
    var idArticleInSectionsOfImageBG: String = "",
    var idArticleBG: Long = 0,
    var nomArticleBG: String = "",
    var ancienPrixBG: Double = 0.0,
    var ancienPrixOnUniterBG: Double = 0.0,
    var newPrixAchatBG: Double = 0.0,
    var quantityAcheteBG: Int = 0,
    var quantityUniterBG: Int = 0,
    var subTotaleBG: Double = 0.0,
    var grossisstBonN: Int = 0,
    var supplierIdBG: Long = 0,
    var supplierNameBG: String = "",
    var uniterCLePlusUtilise: Boolean = false,
    var erreurCommentaireBG: String = "",
    var passeToEndStateBG: Boolean = false,
    var dateCreationBG: String = "",
){
    // Secondary constructor for Firebase
    constructor() : this(0)
}

