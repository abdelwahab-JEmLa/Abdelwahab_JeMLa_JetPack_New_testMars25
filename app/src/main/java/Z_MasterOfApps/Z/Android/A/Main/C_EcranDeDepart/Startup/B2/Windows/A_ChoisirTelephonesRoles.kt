package Z_MasterOfApps.Z.Android.A.Main.C_EcranDeDepart.Startup.B2.Windows

import Z_CodePartageEntreApps.Model.J_AppInstalleDonTelephone
import Z_CodePartageEntreApps.Model.J_AppInstalleDonTelephoneRepository
import Z_CodePartageEntreApps.Model.J_AppInstalleDonTelephoneRepositoryImpl
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmergencyRecording
import androidx.compose.material.icons.filled.MapsHomeWork
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MainScreen_Windows4(
    modifier: Modifier = Modifier,
    viewModel: ViewModelW4 = koinViewModel(),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Role Phone",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            MainList_Windows4(viewModel = viewModel)
        }
    }
}

@Composable
fun MainList_Windows4(
    modifier: Modifier = Modifier,
    viewModel: ViewModelW4 = koinViewModel(),
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(viewModel.j_AppInstalleDonTelephone.modelDatas) { phoneData ->
            MainItem_Windows4(
                phoneData = phoneData,
                onReceiverToggle = {
                    viewModel.setAsReceiverPhone(phoneData)
                }
            )
        }
    }
}

@Composable
fun MainItem_Windows4(
    modifier: Modifier = Modifier,
    phoneData: J_AppInstalleDonTelephone,
    onReceiverToggle: () -> Unit
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = phoneData.infosDeBase.nom,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Screen width: ${phoneData.infosDeBase.widthScreen}px",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(onClick = onReceiverToggle) {
                Icon(
                    imageVector = if (phoneData.etatesMutable.itsReciverTelephone)
                        Icons.Default.EmergencyRecording else Icons.Default.MapsHomeWork,
                    contentDescription = "Toggle receiver status",
                    tint = if (phoneData.etatesMutable.itsReciverTelephone)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreen_Windows4Preview() {
    // Utiliser le repository réel connecté à Firebase
    val firebaseRepository = J_AppInstalleDonTelephoneRepositoryImpl()
    val viewModel = ViewModelW4(firebaseRepository)

    MaterialTheme {
        MainScreen_Windows4(viewModel = viewModel)
    }
}

class ViewModelW4(
    val j_AppInstalleDonTelephone: J_AppInstalleDonTelephoneRepository,
) : ViewModel() {
    fun setAsReceiverPhone(phone: J_AppInstalleDonTelephone) {
        val updatedPhone = j_AppInstalleDonTelephone.modelDatas.find { it.id == phone.id }

        updatedPhone?.etatesMutable?.itsReciverTelephone =
            !updatedPhone?.etatesMutable?.itsReciverTelephone!!

        j_AppInstalleDonTelephone.updatePhones()
    }
}

