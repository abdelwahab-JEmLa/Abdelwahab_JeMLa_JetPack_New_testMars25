package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B.Dialogs

import Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.ViewModel.Startup_Extension
import Z_MasterOfApps.Z.Android.A.Main.SectionsAPP
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
 fun C_SectionAppChoisisuer(
    extentionStartup: Startup_Extension,
    onDissmiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDissmiss() },
        title = { Text("SEction Gere->") },
        text = { Text("Choisissez une option:") },
        confirmButton = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                Button(
                    onClick = {
                        onDissmiss()
                        extentionStartup.sectionDesFragmentAppAfficheMNT = SectionsAPP.MANAGE_ACHATS
                    }
                ) {
                    Text(SectionsAPP.MANAGE_ACHATS.toString())
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onDissmiss()
                        extentionStartup.sectionDesFragmentAppAfficheMNT = SectionsAPP.BASE_DONNE
                    }
                ) {
                    Text(SectionsAPP.BASE_DONNE.toString())
                }
            }
        },
        dismissButton = {}
    )
}
