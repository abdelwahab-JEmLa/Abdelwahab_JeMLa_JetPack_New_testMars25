package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.Option.A_OptionsControlsButtons_FragDepart
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun A_MainScreen_FragDepart(
    coordinator: Coordinator,
    onBackToMainApp: () -> Unit
) {
    val viewModel = coordinator.viewModel
    val state by coordinator.stateFlow.collectAsStateWithLifecycle()
    var fabsVisibility by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(     //-->
            //TODO(1): fait que au click au tab de change fabsVisibility
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
            Scaffold { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    //PS Cava etre change
                    B_MainList(state, coordinator,viewModel)
                }
            }

            if (fabsVisibility) {
                A_OptionsControlsButtons_FragDepart(state, coordinator)
            }
        }
    }
}
