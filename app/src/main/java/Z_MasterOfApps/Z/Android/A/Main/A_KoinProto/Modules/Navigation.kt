package Z_MasterOfApps.Z.Android.A.Main.A_KoinProto.Modules

import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.A_MainScreen
import Z_MasterOfApps.Z.Android.Base.App.SectionsKoinPattarens.FragID_1_EditeProduitsBaseDonne.App.ViewModel.Coordinator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

interface Navigator {
    fun navigate(route: String)
    fun goBack() {}
}

@Composable
fun AppNavigationKoin(onBackToMainApp: () -> Unit) {
    val navController = rememberNavController()

    // Create a shared loading state at the navigation level
    val loadingState = remember { mutableStateOf(false) }
    val progressState = remember { mutableStateOf(0f) }

    // Create a loading context that can be passed down to child composables
    val loadingContext = remember {
        object {
            fun setLoading(isLoading: Boolean) {
                loadingState.value = isLoading
            }

            fun updateProgress(progress: Float) {
                progressState.value = progress
            }
        }
    }

    // Create a custom navigator with back handling
    val navigator = remember {
        object : Navigator {
            override fun navigate(route: String) {
                if (route == "exit_koin_navigation") {
                    // Special route to exit Koin navigation
                    onBackToMainApp()
                } else {
                    navController.navigate(route) {
                        // Standard navigation rules
                        if (route == "main") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            }

            // Add a method specifically for back navigation
            override fun goBack() {
                if (navController.previousBackStackEntry == null) {
                    // If we're at the root level, exit Koin navigation
                    onBackToMainApp()
                } else {
                    // Otherwise just pop the back stack
                    navController.popBackStack()
                }
            }
        }
    }

    // Intercept system back button
    BackHandler {
        if (navController.previousBackStackEntry == null) {
            // If we're at the root level, exit Koin navigation
            onBackToMainApp()
        } else {
            // Otherwise just pop the back stack
            navController.popBackStack()
        }
    }

    // Wrap the NavHost with a Box to allow overlay of loading indicator
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                // Inject the coordinator with the navigator using koinInject instead of get
                val coordinator = koinInject<Coordinator> {
                    parametersOf(navigator)
                }

                // Observe coordinator state to update loading state
                val state by coordinator.stateFlow.collectAsStateWithLifecycle()

                // Update loading states based on coordinator state
                LaunchedEffect(state.isLoading, state.progress) {
                    loadingContext.setLoading(state.isLoading || state.progress < 1.0f)
                    loadingContext.updateProgress(state.progress)
                }

                // Create a wrapper for MainRoute that adds our own back button
                MainRouteWithBackNavigation(
                    coordinator = coordinator,
                    onBackToMainApp = onBackToMainApp
                )
            }
        }

        // Loading overlay - displayed on top of everything when loading
        if (loadingState.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progressState.value },
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRouteWithBackNavigation(
    coordinator: Coordinator,
    onBackToMainApp: () -> Unit
) {
    val state by coordinator.stateFlow.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Catalogue de Produits (Koin)") },
                navigationIcon = {
                    IconButton(onClick = onBackToMainApp) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour à l'app principale"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Original A_MainScreen content
            A_MainScreen(state, coordinator)

        }
    }
}
