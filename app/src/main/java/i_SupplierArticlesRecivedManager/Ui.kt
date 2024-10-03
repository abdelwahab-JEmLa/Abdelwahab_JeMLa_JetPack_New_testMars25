package i_SupplierArticlesRecivedManager


import a_MainAppCompnents.BaseDonneECBTabelle
import a_MainAppCompnents.HeadOfViewModels
import a_MainAppCompnents.TabelleSupplierArticlesRecived
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import b_Edite_Base_Donne.AutoResizedText
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
    var displayeInOutlines by remember { mutableStateOf(true) }
    var currentChangingField by remember { mutableStateOf("") }

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

                    DisplayColorsCards(article,viewModel, onDismiss = onDismiss,
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
            if (couleur != null) {
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
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current



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

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete color")
                }

                IconButton(
                    onClick = {
                        viewModel.tempImageUri = viewModel.createTempImageUri(context)
                        launcher.launch(viewModel.tempImageUri!!)
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Icon(Icons.Default.AddAPhoto, contentDescription = "Add photo")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AutoResizedTextECB(text = couleur)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this color?") },
            confirmButton = {
                Button(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteColor(article, index + 1)
                   if (index==0) onDismiss()
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
        "${viewModel.dossiesStandartImages}/${article.a_c_idarticle_c}_${index + 1}"

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
@Composable
private fun AddColorCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .height(200.dp)
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new color",
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

@Composable
fun OutlineTextECB(
    columnToChange: String,
    abbreviation: String,
    currentChangingField: String,
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
    modifier: Modifier = Modifier,
    onValueChanged: (String) -> Unit
) {

    var textFieldValue by remember { mutableStateOf(
        article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: "") }
    val textValue = if (currentChangingField == columnToChange) textFieldValue else ""
    val labelValue = article.getColumnValue(columnToChange)?.toString()?.replace(',', '.') ?: ""
    val roundedValue = try {
        labelValue.toDouble()
            .let { if (it % 1 == 0.0) it.toInt().toString() else String.format("%.1f", it) }
    } catch (e: NumberFormatException) {
        labelValue
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = textValue,
        onValueChange = { newValue ->
            textFieldValue = newValue.replace(',', '.')
            viewModel.updateAndCalculateAuthersField(textFieldValue, columnToChange, article)
            onValueChanged(columnToChange)
        },
        label = {
            AutoResizedText(
                text = "$abbreviation$roundedValue",
                color = Color.Red,
                modifier = Modifier.fillMaxWidth()
            )
        },
        textStyle = TextStyle(color = Color.Blue, textAlign = TextAlign.Center, fontSize = 14.sp),
        modifier = modifier
            .fillMaxWidth()
            .height(65.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        )
    )
}

@Composable
fun CalculationButtons(
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            ,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            viewModel.updateAndCalculateAuthersField(
                (article.monPrixAchat / article.nmbrUnite).toString(),
                "monPrixAchat",
                article
            )
        }) { Text("/") }
        Button(onClick = {
            viewModel.updateAndCalculateAuthersField(
                (article.monPrixAchat * article.nmbrUnite).toString(),
                "monPrixAchat",
                article
            )
        }) { Text("*") }
    }
}

@Composable
fun ArticleToggleButton(
    article: BaseDonneECBTabelle,
    viewModel: HeadOfViewModels,
    modifier: Modifier
) {
    Button(
        onClick = { viewModel },
        colors = ButtonDefaults.buttonColors(containerColor = if (article.affichageUniteState) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error),
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(text = if (article.affichageUniteState) "Cacher les Unités" else "Afficher les Unités")
    }
}

