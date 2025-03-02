package Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.Screen

import Z_MasterOfApps.Kotlin.Model.H_GroupesCategories
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ID1_CataloguesCategories(
    modifier: Modifier = Modifier,
    viewModel: ViewModelInitApp? = null,
    categoriesList: List<H_GroupesCategories> = emptyList()
) {
    MainList(
        modifier = modifier,
        categoriesList = categoriesList
    )
}

@Composable
fun MainList(
    modifier: Modifier = Modifier,
    categoriesList: List<H_GroupesCategories> = emptyList()
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoriesList) { category ->
            MainItem(
                category = category,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MainItem(
    modifier: Modifier = Modifier,
    category: H_GroupesCategories
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = category.infosDeBase.nom,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Position: ${category.statuesMutable.classmentDonsParentList}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(name = "ID1_CataloguesCategories")
@Composable
private fun PreviewID1_CataloguesCategories() {

    val categories = H_GroupesCategories.onDataBaseChangeListnerAndLoad()
        .collectAsState().value

    ID1_CataloguesCategories(
        categoriesList = categories
    )
}
