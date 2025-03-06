package Z_MasterOfApps.Z.Android.A.Main

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MainScreen_NewComputerPatterns(
    modifier: Modifier=Modifier,
    permissionsGranted: Boolean=true,
) {
    if (permissionsGranted) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigationHost(modifier)
        }
    }
}
