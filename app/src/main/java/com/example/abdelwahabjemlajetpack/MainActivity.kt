package com.example.abdelwahabjemlajetpack

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbdelwahabJeMLaJetPackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyMultiline(
                        myText = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    ScreenContent()
                }
            }
        }
    }
}

@Composable
fun ScreenContent(modifier: Modifier = Modifier) {


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


}
@Composable
fun MyMultiline(myText: String, modifier: Modifier = Modifier, fontSize: Int = 30, fontWeight: FontWeight = FontWeight.Bold) {
    Text(
        text = myText,
        modifier = modifier.width(300.dp),
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        fontFamily = FontFamily.Cursive,
        maxLines = 2,
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AbdelwahabJeMLaJetPackTheme {
    //    MyMultiline("Android ")
        ScreenContent()
    }
}
