package b2_Edite_Base_Donne_With_Creat_New_Articls

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class HeadOfViewModelFactory(
    private val context: Context,
    private val creatAndEditeInBaseDonneRepositery: CreatAndEditeInBaseDonneRepositery
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeadOfViewModels::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeadOfViewModels(context, creatAndEditeInBaseDonneRepositery) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
@Composable
fun AutoResizedTextECB(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    fontSize: TextUnit = MaterialTheme.typography.bodyMedium.fontSize
) {
    var currentFontSize by remember { mutableStateOf(fontSize) }
    var readyToDraw by remember { mutableStateOf(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.capitalize(Locale.current),
            color = color,
            fontSize = currentFontSize,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.drawWithContent { if (readyToDraw) drawContent() },
            onTextLayout = { textLayoutResult ->
                if (textLayoutResult.didOverflowHeight) {
                    currentFontSize *= 0.9f
                } else {
                    readyToDraw = true
                }
            }
        )
    }
}
