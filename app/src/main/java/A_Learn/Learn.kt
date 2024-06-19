package A_Learn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

//@Composable
//fun WellnessScreen(modifier: Modifier = Modifier) {
//    WaterCounter(modifier)
//}
//
//@Composable
//fun WaterCounter(modifier: Modifier = Modifier) {
//    Column(modifier = modifier.padding(16.dp)) {
//        var count by remember { mutableStateOf(0) }
//
//        if (count > 0) {
//            // This text is present if the button has been clicked
//            // at least once; absent otherwise
//            Text("You've had $count glasses.")
//        }
//        Button(onClick = { count++ }, Modifier.padding(top = 8.dp)) {
//            Text("Add one")
//        }
//    }
//}
//@Composable
//fun ScreenContent(modifier: Modifier = Modifier) {
//

//    Text(
//            modifier = Modifier
//                .padding(16.dp)
//                .background(color = Color.Red, shape = RoundedCornerShape(15.dp))
//                .border(width = 2.dp, color = Color.Blue, shape = RoundedCornerShape(15.dp))
//                .padding(16.dp)
//                .width(300.dp)
//                .height(200.dp)
//                .background(color = Color.Red)
//                .
//                ,
//            text = "Text 1"
//        )

//    Button(
//        onClick = {
//            Log.d("MyButton", "Clicked")
//        },
//        colors = ButtonDefaults.buttonColors(
//            containerColor = Color.White,
//            contentColor = Color.Red,
//            disabledContainerColor = Color.Gray,
//            disabledContentColor = Color.Black
//        ),
//        enabled = true,
//        shape = RoundedCornerShape(4.dp),
//        modifier = modifier.width(300.dp)
//    ) {
//        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
//        Text(text = "Button")
//    }
//
//

//
//    Row(
//        modifier = Modifier.fillMaxSize(),
//
//        ) {
//        Text(
//            modifier = Modifier
//                .background(color = Color.Red)
//                .weight(0.2f),
//            text = "Text 1"
//        )
//        Text(
//            modifier = Modifier
//                .background(color = Color.Blue)
//                .weight(0.5f),
//            text = "Text 2"
//        )
//        Text(
//            modifier = Modifier
//                .background(color = Color.Green)
//                .weight(0.3f),
//            text = "Text 3"
//        )

//    }


//}
//@Composable
//fun MyMultiline(myText: String, modifier: Modifier = Modifier, fontSize: Int = 30, fontWeight: FontWeight = FontWeight.Bold) {
//    Text(
//        text = myText,
//        modifier = modifier.width(300.dp),
//        fontSize = fontSize.sp,
//        fontWeight = fontWeight,
//        fontFamily = FontFamily.Cursive,
//        maxLines = 2,
//        textAlign = TextAlign.Center
//    )
//}


@Composable
fun ExportToFirebaseScreen() {
    var message by remember { mutableStateOf("") }
    val database = FirebaseDatabase.getInstance("https://abdelwahab-jemla-com-default-rtdb.europe-west1.firebasedatabase.app/")
    val messagesRef = database.getReference("Message")

    Column(modifier = Modifier.padding(20.dp)) {
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message23") },
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
