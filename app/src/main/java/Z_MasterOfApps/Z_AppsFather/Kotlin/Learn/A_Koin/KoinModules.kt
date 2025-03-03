package Z_MasterOfApps.Z_AppsFather.Kotlin.Learn.A_Koin

import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.Model.Navigator
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.DetailCoordinator
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.DetailViewModel
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.MainCoordinator
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.MainViewModel
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.ProductRepository
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.ProductRepositoryImpl
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.UserRepository
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.UserRepositoryImpl
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
    viewModel { MainViewModel(get()) }
    viewModel { parameters -> DetailViewModel(productId = parameters.get(), repository = get()) }
}
val coordinatorModule = module {
    factory { (navigator: Navigator) -> MainCoordinator(get(), navigator) }
    factory { (productId: String, navigator: Navigator) -> DetailCoordinator(productId, navigator) }
}

// Module principal qui regroupe tous les autres modules
val appModule = module {
    // Inclure d'autres modules dans l'ordre correct
    includes(repositoryModule, viewModelModule, coordinatorModule)
}
