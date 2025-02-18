package com.example.trickytaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    val navController = rememberNavController()

    NavHost(navController, startDestination = "landingPage") {
        composable("landingPage") {
            TrickyTapsLandingPage(
                onSinglePlayer = { navController.navigate("gameScreen") },
                onMultiPlayer = { navController.navigate("multiplayerScreen") }
            )
        }
        composable("gameScreen") { GameScreen() }
        composable("multiplayerScreen") { MultiplayerScreen() }
    }
}


@Preview (showBackground = true)
@Composable
fun MainScreenPreview(){
    MainScreen()
}