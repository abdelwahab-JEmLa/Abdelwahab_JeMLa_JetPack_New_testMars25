package Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.UI

import Z.Views.FragID1.b2_Edite_Base_Donne_With_Creat_New_Articls.ViewModel.ExtensionVM_A4FragID_1
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun A_MainList(
    modifier: Modifier = Modifier,
    extensionvmA4fragid1: ExtensionVM_A4FragID_1?
) {
    var movingItemId by remember { mutableStateOf<Long?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (extensionvmA4fragid1 != null) {
            itemsIndexed(
                items = extensionvmA4fragid1.h_GroupesCategories,
                key = { _, category -> category.id }
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
                        extensionvmA4fragid1=extensionvmA4fragid1,
                        groupeCategory = category,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            if (category.statuesMutable.classmentDonsParentList != 0L) {
                                // Mark this item as moving for animation
                                movingItemId = category.id

                                // Update positions in Firebase
                                extensionvmA4fragid1.reorderCategoriesAfterPromotion(category)
                            }
                        }
                    )
                }
            }
        }
    }
}
