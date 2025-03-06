package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup

import Z_MasterOfApps.A_WorkingOn.C.FragID_1_DialogeCategoryReorderAndSelectionWindow.A_MainScreen_SectionID4_FragmentID1
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.A_OptionsControlsButtons
import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs.A_OptionsDialog.A_OptionsDialog
import Z_MasterOfApps.Z.Android.A.Main.NavigationItems
import Z_MasterOfApps.Z.Android.A.Main.Screen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun A_StartupScreen(
    viewModelInitApp: ViewModelInitApp = viewModel(),
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCategorySelection by remember { mutableStateOf(false) }

    val isManagerPhone = viewModelInitApp._paramatersAppsViewModelModel.cLeTelephoneDuGerant ?: false
    val items = remember(isManagerPhone) { NavigationItems.getItems(isManagerPhone) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = modifier.fillMaxSize()
        ) {
            listOf(
                "القائمة الرئيسية" to { screen: Screen -> screen.route == "StartupIcon_Start" },
                "قسم المدير" to { screen: Screen ->
                    screen.route in setOf("main_screen_f4", "fragment_main_screen_1", "A_ID5_VerificationProduitAcGrossist")
                },
                "قسم العملاء" to { screen: Screen ->
                    screen.route in setOf("main_screen_f2", "مظهر الاماكن لمقسم المنتجات على الزبائن")
                },
                "خريطة التطبيق" to { screen: Screen -> screen.route == "Id_App2Fragment1" }
            ).forEach { (title, filter) ->
                val sectionItems = items.filter(filter)
                if (sectionItems.isNotEmpty() && (title != "قسم المدير" && title != "خريطة التطبيق" || isManagerPhone)) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(sectionItems) { screen ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { onNavigate(screen.route) },
                            colors = CardDefaults.cardColors(containerColor = screen.color),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "ID: ${screen.id}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = screen.icon,
                                        contentDescription = screen.titleArab,
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = screen.titleArab,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (viewModelInitApp._paramatersAppsViewModelModel.fabsVisibility) {
            A_OptionsControlsButtons(
                extensionVM = viewModelInitApp.extentionStartup, // Utilisez l'instance existante
                viewModelInitApp = viewModelInitApp,
                paddingValues = PaddingValues()  ,
            )
        }

        A_OptionsDialog(
            viewModelInitApp = viewModelInitApp,
            onDismiss = { viewModelInitApp.extentionStartup.dialogeOptions = false }
        )

        if (showCategorySelection) {
            A_MainScreen_SectionID4_FragmentID1() { showCategorySelection = false}
        }
    }
}
