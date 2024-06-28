package A_Learn.A_Main_Ui

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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.mycomposeapp.ui.BaseDonne
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MainScreen(navController: NavHostController = rememberNavController()) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopAppBar( coroutineScope) }
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
fun TopAppBar(coroutineScope: CoroutineScope) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
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
            }
        }
    )
}

suspend fun transferFirebaseData() {
    val refSource = Firebase.database.getReference("c_db_de_base_down_test")
    val refDestination = Firebase.database.getReference("d_db_jetPack")

    // Clear existing data in the destination reference
    refDestination.removeValue().await()

    // Retrieve data from the source reference
    val dataSnapshot = refSource.get().await()
    val dataMap = dataSnapshot.value as Map<String, Map<String, Any>>

    // Map the data to a list of BaseDonne objects
    val baseDonneList = dataMap.map { (_, value) ->
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

    // Insert the list of BaseDonne objects into the destination reference
    refDestination.setValue(baseDonneList).await()
}

