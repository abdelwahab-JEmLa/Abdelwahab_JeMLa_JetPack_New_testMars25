package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.UI

import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.I_CategoriesProduits
import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.ViewModel.ViewModel_A4FragID1
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownloadOff
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BottonsActions(
    multiSelectionMode: Boolean,
    renameOrFusionMode: Boolean,
    selectedCategories: List<I_CategoriesProduits>,
    movingCategory: I_CategoriesProduits?,
    reorderMode: Boolean,
    onMultiSelectionModeChange: (Boolean) -> Unit,
    onRenameOrFusionModeChange: (Boolean) -> Unit,
    onReorderModeActivate: () -> Unit,
    onCancelMove: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ViewModel_A4FragID1
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        OutlinedButton(
            onClick = { onMultiSelectionModeChange(!multiSelectionMode) },
            enabled = !renameOrFusionMode && movingCategory == null
        ) {
            Icon(
                imageVector = if (multiSelectionMode) Icons.Default.Clear else Icons.Default.CheckBox,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (multiSelectionMode) "Cancel" else "Select")
        }

        OutlinedButton(
            onClick = { onRenameOrFusionModeChange(!renameOrFusionMode) },
            enabled = !multiSelectionMode && movingCategory == null
        ) {
            Icon(
                imageVector = if (renameOrFusionMode) Icons.Default.Clear else Icons.Default.Merge,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (renameOrFusionMode) "Cancel" else "Merge")
        }

        OutlinedButton(
            onClick = { viewModel.fitelProduits= !viewModel.fitelProduits},
        ) {
            Icon(
                imageVector = Icons.Default.FileDownloadOff,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (renameOrFusionMode) "Cancel" else "Merge")
        }

        if (selectedCategories.size >= 2 && !reorderMode) {
            Button(
                onClick = onReorderModeActivate
            ) {
                Icon(Icons.Default.SwapVert, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Reo")
            }
        }

        if (reorderMode) {
            Button(onClick = onCancelMove) {
                Icon(Icons.Default.Close, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cancel")
            }
        }
    }
}
