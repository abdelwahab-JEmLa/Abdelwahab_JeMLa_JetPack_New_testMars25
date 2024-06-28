package com.example.abdelwahabjemlajetpack

import A_Learn.A_Main_Ui.MainAppViewModel
import A_Learn.A_Main_Ui.MainAppViewModelFactory
import A_Learn.Edite_Base_Donne.A_Edite_Base_Screen
import a_RoomDB.AppDatabase
import a_RoomDB.BaseDonne
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {
    private val PERMISSION_REQUEST_CODE = 101
    private val database by lazy { AppDatabase.getInstance(this) }
    private val viewModel: MainAppViewModel by viewModels {
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
                MyApp(viewModel, )
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
fun MyApp(mainAppViewModel: MainAppViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") { MainScreen(navController,mainAppViewModel) }
        composable("A_Edite_Base_Screen") { A_Edite_Base_Screen(mainAppViewModel = mainAppViewModel) }
    }
}
@Composable
fun MainScreen(navController: NavHostController = rememberNavController(), mainAppViewModel: MainAppViewModel) {
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        topBar = { TopAppBar(coroutineScope, mainAppViewModel) }
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
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(coroutineScope: CoroutineScope, mainAppViewModel: MainAppViewModel) {
    var menuExpanded by remember { mutableStateOf(false) }
    androidx.compose.material3.TopAppBar(
        title = { Text("d_db_jetPack") },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.Menu, contentDescription = "App Menu")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Transfer Firebase Data") },
                    onClick = {
                        coroutineScope.launch {
                            transferFirebaseData()
                        }
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Import Firebase Data") },
                    onClick = {
                        coroutineScope.launch {
                            mainAppViewModel.importFromFirebase()
                        }
                        menuExpanded = false
                    }
                )
            }
        }
    )
}

suspend fun transferFirebaseData() {
    val refSource = Firebase.database.getReference("c_db_de_base_down_test")
    val refDestination = Firebase.database.getReference("d_db_jetPack")

    try {
        // Clear existing data in the destination reference
        refDestination.removeValue().await()

        // Retrieve data from the source reference
        val dataSnapshot = refSource.get().await()
        val dataMap = dataSnapshot.value as Map<String, Map<String, Any>>

        // Map the data to a HashMap of BaseDonne objects
        val baseDonneMap = dataMap.mapValues { (_, value) ->
            BaseDonne(
                idArticle = (value["a00"] as Long).toInt(),
                nomArticleFinale = value["a03"] as String,
                classementCate = (value["a02"] as String).toDouble(),
                nomArab = value["a04"] as String,
                nmbrCat = (value["a05"] as String).toInt(),
                couleur1 = value["a06"] as String?,
                couleur2 = value["a07"] as String?,
                couleur3 = value["a08"] as String?,
                couleur4 = value["a09"] as String?,
                nomCategorie2 = value["a10"] as String?,
                nmbrUnite = (value["a11"] as String).toInt(),
                nmbrCaron = (value["a12"] as String).toInt(),
                affichageUniteState = (value["a13"] as String).toBoolean(),
                commmentSeVent = value["a14"] as String?,
                afficheBoitSiUniter = value["a15"] as String?,
                monPrixAchat = (value["a16"] as String).toDouble(),
                clienPrixVentUnite = (value["a17"] as String).toDouble(),
                minQuan = (value["a18"] as String).toInt(),
                monBenfice = (value["a19"] as String).toDouble(),
                monPrixVent = (value["a20"] as String).toDouble(),
                diponibilityState = value["a21"] as String,
                neaon2 = value["a22"] as String,
                idCategorie = (value["a23"] as String).toDouble(),
                funChangeImagsDimention = (value["a24"] as String).toBoolean(),
                nomCategorie = value["a25"] as String,
                neaon1 = (value["a26"] as String).toDouble(),
                lastUpdateState = value["a27"] as String,
                cartonState = value["a28"] as String,
                dateCreationCategorie = value["a29"] as String
            )
        }

        // Insert the HashMap of BaseDonne objects into the destination reference
        baseDonneMap.forEach { (key, baseDonne) ->
            refDestination.child(baseDonne.idArticle.toString()).setValue(baseDonne).await()
        }
    } catch (e: Exception) {
        Log.e("transferFirebaseData", "Failed to transfer data", e)
    }
}
