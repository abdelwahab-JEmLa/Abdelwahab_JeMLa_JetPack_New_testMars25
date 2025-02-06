package Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.LogUtils

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model.C_GrossistsDataBase
import android.content.ContentValues.TAG
import android.util.Log

fun logProductFilter(
    product: A_ProduitModel,
    clientId: Long,
    grossists: List<C_GrossistsDataBase>
) {
    Log.d(TAG, """
        Product Filter Analysis:
        ID: ${product.id}
        Name: ${product.nom}
        Client ID Filter: $clientId
        Has BonCommend: ${product.bonCommendDeCetteCota != null}
        Position Check: ${product.bonCommendDeCetteCota?.mutableBasesStates?.cPositionCheyCeGrossit}
        Grossist ID: ${product.bonCommendDeCetteCota?.idGrossistChoisi}
        Grossist Name: ${grossists.find { it.id == product.bonCommendDeCetteCota?.idGrossistChoisi }?.nom}
        Client Orders: ${product.bonsVentDeCetteCota.map { it.clientIdChoisi }.joinToString()}
        Is Carton: ${product.statuesBase.seTrouveAuDernieDuCamionCarCCarton}
        ------------------------
    """.trimIndent())
}
