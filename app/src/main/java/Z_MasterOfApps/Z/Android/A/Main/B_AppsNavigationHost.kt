package Z_MasterOfApps.Z.Android.A.Main

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.A_StartupScreen
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.C_SectionAppChoisisuer
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B2.Windows.MainScreen_Windows4
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_1.id4_DeplaceProduitsVerGrossist.A_id4_DeplaceProduitsVerGrossist
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_2.id1_GerantDefinirePosition.A_id1_GerantDefinirePosition
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_3.id5_VerificationProduitAcGrossist.A_ID5_VerificationProduitAcGrossist
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_4.id2_TravaillieurListProduitAchercheChezLeGrossist.A_Id2_TravaillieurListProduitAchercheChezLeGrossist
import Z_MasterOfApps.Z.Android.Base.App.App._1.GerantAfficheurGrossistCommend.App.NH_5.id3_AfficheurDesProduitsPourLeColecteur.A_id3_AfficheurDesProduitsPourLeColecteur
import Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.A_id1_ClientsLocationGps
import Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.GlideDisplayImageBykeyId
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.StarBorderPurple500
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ContentAlpha
import org.koin.androidx.compose.koinViewModel

enum class SectionsAPP {
    BASE_DONNE,
    MANAGE_ACHATS
}

@Composable
fun AppNavigationHost(
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val viewModelInitApp: ViewModelInitApp = koinViewModel()
    val extentionStartup = viewModelInitApp.extentionStartup
    val navController = rememberNavController()
    val isManagerPhone =
        viewModelInitApp._paramatersAppsViewModelModel.cLeTelephoneDuGerant ?: false

    // Make items respond to changes in extentionStartup.sectionDesFragmentAppAfficheMNT
    val items = remember(isManagerPhone, extentionStartup.sectionDesFragmentAppAfficheMNT) {
        NavigationItems.getItems(
            isManagerPhone,
            extentionStartup.sectionDesFragmentAppAfficheMNT
        )
    }

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
                                A_id3_AfficheurDesProduitsPourLeColecteur()
                            }
                            composable(InfosDatas_FramgmentId6.route) {
                                A_id1_ClientsLocationGps(viewModel = viewModelInitApp)
                            }
                            composable(InfosDatas_App4FramgmentId1.route) {
                                MainScreen_Windows4()
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
                                }, onClick = onClick)
                            }
                        }
                    }
                }
            }
        }

        // Show the dialog if requested
        if (showDialog) {
            C_SectionAppChoisisuer(
                extentionStartup = extentionStartup,
            )
            { showDialog = false }
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
    fun getItems(
        isManagerPhone: Boolean,
        sectoionDesFragmentAppAfficheMNT: SectionsAPP? = SectionsAPP.MANAGE_ACHATS
    ) = buildList {
        add(StartupIcon_Start)

        // Select which navigation items to show based on the active section
        when (sectoionDesFragmentAppAfficheMNT) {
            SectionsAPP.MANAGE_ACHATS -> {
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
            SectionsAPP.BASE_DONNE -> {
                // Only add the database section item when in BASE_DONNE mode
                add(InfosDatas_App4FramgmentId1)
            }
            else -> {}
        }
    }
}

data object InfosDatas_App4FramgmentId1 : Screen(
    keyID = "A4F1",
    id = 8,
    icon = Icons.Default.PinDrop,
    route = "InfosDatas_App4FramgmentId1",
    titleArab = "قاعدة البيانات", // Add a proper title
    color = Color(0xFFFF9800)
)

data object InfosDatas_FragmentId1 : Screen(
    1,
    "محدد اماكن المنتجات عند الجمال",
    Color(0xFFFF5722),
    Icons.Default.StarBorderPurple500,
    "fragment_main_screen_1",
)

data object InfosDatas_FramgmentId2 : Screen(
    id = 2,
    icon = Icons.Default.Visibility,
    route = "main_screen_f2",
    titleArab = "مظهر اماكن المنتجات عند الجمال",
    color = Color(0xFFA48E39)
)

data object InfosDatas_FramgmentId3 : Screen(
    id = 3,
    route = "مظهر الاماكن لمقسم المنتجات على الزبائن",
    icon = Icons.Default.Groups,
    titleArab = "مظهر الاماكن لمقسم المنتجات على الزبائن",
    color = Color(0xFF9C27B0)
)

data object InfosDatas_FramgmentId4 : Screen(
    id = 4,
    route = "main_screen_f4",
    icon = Icons.Default.LocalShipping,
    titleArab = "مقسم المنتجات الى الجمالين",
    color = Color(0xFF3F51B5)
)

data object InfosDatas_FramgmentId5 : Screen(
    id = 5,
    icon = Icons.AutoMirrored.Filled.FactCheck,
    route = "A_ID5_VerificationProduitAcGrossist",
    titleArab = "التاكد من فواتير مع المنتجات عند الجمال",
    color = Color(0xFFFF5892)
)

data object InfosDatas_FramgmentId6 : Screen(
    id = 6,
    icon = Icons.Default.PinDrop,
    route = "Id_App2Fragment1",
    titleArab = "محدد اماكن الزبائن GPS",
    color = Color(0xFFFF9800)

)



data object StartupIcon_Start : Screen(
    id = 7,
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
    val keyID: String = ""
)


@Composable
fun NavigationBarWithFab(
    items: List<Screen>,
    viewModelInitApp: ViewModelInitApp,
    currentRoute: String?,
    onClickNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            val middleIndex = items.size / 2

            items.forEachIndexed { index, screen ->
                if (index == middleIndex) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { },
                        icon = { Box(modifier = Modifier.size(48.dp)) },
                        enabled = false
                    )
                }
                NavigationBarItem(
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.titleArab,
                                tint = if (currentRoute == screen.route) screen.color
                                else LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                            )
                            // Add fragment ID text below icon
                            Text(
                                text = "ID: ${screen.id}",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (currentRoute == screen.route) screen.color
                                else LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                            )
                        }
                    },
                    selected = currentRoute == screen.route,
                    onClick = { onClickNavigate(screen.route) },
                    // Disable the button when loading
                    enabled = !viewModelInitApp.isLoading
                )
            }
        }

        // FAB remains unchanged
        Surface(
            modifier = Modifier
                .offset(y = (-28).dp)
                .size(56.dp),
            shape = CircleShape,
        ) {
            Box {
                val fabsVisibility = viewModelInitApp
                    ._paramatersAppsViewModelModel
                    .fabsVisibility
                GlideDisplayImageBykeyId(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .clickable(
                            enabled = !viewModelInitApp.isLoading,
                            onClick = {
                                viewModelInitApp._paramatersAppsViewModelModel.fabsVisibility =
                                    !viewModelInitApp._paramatersAppsViewModelModel.fabsVisibility
                            }
                        ),
                    size = 100.dp
                )

                Icon(
                    imageVector = if (fabsVisibility
                    ) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle FAB",
                    modifier = Modifier.align(Alignment.Center),
                    tint = Color.White
                )
            }
        }
    }
}
