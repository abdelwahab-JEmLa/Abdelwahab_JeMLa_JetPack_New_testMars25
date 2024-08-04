package com.example.abdelwahabjemlajetpack

import a_RoomDB.AppDatabase
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import b_Edite_Base_Donne.A_Edite_Base_Screen
import b_Edite_Base_Donne.ArticleDao
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import b_Edite_Base_Donne.MainAppViewModelFactory
import c_ManageBonsClients.ArticlesAcheteModeleDao
import c_ManageBonsClients.C_ManageBonsClients
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import d_EntreBonsGro.FragmentEntreBonsGro

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 101
    private val database by lazy { AppDatabase.getInstance(this) }
    private val viewModel: EditeBaseDonneViewModel by viewModels {
        MainAppViewModelFactory(database.articleDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.database.setPersistenceEnabled(true)
        if (!checkPermission()) {
            requestPermission()
        }
        setContent {
            AbdelwahabJeMLaJetPackTheme {
                MyApp(viewModel,database.articleDao(),database.articlesAcheteModeleDao() )
            }
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

    @Deprecated("This method has been deprecated ..")
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
fun MyApp(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao,
    articlesAcheteModeleDao: ArticlesAcheteModeleDao
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") { MainScreen(navController, editeBaseDonneViewModel, articleDao) }
        composable("A_Edite_Base_Screen") { A_Edite_Base_Screen(editeBaseDonneViewModel,articleDao) }
        composable("C_ManageBonsClients") { C_ManageBonsClients() }
        composable("FragmentEntreBonsGro") { FragmentEntreBonsGro() }

    }
}

@Composable
fun MainScreen(
    navController: NavHostController = rememberNavController(),
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao
) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = { TopAppBar(coroutineScope, editeBaseDonneViewModel, articleDao) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("A_Edite_Base_Screen")
                        },
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text("Edit Base Screen", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Click to navigate to A_Edite_Base_Screen", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("C_ManageBonsClients")
                        },
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text("C_ManageBonsClients", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Click to navigate to C_ManageBonsClients", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(15.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            navController.navigate("FragmentEntreBonsGro")
                        },
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text("FragmentEntreBonsGro", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Click to navigate to FragmentEntreBonsGro", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}


