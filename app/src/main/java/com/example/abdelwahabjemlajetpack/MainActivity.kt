package com.example.abdelwahabjemlajetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                        MyText = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MyMultiline(MyText: String, modifier: Modifier = Modifier, fontSize: Int = 30, fontWeight: FontWeight = FontWeight.Bold) {
    Text(
        text = MyText,
        modifier = Modifier.width(300.dp),
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
        MyMultiline("Android ")
    }
}
