package Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.StorageFireBaseOffline
/*
import Z_MasterOfApps.Kotlin.Model._ModelAppsFather.Companion.imagesProduitsFireBaseStorageRef
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.io.File

class ImageHandler(private val context: Context) {
companion object {
private const val TAG = "ImageHandler"
}

suspend fun handleImageOperation(storageRef: StorageReference, localFile: File): Boolean {
return try {
    // Step 1: Check if file exists locally and is up to date
    if (localFile.exists()) {
        val metadata = storageRef.metadata.await()
        if (metadata.updatedTimeMillis <= localFile.lastModified()) {
            Log.d(TAG, "Using existing local file: ${localFile.path}")
            return true
        }
    }

    // Step 2: Try to download if online
    if (isOnline()) {
        try {
            storageRef.getFile(localFile).await()
            Log.d(TAG, "Downloaded file successfully: ${localFile.path}")

            // Try to upload if needed
            if (localFile.exists()) {
                val uploadTask = storageRef.putFile(localFile.toUri()).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
                Log.d(TAG, "Uploaded successfully: $downloadUrl")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Download/Upload failed", e)
        }
    }

    // Step 3: Use existing file if available
    if (localFile.exists()) {
        Log.d(TAG, "Using cached file: ${localFile.path}")
        return true
    }

    false
} catch (e: Exception) {
    Log.e(TAG, "Image operation failed", e)
    false
}
}

private fun isOnline(): Boolean {
val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
val network = connectivityManager.activeNetwork
val capabilities = connectivityManager.getNetworkCapabilities(network)
return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
}
}

// Extension function for ViewModelInitApp to handle images
suspend fun ViewModelInitApp.initProductImages() {
val imageHandler = ImageHandler(appContext)

_modelAppsFather.produitsMainDataBase.forEachIndexed { index, produit ->
try {
    loadingProgress = index.toFloat() / _modelAppsFather.produitsMainDataBase.size

    val imageRef = imagesProduitsFireBaseStorageRef.child("${produit.id}_1.jpg")
    val localFile = File("/storage/emulated/0/Abdelwahab_jeMla.com/IMGs/BaseDonne/${produit.id}_1.jpg")

    imageHandler.handleImageOperation(imageRef, localFile)
} catch (e: Exception) {
    Log.e("ViewModelInitApp", "Error handling image for product ${produit.id}", e)
}
}

loadingProgress = 1f
}
*/
