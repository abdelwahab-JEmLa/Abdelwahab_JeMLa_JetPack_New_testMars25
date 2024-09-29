package b2_Edite_Base_Donne_With_Creat_New_Articls

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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




class HeadOfViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HeadOfViewModels::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HeadOfViewModels(CreatAndEditeInBaseDonneRepositery()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
