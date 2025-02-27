package com.example.clientjetpack

import P0_MainScreen.Main.MainScreen
import P6_AiGroupeForSupplier.GenerativeAiViewModel
import Z_MasterOfApps.Kotlin.ViewModel.Init.B_Load.initializeFirebase
import Z_MasterOfApps.Z.Android.Main.Utils.PermissionHandler
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.clientjetpack.Modules.AppDatabase
import com.example.clientjetpack.ViewModel.HeadViewModel
import com.example.clientjetpack.ui.theme.ClientJetPackTheme
import com.google.firebase.FirebaseApp

class MyApplication : Application() {
lateinit var database: AppDatabase
private set

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
    }

    private fun initializeComponents() {
        runCatching {
            AppDatabase.DatabaseModule.getDatabase(this).also { database = it }
            FirebaseApp.initializeApp(this)?.let(::initializeFirebase)
                ?: throw IllegalStateException("Firebase initialization failed")
        }.onFailure {
            // Log error and consider showing a user-friendly message
        }
    }
}

data class AppViewModels(
val headViewModel: HeadViewModel,
val generativeAiViewModel: GenerativeAiViewModel,
)

class ViewModelFactory(
private val context: Context,
private val database: AppDatabase,
) : ViewModelProvider.Factory {
override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
modelClass.isAssignableFrom(HeadViewModel::class.java) ->
HeadViewModel(context.applicationContext, database) as T
modelClass.isAssignableFrom(GenerativeAiViewModel::class.java) ->
GenerativeAiViewModel() as T
else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
}
}

class MainActivity : ComponentActivity() {
private val database by lazy { (application as MyApplication).database }
private val permissionHandler by lazy { PermissionHandler(this) }
private val viewModelFactory by lazy { ViewModelFactory(applicationContext, database) }

    private val headViewModel: HeadViewModel by viewModels { viewModelFactory }
    private val generativeAiViewModel: GenerativeAiViewModel by viewModels { viewModelFactory }
    private val appViewModels by lazy {
        AppViewModels(headViewModel, generativeAiViewModel)
    }

    private var permissionsChecked by mutableStateOf(false)

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupActivityContent()
    }

    private fun setupActivityContent() {
        runCatching {
            setContent {
                ClientJetPackTheme {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (permissionsChecked) {
                            MainScreen(appViewModels)
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                handlePermissions()
            } else {
                permissionsChecked = true
            }
        }.onFailure {
            Toast.makeText(
                this,
                "Une erreur s'est produite. Veuillez redémarrer l'application.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun handlePermissions() {
        if (!permissionHandler.arePermissionsAlreadyGranted()) {
            permissionHandler.checkAndRequestPermissions(object : PermissionHandler.PermissionCallback {
                override fun onPermissionsGranted() {
                    permissionsChecked = true
                }

                override fun onPermissionsDenied() {
                    showPermissionDeniedMessage()
                    permissionsChecked = true
                }

                override fun onPermissionRationale(permissions: Array<String>) {
                    // Handled in PermissionHandler
                }
            })
        } else {
            permissionsChecked = true
        }
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            "Certaines fonctionnalités seront limitées sans les permissions nécessaires",
            Toast.LENGTH_LONG
        ).show()
    }
}
