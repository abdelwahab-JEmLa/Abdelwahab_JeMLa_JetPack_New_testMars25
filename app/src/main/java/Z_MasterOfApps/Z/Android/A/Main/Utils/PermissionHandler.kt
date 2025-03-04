package Z_MasterOfApps.Z.Android.A.Main.Utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.example.abdelwahabjemlajetpack.MainActivity
import com.example.abdelwahabjemlajetpack.R


/*
import com.example.c_serveur.MainActivity
import com.example.c_serveur.R

import com.example.clientjetpack.MainActivity
import com.example.clientjetpack.R

import com.example.abdelwahabjemlajetpack.MainActivity
import com.example.abdelwahabjemlajetpack.R

 */

class PermissionHandler(private val activity: MainActivity) {
    companion object {
        private const val PREFS_NAME = "PermissionPrefs"
        private const val PERMISSIONS_GRANTED_KEY = "PermissionsGranted"
    }

    private val prefs: SharedPreferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var showDialog by mutableStateOf(false)
    private var permissionCallback: PermissionCallback? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private val requiredPermissions = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )
        else -> arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    interface PermissionCallback {
        fun onPermissionsGranted()
        fun onPermissionsDenied()
        fun onPermissionRationale(permissions: Array<String>)
    }

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            savePermissionsGranted()
            permissionCallback?.onPermissionsGranted()
            showDialog = false
        } else {
            permissionCallback?.onPermissionsDenied()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun arePermissionsAlreadyGranted(): Boolean {
        return prefs.getBoolean(PERMISSIONS_GRANTED_KEY, false) &&
                requiredPermissions.all {
                    ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
                }
    }

    private fun savePermissionsGranted() {
        prefs.edit().putBoolean(PERMISSIONS_GRANTED_KEY, true).apply()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun checkAndRequestPermissions(callback: PermissionCallback) {
        this.permissionCallback = callback

        if (arePermissionsAlreadyGranted()) {
            callback.onPermissionsGranted()
            return
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        when {
            permissionsToRequest.isEmpty() -> {
                savePermissionsGranted()
                callback.onPermissionsGranted()
            }
            else -> {
                requestPermissionLauncher.launch(permissionsToRequest)
            }
        }
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(this)
        }
    }
}
enum class LottieJsonGetterR_Raw_Icons(val resourceId: Int) {
    reacticonanimatedjsonurl(R.raw.reacticonanimatedjsonurl),
    afficheFenetre(R.raw.stats_lottie_json) ,
    alimentation(R.raw.alimentation)   ,
    atay(R.raw.atay)
}

class XmlsFilesHandler {

    companion object {
        val xmlResources = listOf(
            Pair("marker_info_window", R.layout.marker_info_window),
            Pair("info_window_container", R.id.info_window_container),
            Pair("location_arrow", R.drawable.location_arrow),
            Pair("reacticonanimatedjsonurl", R.raw.reacticonanimatedjsonurl),
        )

        fun fixXmlResources(name: String): Int {
            return xmlResources.find { it.first == name }?.second
                ?: throw IllegalStateException("Resource '$name' not found")  // Better error message
        }
    }
}



