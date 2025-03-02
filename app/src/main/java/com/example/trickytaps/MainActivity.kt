// MainActivity.kt
package com.example.trickytaps

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.trickytaps.modules.auth.AuthScreen
import com.example.trickytaps.modules.auth.Help
import com.example.trickytaps.modules.auth.TrickyTapsLandingPage
import com.example.trickytaps.modules.auth.UsernameScreen
import com.example.trickytaps.modules.multi.MultiplayerModeSelectionScreen
import com.example.trickytaps.modules.multi.MultiplayerScreen
import com.example.trickytaps.modules.multi.MultiplayerViewModel
import com.example.trickytaps.modules.single.DifficultyModeScreen
import com.example.trickytaps.modules.single.GameOverScreen
import com.example.trickytaps.modules.single.GameScreen
import com.example.trickytaps.modules.single.LeaderboardScreen
import com.example.trickytaps.modules.single.RotateToLandscapeScreen
import com.example.trickytaps.ui.theme.TrickyTapsTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: MultiplayerViewModel = viewModel()
            TrickyTapsTheme {
                AppNavigation(viewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: MultiplayerViewModel) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "landingPage") {
        composable("landingPage") {
            TrickyTapsLandingPage(navController)
        }
        composable("multiplayerModeSelection") {
            MultiplayerModeSelectionScreen(navController, viewModel) // Pass ViewModel
        }
        composable("rotateScreen/{playerCount}") { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getString("playerCount")?.toInt() ?: 2
            RotateToLandscapeScreen(navController, playerCount)
        }
        composable("multiplayerScreen/{playerCount}") {
            MultiplayerScreen(navController, viewModel) // Pass viewModel
        }
        composable("authScreen") { AuthScreen(navController) }
        composable("usernameScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UsernameScreen(navController, userId)
        }
        composable("leaderboardScreen/{username}/{score}/{mode}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Player"
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
            LeaderboardScreen(navController, FirebaseFirestore.getInstance(), username, score, mode)
        }
        composable("gameOverScreen/{username}/{score}") { backStackEntry ->
            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 0
            val username = backStackEntry.arguments?.getString("username") ?: "Player"
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            GameOverScreen(navController, username, score, FirebaseFirestore.getInstance(), initialTime)
        }
        composable("help"){
            Help(navController)
        }
        composable("modeScreen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            DifficultyModeScreen(navController = navController, username)
        }

        composable("gameScreen/{initialTime}/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 0
            GameScreen(navController = navController, initialTime = initialTime, username = username, db = FirebaseFirestore.getInstance(), mode = backStackEntry.arguments?.getString("mode") ?: "easy")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    TrickyTapsLandingPage(navController)
}