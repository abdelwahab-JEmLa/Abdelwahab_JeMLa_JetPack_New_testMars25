package com.example.abdelwahabjemlajetpack

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import b_Edite_Base_Donne.ArticleDao
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    coroutineScope: CoroutineScope,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var dialogOpen by remember { mutableStateOf(false) }

    androidx.compose.material3.TopAppBar(
        title = { Text("d_db_jetPack") },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.Menu, contentDescription = "App Menu")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Transfer Firebase Data") },
                    onClick = {
                        coroutineScope.launch {
                            transferFirebaseData()
                        }
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Import Firebase Data") },
                    onClick = {
                        dialogOpen = true
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Export Firebase Data") },
                    onClick = {
                        coroutineScope.launch {
                            exportToFireBase(articleDao)
                        }
                        menuExpanded = false
                    }
                )
            }
        }
    )
    Dialog(dialogOpen, coroutineScope, editeBaseDonneViewModel, articleDao) { dialogOpen = false }
}

@Composable
private fun Dialog(
    dialogOpen: Boolean,
    coroutineScope: CoroutineScope,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao,
    onDismiss: () -> Unit
) {
    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = "Import Firebase Data")
            },
            text = {
                Text(text = "Choisissez la référence Firebase:")
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                importFromFirebase(
                                    viewModel = editeBaseDonneViewModel,
                                    refFireBase = "d_db_jetPack",
                                    articleDao = articleDao
                                )
                            }
                            onDismiss()
                        }
                    ) {
                        Text("Import d_db_jetPack", color = Color.Red)
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                importFromFirebase(
                                    refFireBase = "e_DBJetPackExport",
                                    articleDao,
                                    editeBaseDonneViewModel
                                )
                            }
                            onDismiss()
                        }
                    ) {
                        Text("Import e_DBJetPackExport", color = Color.Red)
                    }
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                importFromFirebaseToDataBaseDonne(
                                    refFireBase = "e_DBJetPackExport",
                                    editeBaseDonneViewModel
                                )
                            }
                            onDismiss()
                        }
                    ) {
                        Text("Import e_DBJetPackExport to DataBaseDonne", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() }
                ) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
