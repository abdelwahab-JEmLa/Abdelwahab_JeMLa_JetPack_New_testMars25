package b_Edite_Base_Donne

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ab_FilterManager(
    showDialog: Boolean,
    isFilterApplied: Boolean,
    editeBaseDonneViewModel: EditeBaseDonneViewModel
): Boolean {
    var showDialog1 = showDialog
    if (showDialog1) {
        FilterDialog(
            isFilterApplied = isFilterApplied,
            onToggleFilter = {
                editeBaseDonneViewModel.toggleFilter()
                showDialog1 = false
            },
            onDismiss = { showDialog1 = false }
        )
    }
    return showDialog1
}

@Composable
fun FilterDialog(
    isFilterApplied: Boolean,
    onToggleFilter: () -> Unit,
    onDismiss: () -> Unit
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
        }
    )
}
