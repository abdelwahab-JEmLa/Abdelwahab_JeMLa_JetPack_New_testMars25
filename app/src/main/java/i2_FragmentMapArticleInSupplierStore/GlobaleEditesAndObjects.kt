package i2_FragmentMapArticleInSupplierStore
import a_MainAppCompnents.CreatAndEditeInBaseDonnRepositeryModels
import a_MainAppCompnents.HeadOfViewModels
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.example.abdelwahabjemlajetpack.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FabGroup(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    idSupplierOfFloatingButtonClicked: Long?,
    onIdSupplierChanged: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MoveArticlesFAB(
            uiState = uiState,
            viewModel = viewModel,
            idSupplierOfFloatingButtonClicked = idSupplierOfFloatingButtonClicked,
            onIdSupplierChanged = onIdSupplierChanged
        )

        FilterFAB(
            onClick = {
                viewModel.filterArticles(idSupplierOfFloatingButtonClicked)
            }
        )

        MarkAllFAB(
            onClick = {
                viewModel.markAllArticlesAsFound(idSupplierOfFloatingButtonClicked)
            }
        )
    }
}

@Composable
fun FilterFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.FilterList,
            contentDescription = "Filter found articles"
        )
    }
}

@Composable
fun MarkAllFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Mark all articles as found"
        )
    }
}

@Composable
private fun MoveArticlesFAB(
    uiState: CreatAndEditeInBaseDonnRepositeryModels,
    viewModel: HeadOfViewModels,
    idSupplierOfFloatingButtonClicked: Long?,
    onIdSupplierChanged: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    var isActionCompleted by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    val buttonColor by animateColorAsState(
        targetValue = when {
            isActionCompleted -> Color.Yellow
            isPressed -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.secondary
        },
        label = "buttonColor"
    )

    val currentSupplier = uiState.tabelleSuppliersSA.find { it.idSupplierSu == idSupplierOfFloatingButtonClicked }
    val currentClassment = currentSupplier?.classmentSupplier
    val nextClassment = currentClassment?.minus(1.0)
    val nextSupplier = uiState.tabelleSuppliersSA.find { it.classmentSupplier == nextClassment }
    Box(
        modifier = Modifier
            .padding(end = 16.dp)
            .size(56.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        isActionCompleted = false
                        progress = 0f
                        val pressStartTime = System.currentTimeMillis()

                        scope.launch {
                            try {
                                tryAwaitRelease()
                            } finally {
                                isPressed = false
                                val pressDuration = System.currentTimeMillis() - pressStartTime
                                if (pressDuration >= 1000) {
                                    moveNonFindefArticles(
                                        uiState,
                                        viewModel,
                                        idSupplierOfFloatingButtonClicked,
                                        onIdSupplierChanged
                                    )
                                    isActionCompleted = true
                                } else {
                                    progress = 0f
                                }
                            }
                        }

                        // Progress animation
                        scope.launch {
                            repeat(100) {
                                delay(10)
                                if (isPressed) {
                                    progress = (it + 1) / 100f
                                } else {
                                    return@launch
                                }
                            }
                        }
                    }
                )
            }
    ) {
        // Button background
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = buttonColor
        ) {}

        // Progress indicator
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            strokeWidth = 4.dp,
            trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
        )

        // Button text
        Text(
            nextSupplier?.nomVocaleArabeDuSupplier?.take(3) ?: "???",
            color = Color.White,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.align(Alignment.Center)
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
        uiState.tabelleSupplierArticlesRecived.filter {
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
