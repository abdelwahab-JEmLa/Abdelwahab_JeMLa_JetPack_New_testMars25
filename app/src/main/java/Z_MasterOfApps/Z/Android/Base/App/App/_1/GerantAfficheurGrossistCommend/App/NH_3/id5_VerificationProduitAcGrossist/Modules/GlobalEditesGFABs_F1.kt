package Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.Modules

import Z_CodePartageEntreApps.Model.A_ProduitModel
import Z_CodePartageEntreApps.Model._ModelAppsFather
import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.imagesProduitsFireBaseStorageRef
import Z_CodePartageEntreApps.Model._ModelAppsFather.Companion.imagesProduitsLocalExternalStorageBasePath
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z_AppsFather.Kotlin._1.Model.ParamatersAppsModel.DeviceMode
import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardDoubleArrowUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.roundToInt

@Composable
fun GlobalEditesGFABs_F1(
    appsHeadModel: _ModelAppsFather,
    modifier: Modifier = Modifier,
    viewModelInitApp: ViewModelInitApp,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showOptions by remember { mutableStateOf(false) }
    var deviceMode by remember { mutableStateOf(DeviceMode.SERVER) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingProduct by remember { mutableStateOf<A_ProduitModel?>(null) }

    // États pour le déplacement par glisser-déposer
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    suspend fun handleImageCapture(uri: Uri) {
        try {
            if (uri.toString().isEmpty()) {
                throw IllegalArgumentException("Invalid URI")
            }

            pendingProduct?.let { product ->
                val fileName = "${product.id}_1.jpg"
                val localStorageDir = File(imagesProduitsLocalExternalStorageBasePath).apply {
                    if (!exists()) mkdirs()
                }

                val localFile = File(localStorageDir, fileName)
                var uploadSuccess = false

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val imageBytes = inputStream.readBytes()

                    try {
                        // Sauvegarde en stockage local
                        withContext(Dispatchers.IO) {
                            FileOutputStream(localFile).use { output ->
                                output.write(imageBytes)
                            }
                        }

                        // Upload vers Firebase Storage
                        val uploadTask = imagesProduitsFireBaseStorageRef
                            .child(fileName)
                            .putBytes(imageBytes)
                            .await()

                        if (uploadTask.metadata != null) {
                            uploadSuccess = true
                        }

                        if (uploadSuccess && localFile.exists() && localFile.length() > 0) {
                            product.apply {
                                statuesBase.apply {
                                    prePourCameraCapture = false
                                    naAucunImage = false
                                    imageGlidReloadTigger += 1
                                }
                                besoinToBeUpdated = true
                            }

                            _ModelAppsFather.produitsFireBaseRef
                                .child(product.id.toString())
                                .setValue(product)
                                .await()

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "Image téléchargée avec succès",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            throw IOException("La vérification du téléchargement a échoué")
                        }
                    } catch (e: Exception) {
                        if (localFile.exists() && !uploadSuccess) {
                            localFile.delete()
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Échec du téléchargement de l'image: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        throw e
                    }
                }
                    ?: throw IllegalStateException("Impossible d'ouvrir le flux d'entrée pour l'URI de l'image")

            } ?: throw IllegalStateException("Aucun produit en attente trouvé")

        } catch (e: Exception) {
            Log.e("ImageUpload", "Échec du traitement de la capture d'image", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Erreur lors du traitement de l'image: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } finally {
            pendingProduct = null
            tempImageUri = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                scope.launch {
                    handleImageCapture(uri)
                }
            }
        } else {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Échec de la capture d'image", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            if (pendingProduct != null && tempImageUri != null) {
                cameraLauncher.launch(tempImageUri!!)
            }
        } else {
            scope.launch {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Permissions requises pour l'utilisation de la caméra",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun createTempImageUri(): Uri? {
        return try {
            val tempFile = File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
                deleteOnExit()
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            ).also { tempImageUri = it }
        } catch (e: IOException) {
            Log.e("ImageCapture", "Échec de la création du fichier temporaire", e)
            null
        }
    }

    fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val hasPermissions = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasPermissions) {
            permissionLauncher.launch(permissions)
        } else {
            val productForCapture = appsHeadModel.produitsMainDataBase
                .firstOrNull { it.statuesBase.prePourCameraCapture }

            if (productForCapture != null) {
                pendingProduct = productForCapture
                createTempImageUri()?.let { uri ->
                    cameraLauncher.launch(uri)
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FAB Principal
            FloatingActionButton(
                onClick = { showOptions = !showOptions },
                modifier = Modifier.size(48.dp),
                containerColor = Color(0xFF3F51B5)
            ) {
                Icon(
                    imageVector = if (showOptions) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                    contentDescription = if (showOptions) "Masquer les options" else "Afficher les options"
                )
            }

            // Menu d'options
            AnimatedVisibility(visible = showOptions) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // FAB Suppression
                    FloatingActionButton(
                        onClick = {
                            //TODO
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Supprimer"
                        )
                    }

                    // FAB Caméra
                    FloatingActionButton(
                        onClick = { checkAndRequestPermissions() },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddAPhoto,
                            contentDescription = "Prendre une photo"
                        )
                    }

                    // FAB Édition de position
                    FloatingActionButton(
                        onClick = {
                            viewModelInitApp
                                ._paramatersAppsViewModelModel
                                .visibilityClientEditePositionDialog = true
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFFFF5722)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardDoubleArrowUp,
                            contentDescription = "Éditer la position"
                        )
                    }

                    // FAB Basculement de mode
                    FloatingActionButton(
                        onClick = {
                            deviceMode = when (deviceMode) {
                                DeviceMode.SERVER -> DeviceMode.DISPLAY
                                DeviceMode.DISPLAY -> DeviceMode.SERVER
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = Color(0xFFFF5722)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = if (deviceMode == DeviceMode.SERVER)
                                "Passer en mode Affichage" else "Passer en mode Serveur"
                        )
                    }
                }
            }
        }
    }
}
