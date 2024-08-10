package f_credits

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FragmentCredits(viewModel: CreditsViewModel = viewModel()) {
    val suppliers by viewModel.supplierList.collectAsState()

    Scaffold(
        topBar = {//TODO fait que ca couleut rouge et text blach
            TopAppBar(
                title = { Text("Credits") },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(suppliers) { supplier ->
                SupplierItem(supplier)
            }
        }
    }
}

@Composable
fun SupplierItem(supplier: CreditsViewModel.SupplierTabelle) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),//TODO donne a chaque ellement une couleur et le text blanche
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${supplier.idSupplierSu}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Name: ${supplier.nomSupplierSu}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}