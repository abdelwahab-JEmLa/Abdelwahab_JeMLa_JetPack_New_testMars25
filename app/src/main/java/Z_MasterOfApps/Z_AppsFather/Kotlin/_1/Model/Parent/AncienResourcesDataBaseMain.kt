package Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Parent

data class AncienResourcesDataBaseMain(
    val produitsDatabase: List<ProduitsAncienDataBaseMain>,
    val soldArticles: List<Ancien_SoldArticlesTabelle_Main>,
    val couleurs_List: List<Ancien_ColorArticle_Main>,
    val clients_List: List<Ancien_ClientsDataBase_Main>
)

data class ProduitsAncienDataBaseMain  (
    var idArticle: Long = 0,
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
    var pret_pour_deplace_au_grossisst: Boolean = false,
    var id_De_Dernier_Grossisst_Choisi: Long = 0,
    var id_De_Avant_Dernier_Grossisst_Choisi_Pour_Si_Evite: Long = 0,
) {
    constructor() : this(0)
}

data class Ancien_SoldArticlesTabelle_Main internal constructor(
    val vid: Long = 0,
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
)
data class Ancien_ColorArticle_Main(
    val idColore: Long = 0,
    val nameColore: String = "",
    val iconColore: String = "",
    var classementColore: Int = 0
)

data class Ancien_ClientsDataBase_Main(
    val vidSu: Long = 0,
    var idClientsSu: Long = 0,
    var position: Int = 0,
    var nomClientsSu: String = "",
    var nameAggregation: String = "",
    var bonDuClientsSu: String = "",
    val couleurSu: String = "#FFFFFF", // Default color
    var currentCreditBalance: Double = 0.0,
    var itsReadyForEdite: Boolean = false,
)
