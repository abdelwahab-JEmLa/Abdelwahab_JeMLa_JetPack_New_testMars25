package a_MainAppCompnents

import androidx.room.Entity
import androidx.room.PrimaryKey

data class ColorsArticles(
    val idColore: Long = 0,
    val nameColore: String = "",
    val iconColore: String = "",
    var classementColore: Int = 0
)
data class PlacesOfArticelsInCamionette(
    val idPlace: Long = 0,
    val namePlace: String = "",
    var classement: Int = 0
)

data class TabelleSupplierArticlesRecived(
    val aa_vid: Long = 0,
    var a_c_idarticle_c: Long = 0,
    val a_d_nomarticlefinale_c: String = "",
    var idSupplierTSA: Int = 0,
    var nomSupplierTSA: String? = null,
    var idInStoreOfSupp: Long = 0,
    var nmbrCat: Int = 0,
    val trouve_c: Boolean = false,
    val a_u_prix_1_q1_c: Double = 0.0,
    var a_q_prixachat_c: Double = 0.0,
    val a_l_nmbunite_c: Int = 0,
    val a_r_prixdevent_c: Double = 0.0,
    val nomclient: String = "",
    val datedachate: String = "",
    val a_d_nomarticlefinale_c_1: String = "",
    val quantityachete_c_1: Int = 0,
    val a_d_nomarticlefinale_c_2: String = "",
    val quantityachete_c_2: Int = 0,
    val a_d_nomarticlefinale_c_3: String = "",
    val quantityachete_c_3: Int = 0,
    val a_d_nomarticlefinale_c_4: String = "",
    val quantityachete_c_4: Int = 0,
    val totalquantity: Int = 0,
    var itsInFindedAskSupplierSA: Boolean = false,
    var disponibylityStatInSupplierStore: String = "",
    ) {
    constructor() : this(0L)
}

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

data class DataBaseArticles(
    var idArticle: Int = 0,
    var nomArticleFinale: String = "",
    var classementCate: Double = 0.0,
    var nomArab: String = "",
    var autreNomDarticle: String? = null,
    var nmbrCat: Int = 0,
    var couleur1: String? = null,
    var idcolor1: Long = 0,
    var couleur2: String? = null,
    var idcolor2: Long = 0,
    var couleur3: String? = null,
    var idcolor3: Long = 0,
    var couleur4: String? = null,
    var idcolor4: Long = 0,
    var articleHaveUniteImages: Boolean=false,
    var nomCategorie2: String? = null,
    var nmbrUnite: Int = 0,
    var nmbrCaron: Int = 0,
    var affichageUniteState: Boolean = false,
    var commmentSeVent: String? = null,
    var afficheBoitSiUniter: String? = null,
    var monPrixAchat: Double = 0.0,
    var clienPrixVentUnite: Double = 0.0,
    var minQuan: Int = 0,
    var monBenfice: Double = 0.0,
    var monPrixVent: Double = 0.0,
    var diponibilityState: String = "",
    var neaon2: String = "",
    var idCategorie: Double = 0.0,
    var idArticlePlaceInCamionette: Long = 0,
    var funChangeImagsDimention: Boolean = false,
    var idCategorieNewMetode: Long = 0,
    var articleItIdClassementInItCategorieInHVM: Long = 0,
    var nomCategorie: String = "",
    var idPlaceStandartInStoreSupplier: Long = 0,
    var neaon1: Double = 0.0,
    var lastUpdateState: String = "",
    var lastSupplierIdBuyedFrom: Long = 0,
    var dateLastSupplierIdBuyedFrom: String = "",
    var lastIdSupplierChoseToBuy: Long = 0,
    var dateLastIdSupplierChoseToBuy: String = "",
    var cartonState: String = "",
    var dateCreationCategorie: String = "",
    var prixDeVentTotaleChezClient: Double = 0.0,
    var benficeTotaleEntreMoiEtClien: Double = 0.0,
    var benificeTotaleEn2: Double = 0.0,
    var monPrixAchatUniter: Double = 0.0,
    var monPrixVentUniter: Double = 0.0,
    var benificeClient: Double = 0.0,
    var monBeneficeUniter: Double = 0.0,
    var itsNewArrivale: Boolean = false,
    var imageDimention: String = "",
    ) {
    fun getColumnValue(columnName: String): Any? {
        val value = when (columnName) {
            "nomArticleFinale" -> nomArticleFinale
            "classementCate" -> classementCate
            "nomArab" -> nomArab
            "nmbrCat" -> nmbrCat
            "couleur1" -> couleur1
            "couleur2" -> couleur2
            "couleur3" -> couleur3
            "couleur4" -> couleur4
            "nomCategorie2" -> nomCategorie2
            "nmbrUnite" -> nmbrUnite
            "nmbrCaron" -> nmbrCaron
            "affichageUniteState" -> affichageUniteState
            "commmentSeVent" -> commmentSeVent
            "afficheBoitSiUniter" -> afficheBoitSiUniter
            "monPrixAchat" -> monPrixAchat
            "clienPrixVentUnite" -> clienPrixVentUnite
            "minQuan" -> minQuan
            "monBenfice" -> monBenfice
            "monPrixVent" -> monPrixVent
            "diponibilityState" -> diponibilityState
            "neaon2" -> neaon2
            "idCategorie" -> idCategorie
            "funChangeImagsDimention" -> funChangeImagsDimention
            "nomCategorie" -> nomCategorie
            "neaon1" -> neaon1
            "lastUpdateState" -> lastUpdateState
            "cartonState" -> cartonState
            "dateCreationCategorie" -> dateCreationCategorie
            "prixDeVentTotaleChezClient" -> prixDeVentTotaleChezClient
            "benficeTotaleEntreMoiEtClien" -> benficeTotaleEntreMoiEtClien
            "benificeTotaleEn2" -> benificeTotaleEn2
            "monPrixAchatUniter" -> monPrixAchatUniter
            "monPrixVentUniter" -> monPrixVentUniter
            "benificeClient" -> benificeClient
            "monBeneficeUniter" -> monBeneficeUniter
            "idCategorieNewMetode" -> idCategorieNewMetode
            else -> null
        }

        return when (value) {
            is Double -> if (value % 1 == 0.0) value.toInt() else value
            else -> value
        }
    }
}


data class PlacesOfArticelsInEacheSupplierSrore(
    val idCombinedIdArticleIdSupplier: String = "",
    val idPlace: Long= 0,
    val idArticle: Long = 0,
    val idSupplierSu: Long= 0,
)
data class MapArticleInSupplierStore(
    val idPlace: Long = 0,
    val namePlace: String = "",
    val idSupplierOfStore: Long = 0,
    val inRightOfPlace: Boolean = false,
    val itClassement: Int = 0,
    )

@Entity(tableName = "CategoriesTabelleECB")
data class CategoriesTabelleECB(
    @PrimaryKey(autoGenerate = true)
    val idCategorieInCategoriesTabele: Long = 0,
    val nomCategorieInCategoriesTabele: String = "",
    var idClassementCategorieInCategoriesTabele: Int = 0
) {
    constructor() : this(0, "", 0)
}
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

@Entity
data class SoldArticlesTabelle(
    @PrimaryKey(autoGenerate = true) val vid: Long = 0,
    val idArticle: Long = 0,
    val nameArticle: String = "",
    val clientSoldToItId: Long = 0,
    val date: String = "",
    val color1IdPicked: Long = 0,
    val color1SoldQuantity: Int = 0,
    val color2IdPicked: Long = 0,
    val color2SoldQuantity: Int = 0,
    val color3IdPicked: Long = 0,
    val color3SoldQuantity: Int = 0,
    val color4IdPicked: Long = 0,
    val color4SoldQuantity: Int = 0,
    val confimed: Boolean = false,

    ) {
    constructor() : this(0)
}

@Entity
data class GroupeurBonCommendToSupplierTabele(
    @PrimaryKey(autoGenerate = true) var vid: Long = 0,
    val idArticle: Int,
    val nameArticle: String,
    val color1SoldQuantity: Int,
    val color2SoldQuantity: Int,
    val color3SoldQuantity: Int,
    val color4SoldQuantity: Int,
    val clientSoldToItId: String
)
