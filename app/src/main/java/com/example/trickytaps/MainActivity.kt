// MainActivity.kt
package com.example.trickytaps

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.trickytaps.modules.auth.AuthScreen
import com.example.trickytaps.modules.auth.UsernameScreen
import com.example.trickytaps.modules.`landing-page`.Help
import com.example.trickytaps.modules.`landing-page`.TrickyTapsLandingPage
import com.example.trickytaps.modules.landingpage.LeaderBoardsLandingPage
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
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(3) { index ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.bg)
                        .decoderFactory(ImageDecoderDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f) // Makes each GIF take equal height within the available space
                        .fillMaxWidth() // Stretches to fill the width of the screen
                        .graphicsLayer {
                            alpha = 0.5f // Adjust opacity if needed
                            if (index == 1) scaleX = -1f // Flip the second GIF horizontally
                        }
                )
            }
        }

        NavHost(navController, startDestination = "landingPage") {
            composable("landingPage") { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                val initialTime =
                    backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 5
                val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
                TrickyTapsLandingPage(
                    navController,
                    FirebaseFirestore.getInstance(),
                    username,
                    score,
                    initialTime,
                    mode
                )
            }
            composable("multiplayerModeSelection") {
                MultiplayerModeSelectionScreen(navController, viewModel) // Pass ViewModel
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
            composable("leaderboardScreen/{username}/{score}/{initialTime}/{mode}") { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                val initialTime =
                    backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 5
                val mode = backStackEntry.arguments?.getString("mode") ?: "easy"

                LeaderboardScreen(
                    navController,
                    FirebaseFirestore.getInstance(),
                    username,
                    score,
                    initialTime,
                    mode
                )
            }

            composable("leaderboardScreen") { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                val initialTime =
                    backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 5
                val mode = backStackEntry.arguments?.getString("mode") ?: "easy"

                LeaderBoardsLandingPage(
                    navController,
                    FirebaseFirestore.getInstance(),
                    username,
                    score,
                    initialTime,
                    mode
                )
            }

            composable("gameOverScreen/{username}/{score}/{initialTime}/{mode}") { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: "Player"
                val score = backStackEntry.arguments?.getString("score")?.toIntOrNull() ?: 0
                val initialTime =
                    backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 0
                val mode = backStackEntry.arguments?.getString("mode") ?: "easy"
                GameOverScreen(
                    navController,
                    username,
                    score,
                    FirebaseFirestore.getInstance(),
                    initialTime,
                    mode
                )
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
                val initialTime =
                    backStackEntry.arguments?.getString("initialTime")?.toIntOrNull() ?: 0
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
        }
    }
}