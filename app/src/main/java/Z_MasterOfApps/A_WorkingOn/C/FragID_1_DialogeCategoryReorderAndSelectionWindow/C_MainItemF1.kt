package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.GlideDisplayImageBykeyId
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun C_MainItemF1(
    mainItem: A_ProduitModel,
    modifier: Modifier = Modifier,
    onClickOnMain: () -> Unit = {},
    position: Int? = null,
) {
    val height = 50.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)  // Increased height for better visibility
            .background(
                color = if (position != null)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClickOnMain() },
        contentAlignment = Alignment.Center
    ) {
        // Product Image
        GlideDisplayImageBykeyId(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            imageGlidReloadTigger = mainItem.statuesBase.imageGlidReloadTigger,
            mainItem = mainItem,
            size = height
        )

    }
}
