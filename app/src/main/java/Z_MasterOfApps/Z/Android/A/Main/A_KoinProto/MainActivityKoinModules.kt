package Z_MasterOfApps.Z.Android.A.Main.A_KoinProto

import Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules.Navigator
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.CategoriesRepository
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.Model.CategoriesRepositoryImpl
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.ViewModel.Coordinator
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.ViewModel.FragmentViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Module pour les repositories
val repositoryModule = module {
    // Singleton: une seule instance pour toute l'application
    single<CategoriesRepository> { CategoriesRepositoryImpl() }
    // Factory: nouvelle instance à chaque fois
}

// Module pour les ViewModels
val viewModelModule = module {
    viewModel { FragmentViewModel(get()) }
}
val coordinatorModule = module {
    factory { (navigator: Navigator) -> Coordinator(get(), navigator) }
}

// Module principal qui regroupe tous les autres modules
val appModule = module {
    // Inclure d'autres modules dans l'ordre correct
    includes(repositoryModule, viewModelModule, coordinatorModule)
}
