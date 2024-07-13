package b_Edite_Base_Donne

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
    onDismiss: () -> Unit // Add onDismiss parameter
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
            }
        )
    }
}
@Composable
fun FilterDialog(
    isFilterApplied: Boolean,
    onToggleFilter: () -> Unit,
    onDismiss: () -> Unit,
    onOrderByDate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter") },
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
                Text("Cancel")
            }
        },
        text = {
            Button(
                onClick = onOrderByDate,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue
                )
            ) {
                Text("Trier par date")
            }
        }
    )
}

