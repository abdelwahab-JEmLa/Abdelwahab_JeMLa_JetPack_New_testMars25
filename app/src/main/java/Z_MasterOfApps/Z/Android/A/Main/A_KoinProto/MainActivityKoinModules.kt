package Z_MasterOfApps.Z.Android.A.Main.A_KoinProto

import Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules.Navigator
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.ProductRepository
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.ProductRepositoryImpl
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.ViewModel.Coordinator
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.ViewModel.FragmentViewModel
import Z_MasterOfApps.Z_AppsFather.Kotlin.Learn.DetailCoordinator
import Z_MasterOfApps.Z_AppsFather.Kotlin.Learn.DetailViewModel
import Z_MasterOfApps.Z_AppsFather.Kotlin.Learn.UserRepository
import Z_MasterOfApps.Z_AppsFather.Kotlin.Learn.UserRepositoryImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Module pour les repositories
val repositoryModule = module {
    // Singleton: une seule instance pour toute l'application
    single<ProductRepository> { ProductRepositoryImpl() }
    // Factory: nouvelle instance à chaque fois
    factory<UserRepository> { UserRepositoryImpl(get()) }
}

// Module pour les ViewModels
val viewModelModule = module {
    viewModel { FragmentViewModel(get()) }
    viewModel { parameters -> DetailViewModel(productId = parameters.get(), repository = get()) }
}
val coordinatorModule = module {
    factory { (navigator: Navigator) -> Coordinator(get(), navigator) }
    factory { (productId: String, navigator: Navigator) -> DetailCoordinator(productId, navigator) }
}

// Module principal qui regroupe tous les autres modules
val appModule = module {
    // Inclure d'autres modules dans l'ordre correct
    includes(repositoryModule, viewModelModule, coordinatorModule)
}
