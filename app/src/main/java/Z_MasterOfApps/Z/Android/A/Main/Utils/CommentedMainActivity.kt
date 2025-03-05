package Z_MasterOfApps.Z.Android.A.Main.Utils
    /*
import Z_MasterOfApps.Kotlin.ViewModel.Init.Init.initializeFirebase
import Z_MasterOfApps.Z.Android.Main.A_MainScreen_FragDepart
import android.app.Application
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.example.c_serveur.ui.theme.B_ServeurTheme
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)?.let { firebaseApp ->
            initializeFirebase(firebaseApp)
        }
    }
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionsGranted = mutableStateOf(false)
        enableEdgeToEdge()

        PermissionHandler(this).checkAndRequestPermissions(object : PermissionHandler.PermissionCallback {
            override fun onPermissionsGranted() {
                permissionsGranted.value = true
            }

            override fun onPermissionsDenied() {
                finish()
            }

            override fun onPermissionRationale(permissions: Array<String>) {
                // Handle rationale if needed
            }
        })

        setContent {
            B_ServeurTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    A_MainScreen_FragDepart(
                        modifier = Modifier.padding(innerPadding),
                        permissionsGranted = permissionsGranted.value,
                    )
                }
            }
        }
    }
}
*/
