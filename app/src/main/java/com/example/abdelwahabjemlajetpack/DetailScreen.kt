package A_Learn.LazyC

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.abdelwahabjemlajetpack.ui.theme.AbdelwahabJeMLaJetPackTheme

@Composable
fun DetailScreen(index: Int) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Item index: $index", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDetailScreen() {
    AbdelwahabJeMLaJetPackTheme {
        DetailScreen(index = 0)
    }
}
