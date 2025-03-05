package Z_MasterOfApps.A_WorkingOn.A.App

import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.B.EcranDepartApp.ViewModel.UiState
import Z_MasterOfApps.Kotlin.Model.H_GroupeCategories
import Z_MasterOfApps.Kotlin.Model.I_CategoriesProduits
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
                coordinator.onCategorieChoisi(it,categorie.id)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                showDialog = true
            }),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun GroupeCategoriesDialog(
    groupeCategories: List<H_GroupeCategories>,
    onCategorieChoisi: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Groupes de Catégories") },
        text = {
            LazyColumn {
                items(groupeCategories) { groupe ->
                    Text(
                        text = groupe.infosDeBase.nom,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                onCategorieChoisi(groupe.id)
                            }
                    )
                }
            }
        },
        confirmButton = {
            Text(
                text = "Fermer",
                modifier = Modifier.clickable { onDismiss() }
            )
        }
    )
}
