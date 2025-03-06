package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.A.Main.Screen
import Z_MasterOfApps.Z_AppsFather.Kotlin._4.Modules.GlideDisplayImageBykeyId
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ContentAlpha

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
