package a_MainAppCompnents



data class TabelleSupplierArticlesRecived(
    val aa_vid: String = "",
    val a_c_idarticle_c: String = "",
    val a_d_nomarticlefinale_c: String = "",
    var idSupplierTSA: String = "",
    var nomSupplierTSA: String = "",
    var nmbrCat: String = "",
    val trouve_c: String = "",
    val a_u_prix_1_q1_c: String = "",
    var a_q_prixachat_c: String = "",
    val a_l_nmbunite_c: String = "",
    val a_r_prixdevent_c: String = "",
    val nomclient: String = "",
    val datedachate: String = "",
    val a_d_nomarticlefinale_c_1: String = "",
    val quantityachete_c_1: String = "",
    val a_d_nomarticlefinale_c_2: String = "",
    val quantityachete_c_2: String = "",
    val a_d_nomarticlefinale_c_3: String = "",
    val quantityachete_c_3: String = "",
    val a_d_nomarticlefinale_c_4: String = "",
    val quantityachete_c_4: String = "",
    val totalquantity: String = "",
    val etatdecommendcolum: String = "",
    var disponibility: String = ""
) {
    constructor() : this("")

    companion object {
        fun fromMap(map: Map<String, Any?>): TabelleSupplierArticlesRecived {
            return TabelleSupplierArticlesRecived(
                aa_vid = (map["a00"] as? String) ?: "",
                a_c_idarticle_c = (map["a01"] as? String) ?: "",
                a_d_nomarticlefinale_c = (map["a02"] as? String) ?: "",
                idSupplierTSA = (map["a03"] as? String) ?: "",
                nomSupplierTSA = (map["a04"] as? String) ?: "",
                nmbrCat = (map["a05"] as? String) ?: "",
                trouve_c = (map["a06"] as? String) ?: "",
                a_u_prix_1_q1_c = (map["a07"] as? String) ?: "",
                a_q_prixachat_c = (map["a08"] as? String) ?: "",
                a_l_nmbunite_c = (map["a09"] as? String) ?: "",
                a_r_prixdevent_c = (map["a10"] as? String) ?: "",
                nomclient = (map["a11"] as? String) ?: "",
                datedachate = (map["a12"] as? String) ?: "",
                a_d_nomarticlefinale_c_1 = (map["a13"] as? String) ?: "",
                quantityachete_c_1 = (map["a14"] as? String) ?: "",
                a_d_nomarticlefinale_c_2 = (map["a15"] as? String) ?: "",
                quantityachete_c_2 = (map["a16"] as? String) ?: "",
                a_d_nomarticlefinale_c_3 = (map["a17"] as? String) ?: "",
                quantityachete_c_3 = (map["a18"] as? String) ?: "",
                a_d_nomarticlefinale_c_4 = (map["a19"] as? String) ?: "",
                quantityachete_c_4 = (map["a20"] as? String) ?: "",
                totalquantity = (map["a21"] as? String) ?: "",
                etatdecommendcolum = (map["a22"] as? String) ?: "",
                disponibility = (map["a23"] as? String) ?: ""
            )
        }
    }
}
       //telegram
data class TabelleSuppliersSA(
    var vidSupplierSA: Long = 0,
    var nomSupplierSA: String = "",
    var bonDuSupplierSA: String = "",
    val couleurSA: String = "#FFFFFF", // Default color
    var currentCreditBalanceSA: Double = 0.0, // New field for current credit balance
    var isLongTermCreditSA : Boolean = false,
    var ignoreItProdectsSA : Boolean = false,
) {
    constructor() : this(0)
}

data class BaseDonneECBTabelle(
    var idArticleECB: Int = 0,
    var nomArticleFinale: String = "",
    var classementCate: Double = 0.0,
    var nomArab: String = "",
    var autreNomDarticle: String? = null,
    var nmbrCat: Int = 0,
    var couleur1: String? = null,
    var couleur2: String? = null,
    var couleur3: String? = null,
    var couleur4: String? = null,
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
    var funChangeImagsDimention: Boolean = false,
    var nomCategorie: String = "",
    var neaon1: Double = 0.0,
    var lastUpdateState: String = "",
    var cartonState: String = "",
    var dateCreationCategorie: String = "",
    var prixDeVentTotaleChezClient: Double = 0.0,
    var benficeTotaleEntreMoiEtClien: Double = 0.0,
    var benificeTotaleEn2: Double = 0.0,
    var monPrixAchatUniter: Double = 0.0,
    var monPrixVentUniter: Double = 0.0,
    var benificeClient: Double = 0.0,
    var monBeneficeUniter: Double = 0.0
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
            else -> null
        }

        return when (value) {
            is Double -> if (value % 1 == 0.0) value.toInt() else value
            else -> value
        }
    }
}

data class CategoriesTabelleECB(
    val idCategorieInCategoriesTabele: Long = 0,
    var idClassementCategorieInCategoriesTabele: Double = 0.0,
    val nomCategorieInCategoriesTabele: String = "",
)
