package Z_MasterOfApps.A_WorkingOn.A.App

import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.Coordinator
import Z_MasterOfApps.A_WorkingOn.A.App.ViewModel.UiState
import Z_MasterOfApps.Kotlin.Model.I_CategoriesProduits
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                //-->
                //TODO(1): fait au click d ouvrire dialoge won contien state.groupeCategories list
                //au choi un buton ca ce ferm et lance   onCategorieChoisi
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

