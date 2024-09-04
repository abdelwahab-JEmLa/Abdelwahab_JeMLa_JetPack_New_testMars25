package b_Edite_Base_Donne

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun Ab_FilterManager(
    showDialog: Boolean,
    isFilterApplied: Boolean,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    onOrderClick: () -> Unit,  // Le type de paramètre doit correspondre à la liste triée
    onDismiss: () -> Unit,
) {
    if (showDialog) {
        FilterDialog(
            isFilterApplied = isFilterApplied,
            onToggleFilter = {
                editeBaseDonneViewModel.toggleFilter()
                onDismiss()
            },
            onDismiss = onDismiss,
            onOrderByDate = {
                editeBaseDonneViewModel.orderByDateCreation()
                onDismiss()
            },
            onOrderByIdAndFilterByDiponibility = {
                editeBaseDonneViewModel.orderByIdAndFilterByDiponibility()
                onDismiss()
            },
            deletArticlesWher = {
                editeBaseDonneViewModel.deleteFilteredArticles()
                onDismiss()
            },
            orderDateDao = {
                    onOrderClick()
                    onDismiss()

            },
        )
    }
}




@Composable
fun FilterDialog(
    isFilterApplied: Boolean,
    onToggleFilter: () -> Unit,
    onDismiss: () -> Unit,
    onOrderByDate: () -> Unit,
    onOrderByIdAndFilterByDiponibility: () -> Unit,
    deletArticlesWher: () -> Unit,
    orderDateDao: () -> Unit,

    ) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filtre") },
        confirmButton = {
            Button(
                onClick = onToggleFilter,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFilterApplied) Color.Red else Color.Gray
                )
            ) {
                Text(text = if (isFilterApplied) "Enlever le filtre" else "Appliquer le filtre")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Annuler")
            }
        },
        text = {
            Column {
                Button(
                    onClick = onOrderByDate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue
                    )
                ) {
                    Text("Trier par date")
                }
                Button(
                    onClick = onOrderByIdAndFilterByDiponibility,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green
                    )
                ) {
                    Text("Trier par ID et Filtrer par disponibilité")
                }
                Button(
                    onClick = deletArticlesWher,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("deletArticlesWher")
                }
                Button(
                    onClick = orderDateDao,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue
                    )
                ) {
                    Text("Order Par Date par Dao")
                }
            }
        }
    )
}


