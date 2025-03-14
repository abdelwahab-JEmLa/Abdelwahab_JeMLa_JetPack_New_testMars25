package Z_MasterOfApps.Z.Android.Base.App.Sections.ProtoMars.App.FragID_1_DialogeCategoryReorderAndSelectionWindow.UI

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
 fun SearchField_A4F1(
    filterText: String,
    onFilterTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = filterText,
        onValueChange = onFilterTextChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        placeholder = { Text("Filter or new category") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        singleLine = true
    )
}

