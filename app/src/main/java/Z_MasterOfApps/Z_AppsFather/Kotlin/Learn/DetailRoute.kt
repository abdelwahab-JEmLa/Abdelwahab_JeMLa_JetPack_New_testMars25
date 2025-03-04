package Z_MasterOfApps.Z_AppsFather.Kotlin.Learn

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DetailRoute(coordinator: DetailCoordinator) {
    val state by coordinator.stateFlow.collectAsStateWithLifecycle()

    DetailScreen(
        state = state,
        onBackClick = coordinator::onBackClick,
        onRetry = coordinator::onRetry
    )
}
