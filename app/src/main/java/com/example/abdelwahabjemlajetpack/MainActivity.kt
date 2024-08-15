package com.example.abdelwahabjemlajetpack

import a_RoomDB.AppDatabase
import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import c_ManageBonsClients.FragmentManageBonsClients
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import d_EntreBonsGro.FragmentEntreBonsGro
import f_credits.CreditsViewModel
import f_credits.FragmentCredits
import f_credits.f_2_CreditsClients.CreditsClientsViewModel
import f_credits.f_2_CreditsClients.FragmentCreditsClients

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 101
    private val database by lazy { AppDatabase.getInstance(this) }
    private val viewModel: EditeBaseDonneViewModel by viewModels {
        MainAppViewModelFactory(database.articleDao())
    }
    private val creditsViewModel: CreditsViewModel by viewModels()
    private val creditsClientsViewModel: CreditsClientsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!checkPermission()) {
            requestPermission()
        }
        setContent {
            AbdelwahabJeMLaJetPackTheme {
                MyApp(
                    viewModel,
                    database.articleDao(),
                    creditsViewModel,
                    creditsClientsViewModel
                )
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
    creditsViewModel: CreditsViewModel,
    creditsClientsViewModel: CreditsClientsViewModel
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(
                navController = navController,
                editeBaseDonneViewModel = editeBaseDonneViewModel,
                articleDao = articleDao
            )
        }
        composable("A_Edite_Base_Screen") { A_Edite_Base_Screen(editeBaseDonneViewModel, articleDao) }
        composable("C_ManageBonsClients") { FragmentManageBonsClients() }
        composable("FragmentEntreBonsGro") { FragmentEntreBonsGro(articleDao) }
        composable("FragmentCredits") { FragmentCredits(creditsViewModel) }
        composable("FragmentCreditsClients") { FragmentCreditsClients(creditsClientsViewModel) }
    }
}

@Composable
fun MainScreen(
    navController: NavHostController,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                coroutineScope = coroutineScope,
                editeBaseDonneViewModel = editeBaseDonneViewModel,
                articleDao = articleDao
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            LazyVerticalGrid(//TODO Donne a chaque element couleur et text blanche et donne le une icone corespondent fait lagrondire et centre le
                //TODo le text on bas centre avec majusscule au debut
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    MenuCard("Edit Base Screen", "A_Edite_Base_Screen", navController)
                }
                item {
                    MenuCard("Manage Bons Clients", "C_ManageBonsClients", navController)
                }
                item {
                    MenuCard("Entre Bons Gro", "FragmentEntreBonsGro", navController)
                }
                item {
                    MenuCard("Credits", "FragmentCredits", navController)
                }
                item {
                    MenuCard("CreditsClients", "FragmentCreditsClients", navController)
                }
            }
        }
    }
}

@Composable
fun MenuCard(title: String, route: String, navController: NavHostController) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { navController.navigate(route) },
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
    }
}