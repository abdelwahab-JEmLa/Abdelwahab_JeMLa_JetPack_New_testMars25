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
            a_c_idarticle_c = (value["a00"] as Long).toInt(),
            a_d_nomarticlefinale_c = value["a03"] as String,
            a_b_classementc = (value["a02"] as String).toDouble(),
            a_e_nomarab_c = value["a04"] as String,
            a_f_nombrcat_c = (value["a05"] as String).toInt(),
            a_g_cat1_c = value["a06"] as String?,
            a_h_cat2_c = value["a07"] as String?,
            a_i_cat3_c = value["a08"] as String?,
            a_j_cat4_c = value["a09"] as String?,
            a_k_catego_c = value["a10"] as String?,
            a_l_nmbunite_c = (value["a11"] as String).toInt(),
            a_m_nmbucaron_c = (value["a12"] as String).toInt(),
            a_n_affichageu_c = (value["a13"] as String).toBoolean(),
            a_o_commment_se_vent_c = value["a14"] as String?,
            a_p_affiche_boit_si_uniter_sidispo_c = value["a15"] as String?,
            a_q_prixachat_c = (value["a16"] as String).toDouble(),
            a_r_prixdevent_c = (value["a17"] as String).toDouble(),
            a_s_quan__1_c = (value["a18"] as String).toInt(),
            a_t_benfice_prix_1_q1_c = (value["a19"] as String).toDouble(),
            a_u_prix_1_q1_c = (value["a20"] as String).toDouble(),
            a_v_nomvocale = value["a21"] as String,
            a_w_idcatalogue_categorie = value["a22"] as String,
            a_x_idcategorie = (value["a23"] as String).toDouble(),
            funChangeImagsDimention = (value["a24"] as String).toBoolean(),
            a_z_namecate = value["a25"] as String,
            b_a_idcatalogue = (value["a26"] as String).toDouble(),
            b_b_idcatalogueac0 = value["a27"] as String,
            b_c_namecatalogue = value["a28"] as String,
            b_d_datecreationcategorie = value["a29"] as String
        )
    }

    // Insert the list of BaseDonne objects into the destination reference
    refDestination.setValue(baseDonneList).await()
}

