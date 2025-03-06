package Z_MasterOfApps.Z.Android.A.Main

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.A_StartupScreen
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.NavigationBarWithFab
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_1.id4_DeplaceProduitsVerGrossist.A_id4_DeplaceProduitsVerGrossist
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.A_id1_GerantDefinirePosition
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.A_ID5_VerificationProduitAcGrossist
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.A_Id2_TravaillieurListProduitAchercheChezLeGrossist
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_5.id3_AfficheurDesProduitsPourLeColecteur.A_id3_AfficheurDesProduitsPourLeColecteur
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.A_id1_ClientsLocationGps
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.androidx.compose.koinViewModel

enum class SectionsAPP{
    BASE_DONNE,
    MANAGE_ACHATS
}
@Composable
fun AppNavigationHost(
    modifier: Modifier,
) {
    val viewModelInitApp: ViewModelInitApp = koinViewModel()

    val navController = rememberNavController()
    val isManagerPhone = viewModelInitApp._paramatersAppsViewModelModel.cLeTelephoneDuGerant ?: false
    var sectionDesFragmentAppAfficheMNT by remember { mutableStateOf(SectionsAPP.MANAGE_ACHATS) }


    val items = remember(isManagerPhone) { NavigationItems.getItems(isManagerPhone,sectionDesFragmentAppAfficheMNT) }

    val startDestination = StartupIcon_Start.route
    val currentRoute = navController.currentBackStackEntryAsState()
        .value?.destination?.route

    // State to track if we're already on the StartupIcon_Start route
    var isOnStartupRoute by remember { mutableStateOf(false) }
    // State to control dialog visibility
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                Box(modifier = modifier.fillMaxSize()) {
                    if (viewModelInitApp.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(48.dp)
                                .align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        NavHost(
                            navController = navController,
                            startDestination = startDestination,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            composable(InfosDatas_FramgmentId4.route) {
                                A_id4_DeplaceProduitsVerGrossist(viewModelInitApp = viewModelInitApp)
                            }
                            composable(InfosDatas_FragmentId1.route) {
                                A_id1_GerantDefinirePosition(viewModel = viewModelInitApp)
                            }
                            composable(InfosDatas_FramgmentId5.route) {
                                A_ID5_VerificationProduitAcGrossist(viewModel = viewModelInitApp)
                            }
                            composable(InfosDatas_FramgmentId2.route) {
                                A_Id2_TravaillieurListProduitAchercheChezLeGrossist(viewModel = viewModelInitApp)
                            }
                            composable(InfosDatas_FramgmentId3.route) {
                                A_id3_AfficheurDesProduitsPourLeColecteur(viewModelInitApp = viewModelInitApp)
                            }
                            composable(InfosDatas_FramgmentId6.route) {
                                A_id1_ClientsLocationGps(viewModel = viewModelInitApp)
                            }
                            composable(StartupIcon_Start.route) {
                                isOnStartupRoute = true
                                A_StartupScreen(viewModelInitApp, { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }

        // Show the dialog if requested
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("SEction Gere->") },
                text = { Text("Choisissez une option:") },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                // Handle "Achats" action
                                showDialog = false
                                sectionDesFragmentAppAfficheMNT=SectionsAPP.MANAGE_ACHATS                            }
                        ) {
                            Text(SectionsAPP.MANAGE_ACHATS.toString())
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                // Handle "BaseDone" action
                                showDialog = false
                                sectionDesFragmentAppAfficheMNT=SectionsAPP.BASE_DONNE                            }
                        ) {
                            Text(SectionsAPP.BASE_DONNE.toString())
                        }
                    }
                },
                dismissButton = {}
            )
        }

        AnimatedVisibility(
            visible = true,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NavigationBarWithFab(
                items = items,
                viewModelInitApp = viewModelInitApp,
                currentRoute = currentRoute,
                onClickNavigate = { route ->
                    // Disable navigation when loading
                    if (!viewModelInitApp.isLoading) {
                        // Check if the user clicked on StartupIcon_Start while already on that route
                        if (route == StartupIcon_Start.route && currentRoute == StartupIcon_Start.route) {
                            // If already on the startup route and clicked again, show the dialog
                            showDialog = true
                        } else {
                            // Normal navigation for other routes or first click on startup
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

object NavigationItems {
    fun getItems(isManagerPhone: Boolean, sectoionDesFragmentAppAfficheMNT: SectionsAPP? =SectionsAPP.MANAGE_ACHATS) = buildList {
        add(StartupIcon_Start)

        if (sectoionDesFragmentAppAfficheMNT==SectionsAPP.MANAGE_ACHATS) {
            //Manageur_Fragments
            if (isManagerPhone) {
                add(InfosDatas_FramgmentId4)
            }
            add(InfosDatas_FragmentId1)
            add(InfosDatas_FramgmentId5)

            //Clients_Fragments
            add(InfosDatas_FramgmentId2)
            add(InfosDatas_FramgmentId3)

            //MapApp_Fragments
            if (isManagerPhone) {
                add(InfosDatas_FramgmentId6)
            }
        }
    }
}

data object InfosDatas_FragmentId1 : Screen(1,"محدد اماكن المنتجات عند الجمال",Color(0xFFFF5722),Icons.Default.LocationOn, "fragment_main_screen_1",)

data object InfosDatas_FramgmentId2 : Screen(
    id =2,
    icon = Icons.Default.Visibility,
    route = "main_screen_f2",
    titleArab = "مظهر اماكن المنتجات عند الجمال",
    color = Color(0xFFA48E39)
)

data object InfosDatas_FramgmentId3 : Screen(
    id =3,
    route ="مظهر الاماكن لمقسم المنتجات على الزبائن",
    icon = Icons.Default.Groups,
    titleArab = "مظهر الاماكن لمقسم المنتجات على الزبائن",
    color = Color(0xFF9C27B0)
)

data object InfosDatas_FramgmentId4 : Screen(
    id =4,
    route = "main_screen_f4",
    icon = Icons.Default.LocalShipping,
    titleArab = "مقسم المنتجات الى الجمالين",
    color = Color(0xFF3F51B5)
)

data object InfosDatas_FramgmentId5 : Screen(
    id =5,
    icon = Icons.AutoMirrored.Filled.FactCheck,
    route = "A_ID5_VerificationProduitAcGrossist",
    titleArab = "التاكد من فواتير مع المنتجات عند الجمال",
    color = Color(0xFFFF5892)
)

data object InfosDatas_FramgmentId6 : Screen(
    id =6,
    icon = Icons.Default.PinDrop,
    route = "Id_App2Fragment1",
    titleArab = "محدد اماكن الزبائن GPS",
    color = Color(0xFFFF9800)

)
data object InfosDatas_App4FramgmentId  : Screen(
    keyID = "A4F1",
    id =8,
    icon = Icons.Default.PinDrop,
    route = "Id_App2Fragment1",
    titleArab = "",
    color = Color(0xFFFF9800)

)
data object StartupIcon_Start : Screen(
    id =7,
    icon = Icons.Default.Home,
    color = Color(0xFF3A3533),
    route = "StartupIcon_Start",
    titleArab = "المدخل الرئيسي"
)

abstract class Screen(
    val id: Long,
    val titleArab: String,
    val color: Color,
    val icon: ImageVector,
    val route: String,
    val keyID: String=""
)


