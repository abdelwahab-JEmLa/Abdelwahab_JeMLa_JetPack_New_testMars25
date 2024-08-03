package c_ManageBonsClients

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ArticlesAcheteModele(
    @PrimaryKey(autoGenerate = true) val vid: Long = 0,
    val idArticle: Long = 0,
    val nomArticleFinale: String = "",
    val prixAchat: Double = 0.0,
    val nmbrunitBC: Double = 0.0,
    val clientPrixVentUnite: Double = 0.0,
    val nomClient: String = "",
    val dateDachate: String = "",
    val nomCouleur1: String = "",
    val quantityAcheteCouleur1: Int = 0,
    val nomCouleur2: String = "",
    val quantityAcheteCouleur2: Int = 0,
    val nomCouleur3: String = "",
    val quantityAcheteCouleur3: Int = 0,
    val nomCouleur4: String = "",
    val quantityAcheteCouleur4: Int = 0,
    val totalQuantity: Int = 0,
    val nonTrouveState: Boolean = false,
    val verifieState: Boolean = false,
    var changeCaronState: String = "",
    var monPrixAchatUniterBC: Double =  0.0,
    var benificeDivise: Double =  0.0,

    //Stats
    var typeEmballage: String = "",
    var choisirePrixDepuitFireStoreOuBaseBM: String = "",

    //FireBase PrixEditeur
    val monPrixVentBM: Double = 0.0,
    var monPrixVentUniterBM: Double =  0.0,

    var monBenificeBM: Double =  0.0,
    var monBenificeUniterBM: Double =  0.0,
    var totalProfitBM: Double =  0.0,


    var clientBenificeBM: Double =  0.0,

    //FireStore
    var monPrixVentFireStoreBM: Double =  0.0,
    var monPrixVentUniterFireStoreBM: Double =  0.0,

    var monBenificeFireStoreBM: Double =  0.0,
    var monBenificeUniterFireStoreBM: Double =  0.0,
    var totalProfitFireStoreBM: Double =  0.0,

    var clientBenificeFireStoreBM: Double =  0.0,

    ) {
    // Constructeur sans argument nécessaire pour Firebase
    constructor() : this(0)
    fun getColumnValue(columnName: String): Any = when (columnName) {
        "nomArticleFinale" -> nomArticleFinale

        "clientPrixVentUnite" -> clientPrixVentUnite
        "nmbrunitBC" -> nmbrunitBC
        "prixAchat" -> prixAchat
        "monPrixAchatUniterBC" -> monPrixAchatUniterBC

        "benificeDivise" -> benificeDivise
        "totalQuantity" -> totalQuantity


        //FireBase PrixEditeur
        "monPrixVentBM" -> monPrixVentBM
        "monPrixVentUniterBM" -> monPrixVentUniterBM

        "monBenificeBM" -> monBenificeBM
        "monBenificeUniterBM" -> monBenificeUniterBM
        "totalProfitBM" -> totalProfitBM

        "clientBenificeBM" -> clientBenificeBM

        //FireStore
        "monPrixVentFireStoreBM" -> monPrixVentFireStoreBM
        "monPrixVentUniterFireStoreBM" -> monPrixVentUniterFireStoreBM

        "monBenificeFireStoreBM" -> monBenificeFireStoreBM
        "monBenificeUniterFireStoreBM" -> monBenificeUniterFireStoreBM
        "totalProfitFireStoreBM" -> totalProfitFireStoreBM

        "clientBenificeFireStoreBM" -> clientBenificeFireStoreBM

        else -> ""
    }
}