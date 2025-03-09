// CreateOnlineGameScreen.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
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

    var gameId by remember { mutableStateOf<String?>(null) }
    var secondPlayerName by remember { mutableStateOf<String?>(null) }
    var isSecondPlayerJoined by remember { mutableStateOf(false) }
    var isPlayerReady by remember { mutableStateOf(false) }
    var isGameReady by remember { mutableStateOf(false) }

    // Create a new game session
    LaunchedEffect(Unit) {
        try {
            gameId = viewModel.createGame(playerName) // Ensure this creates the game properly (Player 1)
            if (gameId != null && gameId!!.isNotEmpty()) {
                // Listen for updates from Firestore when the second player joins
                viewModel.listenForGameUpdates(gameId!!) { secondPlayerNameFromServer ->
                    secondPlayerName = secondPlayerNameFromServer
                    isSecondPlayerJoined = true
                }
            } else {
                Toast.makeText(context, "Error creating game: Invalid gameId", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CreateOnlineGameScreen", "Error creating game: ${e.message}")
            Toast.makeText(context, "Error creating game: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Listen for both players to be ready
    LaunchedEffect(isPlayerReady, isSecondPlayerJoined) {
        if (isPlayerReady && isSecondPlayerJoined) {
            isGameReady = true
            // Both players are ready, navigate to the game screen
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
            // Show loading indicator while game is being created
            CircularProgressIndicator()
            Text(text = "Creating Game...", fontSize = 24.sp)
        } else {
            // Show the game ID after the game is created
            Text(text = "Game ID: $gameId", fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(20.dp))

            // Show waiting message until second player joins
            Text(text = "Waiting for player to join...", fontSize = 20.sp)

            Spacer(modifier = Modifier.height(20.dp))

            // Show the second player's name if they joined
            if (secondPlayerName != null) {
                Text(text = "Second player: $secondPlayerName", fontSize = 20.sp)
            }

            // If second player joined, show the "Ready" button
            if (isSecondPlayerJoined) {
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

            // If both players are ready, navigate to the game screen
            if (isGameReady) {
                Text(text = "Both players are ready. Starting the game...", fontSize = 24.sp)
            }
        }
    }
}