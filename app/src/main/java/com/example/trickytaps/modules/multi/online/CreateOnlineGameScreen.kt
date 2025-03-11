// CreateOnlineGameScreen.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateOnlineGameScreen(navController: NavController, playerName: String) {
    val context = LocalContext.current
    val viewModel: OnlineMultiplayerViewModel = viewModel()

    // Observe the gameState
    val gameState by viewModel.gameState.collectAsState()

    var gameId by rememberSaveable { mutableStateOf<String?>(null) }
    var secondPlayerName by remember { mutableStateOf<String?>(null) }
    var isSecondPlayerJoined by remember { mutableStateOf(false) }
    var isPlayerReady by remember { mutableStateOf(false) }
    var isGameReady by remember { mutableStateOf(false) }

    // Flag to ensure the game is only created once
    var gameCreated by remember { mutableStateOf(false) }

    // Only create a game once and listen for updates
    LaunchedEffect(gameCreated) {
        if (!gameCreated) {
            try {
                // Game creation is now handled by ViewModel
                if (gameId == null) {
                    gameId = viewModel.createGame(playerName) // ViewModel handles game creation
                    Log.d("CreateOnlineGameScreen", "Game session created with ID: $gameId")

                    if (gameId != null && gameId!!.isNotEmpty()) {
                        // Listen for updates from Firestore when the second player joins
                        viewModel.listenForGameUpdates(gameId!!) { secondPlayerNameFromServer ->
                            secondPlayerName = secondPlayerNameFromServer
                            isSecondPlayerJoined = true
                        }

                        // Set gameCreated to true to prevent re-creating the game
                        gameCreated = true
                    } else {
                        Toast.makeText(context, "Error creating game: Invalid gameId", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("CreateOnlineGameScreen", "Error creating game: ${e.message}")
                Toast.makeText(context, "Error creating game: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Listen for both players to be ready and navigate to the game screen
    LaunchedEffect(gameState, isPlayerReady, isSecondPlayerJoined) {
        // Check if both players are ready and game state is ready
        if (gameState?.status == "ready" && isPlayerReady && isSecondPlayerJoined) {
            navController.navigate("onlineMultiplayerGame/$gameId/$playerName")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (gameId == null) {
            CircularProgressIndicator()
            Text(text = "Creating Game...", fontSize = 24.sp)
        } else {
            Text(text = "Game ID: $gameId", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Waiting for player to join...", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(20.dp))

            if (secondPlayerName != null) {
                Text(text = "Second player: $secondPlayerName", fontSize = 20.sp)
            }

            // Only show "Ready" button if player is not ready yet
            if (isSecondPlayerJoined && !isPlayerReady) {
                Button(
                    onClick = {
                        isPlayerReady = true
                        viewModel.updatePlayerReadyStatus(gameId!!, playerName, true)
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(text = "Ready")
                }
            }

            // Indicate that the game is ready
            if (isGameReady) {
                Text(text = "Both players are ready. Starting the game...", fontSize = 24.sp)
            }
        }
    }
}






