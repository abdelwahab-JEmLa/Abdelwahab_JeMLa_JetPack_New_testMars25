package Z_MasterOfApps.Z.Android.A.Main.A_KoinProto

import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.FragmentViewModel
import Z_MasterOfApps.Kotlin.Model.CategoriesRepository
import Z_MasterOfApps.Kotlin.Model.CategoriesRepositoryImpl
import Z_MasterOfApps.Kotlin.Model.GroupesCategoriesRepository
import Z_MasterOfApps.Kotlin.Model.GroupesCategoriesRepositoryImpl
import Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules.Navigator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

// Module pour les repositories
val repositoryModule = module {
    // Singleton: une seule instance pour toute l'application
    single<CategoriesRepository> { CategoriesRepositoryImpl() }
    single<GroupesCategoriesRepository> { GroupesCategoriesRepositoryImpl() }

    // Factory: nouvelle instance à chaque fois
}

// Module pour les ViewModels
val viewModelModule = module {
    viewModel { FragmentViewModel(get()) }   //->
    //TODO(FIXME):Fix erreur No value passed for parameter 'groupesCategoriesRepository'
    //-->
    //TODO(1): pk  GroupesCategoriesRepository ne s inject pas normalement koin inject le 
}
val coordinatorModule = module {
    factory { (navigator: Navigator) -> Coordinator(get(), navigator) }
}

// Module principal qui regroupe tous les autres modules
val appModule = module {
    // Inclure d'autres modules dans l'ordre correct
    includes(repositoryModule, viewModelModule, coordinatorModule)
}
