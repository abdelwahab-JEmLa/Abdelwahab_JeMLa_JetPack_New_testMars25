package Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.Modules

import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.Model.Navigator
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.View.DetailScreen
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.View.MainScreen
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.DetailCoordinator
import Z_MasterOfApps.Z.Android.Base.App.Section.FragID_1_EditeProduitsBaseDonne.App.ViewModel.MainCoordinator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Créer un navigator qui peut être injecté
    val navigator = remember {
        Navigator { route ->
            navController.navigate(route) {
                // Pop back stack jusqu'à la route principale si on navigue vers "main"
                if (route == "main") {
                    popUpTo("main") { inclusive = true }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            // Injecter le coordinator avec le navigator comme paramètre
            val coordinator =
                org.koin.androidx.compose.get<MainCoordinator> { parametersOf(navigator) }
            MainRoute(coordinator)
        }

        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            // Injecter le coordinator avec le productId et le navigator comme paramètres
            val coordinator = org.koin.androidx.compose.get<DetailCoordinator> {
                parametersOf(productId, navigator)
            }
            DetailRoute(coordinator)
        }
    }
}

// Route principale
@Composable
fun MainRoute(coordinator: MainCoordinator) {
    val state by coordinator.stateFlow.collectAsStateWithLifecycle()

    MainScreen(
        state = state,
        onProductClick = coordinator::onProductClick,
        onRetry = coordinator::onRetry
    )
}

// Route détail
@Composable
fun DetailRoute(coordinator: DetailCoordinator) {
    val state by coordinator.stateFlow.collectAsStateWithLifecycle()

    DetailScreen(
        state = state,
        onBackClick = coordinator::onBackClick,
        onRetry = coordinator::onRetry
    )
}
