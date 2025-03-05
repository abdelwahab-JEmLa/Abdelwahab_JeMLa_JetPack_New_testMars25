package Z_MasterOfApps.Z.Android.A.Main.A_KoinProto

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.FragmentViewModel
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Init.InitDataBasesGenerateur
import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepository
import Z_MasterOfApps.Kotlin.Model.A_ProduitModelRepositoryImpl
import Z_MasterOfApps.Kotlin.Model.CategoriesRepository
import Z_MasterOfApps.Kotlin.Model.CategoriesRepositoryImpl
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategoriesRepository
import Z_MasterOfApps.Kotlin.Model.H_GroupesCategoriesRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Module pour les repositories
val repositoryModule = module {
    single<A_ProduitModelRepository> { (scope: CoroutineScope) ->
        A_ProduitModelRepositoryImpl(scope) // Injectez le CoroutineScope ici
    }

    single<CategoriesRepository> { CategoriesRepositoryImpl() }
    single<H_GroupesCategoriesRepository> { H_GroupesCategoriesRepositoryImpl() }
}

// Module pour les ViewModels
val viewModelModule = module {
    viewModel { FragmentViewModel(get(), get(), get()) }
}

val coordinatorModule = module {
    factory { (navigator: Navigator) -> Coordinator(get(), navigator) }
}

// Module pour InitDataBasesGenerateur
val initDataBasesGenerateurModule = module {
    factory { (fragmentViewModel: FragmentViewModel) ->
        InitDataBasesGenerateur(get(), fragmentViewModel)
    }
}

// Module principal qui regroupe tous les autres modules
val appModule = module {
    // Inclure d'autres modules dans l'ordre correct
    includes(repositoryModule, viewModelModule, coordinatorModule, initDataBasesGenerateurModule)
}
