package Z_MasterOfApps.Z_AppsFather.Kotlin.HelloWorldLearn.A_KoinProto.EcranDepartApp

import Z_MasterOfApps.Z_AppsFather.Kotlin.HelloWorldLearn.A_KoinProto.EcranDepartApp.ViewModel.Coordinator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
    val progressState = remember { mutableFloatStateOf(0f) }

    val loadingContext = remember {
        object {
            fun setLoading(isLoading: Boolean) {
                loadingState.value = isLoading
            }

            fun updateProgress(progress: Float) {
                progressState.floatValue = progress
            }
        }
    }

    // Create a custom navigator with back handling
    val navigator = remember {
        object : Navigator {
            override fun navigate(route: String) {
                if (route == "exit_koin_navigation") {
                    onBackToMainApp()
                } else {
                    navController.navigate(route) {
                        if (route == "main") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            }

            override fun goBack() {
                if (navController.previousBackStackEntry == null) {
                    onBackToMainApp()
                } else {
                    navController.popBackStack()
                }
            }
        }
    }

    // Intercept system back button
    BackHandler {
        if (navController.previousBackStackEntry == null) {
            onBackToMainApp()
        } else {
            navController.popBackStack()
        }
    }

    // Wrap the NavHost with a Box to allow overlay of loading indicator
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "main") {
            composable("main") {
                val coordinator = koinInject<Coordinator> {
                    parametersOf(navigator)
                }

                val state by coordinator.stateFlow.collectAsStateWithLifecycle()

                LaunchedEffect(state.isLoading, state.progress) {
                    loadingContext.setLoading(state.isLoading || state.progress < 1.0f)
                    loadingContext.updateProgress(state.progress)
                }

                A_MainScreen_FragDepart(
                    coordinator = coordinator,
                    onBackToMainApp = onBackToMainApp
                )
            }
        }

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

