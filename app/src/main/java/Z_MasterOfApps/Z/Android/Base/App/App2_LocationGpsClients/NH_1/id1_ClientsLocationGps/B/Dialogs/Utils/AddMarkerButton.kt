package Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.Utils

import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.B.Dialogs.ControlButton
import Z_MasterOfApps.Z.Android.Base.App.App2_LocationGpsClients.NH_1.id1_ClientsLocationGps.ViewModel.Extension.ViewModelExtension_App2_F1
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.osmdroid.views.MapView

@Composable
fun AddMarkerButton(
    extensionVM: ViewModelExtension_App2_F1,
    showLabels: Boolean,
    mapView: MapView,
) {
    ControlButton(
        onClick = {
            extensionVM
                .onClickAddMarkerButton(
                    mapView
                )
        },
        icon = Icons.Default.Add, /*   //-->
        //TODO(1):
        j ai un json url   qui se trouve a  XmlsFilesHandler Art

                Copy
                {"v":"5.7.5","fr":100,"ip":0,"op":300,"w":2000,"h":1200,"nm":"Comp 1","ddd":0,"metadata":{"backgroundColor":{"r":255,"g":255,"b":255}},"assets":[],"layers":[{"ddd":0,"ind":12345679,"ty":4,"nm":"Group Layer 8","sr":1,"ks":{"p":{"a":0,"k":[1476,1073.5409836065573,0],"ix":2},"a":{"a":0,"k":[0,0],"ix":2},"s":{"a":0,"k":[204.91803278688522,204.91803278688522,100],"ix":2},"r":{"a":0,"k":0,"ix":2},"o":{"a":0,"k":100,"ix":2}},"ao":0,"shapes":[{"ty":"gr","it":[{"ty":"gr","it":
                    comme utilise la Lib Compottie    */
        contentDescription = "Add marker",
        showLabels = showLabels,
        labelText = "Add",
        containerColor = Color(0xFF2196F3)
    )
}
