package a_RoomDB

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class BaseDonne(
    @PrimaryKey val idArticle: Int = 0,
    var nomArticleFinale: String = "",
    var classementCate: Double = 0.0,
    val nomArab: String = "",
    val nmbrCat: Int = 0,
    var couleur1: String? = null,
    var couleur2: String? = null,
    var couleur3: String? = null,
    var couleur4: String? = null,
    var nomCategorie2: String? = null,
    val nmbrUnite: Int = 0,
    val nmbrCaron: Int = 0,
    var affichageUniteState: Boolean = false,
    val commmentSeVent: String? = null,
    val afficheBoitSiUniter: String? = null,
    var monPrixAchat: Double = 0.0,
    var clienPrixVentUnite: Double = 0.0,
    val minQuan: Int = 0,
    var monBenfice: Double = 0.0,
    var monPrixVent: Double = 0.0,
    var diponibilityState: String = "",
    val neaon2: String = "",
    var idCategorie: Double = 0.0,
    var funChangeImagsDimention: Boolean = false,
    var nomCategorie: String = "",
    var neaon1: Double = 0.0,
    val lastUpdateState: String = "",
    var cartonState: String = "",
    val dateCreationCategorie: String = ""
) {
    // Constructeur sans argument nécessaire pour Firebase
    constructor() : this(0)
}
