package Z_MasterOfApps.Z.Android.A.Main.A_KoinProto

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.FragmentViewModel
import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepository
import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepositoryImpl
import Z_MasterOfApps.Kotlin.Model.CategoriesRepositoryImpl
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategoriesRepository
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategoriesRepositoryImpl
import Z_MasterOfApps.Kotlin.Model.I_CategoriesRepository
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Module pour les repositories
val repositoryModule = module {
    // Singleton: une seule instance pour toute l'application
    single<A_ProduitModelRepository> { A_ProduitModelRepositoryImpl() }

    single<I_CategoriesRepository> { CategoriesRepositoryImpl() }
    single<H_GroupesCategoriesRepository> { H_GroupesCategoriesRepositoryImpl() }
}

// Module pour les ViewModels
val viewModelModule = module {
    viewModel { FragmentViewModel(get(), get(), get()) }
    viewModel { ViewModelInitApp(get()) }  // Injection de A_ProduitModelRepository
}

val coordinatorModule = module {
    factory { (navigator: Navigator) -> Coordinator(get(), navigator) }
}

// Module principal qui regroupe tous les autres modules
val appModule = module {
    // Inclure d'autres modules dans l'ordre correct
    includes(repositoryModule, viewModelModule, coordinatorModule )
}
