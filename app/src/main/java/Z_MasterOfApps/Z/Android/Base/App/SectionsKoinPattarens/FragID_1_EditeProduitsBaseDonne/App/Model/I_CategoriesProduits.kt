package Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model

import com.google.firebase.database.IgnoreExtraProperties

class I_CategoriesProduits(
    var id: Long = 0,
    var infosDeBase: InfosDeBase = InfosDeBase(),
    var statuesMutable: StatuesMutable = StatuesMutable(),
) {
    @IgnoreExtraProperties
    class InfosDeBase(
        var nom: String = "Non Defini",
    )

    @IgnoreExtraProperties
    class StatuesMutable(
        var classmentDonsParentList: Long = 0,
    )
}

