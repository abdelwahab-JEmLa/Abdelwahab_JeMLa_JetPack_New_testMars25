package Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.A

import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils.A_ChangeIdColor
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils.AddMarkerButton
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils.ClearHistoryButton
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils.LabelsButton
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils.LocationTrackingButton
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils.MenuButton
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils.rememberLocationTracker
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.ViewModel.Extension.ViewModelExtension_App2_F1
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.ViewModel.Extension.VisbleClientsNow
import Z_MasterOfApps.Z.Android.A.Main.Utils.LottieJsonGetterR_Raw_Icons
import Z_MasterOfApps.Z.Android.A.Main.Utils.XmlsFilesHandler.Companion.xmlResources
import Z_MasterOfApps.Z_AppsFather.Kotlin.Partage.Views.AnimatedIconLottieJsonFile
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.roundToInt

@Composable
fun A_GlobalOptionsControlsFloatingActionButtons_FragId1(
    extensionVM: ViewModelExtension_App2_F1,
    mapView: MapView,
    viewModelInitApp: ViewModelInitApp,
    onClear: () -> Unit,
    onFilterMarkers: () -> Unit,
    currentFilterMode: VisbleClientsNow,
    ) {
    var showMenu by remember { mutableStateOf(false) }
    var showLabels by remember { mutableStateOf(false) }
    val proximiteMeter = 50.0
    val context = mapView.context
    val packageName = context.packageName

    // Create LocationTracker
    val locationTracker = rememberLocationTracker(
        mapView = mapView,
        proxim = proximiteMeter,
        xmlResources = xmlResources
    )

    // États pour le drag
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (showMenu) {
                    LocationTrackingButton(
                        showLabels = showLabels,
                        mapView = mapView,
                        proximiteMeter = proximiteMeter,
                        xmlResources = xmlResources
                    )

                    But1_NearbyMarkersButton(
                        showLabels = showLabels,
                        viewModelInitApp = viewModelInitApp,
                        markers = mapView.overlays.filterIsInstance<Marker>().toMutableList(),
                        locationTracker = locationTracker,
                        proximiteMeter = proximiteMeter,
                        mapView = mapView
                    )

                    AddMarkerButton(
                        extensionVM = extensionVM,
                        showLabels = showLabels,
                        mapView = mapView,
                    )

                    FragId1But_3(
                        showLabels = showLabels,
                        extensionVM=extensionVM,
                    )

                    if (!packageName.contains("clientje") ) {
                        But_2(
                            currentFilterMode=currentFilterMode,

                            extensionVM = extensionVM,
                            viewModel = viewModelInitApp,
                            showLabels = showLabels,
                            onClick = onFilterMarkers
                        )

                        A_ChangeIdColor(
                            viewModelInitApp = viewModelInitApp,
                            showLabels = showLabels,
                        )

                        ClearHistoryButton(
                            viewModelInitApp = viewModelInitApp,
                            showLabels = showLabels,
                            onClear,
                        )
                    }
                }

                LabelsButton(
                    showLabels = showLabels,
                    onShowLabelsChange = { showLabels = it }
                )

                MenuButton(
                    showLabels = showLabels,
                    showMenu = showMenu,
                    onShowMenuChange = { showMenu = it }
                )
            }
        }
    }
}

@Composable
fun ControlButton(
    onClick: () -> Unit,
    icon: Any,
    contentDescription: String,
    showLabels: Boolean,
    labelText: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (icon) {
            is ImageVector -> {
                FloatingActionButton(
                    onClick = {
                        onClick()
                    },
                    modifier = modifier.size(40.dp),
                    containerColor = containerColor
                ) {
                    Icon(icon, contentDescription)
                }
            }
            is LottieJsonGetterR_Raw_Icons -> {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            onClick()
                        }
                        .background(
                            color = containerColor,
                            shape = CircleShape
                        )
                        .also {
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedIconLottieJsonFile(
                        ressourceXml = icon,
                        onClick = onClick
                    )
                }
            }
            else -> {
                throw IllegalArgumentException("Unsupported icon type")
            }
        }

        if (showLabels) {
            Text(
                labelText,
                modifier = Modifier
                    .background(containerColor)
                    .padding(4.dp),
                color = Color.White
            )
        }
    }
}
