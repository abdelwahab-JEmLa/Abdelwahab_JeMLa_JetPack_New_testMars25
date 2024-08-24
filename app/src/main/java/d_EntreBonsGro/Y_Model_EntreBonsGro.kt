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

//TODO utilise cette metode pour ajoute article
//val newVid = (articlesList.maxOfOrNull { it.vidBG } ?: 0) + 1
//    var quantityUniterBG = 1
//
//
//    val currentDate = LocalDate.now().toString()
//
//    val newArticle = supplier?.idSupplierSu?.let {
//        EntreBonsGrosTabele(
//            vidBG = newVid,
//idDividersCorrespondont = //TODO supplierIdBG "-"  +  currentDate + (idDividersCorrespondont : max +1)
//            idArticleBG = 0,
//            nomArticleBG = "",
//            ancienPrixBG = 0.0,
//            newPrixAchatBG = 0.0,
//            quantityAcheteBG = 0.0,
//            quantityUniterBG = 0.0,
//            subTotaleBG = 0.0,
//            grossisstBonN = founisseurNowIs ?: 0,
//            supplierIdBG = it,
//            supplierNameBG = "",
//            uniterCLePlusUtilise = false,
//            erreurCommentaireBG = "",
//            passeToEndStateBG = true,
//            dateCreationBG = currentDate
//        )
//    }
//
//    newArticle?.let {
//        articlesRef.child(newVid.toString()).setValue(it)
//            .addOnSuccessListener {
//                println("New article inserted successfully")
//            }
//            .addOnFailureListener { e ->
//                println("Error inserting new article: ${e.message}")
//            }
//    }