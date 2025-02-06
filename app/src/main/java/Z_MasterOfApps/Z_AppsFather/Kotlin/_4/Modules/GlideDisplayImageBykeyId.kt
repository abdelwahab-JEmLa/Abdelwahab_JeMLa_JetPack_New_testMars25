package Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules

import Z_MasterOfApps.Kotlin.Model.A_ProduitModel
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.imagesProduitsLocalExternalStorageBasePath
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.signature.ObjectKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun GlideDisplayImageBykeyId(
    imageGlidReloadTigger: Int = 0,
    mainItem: A_ProduitModel? = null,
    modifier: Modifier = Modifier,
    size: Dp? = null,
    onLoadComplete: () -> Unit = {}
) {
    var imageFile by remember { mutableStateOf<File?>(null) }
    var forceReload by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableFloatStateOf(0f) }
    var reloadSuccess by remember { mutableStateOf(false) }

    val keyImageId = if (mainItem == null) "null" else "${mainItem.id}_1"
    var shouldUseDefaultImage by remember {
        mutableStateOf(keyImageId == "null" || mainItem?.coloursEtGouts?.any { it.sonImageNeExistPas } == true)
    }

    // Function to check if file exists and is valid
    suspend fun findValidImageFile(basePath: String): File? {
        return withContext(Dispatchers.IO) {
            val extensions = listOf("jpg", "jpeg", "png", "webp")
            for (ext in extensions) {
                val file = File("$basePath.$ext")
                if (file.exists() && file.length() > 0) {
                    Log.d("GlideDisplay", "Found valid image file: ${file.absolutePath}")
                    return@withContext file
                }
            }
            Log.d("GlideDisplay", "No valid image file found at $basePath")
            null
        }
    }

    // Track both imageGlidReloadTigger and mainItem changes
    LaunchedEffect(imageGlidReloadTigger, mainItem?.statuesBase?.imageGlidReloadTigger, keyImageId) {
        val shouldReload =
            imageGlidReloadTigger > 0 || (mainItem?.statuesBase?.imageGlidReloadTigger ?: 0) > 0
        if (shouldReload) {
            Log.d("GlideDisplay", "Reload triggered - Global: $imageGlidReloadTigger, Item: ${mainItem?.statuesBase?.imageGlidReloadTigger}")

            isLoading = true
            forceReload++
            reloadSuccess = true

            // Ensure storage directory exists
            withContext(Dispatchers.IO) {
                File(imagesProduitsLocalExternalStorageBasePath).mkdirs()
            }

            delay(100) // Short delay to allow file system operations to complete

            val basePath = "$imagesProduitsLocalExternalStorageBasePath/$keyImageId"
            val validFile = findValidImageFile(basePath)

            if (validFile != null) {
                Log.d("GlideDisplay", "Using valid image file: ${validFile.absolutePath}")
                shouldUseDefaultImage = false
                imageFile = validFile
            } else {
                Log.d("GlideDisplay", "No valid image found, using default")
                shouldUseDefaultImage = true
                imageFile = File("$imagesProduitsLocalExternalStorageBasePath/logo.webp")
            }
        }
    }

    // Load image file with proper error handling
    LaunchedEffect(keyImageId, forceReload, shouldUseDefaultImage) {
        withContext(Dispatchers.IO) {
            try {
                val defaultPath = "$imagesProduitsLocalExternalStorageBasePath/logo.webp"
                if (!shouldUseDefaultImage && keyImageId != "null") {
                    val basePath = "$imagesProduitsLocalExternalStorageBasePath/$keyImageId"
                    val validFile = findValidImageFile(basePath)
                    imageFile = validFile ?: File(defaultPath)
                } else {
                    imageFile = File(defaultPath)
                }
                Log.d("GlideDisplay", "Final image path: ${imageFile?.absolutePath}")
            } catch (e: Exception) {
                Log.e("GlideDisplay", "Error loading image file", e)
                imageFile = File("$imagesProduitsLocalExternalStorageBasePath/logo.webp")
            }
        }
    }

    Box(
        modifier = modifier.then(size?.let { Modifier.size(it) } ?: Modifier.fillMaxSize()),
        contentAlignment = Alignment.Center
    ) {
        GlideImage(
            model = imageFile ?: File("$imagesProduitsLocalExternalStorageBasePath/logo.webp"),
            contentDescription = "Product $keyImageId",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        ) { builder ->
            builder
                .downsample(com.bumptech.glide.load.resource.bitmap.DownsampleStrategy.AT_MOST)
                .encodeQuality(3)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .signature(ObjectKey("${keyImageId}_${forceReload}_${System.currentTimeMillis()}"))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e("GlideDisplay", "Load failed for $keyImageId", e)
                        isLoading = false
                        loadProgress = 0f
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("GlideDisplay", "Load complete for $keyImageId")
                        isLoading = false
                        loadProgress = 1f
                        if (reloadSuccess) {
                            onLoadComplete()
                            reloadSuccess = false
                        }
                        return false
                    }
                })
        }

        if (isLoading) {
            CircularProgressIndicator(
                progress = { loadProgress },
                modifier = Modifier.size(48.dp),
                color = Color.Blue,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            )
        }
    }
}
