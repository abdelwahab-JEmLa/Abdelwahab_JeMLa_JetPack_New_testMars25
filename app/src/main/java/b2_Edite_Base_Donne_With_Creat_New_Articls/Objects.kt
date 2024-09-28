package b2_Edite_Base_Donne_With_Creat_New_Articls

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.abdelwahabjemlajetpack.R
import java.io.File

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun ImageDisplayerWithGlideECB(article: BaseDonneECBTabelle) {
    val imagePath =
        "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.idArticleECB}_1"
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
}

@Composable
fun DisponibilityOverlayECB(state: String) {
    when (state) {
        "Non Dispo" -> OverlayContentECB(color = Color.Black, icon = Icons.Default.TextDecrease)
        "NonForNewsClients" -> OverlayContentECB(color = Color.Gray, icon = Icons.Default.Person)
    }
}
@Composable
fun AutoResizedTextECB(
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
