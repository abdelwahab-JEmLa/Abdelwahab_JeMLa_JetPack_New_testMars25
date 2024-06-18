package com.example.abdelwahabjemlajetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme
import com.google.firebase.database.*

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
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            ExportToFirebaseScreen()
            Spacer(modifier = Modifier.height(16.dp))
            MessagesList()
            ExportToFirebaseScreen()
            WellnessScreen()
        }
    }
}
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AbdelwahabJeMLaJetPackTheme {
        WellnessScreen()
    }
}
@Composable
fun ExportToFirebaseScreen() {
    var message by remember { mutableStateOf("") }
    val database = FirebaseDatabase.getInstance("https://abdelwahab-jemla-com-default-rtdb.europe-west1.firebasedatabase.app/")
    val messagesRef = database.getReference("Message")

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Button(
            onClick = {
                val messageId = messagesRef.push().key ?: ""
                messagesRef.child(messageId).setValue(message)
                    .addOnSuccessListener { /* Handle success */ }
                    .addOnFailureListener { /* Handle failure */ }
                message = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export to Firebase")
        }
    }
}

@Composable
fun MessagesList() {
    val database = FirebaseDatabase.getInstance("https://abdelwahab-jemla-com-default-rtdb.europe-west1.firebasedatabase.app/")
    val messagesRef = database.getReference("Message")
    var messages by remember { mutableStateOf<List<String>>(emptyList()) }

    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newMessages = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                messages = newMessages
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        }
        messagesRef.addValueEventListener(listener)
        onDispose {
            messagesRef.removeEventListener(listener)
        }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(messages) { message ->
            Text(
                text = message,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}




