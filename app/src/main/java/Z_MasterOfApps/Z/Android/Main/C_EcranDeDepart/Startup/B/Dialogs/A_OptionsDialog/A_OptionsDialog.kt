package Z_MasterOfApps.Z.Android.Main.C_EcranDeDepart.Startup.B.Dialogs.A_OptionsDialog

import Z_MasterOfApps.Kotlin.Model.E_AppsOptionsStates
import Z_MasterOfApps.Kotlin.ViewModel.ViewModelInitApp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun A_OptionsDialog(
    viewModelInitApp: ViewModelInitApp,
    onDismiss: () -> Unit,
) {
    var showAddPrototypeDialog by remember { mutableStateOf(false) }
    var selectedPrototype by remember {
        mutableStateOf<E_AppsOptionsStates.F_PrototypseDeProgramationInfos?>(
            null
        )
    }

    if (viewModelInitApp.extentionStartup.dialogeOptions) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("خيارات البرنامج") },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPrototype = null
                                    showAddPrototypeDialog = true
                                },
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add prototype"
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text("إضافة نموذج برمجة جديد")
                            }
                        }
                    }

                    items(viewModelInitApp._modelAppsFather.f_PrototypseDeProgramationInfos) { prototype ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPrototype = prototype
                                    showAddPrototypeDialog = true
                                },
                            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = prototype.titre ?: "بدون عنوان",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "التاريخ: ${prototype.dateInString}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (prototype.commentairesDesChangements.isNotEmpty()) {
                                    Text(
                                        text = "التعليقات: ${prototype.commentairesDesChangements.joinToString()}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            ),
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("إغلاق")
                }
            }
        )
    }

    if (showAddPrototypeDialog) {
        AddPrototypeDialog(
            onDismiss = { showAddPrototypeDialog = false },
            onConfirm = { title, productId, comment ->
                if (selectedPrototype == null) {
                    val newPrototype = E_AppsOptionsStates.F_PrototypseDeProgramationInfos().apply {
                        vid = System.currentTimeMillis()
                        titre = title
                        dateInString = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                        idPremierProduitOuCesChangementEstAplique = productId
                        if (comment.isNotBlank()) {
                            commentairesDesChangements.add(comment)
                        }
                    }
                    viewModelInitApp.extentionStartup.addPrototype(newPrototype)
                } else {
                    if (comment.isNotBlank()) {
                        selectedPrototype!!.commentairesDesChangements.add(comment)
                        viewModelInitApp.extentionStartup.updatePrototype(selectedPrototype!!)
                    }
                }
                showAddPrototypeDialog = false
            },
            prototype = selectedPrototype
        )
    }
}

@Composable
private fun AddPrototypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, productId: Long, comment: String) -> Unit,
    prototype: E_AppsOptionsStates.F_PrototypseDeProgramationInfos? = null
) {
    var title by remember { mutableStateOf(prototype?.titre ?: "") }
    var productId by remember {
        mutableStateOf(
            prototype?.idPremierProduitOuCesChangementEstAplique?.toString() ?: ""
        )
    }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (prototype == null) "إضافة نموذج جديد" else "تحديث النموذج") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("titre Changements") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = prototype == null
                )
                OutlinedTextField(
                    value = productId,
                    onValueChange = { productId = it },
                    label = { Text("idPremierProduitOuCesChangementEstAplique") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = prototype == null
                )
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("comment") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (prototype != null && prototype.commentairesDesChangements.isNotEmpty()) {
                    Text(
                        "التعليقات السابقة:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    prototype.commentairesDesChangements.forEach { existingComment ->
                        Text(
                            existingComment,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val id = productId.toLongOrNull() ?: 0L
                    onConfirm(title, id, comment)
                },
                enabled = (title.isNotBlank() && productId.isNotBlank()) || (prototype != null && comment.isNotBlank())
            ) {
                Text(if (prototype == null) "إضافة" else "تحديث")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
