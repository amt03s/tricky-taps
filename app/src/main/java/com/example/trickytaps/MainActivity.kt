// MainActivity.kt
package com.example.trickytaps

import android.media.MediaPlayer
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
import com.example.trickytaps.modules.auth.MultiplayerAuthScreen
import com.example.trickytaps.modules.auth.MultiplayerUsernameScreen
import com.example.trickytaps.modules.auth.UsernameScreen
import com.example.trickytaps.modules.`landing-page`.Help
import com.example.trickytaps.modules.`landing-page`.TrickyTapsLandingPage
import com.example.trickytaps.modules.multi.MultiplayerModeSelection
import com.example.trickytaps.modules.multi.local.MultiplayerModeSelectionScreen
import com.example.trickytaps.modules.multi.local.MultiplayerScreen
import com.example.trickytaps.modules.multi.local.MultiplayerViewModel
import com.example.trickytaps.modules.multi.online.CreateOnlineGameScreen
import com.example.trickytaps.modules.multi.online.JoinOnlineGameScreen
import com.example.trickytaps.modules.multi.online.OnlineMultiplayerGameScreen
import com.example.trickytaps.modules.multi.online.OnlineOpponentMultiplayerGameScreen
import com.example.trickytaps.modules.multi.online.OnlineMultiplayerModeSelectionScreen
import com.example.trickytaps.modules.multi.online.ReadyScreen
import com.example.trickytaps.modules.single.DifficultyModeScreen
import com.example.trickytaps.modules.single.GameOverScreen
import com.example.trickytaps.modules.single.GameScreen
import com.example.trickytaps.modules.single.LeaderboardScreen
import com.example.trickytaps.modules.single.RotateToLandscapeScreen
import com.example.trickytaps.ui.theme.TrickyTapsTheme
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaPlayerManager.initialize(this) // Initialize once
            val viewModel: MultiplayerViewModel = viewModel()
            TrickyTapsTheme {
                AppNavigation(viewModel, ::setMusicVolume) // Pass function as a parameter
            }
        }
    }
    private fun startBackgroundMusic() {
        mediaPlayer = MediaPlayer.create(this, R.raw.loop).apply {
            isLooping = true // Ensure the sound loops
            start()
        }
    }
    fun setMusicVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume) // Set left and right volume
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release() // Release MediaPlayer when activity is destroyed
        mediaPlayer = null
    }
}

@Composable
fun AppNavigation(viewModel: MultiplayerViewModel, onVolumeChange: (Float) -> Unit) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "landingPage") {
        composable("landingPage") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 5
            val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
            TrickyTapsLandingPage(navController, FirebaseFirestore.getInstance(), username, score, initialTime, mode)
        }
        composable("multiplayerModeSelectionScreen") {
            MultiplayerModeSelectionScreen(navController, viewModel) // Pass ViewModel
        }
        composable("OnlineMultiplayerModeSelectionScreen") {
            OnlineMultiplayerModeSelectionScreen(navController = navController)
        }

        composable("multiplayerModeSelection") {
            MultiplayerModeSelection(navController) // Pass ViewModel
        }
        composable("multiplayerAuthScreen") { MultiplayerAuthScreen(navController) }
        composable("multiplayerUsernameScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            MultiplayerUsernameScreen(navController, userId)
        }
        composable("rotateScreen/{playerCount}") { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getString("playerCount")?.toInt() ?: 2
            RotateToLandscapeScreen(navController, playerCount)
        }
        composable("multiplayerScreen/{playerCount}") {
            MultiplayerScreen(navController, viewModel, onVolumeChange = { newVolume ->
                MediaPlayerManager.setVolume(newVolume)
            }) // Pass viewModel
        }
        composable("authScreen") { AuthScreen(navController) }
        composable("usernameScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UsernameScreen(navController, userId)
        }
//        composable("leaderboardScreen/{username}/{score}/{mode}") { backStackEntry ->
//            val username = backStackEntry.arguments?.getString("username") ?: "Player"
//            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
//            val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
//            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 5  // Default to 5
//
//            LeaderboardScreen(navController, FirebaseFirestore.getInstance(), username, score, initialTime, mode)
//        }
        composable("leaderboardScreen/{username}/{score}/{initialTime}/{mode}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 5
            val mode = backStackEntry.arguments?.getString("mode") ?: "easy"

            LeaderboardScreen(navController, FirebaseFirestore.getInstance(), username, score, initialTime, mode)
        }

//        composable("gameOverScreen/{username}/{score}") { backStackEntry ->
//            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 0
//            val username = backStackEntry.arguments?.getString("username") ?: "Player"
//            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
//            val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
//            GameOverScreen(navController, username, score, FirebaseFirestore.getInstance(), initialTime, mode)
//        }
        composable("gameOverScreen/{username}/{score}/{initialTime}/{mode}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Player"
            val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 0
            val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
            GameOverScreen(navController, username, score, FirebaseFirestore.getInstance(), initialTime, mode)
        }

        composable("help") {
            Help(navController)
        }
        composable("modeScreen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            DifficultyModeScreen(navController = navController, username)
        }
        composable("gameScreen/{initialTime}/{username}/{mode}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val initialTime = backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 0
            val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
            GameScreen(
                navController = navController,
                initialTime = initialTime,
                username = username,
                db = FirebaseFirestore.getInstance(),
                onVolumeChange = { newVolume ->
                    MediaPlayerManager.setVolume(newVolume)
                },
                mode = mode
            )
        }

        composable("localMultiplayerModeSelection") {
            // Existing local multiplayer screen
            MultiplayerModeSelectionScreen(navController, viewModel)
        }

        composable("onlineMultiplayerModeSelection") {
            // New online multiplayer screen (to be implemented)
            OnlineMultiplayerModeSelectionScreen(navController)
        }
        composable("createOnlineGame/{playerName}") { backStackEntry ->
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            CreateOnlineGameScreen(navController = navController, playerName = playerName)
        }

        composable("joinOnlineGame/{playerName}") { backStackEntry ->
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            JoinOnlineGameScreen(navController, playerName)
        }
        composable("onlineMultiplayerGame/{gameId}/{playerName}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            OnlineMultiplayerGameScreen(navController = navController, gameId = gameId, playerName = playerName)
        }
        composable("readyScreen/{gameId}/{playerName}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            ReadyScreen(navController, gameId, playerName)
        }
        composable("onlineOpponentMultiplayerGame/{gameId}/{playerName}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val playerName = backStackEntry.arguments?.getString("playerName") ?: ""
            OnlineOpponentMultiplayerGameScreen(navController = navController, gameId = gameId, playerName = playerName)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    //TrickyTapsLandingPage(navController)
}