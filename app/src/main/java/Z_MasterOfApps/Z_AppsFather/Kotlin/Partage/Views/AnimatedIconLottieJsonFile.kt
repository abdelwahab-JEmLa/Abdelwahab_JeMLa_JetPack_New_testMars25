package Z_MasterOfApps.Z_AppsFather.Kotlin.Partage.Views

import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun AnimatedIconLottieJsonFile(
    ressourceXml: LottieJsonGetterR_Raw_Icons,
    onClick: () -> Unit ={}
) {
    val isPlaying by remember { mutableStateOf(true) }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(ressourceXml.resourceId)
    )

    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        iterations = LottieConstants.IterateForever,
        speed = 1.5f
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .semantics {
                contentDescription = "tout les produit clear"
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
                    .offset(x = (-2).dp, y = 0.dp),
                contentScale = ContentScale.FillBounds
            )
        }
    }
}
