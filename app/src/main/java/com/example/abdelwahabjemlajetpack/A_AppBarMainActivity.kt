package com.example.abdelwahabjemlajetpack

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import b_Edite_Base_Donne.ArticleDao
import b_Edite_Base_Donne.EditeBaseDonneViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    coroutineScope: CoroutineScope,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var dialogOpen by remember { mutableStateOf(false) }
    var showProgressBar by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    Column {
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
        if (showProgressBar) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    Dialog(
        context,
        dialogOpen,
        coroutineScope,
        editeBaseDonneViewModel,
        articleDao,
        onDismiss = { dialogOpen = false },
        onStartImport = { showProgressBar = true },
        onProgressUpdate = { newProgress -> progress = newProgress },
        onFinishImport = { showProgressBar = false }
    )
}


@Composable
private fun Dialog(
    context: Context,
    dialogOpen: Boolean,
    coroutineScope: CoroutineScope,
    editeBaseDonneViewModel: EditeBaseDonneViewModel,
    articleDao: ArticleDao,
    onDismiss: () -> Unit,
    onStartImport: () -> Unit,
    onProgressUpdate: (Float) -> Unit,
    onFinishImport: () -> Unit
) {
    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = "Import Firebase Data",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Choisissez la référence Firebase:",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DialogButton(
                        text = "Import d_db_jetPack",
                        icon = Icons.Default.CloudDownload,
                        onClick = {
                            coroutineScope.launch {
                                importFromFirebase(
                                    viewModel = editeBaseDonneViewModel,
                                    refFireBase = "d_db_jetPack",
                                    articleDao = articleDao
                                )
                            }
                            onDismiss()
                        },
                        tint2 = Color.Black
                    )
                    DialogButton(
                        text = "Import e_DBJetPackExport",
                        icon = Icons.Default.DataUsage,
                        onClick = {
                            coroutineScope.launch {
                                importFromFirebase(
                                    refFireBase = "e_DBJetPackExport",
                                    articleDao,
                                    editeBaseDonneViewModel
                                )
                            }
                            onDismiss()
                        },
                        tint2 = Color.Black
                    )
                    DialogButton(
                        text = "Import e_DBJetPackExport",
                        icon = Icons.Default.CloudDownload,
                        onClick = {
                            coroutineScope.launch {
                                importFromFirebaseToDataBaseDonne(
                                    refFireBase = "e_DBJetPackExport",
                                    editeBaseDonneViewModel
                                )
                            }
                            onDismiss()
                        },
                        tint2 = Color.Red
                    )
                    DialogButton(
                        text = "Transfer FirebaseData ArticlesAcheteModele",
                        icon = Icons.Default.Transform,
                        tint2 = Color.Black,
                        onClick = {
                            coroutineScope.launch {
                                onStartImport()
                                importFromFirebaseToDataBaseDonne(
                                    refFireBase = "e_DBJetPackExport",
                                    editeBaseDonneViewModel
                                )
                                transferFirebaseDataArticlesAcheteModele(
                                    context,
                                    articleDao,
                                    onProgressUpdate
                                )

                                onFinishImport()
                            }
                            onDismiss()
                        }
                    )
                    if (false){
                    DialogButton(
                        text = "Export Names List to Firebase",
                        icon = Icons.Default.Upload,
                        onClick = {
                            coroutineScope.launch {
                                exportNamesListToFirebase(articleDao)
                            }
                            onDismiss()
                        },
                        tint2 = Color.Blue
                    )
                    }
                    DialogButton(
                        text = "Import Arab Names",
                        icon = Icons.Default.CloudDownload,
                        onClick = {
                            coroutineScope.launch {
                                importArabNamesToarticleDao(articleDao)
                            }
                            onDismiss()
                        },
                        tint2 = Color.Green
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel",
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cancel",
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        )
    }
}
suspend fun importArabNamesToarticleDao(articleDao: ArticleDao) {
    val refFirebase = FirebaseDatabase.getInstance().getReference("tasks").child("arab")
    val snapshot = refFirebase.get().await()
    val arabNames = snapshot.getValue(String::class.java)

    arabNames?.let { names ->
        val namesList = names.split(",").map { it.trim() }
        val articles = articleDao.getAllArticlesOrder() // This now returns articles ordered by idCategorie and classementCate

        articles.forEachIndexed { index, article ->
            if (index < namesList.size) {
                article.nomArab = namesList[index]
                articleDao.updateArticle(article)
            }
        }
    }
}
suspend fun exportNamesListToFirebase(articleDao: ArticleDao) {
    val articles = articleDao.getAllArticlesOrder()
    val namesList = articles.joinToString(",") { it.nomArticleFinale }

    val refFirebase = FirebaseDatabase.getInstance().getReference("nameslist")
    refFirebase.setValue(namesList)
}

@Composable
private fun DialogButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tint2: Color? = null
) {
    val iconTint = tint2 ?: Color.Black // Use tint2 if not null, otherwise use Color.Red as default
    val textColor = tint2 ?: Color.Black // Use tint2 if not null, otherwise use Color.Black as default

    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = iconTint
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

