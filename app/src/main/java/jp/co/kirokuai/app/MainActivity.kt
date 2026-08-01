package jp.co.kirokuai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.co.kirokuai.app.ui.theme.KirokuAITheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            KirokuAITheme {
                Scaffold { innerPadding ->
                    HomeScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Kiroku AI",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "AI会議アシスタント",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {}
        ) {
            Text("🎤 新しい会議")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {}
        ) {
            Text("📁 会議履歴")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {}
        ) {
            Text("⚙️ 設定")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Version 0.1.0",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    KirokuAITheme {
        HomeScreen()
    }
}