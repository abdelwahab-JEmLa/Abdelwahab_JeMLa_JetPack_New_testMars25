package i2_FragmentMapArticleInSupplierStore
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import c_ManageBonsClients.TAG
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.abdelwahabjemlajetpack.R
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File



@Composable
fun FabGroup(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    idSupplierOfFloatingButtonClicked: Long?,
    onFilterDispoActivate: () -> Unit
) {
    val allArticlesMarked = remember(uiState.articlesCommendForSupplierList, idSupplierOfFloatingButtonClicked) {
        uiState.articlesCommendForSupplierList
            .filter { it.idSupplierTSA.toLong() == idSupplierOfFloatingButtonClicked }
            .all { it.itsInFindedAskSupplierSA }
    }

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        FilterFAB(
            onClick = onFilterDispoActivate,
            isActive = uiState.showOnlyWithFilter
        )

        MarkAllFAB(
            onClick = {
                viewModel.toggleMarkAllArticles(idSupplierOfFloatingButtonClicked, !allArticlesMarked)
            },
            allMarked = allArticlesMarked
        )
    }
}

@Composable
fun MarkAllFAB(onClick: () -> Unit, allMarked: Boolean) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Icon(
            imageVector = if (allMarked) Icons.Default.RemoveCircle else Icons.Default.CheckCircle,
            contentDescription = if (allMarked) "Unmark all articles" else "Mark all articles as found"
        )
    }
}

fun HeadOfViewModels.toggleMarkAllArticles(supplierId: Long?, markAll: Boolean) {
    viewModelScope.launch {
        try {
            val updates = mutableMapOf<String, Any>()

            _uiState.update { currentState ->
                val updatedArticles = currentState.articlesCommendForSupplierList.map { article ->
                    if (article.idSupplierTSA.toLong() == supplierId) {
                        updates["${article.vid}/itsInFindedAskSupplierSA"] = markAll
                        article.copy(itsInFindedAskSupplierSA = markAll)
                    } else {
                        article
                    }
                }
                currentState.copy(articlesCommendForSupplierList = updatedArticles)
            }

            refTabelleSupplierArticlesRecived.updateChildren(updates).await()

            updateSmothUploadProgressBarCounterAndItText(
                nameFunInProgressBar = if (markAll) "Marked all articles as found" else "Unmarked all articles",
                progressDimunuentDe100A0 = 0,
                end = true,
                delayUi = 1000
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling articles mark state", e)
            updateSmothUploadProgressBarCounterAndItText(
                nameFunInProgressBar = "Error updating articles",
                progressDimunuentDe100A0 = 100,
                end = true,
                delayUi = 1000
            )
        }
    }
}

@Composable
fun FilterFAB(onClick: () -> Unit, isActive: Boolean) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.FilterListOff else Icons.Default.FilterList,
            contentDescription = if (isActive) "Disable filter" else "Enable filter"
        )
    }
}

fun moveNonFindefArticles(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    idSupplierOfFloatingButtonClicked: Long?,
    onIdSupplierChanged: (Long) -> Unit
) {
    val filterBytabelleSupplierArticlesRecived =
        uiState.articlesCommendForSupplierList.filter {
            it.itsInFindedAskSupplierSA
        }

    val currentSupplier = uiState.tabelleSuppliersSA.find { it.idSupplierSu == idSupplierOfFloatingButtonClicked }
    val currentClassment = currentSupplier?.classmentSupplier

    if (currentClassment != null) {
        val nextClassment = currentClassment - 1.0
        val nextSupplier = uiState.tabelleSuppliersSA.find { it.classmentSupplier == nextClassment }

        if (nextSupplier != null) {
            viewModel.moveArticlesToSupplier(
                articlesToMove = filterBytabelleSupplierArticlesRecived,
                toSupp = nextSupplier.idSupplierSu
            )
            onIdSupplierChanged(nextSupplier.idSupplierSu)
        }
    }
}


@Composable
fun DisplayeImageById(
    idArticle: Long,
    index: Int = 0,
    reloadKey: Any = Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val baseImagePath =
        "/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${idArticle}_${index + 1}"

    val imageExist = remember(reloadKey) {
        listOf("jpg", "webp").firstNotNullOfOrNull { extension ->
            listOf(baseImagePath).firstOrNull { path ->
                File("$path.$extension").exists()
            }?.let { "$it.$extension" }
        }
    }

    val imageSource = imageExist ?: R.drawable.blanc

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageSource)
            .size(Size.ORIGINAL)
            .crossfade(true)
            .build()
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center
    )
}
