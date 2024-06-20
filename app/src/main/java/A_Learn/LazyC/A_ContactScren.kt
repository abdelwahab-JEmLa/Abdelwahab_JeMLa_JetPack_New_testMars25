package A_Learn.LazyC

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.abdelwahabjemlajetpack.R
import java.io.File

@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(5.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        StaggeredPhotoGrid(navController)
    }
}

@Composable
fun StaggeredPhotoGrid(navController: NavController) {
    val itemsIndexedList = listOf(
        "A", "Bggggggggggggggggggggggg", "Cddddddddddddddddddddddd", "D", "E",
        "F", "G", "H", "I", "J"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(itemsIndexedList) { index, _ ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .shadow(8.dp, shape = RoundedCornerShape(4.dp))
                    .graphicsLayer {
                        shape = RoundedCornerShape(4.dp)
                        clip = true
                    }
                    .clickable {
                        navController.navigate("detail_screen/$index")
                    },
                elevation = CardDefaults.cardElevation(15.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                ) {
                    val imagePath = "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${index + 592}_1"
                    LoadImageFromPath(imagePath = imagePath)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Item at index $index is ${index + 592}_1", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun LoadImageFromPath(imagePath: String) {
    val defaultDrawable = R.drawable.neaveau
    val imageExist: String? = when {
        File("$imagePath.jpg").exists() -> "$imagePath.jpg"
        File("$imagePath.webp").exists() -> "$imagePath.webp"
        else -> null
    }

    val painter = rememberAsyncImagePainter(imageExist ?: defaultDrawable)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)

        )
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true)
@Composable
fun PreviewContactScreen() {
    MainScreen(navController = rememberNavController(), modifier = Modifier)
}
