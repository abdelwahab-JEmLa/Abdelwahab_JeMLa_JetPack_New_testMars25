package c_ManageBonsClients

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ArticlesAcheteModele(
    @PrimaryKey(autoGenerate = true) val vid: Long = 0,
    val idArticle: Long = 0,
    val nomArticleFinale: String = "",
    val monPrixVentBons: Double = 0.0,
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
    var monBenificeBC: Double =  0.0,
    var monBenificeUniterBC: Double =  0.0,
    var monPrixAchatUniterBC: Double =  0.0,
    var monPrixVentUniterBC: Double =  0.0,
    var benificeDivise: Double =  0.0,
    var benificeClient: Double =  0.0,
    var typeEmballage: String = "",

    ) {
    // Constructeur sans argument nécessaire pour Firebase
    constructor() : this(0)
    fun getColumnValue(columnName: String): Any = when (columnName) {
        "clientPrixVentUnite" -> clientPrixVentUnite
        "nmbrunitBC" -> nmbrunitBC
        "monPrixAchatUniterBC" -> monPrixAchatUniterBC
        "prixAchat" -> prixAchat
        "monPrixVentUniterBC" -> monPrixVentUniterBC
        "monPrixVentBons" -> monPrixVentBons
        "monBenificeUniterBC" -> monBenificeUniterBC
        "monBenificeBC" -> monBenificeBC
        "benificeDivise" -> benificeDivise
        "benificeClient" -> benificeClient
        "totalQuantity" -> totalQuantity

        else -> ""
    }
}