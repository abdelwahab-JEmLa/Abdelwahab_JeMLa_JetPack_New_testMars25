package i_SupplierArticlesRecivedManager


import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.abdelwahabjemlajetpack.R
import java.io.File


@Composable
fun ArticleDetailWindow(
    article: TabelleSupplierArticlesRecived,
    onDismiss: () -> Unit,
    viewModel: HeadOfViewModels,
    modifier: Modifier, onReloadTrigger: () -> Unit, relodeTigger: Int
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large
        ) {
            Card(
                modifier = modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = modifier.fillMaxWidth()) {

                    DisplayColorsCards( article,viewModel, onDismiss = onDismiss,
                        onReloadTrigger = onReloadTrigger,
                        relodeTigger = relodeTigger
                    )

                    // Article name
                    AutoResizedTextECB(
                        text = article.a_d_nomarticlefinale_c.capitalize(Locale.current),
                        fontSize = 25.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = modifier.fillMaxWidth()
                    )

                }
            }
        }
    }
}

@Composable
fun DisplayColorsCards(
    article: TabelleSupplierArticlesRecived, viewModel: HeadOfViewModels, modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onReloadTrigger: () -> Unit,
    relodeTigger: Int
) {
    val couleursList = listOf(
        article.a_d_nomarticlefinale_c_1,
        article.a_d_nomarticlefinale_c_2,
        article.a_d_nomarticlefinale_c_3,
        article.a_d_nomarticlefinale_c_4
    ).filterNot { it.isEmpty() }


    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(couleursList) { index, couleur ->
            ColorCard(
                article,
                index,
                couleur,
                viewModel,
                onDismiss,
                onReloadTrigger,
                relodeTigger,)
        }

    }
}

@Composable
private fun ColorCard(
    article: TabelleSupplierArticlesRecived,
    index: Int,
    couleur: String,
    viewModel: HeadOfViewModels,
    onDismiss: () -> Unit,
    onReloadTrigger: () -> Unit,
    relodeTigger: Int,

    ) {

    Card(
        modifier = Modifier
            .height(200.dp)
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            ) {

                DisplayeImage(article=article,
                    viewModel=viewModel,
                    index=index,
                    reloadKey =relodeTigger
                )

            }
            Spacer(modifier = Modifier.height(8.dp))
            AutoResizedTextECB(text = couleur)
        }
    }

}

@Composable
fun DisplayeImage(
    article: TabelleSupplierArticlesRecived,
    viewModel: HeadOfViewModels,
    index: Int = 0,
    reloadKey: Any = Unit
) {
    val context = LocalContext.current
    val baseImagePath =
        "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${article.a_c_idarticle_c}_${index + 1}"
    val downloadsImagePath =
        "${viewModel.dossiesStandartOFImages}/${article.a_c_idarticle_c}_${index + 1}"

    val imageExist = remember( reloadKey) {
        listOf("jpg", "webp").firstNotNullOfOrNull { extension ->
            listOf(downloadsImagePath, baseImagePath).firstOrNull { path ->
                File("$path.$extension").exists()
            }?.let { "$it.$extension" }
        }
    }

    val imageSource = imageExist ?: R.drawable.blanc

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageSource)
            .size(Size(1000, 1000))
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Fit
    )
}
