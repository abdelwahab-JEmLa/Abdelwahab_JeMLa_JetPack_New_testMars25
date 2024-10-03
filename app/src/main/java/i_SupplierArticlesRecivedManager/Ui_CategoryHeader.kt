package i_SupplierArticlesRecivedManager


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector


@Composable
fun OverlayContentECB(color: Color, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White)
    }
}

@Composable
fun DisponibilityOverlayECB(state: String) {
    when (state) {
        "Non Dispo" -> OverlayContentECB(color = Color.Black, icon = Icons.Default.TextDecrease)
        "NonForNewsClients" -> OverlayContentECB(color = Color.Gray, icon = Icons.Default.Person)
    }
}

