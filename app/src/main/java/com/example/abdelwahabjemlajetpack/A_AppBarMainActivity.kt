package com.example.abdelwahabjemlajetpack

import a_RoomDB.BaseDonne
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Expand
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Try
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
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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

    var fenetre_baseDonnePourBakupSiNaissaire by remember { mutableStateOf(false) }
    var windosBakupsManager by remember { mutableStateOf(false) }

    var windosDaoToBakup by remember { mutableStateOf(false) }
    var windosBakupToDao by remember { mutableStateOf(false) }

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
                        text = { Text("Export Firebase Data") },
                        onClick = {
                            coroutineScope.launch {
                                exportToFireBase(articleDao, "e_DBJetPackExport")
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
                            text = { Text("Export Manager") },
                    onClick = {
                        windosBakupsManager = true
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
    if (windosBakupsManager) {
        AlertDialog(
            onDismissRequest = { windosBakupsManager=false },
            title = {
                Text(
                    text = "Firebase Data",
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
                        text = "Update.Bakup From Dao.baseDonne >To> BaseDonne_Bakup?? ",
                        icon = Icons.Default.Expand,
                        onClick = {windosDaoToBakup=true
                            windosBakupsManager = false
                        },
                        tint2 = Color.Red
                    )
                    DialogButton(
                        text = "Bakup From BaseDonne_Bakup?? >To> Dao.baseDonne ",
                        icon = Icons.Default.Expand,
                        onClick = {windosBakupToDao=true
                            windosBakupsManager = false
                        },
                        tint2 = Color.Blue
                    )
                }
            },
        )

    }




    WindosDaoToBakup(
        windosDaoToBakup,
        coroutineScope,
        articleDao,
        onDismiss = { windosDaoToBakup = false }
    )

    WindosBakupToDao(
        windosBakupToDao,
        coroutineScope,
        articleDao,
        onDismiss = { windosBakupToDao = false },
        onStartImport = { showProgressBar = true },
        onProgressUpdate = { newProgress -> progress = newProgress },
        onFinishImport = { showProgressBar = false } ,
        editeBaseDonneViewModel
    )

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
private fun WindosDaoToBakup(
    dialogOpen: Boolean,
    coroutineScope: CoroutineScope,
    articleDao: ArticleDao,
    onDismiss: () -> Unit,
) {

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = "Firebase Data",
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
                        text = "Dao>Bakup1",
                        icon = Icons.Default.Expand,
                        onClick = {
                            coroutineScope.launch {
                                exportToFireBase(articleDao,refFireBase="BaseDonne_Bakup1")
                            }
                            onDismiss()
                        },
                        tint2 = Color.Red
                    )
                    DialogButton(
                        text = "Dao>Bakup2",
                        icon = Icons.Default.Expand,
                        onClick = {
                            coroutineScope.launch {
                                exportToFireBase(articleDao,refFireBase="BaseDonne_Bakup2")
                            }
                            onDismiss()
                        },
                        tint2 = Color.Red
                    )


                }
            },
        )
    }

}
@Composable
private fun WindosBakupToDao(
    dialogOpen: Boolean,
    coroutineScope: CoroutineScope,
    articleDao: ArticleDao,
    onDismiss: () -> Unit,
    onStartImport: () -> Unit,
    onProgressUpdate: (Float) -> Unit,
    onFinishImport: () -> Unit,
    editeBaseDonneViewModel: EditeBaseDonneViewModel
) {


    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    text = "Firebase Data",
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
                        text = "Bakup1 >Au> Dao",
                        icon = Icons.Default.Try,
                        onClick = {
                            coroutineScope.launch {
                                onStartImport()
                                // Passer onProgressUpdate ici
                                TrensfertDeBaseDonnePourBakuAuRefe_DBJetPackExport(articleDao,refFireBase="BaseDonne_Bakup1") { newProgress ->
                                    onProgressUpdate(newProgress)
                                }
                                editeBaseDonneViewModel.initBaseDonneStatTabel()
                                editeBaseDonneViewModel.initDataBaseDonneForNewByStatInCompos()

                                onFinishImport()
                            }
                            onDismiss()
                        },
                        tint2 = Color.Blue
                    )
                    DialogButton(
                        text = "Bakup2 >Au> Dao",
                        icon = Icons.Default.Try,
                        onClick = {
                            coroutineScope.launch {
                                onStartImport()
                                // Passer onProgressUpdate ici
                                TrensfertDeBaseDonnePourBakuAuRefe_DBJetPackExport(articleDao,refFireBase="BaseDonne_Bakup2") { newProgress ->
                                    onProgressUpdate(newProgress)
                                }
                                editeBaseDonneViewModel.initBaseDonneStatTabel()
                                editeBaseDonneViewModel.initDataBaseDonneForNewByStatInCompos()

                                onFinishImport()
                            }
                            onDismiss()
                        },
                        tint2 = Color.Blue
                    )

                }
            }
            ,
        )
    }

}
private fun TrensfertDeBaseDonnePourBakuAuRefe_DBJetPackExport(
    articleDao: ArticleDao,
    refFireBase: String,
    onProgressUpdate: (Float) -> Unit
) {
    val firebase = Firebase.database
    val dbJetPackExportRefSource = firebase.getReference(refFireBase)
    val dbJetPackExportRefDest = firebase.getReference("e_DBJetPackExport")

    dbJetPackExportRefSource.get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            val totalItems = snapshot.childrenCount
            var processedItems = 0L

            dbJetPackExportRefDest.setValue(snapshot.value).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    CoroutineScope(Dispatchers.IO).launch {
                        articleDao.deleteAll()

                        snapshot.children.forEach { child ->
                            val article = child.getValue(BaseDonne::class.java)
                            if (article != null) {
                                articleDao.insert(article)
                            }
                            processedItems++
                            val progress = processedItems.toFloat() / totalItems
                            withContext(Dispatchers.Main) {
                                onProgressUpdate(progress)
                            }
                        }
                        Log.d("Transfert", "Les données ont été mises à jour dans articleDao!")
                    }
                } else {
                    Log.e("Transfert", "Erreur lors du transfert: ${task.exception?.message}")
                }
            }
        } else {
            Log.e("Transfert", "La source de données est vide ou inexistante.")
        }
    }.addOnFailureListener { exception ->
        Log.e("Transfert", "Erreur lors de la lecture des données: ${exception.message}")
    }
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
    var showNameListDialog by remember { mutableStateOf(false) }

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
                        text = "Import From [d_db_jetPack] >to> Dao.BaseDonne",
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
                        text = "Import Depuit REF: [e_DBJetPackExport] Au Dao.BaseDonne ",
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
                    DialogButton(
                        text = "Name List Operations",
                        icon = Icons.Default.List,
                        onClick = { showNameListDialog = true },
                        tint2 = Color.Blue
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

    if (showNameListDialog) {
        NameListDialog(
            coroutineScope = coroutineScope,
            articleDao = articleDao,
            onDismiss = { showNameListDialog = false }
        )
    }
}

@Composable
private fun NameListDialog(
    coroutineScope: CoroutineScope,
    articleDao: ArticleDao,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Name List Operations",
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
private suspend fun importArabNamesToarticleDao(articleDao: ArticleDao) {
    val refFirebase = FirebaseDatabase.getInstance().getReference("tasks").child("arab")
    val snapshot = refFirebase.get().await()
    val arabNames = snapshot.getValue(String::class.java)

    arabNames?.let { names ->
        val namesList = names.split(",").map { it.trim() }
        val articles = articleDao.getAllArticlesOrder()

        articles.forEachIndexed { index, article ->
            if (index < namesList.size) {
                article.nomArab = namesList[index]
                articleDao.updateArticle(article)
            }
        }
    }
}

private suspend fun exportNamesListToFirebase(articleDao: ArticleDao) {
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
    val iconTint = tint2 ?: Color.Black
    val textColor = tint2 ?: Color.Black

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

