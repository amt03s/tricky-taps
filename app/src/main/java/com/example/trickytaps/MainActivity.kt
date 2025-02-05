package com.example.trickytaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.trickytaps.ui.theme.TrickyTapsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrickyTapsTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    TrickyTapsLandingPage(
        onSinglePlayer = { /* Navigate to Single Player Screen */ },
        onMultiPlayer = { /* Navigate to Multiplayer Screen */ }
    )
}

@Preview (showBackground = true)
@Composable
fun MainScreenPreview(){
    MainScreen()
}