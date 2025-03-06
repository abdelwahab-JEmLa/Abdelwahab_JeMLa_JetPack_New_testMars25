package Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow

import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Archives.CategoriesTabelleECB
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun CategoryItemFragID_1(
    category: CategoriesTabelleECB,
    isSelected: Boolean,
    isMoving: Boolean,
    isHeld: Boolean,
    isReorderTarget: Boolean,
    selectionOrder: Int,
    onClick: () -> Unit
) {
    val itsAddNewCatItem = category.nomCategorieInCategoriesTabele == "Add New Category"
    val isSpecialCategory = category.idCategorieInCategoriesTabele in 1..3

    val backgroundColor = when {
        isSpecialCategory -> Color.Red
        isHeld -> MaterialTheme.colorScheme.primaryContainer
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        isMoving -> MaterialTheme.colorScheme.tertiaryContainer
        isReorderTarget -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        itsAddNewCatItem -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .border(
                BorderStroke(
                    width = if (isSelected || isHeld || isMoving) 2.dp else 1.dp,
                    color = when {
                        isHeld -> MaterialTheme.colorScheme.primary
                        isSelected -> MaterialTheme.colorScheme.secondary
                        isMoving -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    }
                ),
                shape = MaterialTheme.shapes.medium
            ),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (selectionOrder > 0) {
                Text(
                    text = selectionOrder.toString(),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.small
                        )
                        .padding(4.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                text = category.nomCategorieInCategoriesTabele,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSpecialCategory) Color.White else MaterialTheme.colorScheme.onSurface
            )

            if (itsAddNewCatItem) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
