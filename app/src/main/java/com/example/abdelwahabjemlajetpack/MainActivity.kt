package com.example.abdelwahabjemlajetpack

import ZA_Learn_WhelPiker.PickerExample
import a_RoomDB.AppDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditRoad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ContentAlpha
import b2_Edite_Base_Donne_With_Creat_New_Articls.HeadOfViewModelFactory
import b2_Edite_Base_Donne_With_Creat_New_Articls.HeadOfViewModels
import b2_Edite_Base_Donne_With_Creat_New_Articls.MainFragmentEditDatabaseWithCreateNewArticles
import b2_Edite_Base_Donne_With_Creat_New_Articls.RepositeryCreatAndEditeDataBase
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
import java.util.Locale

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
    private val repositeryCreatAndEditeDataBase by lazy {
        RepositeryCreatAndEditeDataBase() // Assurez-vous que cette classe existe et peut être instanciée ainsi
    }
    private val headOfViewModels: HeadOfViewModels by viewModels {
        HeadOfViewModelFactory(
            context = this@MainActivity,
            repositeryCreatAndEditeDataBase = repositeryCreatAndEditeDataBase
        )
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

                var showProgressBar by remember { mutableStateOf(false) }
                var progress by remember { mutableStateOf(0f) }

                Scaffold(
                    bottomBar = {
                        if (isNavBarVisible) {
                            Column {
                                if (showProgressBar) {
                                    ProgressIndicator(progress)
                                }
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
                            ToggleNavBarButton(isNavBarVisible) { isNavBarVisible = !isNavBarVisible }
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
                            onUpdateStart = { showProgressBar = true },
                            onUpdateProgress = { progress = it },
                            onUpdateComplete = {
                                showProgressBar = false
                                progress = 0f
                            }
                        )
                    }
                }
            }
        }
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
fun ToggleNavBarButton(isNavBarVisible: Boolean, onToggle: () -> Unit) {
    FloatingActionButton(onClick = onToggle) {
        Icon(
            if (isNavBarVisible) Icons.Filled.KeyboardArrowDown else Icons.Filled.Home,
            contentDescription = "Toggle Navigation Bar"
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    database: AppDatabase,
    viewModels: AppViewModels,
    onToggleNavBar: () -> Unit,
    onUpdateStart: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    onUpdateComplete: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "main_screen",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("main_screen") {
            MainScreen(
                navController = navController,
                editeBaseDonneViewModel = viewModels.editeBaseDonneViewModel,
                articleDao = database.articleDao(),
                boardStatistiquesStatViewModel = viewModels.boardStatistiquesStatViewModel,
            )
        }
        composable("A_Edite_Base_Screen") { A_Edite_Base_Screen(viewModels.editeBaseDonneViewModel, database.articleDao()) }
        composable("C_ManageBonsClients") { FragmentManageBonsClients(viewModels.boardStatistiquesStatViewModel) }
        composable("FragmentEntreBonsGro") { FragmentEntreBonsGro(database.articleDao()) }
        composable("FragmentCredits") { FragmentCredits(viewModels.creditsViewModel, onToggleNavBar = onToggleNavBar) }
        composable("FragmentCreditsClients") {
            FragmentCreditsClients(
                viewModels.creditsClientsViewModel,
                boardStatistiquesStatViewModel = viewModels.boardStatistiquesStatViewModel,
                onToggleNavBar = onToggleNavBar,
            )
        }
        composable("Main_FactoryClassemntsArticles") {
            MainFactoryClassementsArticles(
                viewModels.classementsArticlesViewModel,
                onToggleNavBar = onToggleNavBar,
                onUpdateStart = onUpdateStart,
                onUpdateProgress = onUpdateProgress,
                onUpdateComplete = onUpdateComplete
            )
        }
        composable("PickerExample") { PickerExample() }
        composable("main_fragment_edit_database_with_create_new_articles") {
            MainFragmentEditDatabaseWithCreateNewArticles(
                viewModel = viewModels.headOfViewModels,
                onToggleNavBar = onToggleNavBar,
                onUpdateStart = onUpdateStart,
                onUpdateProgress = onUpdateProgress,
                onUpdateComplete = onUpdateComplete
            )
        }
    }
}

object NavigationItems {
    fun getItems() = listOf(
        Screen.MainScreen,
        Screen.ManageBonsClients,
        Screen.EntreBonsGro,
        Screen.Credits,
        Screen.CreditsClients,
        Screen.FactoryClassemntsArticles,
        Screen.EditBaseScreen,
        Screen.EditDatabaseWithCreateNewArticles
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
    data  object ManageBonsClients : Screen("C_ManageBonsClients",
        Icons.AutoMirrored.Filled.List, "Manage Bons", Color(0xFFFFC107))
    data  object EntreBonsGro : Screen("FragmentEntreBonsGro", Icons.Default.Add, "Entre Bons", Color(0xFFE91E63))
    data   object Credits : Screen("FragmentCredits", Icons.Default.Info, "Credits", Color(0xFF9C27B0))
    data   object EditBaseScreen : Screen("A_Edite_Base_Screen", Icons.Default.Edit, "Edit Base", Color(0xFF2196F3))
    data object EditDatabaseWithCreateNewArticles : Screen("main_fragment_edit_database_with_create_new_articles", Icons.Default.EditRoad, "Create New Articles", Color(
        0xFFE30E0E
    )
    )
    data   object FactoryClassemntsArticles : Screen("Main_FactoryClassemntsArticles", Icons.Default.Refresh, "Classements", Color(0xFFFF5722))
}

@Composable
fun MainScreen(
    navController: NavHostController,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao,
    boardStatistiquesStatViewModel: BoardStatistiquesStatViewModel
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                CardBoardStatistiques(boardStatistiquesStatViewModel)

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        MenuCard("Edit Base Screen", "A_Edite_Base_Screen", navController, Icons.Default.Edit)
                    }
                    item {
                        MenuCard("Manage Bons Clients", "C_ManageBonsClients", navController, Icons.Default.List)
                    }
                    item {
                        MenuCard("Entre Bons Gro", "FragmentEntreBonsGro", navController, Icons.Default.Add)
                    }
                    item {
                        MenuCard("Credits", "FragmentCredits", navController, Icons.Default.MonetizationOn)
                    }
                    item {
                        MenuCard("CreditsClients", "FragmentCreditsClients", navController, Icons.Default.People)
                    }
                    item {
                        MenuCard("FactoryClassemntsArticles", "Main_FactoryClassemntsArticles", navController, Icons.Default.List)
                    }
                    item {
                        MenuCard("Picker Example", "PickerExample", navController, Icons.Default.DateRange)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(title: String, route: String, navController: NavHostController, icon: ImageVector) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { navController.navigate(route) },
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp)
            )
            Text(
            text = title.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
}


