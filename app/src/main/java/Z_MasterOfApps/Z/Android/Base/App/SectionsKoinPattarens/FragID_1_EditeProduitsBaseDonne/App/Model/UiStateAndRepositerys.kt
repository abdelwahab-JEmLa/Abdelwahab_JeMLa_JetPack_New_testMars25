package Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model

import Z_MasterOfApps.Z_AppsFather.Kotlin.Learn.Product

data class UiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Repository interfaces
interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: String): Product?
}

// Repository implementations
class ProductRepositoryImpl : ProductRepository {
    override suspend fun getProducts(): List<Product> = listOf(
        Product(
            "1",
            "Smartphone Galaxy S23",
            899.99,
            "Un smartphone haut de gamme avec une caméra exceptionnelle"
        ),
        Product(
            "2",
            "Laptop UltraBook Pro",
            1299.99,
            "Ordinateur portable fin et léger avec une excellente autonomie"
        ),
        Product(
            "3",
            "Écouteurs sans fil NoiseCancel",
            199.99,
            "Écouteurs avec réduction de bruit active et son immersif"
        ),
        Product(
            "4",
            "Montre connectée FitTech",
            249.99,
            "Montre connectée avec suivi d'activité et notifications"
        ),
        Product(
            "5",
            "Tablette MediaTab 10",
            349.99,
            "Tablette 10 pouces avec écran haute définition pour le multimédia"
        )
    )

    override suspend fun getProductById(id: String): Product? =
        getProducts().find { it.id == id }
}
