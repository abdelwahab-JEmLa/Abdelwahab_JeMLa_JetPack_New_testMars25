package com.example.abdelwahabjemlajetpack

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbdelwahabJeMLaJetPackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold(
        topBar = { WellnessTopAppBar() }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            Column {
                Spacer(modifier = Modifier.height(20.dp))
                // Assume WellnessScreen is defined elsewhere
                // WellnessScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WellnessTopAppBar( ) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Wellness App") },
        actions = {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.Menu, contentDescription = "Import")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Import from Firebase") },
                    onClick = {
                        WellnessViewModel().importFromFirebase()
                        menuExpanded = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Sync Initial Tasks with Firebase") },
                    onClick = {
                        WellnessViewModel().syncInitialTasksWithFirebase(WellnessViewModel().tasks)
                        menuExpanded = false
                    }
                )
            }
        }
    )
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true, widthDp = 350, heightDp = 600)
@Composable
fun WellnessTopAppBarPreview() {
    AbdelwahabJeMLaJetPackTheme {
        Scaffold(
            topBar = { WellnessTopAppBar() }
        ) {
            // Provide some content here, such as a placeholder or an actual component
            Text(text = "Placeholder Content")
        }
    }
}

