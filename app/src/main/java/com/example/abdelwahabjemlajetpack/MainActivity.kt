package com.example.abdelwahabjemlajetpack

import A_Learn.LazyC.DetailScreen
import A_Learn.LazyC.MainScreen
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbdelwahabJeMLaJetPackTheme {
                MyApp()
            }
        }

        if (!checkPermission()) {
            requestPermission()
        }
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    // Permission denied
                }
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") { MainScreen(navController) }
        composable("detail_screen/{index}") { backStackEntry ->
            val index = backStackEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
            DetailScreen(index)
        }
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, widthDp = 350, heightDp = 600)
@Composable
fun WellnessTopAppBarPreview() {
    AbdelwahabJeMLaJetPackTheme {
        Scaffold(
            topBar = { WellnessTopAppBar() }
        ) {
      //      MainScreen2(modifier = Modifier)
            }
    }
}
