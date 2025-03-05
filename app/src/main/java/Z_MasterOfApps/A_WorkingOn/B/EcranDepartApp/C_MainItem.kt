package Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.UiState
import Z_MasterOfApps.Kotlin.Model.I_CategoriesProduits
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun C_MainItem(
    categorie: I_CategoriesProduits,
    coordinator: Coordinator,
    state: UiState
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        GroupeCategoriesDialog(
            groupeCategories = state.groupesCategories,
            onCategorieChoisi = {
                // Trigger the coordinator function when a group is clicked
                coordinator.onCategorieChoisi(it, categorie.id)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    // Check if this category is the currently held category
    val isSelected = categorie.id == state.holdedCategoryID

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                coordinator.viewModel.updateHoldedCategoryID(categorie.id)
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Add color based on selection state
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = categorie.infosDeBase.nom,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Added IconButton to trigger dialog
            IconButton(
                onClick = { showDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Categories Groups"
                )
            }
        }
    }
}
