package b2_Edite_Base_Donne_With_Creat_New_Articls

import a_MainAppCompnents.BaseDonneECBTabelle
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels

class RepositeryUpdateCalculateColumnsOfCreatAndEditeDataBase {
    fun toggleFilter(currentState: CreatAndEditeInBaseDonnRepositeryModels) =
        currentState.copy(showOnlyWithFilter = !currentState.showOnlyWithFilter)

    fun updateAndCalculateAuthersField(
        textFieldValue: String,
        columnToChange: String,
        article: BaseDonneECBTabelle
    ): BaseDonneECBTabelle {
        val newValue = textFieldValue.toDoubleOrNull() ?: return article
        return article.copy().apply {
            calculateWithoutCondition(columnToChange, textFieldValue, newValue)
            calculateWithCondition(columnToChange, newValue)
        }
    }

    private fun BaseDonneECBTabelle.calculateWithoutCondition(
        columnToChange: String,
        textFieldValue: String,
        newValue: Double
    ) {
        when (columnToChange) {
            "nmbrUnite" -> nmbrUnite = newValue.toInt()
            "clienPrixVentUnite" -> clienPrixVentUnite = newValue
            else -> setField(columnToChange, textFieldValue)
        }

        prixDeVentTotaleChezClient = nmbrUnite * clienPrixVentUnite
        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
    }

    private fun BaseDonneECBTabelle.calculateWithCondition(columnToChange: String, newValue: Double) {
        when (columnToChange) {
            "monPrixVent" -> updateMonPrixVent(newValue)
            "monBenfice" -> updateMonBenfice(newValue)
            "benificeClient" -> updateBenificeClient(newValue)
            "monPrixAchat" -> updateMonPrixAchat(newValue)
            "monPrixAchatUniter" -> updateMonPrixAchatUniter(newValue)
            "monPrixVentUniter" -> updateMonPrixVentUniter(newValue)
            "monBeneficeUniter" -> updateMonBeneficeUniter(newValue)
        }
    }

    private fun BaseDonneECBTabelle.updateMonPrixVent(newValue: Double) {
        monPrixVent = newValue
        monBenfice = monPrixVent - monPrixAchat
        monPrixVentUniter = monPrixVent / nmbrUnite
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.updateMonBenfice(newValue: Double) {
        monBenfice = newValue
        monPrixVent = monBenfice + monPrixAchat
        monPrixVentUniter = monPrixVent / nmbrUnite
        monBeneficeUniter = monBenfice / nmbrUnite
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.updateBenificeClient(newValue: Double) {
        benificeClient = newValue
        monPrixVent = prixDeVentTotaleChezClient - benificeClient
        monBenfice = monPrixVent - monPrixAchat
        monPrixVentUniter = monPrixVent / nmbrUnite
        monBeneficeUniter = monBenfice / nmbrUnite
    }

    private fun BaseDonneECBTabelle.updateMonPrixAchat(newValue: Double) {
        monPrixAchat = newValue
        monPrixAchatUniter = monPrixAchat / nmbrUnite
        monBenfice = monPrixVent - monPrixAchat
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
    }

    private fun BaseDonneECBTabelle.updateMonPrixAchatUniter(newValue: Double) {
        monPrixAchatUniter = newValue
        monPrixAchat = monPrixAchatUniter * nmbrUnite
        monBenfice = monPrixVent - monPrixAchat
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benficeTotaleEntreMoiEtClien = prixDeVentTotaleChezClient - monPrixAchat
        benificeTotaleEn2 = benficeTotaleEntreMoiEtClien / 2
    }

    private fun BaseDonneECBTabelle.updateMonPrixVentUniter(newValue: Double) {
        monPrixVentUniter = newValue
        monPrixVent = monPrixVentUniter * nmbrUnite
        monBenfice = monPrixVent - monPrixAchat
        monBeneficeUniter = monPrixVentUniter - monPrixAchatUniter
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.updateMonBeneficeUniter(newValue: Double) {
        monBeneficeUniter = newValue
        monBenfice = monBeneficeUniter * nmbrUnite
        monPrixVentUniter = monPrixAchatUniter + monBeneficeUniter
        monPrixVent = monPrixVentUniter * nmbrUnite
        benificeClient = prixDeVentTotaleChezClient - monPrixVent
    }

    private fun BaseDonneECBTabelle.setField(fieldName: String, value: String) {
        val field = this::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        when (field.type) {
            Int::class.java -> field.setInt(this, value.toIntOrNull() ?: 0)
            Double::class.java -> field.setDouble(this, value.toDoubleOrNull() ?: 0.0)
            String::class.java -> field.set(this, value)
            Boolean::class.java -> field.setBoolean(this, value.toBoolean())
        }
    }
}
