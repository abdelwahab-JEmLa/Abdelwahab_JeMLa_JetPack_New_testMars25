package b_Edite_Base_Donne

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class BaseDonneStatTabel(
    idArticle: Int,
    nomArticleFinale: String = "",
    classementCate: Double = 0.0,
    nomArab: String = "",
    nmbrCat: Int = 0,
    couleur1: String? = null,
    couleur2: String? = null,
    couleur3: String? = null,
    couleur4: String? = null,
    nomCategorie2: String? = null,
    nmbrUnite: Int = 0,
    nmbrCaron: Int = 0,
    affichageUniteState: Boolean = false,
    commmentSeVent: String? = null,
    afficheBoitSiUniter: String? = null,
    monPrixAchat: Double = 0.0,
    clienPrixVentUnite: Double = 0.0,
    minQuan: Int = 0,
    monBenfice: Double = 0.0,
    monPrixVent: Double = 0.0,
    diponibilityState: String = "",
    neaon2: String = "",
    idCategorie: Double = 0.0,
    funChangeImagsDimention: Boolean = false,
    nomCategorie: String = "",
    neaon1: Double = 0.0,
    lastUpdateState: String = "",
    cartonState: String = "",
    dateCreationCategorie: String = "",
    prixDeVentTotaleChezClient: Double = 0.0,
    benficeTotaleEntreMoiEtClien: Double = 0.0,
    benificeTotaleEn2: Double = 0.0,
    monPrixAchatUniter: Double = 0.0,
    monPrixVentUniter: Double = 0.0,
    benificeClient: Double = 0.0,
    monBeneficeUniter: Double = 0.0,

    ) {
    var idArticle by mutableIntStateOf(idArticle)
    var nomArticleFinale by mutableStateOf(nomArticleFinale)
    var classementCate by mutableDoubleStateOf(classementCate)
    var nomArab by mutableStateOf(nomArab)
    var nmbrCat by mutableIntStateOf(nmbrCat)
    var couleur1 by mutableStateOf(couleur1)
    var couleur2 by mutableStateOf(couleur2)
    var couleur3 by mutableStateOf(couleur3)
    var couleur4 by mutableStateOf(couleur4)
    var nomCategorie2 by mutableStateOf(nomCategorie2)
    var nmbrUnite by mutableIntStateOf(nmbrUnite)
    var nmbrCaron by mutableIntStateOf(nmbrCaron)
    var affichageUniteState by mutableStateOf(affichageUniteState)
    var commmentSeVent by mutableStateOf(commmentSeVent)
    var afficheBoitSiUniter by mutableStateOf(afficheBoitSiUniter)
    var monPrixAchat by mutableDoubleStateOf(monPrixAchat)
    var clienPrixVentUnite by mutableDoubleStateOf(clienPrixVentUnite)
    var minQuan by mutableIntStateOf(minQuan)
    var monBenfice by mutableDoubleStateOf(monBenfice)
    var monPrixVent by mutableDoubleStateOf(monPrixVent)
    var diponibilityState by mutableStateOf(diponibilityState)
    var neaon2 by mutableStateOf(neaon2)
    var idCategorie by mutableDoubleStateOf(idCategorie)
    var funChangeImagsDimention by mutableStateOf(funChangeImagsDimention)
    var nomCategorie by mutableStateOf(nomCategorie)
    var neaon1 by mutableDoubleStateOf(neaon1)
    var lastUpdateState by mutableStateOf(lastUpdateState)
    var cartonState by mutableStateOf(cartonState)
    var dateCreationCategorie by mutableStateOf(dateCreationCategorie)
    var prixDeVentTotaleChezClient by mutableDoubleStateOf(prixDeVentTotaleChezClient)
    var benficeTotaleEntreMoiEtClien by mutableDoubleStateOf(benficeTotaleEntreMoiEtClien)
    var benificeTotaleEn2 by mutableDoubleStateOf(benificeTotaleEn2)
    var monPrixAchatUniter by mutableDoubleStateOf(monPrixAchatUniter)
    var monPrixVentUniter by mutableDoubleStateOf(monPrixVentUniter)
    var benificeClient by mutableDoubleStateOf(benificeClient)
    var monBeneficeUniter by mutableDoubleStateOf(monBeneficeUniter)

    // Fonction pour obtenir la valeur d'une colonne
    fun getColumnValue(columnName: String): Any? {
        return when (columnName) {
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
    }
}


