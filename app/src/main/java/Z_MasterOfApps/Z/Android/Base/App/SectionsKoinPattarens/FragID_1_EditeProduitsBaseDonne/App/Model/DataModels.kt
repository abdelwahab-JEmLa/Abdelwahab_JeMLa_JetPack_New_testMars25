package Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model

interface Navigator {
    fun navigate(route: String)
    fun goBack() {}
}

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val description: String = ""
)

data class User(val id: String, val name: String)

