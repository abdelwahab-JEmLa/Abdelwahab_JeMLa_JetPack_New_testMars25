package h_FactoryClassemntsArticles

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.abdelwahabjemlajetpack.R
import java.io.File

@Composable
fun CategoryHeader(
    category: CategoriesTabelle,
    isSelected: Boolean,
    onCategoryClick: (CategoriesTabelle) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .clickable { onCategoryClick(category) }
    ) {
        Text(
            text = category.nomCategorieInCategoriesTabele,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ArticleItem(article: ClassementsArticlesTabel, onDisponibilityChange: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clickable { onDisponibilityChange(getNextDisponibilityState(article.diponibilityState)) },
                contentAlignment = Alignment.Center
            ) {
                val imagePath =
                    "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticle}_1"
                val imageExist =
                    listOf("jpg", "webp").firstOrNull { File("$imagePath.$it").exists() }
                GlideImage(
                    model = imageExist?.let { "$imagePath.$it" } ?: R.drawable.blanc,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    it.diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(1000)
                        .thumbnail(0.25f)
                        .fitCenter()
                        .transition(DrawableTransitionOptions.withCrossFade())
                }
                DisponibilityOverlay(article.diponibilityState)
            }
            AutoResizedTextClas(text = article.nomArticleFinale,)
        }
    }
}

@Composable
fun DisponibilityOverlay(state: String) {
    when (state) {
        "Non Dispo" -> OverlayContent(color = Color.Black, icon = Icons.Default.TextDecrease)
        "NonForNewsClients" -> OverlayContent(color = Color.Gray, icon = Icons.Default.Person)
    }
}
fun getNextDisponibilityState(currentState: String): String = when (currentState) {
    "" -> "Non Dispo"
    "Non Dispo" -> "NonForNewsClients"
    else -> ""
}
@Composable
fun OverlayContent(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White)
    }
}
@Composable
fun AutoResizedTextClas(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE
) {
    val initialFontSize = MaterialTheme.typography.bodyMedium.fontSize
    var fontSize by remember { mutableStateOf(initialFontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.drawWithContent { if (readyToDraw) drawContent() },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowHeight) {
                fontSize *= 0.9f
            } else {
                readyToDraw = true
            }
        }
    )
}
