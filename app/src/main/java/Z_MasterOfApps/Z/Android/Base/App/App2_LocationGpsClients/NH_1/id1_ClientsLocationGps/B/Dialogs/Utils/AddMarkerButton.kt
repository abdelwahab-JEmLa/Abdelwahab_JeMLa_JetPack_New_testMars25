package Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils

import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.ViewModel.Extension.ViewModelExtension_App2_F1
import Z_MasterOfApps.Z.Android.A.Main.Utils.XmlsFilesHandler.Companion.fixXmlResources
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.osmdroid.views.MapView

@Composable
fun AddMarkerButton(
    extensionVM: ViewModelExtension_App2_F1,
    showLabels: Boolean,
    mapView: MapView,
) {
    var isPlaying by remember { mutableStateOf(false) }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(fixXmlResources("reacticonanimatedjsonurl"))
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = 1,
        speed = 1.5f
    )

    LaunchedEffect(progress) {
        if (progress == 1f) {
            isPlaying = false
        }
    }

    IconButton(
        onClick = {
            isPlaying = true
            extensionVM.onClickAddMarkerButton(mapView)
        },
        modifier = Modifier
            .size(40.dp)
            .semantics {
                contentDescription = "Add marker to map"
            }
    ) {
        Box(
            modifier = Modifier.size(70.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier
                    .size(70.dp)
                    .offset(x = (-2).dp, y = 0.dp),  // Ajustement pour centrer l'animation
                contentScale = ContentScale.FillBounds
            )
        }
    }
}
