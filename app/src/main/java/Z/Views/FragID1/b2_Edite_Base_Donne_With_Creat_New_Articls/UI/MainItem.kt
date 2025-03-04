package Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.UI

import Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.ViewModel.ExtensionVM_A4FragID_1
import Z_MasterOfApps.Kotlin.Model.H_GroupeCategories
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MainItem(
    modifier: Modifier = Modifier,
    groupeCategory: H_GroupeCategories,
    onClick: () -> Unit = {},
    extensionvmA4fragid1: ExtensionVM_A4FragID_1
) {
    Card(
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = groupeCategory.infosDeBase.nom,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Position: ${groupeCategory.statuesMutable.classmentDonsParentList}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}
