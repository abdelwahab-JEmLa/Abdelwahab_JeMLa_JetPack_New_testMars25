package com.example.abdelwahabjemlajetpack

import ZA_Learn_WhelPiker.PickerExample
import a_MainAppCompnents.BaseDonneECBTabelle
import a_MainAppCompnents.HeadOfViewModelFactory
import a_MainAppCompnents.HeadOfViewModels
import a_RoomDB.AppDatabase
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditRoad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ContentAlpha
import b2_Edite_Base_Donne_With_Creat_New_Articls.ArticleDetailWindow
import b2_Edite_Base_Donne_With_Creat_New_Articls.MainFragmentEditDatabaseWithCreateNewArticles
import b_Edite_Base_Donne.A_Edite_Base_Screen
import b_Edite_Base_Donne.ArticleDao
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import com.example.abdelwahabjemlajetpack.c_ManageBonsClients.FragmentManageBonsClients
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme
import d_EntreBonsGro.FragmentEntreBonsGro
import f_credits.CreditsViewModel
import f_credits.FragmentCredits
import g_BoardStatistiques.BoardStatistiquesStatViewModel
import g_BoardStatistiques.CardBoardStatistiques
import g_BoardStatistiques.f_2_CreditsClients.CreditsClientsViewModel
import g_BoardStatistiques.f_2_CreditsClients.FragmentCreditsClients
import h_FactoryClassemntsArticles.ClassementsArticlesViewModel
import h_FactoryClassemntsArticles.MainFactoryClassementsArticles
import i_SupplierArticlesRecivedManager.Fragment_SupplierArticlesRecivedManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {
    private lateinit var permissionHandler: PermissionHandler
    private val database by lazy { AppDatabase.getInstance(this) }
    private val editeBaseDonneViewModel: EditeBaseDonneViewModel by viewModels {
        MainAppViewModelFactory(database.articleDao())
    }
    private val creditsViewModel: CreditsViewModel by viewModels()
    private val creditsClientsViewModel: CreditsClientsViewModel by viewModels()
    private val boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel by viewModels()
    private val classementsArticlesViewModel: ClassementsArticlesViewModel by viewModels()
    private val headOfViewModels: HeadOfViewModels by viewModels {
        HeadOfViewModelFactory(
            context = this@MainActivity,)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionHandler = PermissionHandler(this)
        permissionHandler.checkAndRequestPermissions()

        setContent {
            AbdelwahabJeMLaJetPackTheme {
                val navController = rememberNavController()
                val items = NavigationItems.getItems()

                var isNavBarVisible by remember { mutableStateOf(true) }
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val uploadProgress by headOfViewModels.uploadProgress.collectAsState()
                val textProgress by headOfViewModels.textProgress.collectAsState()

                Scaffold(
                    bottomBar = {
                        if (isNavBarVisible) {
                            Column {
                                ProgressBarWithAnimation(uploadProgress, textProgress)
                                CustomNavigationBar(
                                    items = items,
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId)
                                            launchSingleTop = true
                                        }
                                    }
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (currentRoute == Screen.MainScreen.route) {
                            Column {
                                ToggleNavBarButton(isNavBarVisible) { isNavBarVisible = !isNavBarVisible }
                                Spacer(modifier = Modifier.height(16.dp))
                                MainActionsFab(
                                    headOfViewModels = headOfViewModels,
                                )
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AppNavHost(
                            navController = navController,
                            database = database,
                            viewModels = AppViewModels(
                                headOfViewModels,
                                editeBaseDonneViewModel,
                                creditsViewModel,
                                creditsClientsViewModel,
                                boardStatistiquesStatViewModel,
                                classementsArticlesViewModel
                            ),
                            onToggleNavBar = { isNavBarVisible = !isNavBarVisible },
                            headOfViewModels = headOfViewModels,

                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressBarWithAnimation(progress: Float, buttonName: String) {
    var isBlinking by remember { mutableStateOf(false) }
    var showProgressBar by remember { mutableStateOf(false) }

    LaunchedEffect(progress) {
        if (progress > 0f) {
            showProgressBar = true
            isBlinking = false
        } else if (progress == 0f && showProgressBar) {
            isBlinking = true
            delay(3000) // Blink for 3 seconds
            isBlinking = false
            showProgressBar = false
        }
    }

    if (showProgressBar) {
        val transition = rememberInfiniteTransition(label = "")
        val alpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = ""
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp) // Increased height
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .alpha(if (isBlinking) alpha else 1f)
        ) {
            // Red part (30% of the width)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.3f)
                    .background(Color.Red)
            )

            // Progress part
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress / 100f)
                    .background(MaterialTheme.colorScheme.primary)
            )

            // Text
            Text(
                text = buttonName,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun MainActionsFab(headOfViewModels: HeadOfViewModels) {
    val coroutineScope = rememberCoroutineScope()
    val isTimerActive by headOfViewModels.isTimerActive.collectAsState()

    FloatingActionButton(
        onClick = {
            coroutineScope.launch {
                headOfViewModels.updateColorsFromArticles()
            }
        },
        containerColor = Color.Red,
        modifier = Modifier.size(56.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ThumbUp,
            contentDescription = "Update Colors",
            tint = Color.White
        )
    }

    PressHoldButton(
        onPress = { headOfViewModels.startTimer() },
        onRelease = { headOfViewModels.stopTimer() },
        isActive = isTimerActive
    )
}

@Composable
fun PressHoldButton(
    onPress: () -> Unit,
    onRelease: () -> Unit,
    isActive: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(pressed) {
        if (pressed) {
            onPress()
        } else {
            onRelease()
        }
    }

    FloatingActionButton(
        onClick = { /* Do nothing on click */ },
        modifier = Modifier.size(56.dp),
        containerColor = if (isActive) Color.Gray else Color.Blue,
        interactionSource = interactionSource
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Activate Timer",
            tint = Color.White
        )
    }
}


@Composable
fun CustomNavigationBar(
    items: List<Screen>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = screen.title,
                        tint = screen.color
                    )
                },
                selected = currentRoute == screen.route,
                onClick = { onNavigate(screen.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = screen.color,
                    unselectedIconColor = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                )
            )
        }
    }
}
@Composable
fun MainScreen(
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao,
    boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel,
    headOfViewModels: HeadOfViewModels,
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                coroutineScope = coroutineScope,
                editeBaseDonneViewModel = editeBaseDonneViewModel,
                articleDao = articleDao,
                headOfViewModels = headOfViewModels
            )
        },
        floatingActionButton = {
            MainActionsFab(
                headOfViewModels = headOfViewModels,
            )
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CardBoardStatistiques(boardStatistiquesStatViewModel)
            }
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    database: AppDatabase,
    viewModels: AppViewModels,
    onToggleNavBar: () -> Unit,
    headOfViewModels: HeadOfViewModels,
    modifier: Modifier = Modifier,
) {
    val uiState by headOfViewModels.uiState.collectAsState()

    var localProgress by remember { mutableStateOf(0f) }
    val uploadProgress by viewModels.headOfViewModels.uploadProgress.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var dialogeDisplayeDetailleChanger by remember { mutableStateOf<BaseDonneECBTabelle?>(null) }
    val currentEditedArticle by headOfViewModels.currentEditedArticle.collectAsState()
    var reloadTrigger by remember { mutableIntStateOf(0) }
    // Remove this LaunchedEffect as it's overwriting our manual settings
    LaunchedEffect(currentEditedArticle) {
        dialogeDisplayeDetailleChanger = currentEditedArticle
    }

    // Logging for debugging
    LaunchedEffect(dialogeDisplayeDetailleChanger) {
        Log.d("MainFragment", "dialogeDisplayeDetailleChanger updated: $dialogeDisplayeDetailleChanger")
    }

    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(
                editeBaseDonneViewModel = viewModels.editeBaseDonneViewModel,
                articleDao = database.articleDao(),
                boardStatistiquesStatViewModel = viewModels.boardStatistiquesStatViewModel,
                headOfViewModels = headOfViewModels,
            )
        }
        composable("A_Edite_Base_Screen") {
            A_Edite_Base_Screen(viewModels.editeBaseDonneViewModel, database.articleDao())
        }
        composable("C_ManageBonsClients") {
            FragmentManageBonsClients(viewModels.boardStatistiquesStatViewModel, headOfViewModels)
        }
        composable("Fragment_SupplierArticlesRecivedManager") {
            Fragment_SupplierArticlesRecivedManager(viewModels.headOfViewModels,
                onToggleNavBar = onToggleNavBar,
                modifier=modifier ,
                onNewArticleAdded={dialogeDisplayeDetailleChanger=it}
            )
        }
        composable("FragmentEntreBonsGro") {
            FragmentEntreBonsGro(database.articleDao())
        }
        composable("FragmentCredits") {
            FragmentCredits(viewModels.creditsViewModel, onToggleNavBar = onToggleNavBar)
        }
        composable("FragmentCreditsClients") {
            FragmentCreditsClients(
                viewModels.creditsClientsViewModel,
                boardStatistiquesStatViewModel = viewModels.boardStatistiquesStatViewModel,
                onToggleNavBar = onToggleNavBar,
            )
        }
        composable("Main_FactoryClassemntsArticles") {
            Box(modifier = Modifier.fillMaxSize()) {
                MainFactoryClassementsArticles(
                    viewModels.classementsArticlesViewModel,
                    onToggleNavBar = onToggleNavBar,
                    onUpdateStart = {
                        headOfViewModels.totalSteps=3
                        headOfViewModels.currentStep=0

                        coroutineScope.launch {
                            viewModels.headOfViewModels.updateUploadProgressBarCounterAndItText(
                                "Starting Classements Articles Update",
                                headOfViewModels.currentStep++,
                                0f
                            )
                        }
                    },
                    onUpdateProgress = { progress ->
                        coroutineScope.launch {
                            viewModels.headOfViewModels.updateUploadProgressBarCounterAndItText(
                                "Updating Classements Articles",
                                headOfViewModels.currentStep++,
                                progress
                            )
                        }
                    },
                    onUpdateComplete = {
                        coroutineScope.launch {
                            viewModels.headOfViewModels.updateUploadProgressBarCounterAndItText(
                                "Classements Articles Update Complete",
                                headOfViewModels.currentStep++,
                                100f
                            )
                        }

                    }
                )
            }
        }
        composable("PickerExample") {
            PickerExample()
        }
        composable("main_fragment_edit_database_with_create_new_articles") {
            Box(modifier = Modifier.fillMaxSize()) {
                MainFragmentEditDatabaseWithCreateNewArticles(
                    viewModel = viewModels.headOfViewModels,
                    onToggleNavBar = onToggleNavBar ,
                    onNewArticleAdded={dialogeDisplayeDetailleChanger=it} ,
                    reloadTrigger=reloadTrigger
                )
                if (uploadProgress > 0f && uploadProgress < 100f) {
                    CircularProgressIndicator(
                        progress = { uploadProgress / 100f },
                        modifier = Modifier.align(Alignment.Center),
                        trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                    )
                }
            }
        }
    }
    dialogeDisplayeDetailleChanger?.let { article ->
        Log.d("MainFragment", "Displaying ArticleDetailWindow for: $article")
        ArticleDetailWindow(
            article = article,
            uiState=uiState,
            onDismiss = {
                Log.d("MainFragment", "ArticleDetailWindow dismissed")
                dialogeDisplayeDetailleChanger=null
                headOfViewModels.updateCurrentEditedArticle(null)

                // Check if the article is new or if key changes occurred
                if (article.nomCategorie.contains("New", ignoreCase = true) ||
                    article.idArticleECB != dialogeDisplayeDetailleChanger?.idArticleECB
                ) {
                    // Trigger image reload
                    coroutineScope.launch {
                        for (i in 1..4) {
                            val fileName = "${article.idArticleECB}_$i.jpg"
                            val sourceFile = File(headOfViewModels.dossiesStandartOFImages, fileName)
                            if (sourceFile.exists()) {
                                headOfViewModels.setImagesInStorageFireBase(article.idArticleECB, i)
                            }
                        }
                        // Increment reloadTrigger to force recomposition
                        reloadTrigger += 1
                        Log.d("MainFragment", "Image reload triggered, reloadTrigger: $reloadTrigger")
                    }
                }
            },
            viewModel = headOfViewModels,
            modifier = Modifier.padding(horizontal = 3.dp),
            onReloadTrigger = { reloadTrigger += 1 },
            reloadTrigger = reloadTrigger
        )
    }

}

@Composable
fun ToggleNavBarButton(isNavBarVisible: Boolean, onToggle: () -> Unit) {
    FloatingActionButton(onClick = onToggle) {
        Icon(
            if (isNavBarVisible) Icons.Filled.KeyboardArrowDown else Icons.Filled.Home,
            contentDescription = "Toggle Navigation Bar"
        )
    }
}
object NavigationItems {
    fun getItems() = listOf(
        Screen.MainScreen,
        Screen.ManageBonsClients,
        Screen.Fragment_SupplierArticlesRecivedManager,
        Screen.EntreBonsGro,
        Screen.Credits,
        Screen.CreditsClients,
        Screen.FactoryClassemntsArticles,
        Screen.EditBaseScreen,
        Screen.EditDatabaseWithCreateNewArticles ,
    )
}

data class AppViewModels(
    val headOfViewModels: HeadOfViewModels,
    val editeBaseDonneViewModel: EditeBaseDonneViewModel,
    val creditsViewModel: CreditsViewModel,
    val creditsClientsViewModel: CreditsClientsViewModel,
    val boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel,
    val classementsArticlesViewModel: ClassementsArticlesViewModel
)

sealed class Screen(val route: String, val icon: ImageVector, val title: String, val color: Color) {
    data object MainScreen : Screen("main_screen", Icons.Default.Home, "Home", Color(0xFF4CAF50))
    data object CreditsClients : Screen("FragmentCreditsClients", Icons.Default.Person, "Credits Clients", Color(0xFF3F51B5))
    data  object ManageBonsClients : Screen("C_ManageBonsClients", Icons.AutoMirrored.Filled.List, "Manage Bons", Color(0xFFFFC107))
    data  object Fragment_SupplierArticlesRecivedManager : Screen("Fragment_SupplierArticlesRecivedManager", Icons.Default.LiveTv, "Fragment_SupplierArticlesRecivedManager", Color(0xFFF44336))
    data  object EntreBonsGro : Screen("FragmentEntreBonsGro", Icons.Default.Add, "Entre Bons", Color(0xFFE91E63))
    data   object Credits : Screen("FragmentCredits", Icons.Default.Info, "Credits", Color(0xFF9C27B0))
    data   object EditBaseScreen : Screen("A_Edite_Base_Screen", Icons.Default.Edit, "Edit Base", Color(0xFF2196F3))
    data object EditDatabaseWithCreateNewArticles : Screen("main_fragment_edit_database_with_create_new_articles", Icons.Default.EditRoad, "Create New Articles", Color(
        0xFFE30E0E
    )
    )
    data   object FactoryClassemntsArticles : Screen("Main_FactoryClassemntsArticles", Icons.Default.Refresh, "Classements", Color(0xFFFF5722))
}



