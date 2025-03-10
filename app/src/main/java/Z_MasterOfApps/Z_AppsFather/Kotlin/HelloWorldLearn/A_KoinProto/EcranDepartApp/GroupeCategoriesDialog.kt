package Z_MasterOfApps.Z_AppsFather.Kotlin.HelloWorldLearn.A_KoinProto.EcranDepartApp

import Z_MasterOfApps.Kotlin.Model.H_GroupeCategories
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
