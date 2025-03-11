package Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.B.Dialogs

import Z_CodePartageEntreApps.Model.B_ClientsDataBase
import Z_CodePartageEntreApps.Model.B_ClientsDataBase.Companion.updateClientsDataBase
import Z_CodePartageEntreApps.Model._ModelAppsFather
import Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.ViewModel.Extension.Utils.updateLongAppSetting
import Z_CodePartageEntreApps.SectionApp.A_LocationGpsClients.App.ViewModel.Extension.ViewModelExtension_App2_F1
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.views.overlay.Marker

@Composable
fun MarkerStatusDialog(
    extensionVM: ViewModelExtension_App2_F1,
    viewModel: ViewModelInitApp,
    selectedMarker: Marker?,
    onDismiss: () -> Unit,
    onUpdateLongAppSetting: () -> Unit = {},
    onClickToEditeMarquerPosition: (Long) -> Unit,
    onRemoveMark: (Marker?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var editedPhone by remember { mutableStateOf("") }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    if (selectedMarker == null) return

    Dialog(onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showEditDialog = true }
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedMarker.title ?: "Client",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        // Display phone number if available
                        val client = viewModel._modelAppsFather.clientDataBase.find {
                            it.id.toString() == selectedMarker.id
                        }
                        if (!client?.statueDeBase?.numTelephone.isNullOrEmpty()) {
                            Text(
                                text = client?.statueDeBase?.numTelephone ?: "",
                                modifier = Modifier.clickable { showPhoneDialog = true },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Row {
                        // GPS position editing button
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Edit location",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable {
                                    onClickToEditeMarquerPosition(selectedMarker.id.toLong())
                                    onDismiss()
                                }
                        )

                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit client"
                        )

                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete client",
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .clickable {
                                    // Show confirmation dialog instead of deleting immediately
                                    showDeleteConfirmationDialog = true
                                }
                        )
                    }
                }

                // Status buttons remain the same
                StatusButton(
                    text = "Mode Commande",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(ContextCompat.getColor(context, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.ON_MODE_COMMEND_ACTUELLEMENT.color)),
                    onClick = {
                        coroutineScope.launch {
                            extensionVM.updateLongAppSetting(selectedMarker.id.toLong())
                            onUpdateLongAppSetting()
                            onDismiss()
                        }
                    }
                )

                StatusButton(
                    text = "Client Cible",
                    icon = Icons.Default.Person,
                    color = Color(ContextCompat.getColor(context, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.Cible.color)),
                    onClick = {
                        coroutineScope.launch {
                            extensionVM.updateStatueClient(selectedMarker, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.Cible)
                            onDismiss()
                        }
                    }
                )

                StatusButton(
                    text = "CIBLE_POUR_2",
                    icon = Icons.Default.Person,
                    color = Color(ContextCompat.getColor(context, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.CIBLE_POUR_2.color)),
                    onClick = {
                        coroutineScope.launch {
                            extensionVM.updateStatueClient(selectedMarker, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.CIBLE_POUR_2)
                            onDismiss()
                        }
                    }
                )

                StatusButton(
                    text = "Client Absent",
                    icon = Icons.Default.Person,
                    color = Color(ContextCompat.getColor(context, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.CLIENT_ABSENT.color)),
                    onClick = {
                        coroutineScope.launch {
                            extensionVM.updateStatueClient(selectedMarker, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.CLIENT_ABSENT)
                            onDismiss()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatusButton(
                    text = "Avec Marchandise",
                    icon = Icons.Default.ShoppingCart,
                    color = Color(ContextCompat.getColor(context, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.AVEC_MARCHANDISE.color)),
                    onClick = {
                        coroutineScope.launch {
                            extensionVM.updateStatueClient(selectedMarker, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.AVEC_MARCHANDISE)
                            onDismiss()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                StatusButton(
                    text = "Fermé",
                    icon = Icons.Default.Lock,
                    color = Color(ContextCompat.getColor(context, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.FERME.color)),
                    onClick = {
                        coroutineScope.launch {
                            extensionVM.updateStatueClient(selectedMarker, B_ClientsDataBase.GpsLocation.DernierEtatAAffiche.FERME)
                            onDismiss()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Fermer")
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modifier les informations") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Nom du client") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text("Numéro de téléphone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            selectedMarker.title = editedName
                            val client = viewModel._modelAppsFather.clientDataBase.find {
                                it.id.toString() == selectedMarker.id
                            }

                            client?.let { foundClient ->
                                foundClient.nom = editedName
                                foundClient.statueDeBase.numTelephone = editedPhone
                                foundClient.updateClientsDataBase(viewModel)

                                viewModel._modelAppsFather.produitsMainDataBase
                                    .filter { product ->
                                        product.bonsVentDeCetteCota.any { bonVent ->
                                            bonVent.clientIdChoisi == foundClient.id
                                        }
                                    }
                                    .forEach { product ->
                                        _ModelAppsFather.updateProduit(product, viewModel)
                                    }
                            }
                            selectedMarker.showInfoWindow()
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showPhoneDialog) {
        val client = viewModel._modelAppsFather.clientDataBase.find {
            it.id.toString() == selectedMarker.id
        }
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false },
            title = { Text("Numéro de téléphone") },
            text = {
                Text(
                    text = client?.statueDeBase?.numTelephone ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { showPhoneDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // New delete confirmation dialog
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirmer la suppression") },
            text = {
                Text("Êtes-vous sûr de vouloir supprimer ce client ? Cette action est irréversible.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            val clientToDelete = viewModel._modelAppsFather.clientDataBase.find {
                                it.id.toString() == selectedMarker.id
                            }

                            clientToDelete?.let { client ->
                                B_ClientsDataBase.refClientsDataBase
                                    .child(client.id.toString())
                                    .removeValue()
                                    .await()

                                viewModel._modelAppsFather.clientDataBase.remove(client)
                                onRemoveMark(selectedMarker)
                                onDismiss()
                            }
                            showDeleteConfirmationDialog = false
                        }
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun StatusButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(text)
        }
    }
}
