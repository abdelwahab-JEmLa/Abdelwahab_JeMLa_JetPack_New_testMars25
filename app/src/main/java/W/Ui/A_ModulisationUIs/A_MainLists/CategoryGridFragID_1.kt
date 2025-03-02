package W.Ui.A_ModulisationUIs.A_MainLists

import W.Ui.A_ModulisationUIs.B_MainItems.CategoryItemFragID_1
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.Archives.CategoriesTabelleECB
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun CategoryGridFragID_1(
    categories: List<CategoriesTabelleECB>,
    selectedCategories: List<CategoriesTabelleECB>,
    movingCategory: CategoriesTabelleECB?,
    heldCategory: CategoriesTabelleECB?,
    reorderMode: Boolean,
    onCategoryClick: (CategoriesTabelleECB) -> Unit,
    modifier: Modifier = Modifier
) {
    val addNewCategoryItem = CategoriesTabelleECB(
        idCategorieInCategoriesTabele = (categories.maxOfOrNull { it.idCategorieInCategoriesTabele }
            ?: 0) + 1,
        nomCategorieInCategoriesTabele = "Add New Category",
        idClassementCategorieInCategoriesTabele = 0
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = PaddingValues(8.dp),
        modifier = modifier
    ) {
        items(listOf(addNewCategoryItem) + categories) { category ->
            CategoryItemFragID_1(
                category = category,
                isSelected = category in selectedCategories,
                isMoving = category == movingCategory,
                isHeld = category == heldCategory,
                isReorderTarget = reorderMode && category !in selectedCategories,
                selectionOrder = selectedCategories.indexOf(category) + 1,
                onClick = { onCategoryClick(category) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview
@Composable
fun CategoryGridPreview() {
    val categories = remember { mutableStateListOf<CategoriesTabelleECB>() }

    // Fetch categories from Firebase
    DisposableEffect(Unit) {
        val database = FirebaseDatabase.getInstance()
        val categoriesRef = database.getReference("H_CategorieTabele")

        val listener = categoriesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fetchedCategories = mutableListOf<CategoriesTabelleECB>()
                for (child in snapshot.children) {
                    val category = child.getValue(CategoriesTabelleECB::class.java)
                    if (category != null) {
                        fetchedCategories.add(category)
                    }
                }

                // Sort categories by idClassementCategorieInCategoriesTabele
                fetchedCategories.sortBy { it.idClassementCategorieInCategoriesTabele }

                categories.clear()
                categories.addAll(fetchedCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle potential errors
            }
        })

        // Cleanup listener when the composable is disposed
        onDispose {
            categoriesRef.removeEventListener(listener)
        }
    }

    CategoryGridFragID_1(
        categories = categories,
        selectedCategories = emptyList(),
        movingCategory = null,
        heldCategory = null,
        reorderMode = false,
        onCategoryClick = {}
    )
}
