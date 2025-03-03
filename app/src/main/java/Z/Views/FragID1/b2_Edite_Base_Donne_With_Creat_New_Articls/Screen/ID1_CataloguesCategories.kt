package Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.Screen

import Z_MasterOfApps.Kotlin.Model.H_GroupesCategories
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    // Keep track of which item is being moved for animation
    var movingItemId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(
            items = categoriesList,
            key = { _, category -> category.id }  // Use id as key for stable animations
        ) { index, category ->
            val visibleState = remember {
                MutableTransitionState(true).apply {
                    // For the item being moved, initialize to invisible for entrance animation
                    if (movingItemId == category.id && index == 0) {
                        targetState = false
                    }
                }
            }

            // If this is the moving item and it's at position 0, animate it in
            if (movingItemId == category.id && index == 0) {
                visibleState.targetState = true
                // Reset after animation completes
                movingItemId = null
            }

            AnimatedVisibility(
                visibleState = visibleState,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { -it }  // Slide in from top
                ) + fadeIn(),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 150),
                    targetOffsetY = { -it }  // Slide out to top
                ) + fadeOut()
            ) {
                MainItem(
                    category = category,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (category.statuesMutable.classmentDonsParentList != 0L) {
                            // Mark this item as moving for animation
                            movingItemId = category.id
                            // Update positions in Firebase
                            H_GroupesCategories.reorderCategoriesAfterPromotion(category)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun MainItem(
    modifier: Modifier = Modifier,
    category: H_GroupesCategories,
    onClick: () -> Unit = {}
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
