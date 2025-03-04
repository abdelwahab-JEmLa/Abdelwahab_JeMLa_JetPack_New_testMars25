package Z_MasterOfApps.Z_AppsFather.Kotlin.Learn

data class DetailState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
