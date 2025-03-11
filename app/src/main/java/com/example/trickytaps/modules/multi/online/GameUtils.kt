// GameUtils.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Modified GameOverScreen to accept viewModel as a parameter
@Composable
fun GameOverScreen(navController: NavController, gameState: GameState, viewModel: OnlineMultiplayerViewModel) {
    var winner by remember { mutableStateOf("No Winner") }

    // Fetch winner asynchronously
    LaunchedEffect(gameState) {
        getWinner(gameState, viewModel) { determinedWinner ->
            winner = determinedWinner
        }
    }

    // Display the Game Over screen
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Over",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Red
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Winner: $winner",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                // Navigate back to the OnlineMultiplayerModeSelectionScreen instead of just popping the back stack
                navController.navigate("OnlineMultiplayerModeSelectionScreen") {
                    // Pop the back stack to remove all previous destinations, ensuring that the user cannot return to the game screen
                    popUpTo("OnlineMultiplayerModeSelectionScreen") { inclusive = true }
                }
                // Call this after the user exits
                viewModel.deleteGameSession(gameState.gameId)
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = "Exit Game")
        }
    }
}

// Helper function to determine the winner
fun getWinner(gameState: GameState, viewModel: OnlineMultiplayerViewModel, onWinnerDetermined: (String) -> Unit) {
    val playerNames = gameState.players.keys.toList()
    val playerName1 = playerNames[0]
    val playerName2 = playerNames[1]

    var player1Score = 0
    var player2Score = 0

    // Fetch player 1 score
    viewModel.getPlayerScore(gameState.gameId, playerName1) { score1 ->
        player1Score = score1

        // Fetch player 2 score
        viewModel.getPlayerScore(gameState.gameId, playerName2) { score2 ->
            player2Score = score2

            // Determine the winner based on the scores
            val winner = if (player1Score > player2Score) playerName1 else playerName2
            onWinnerDetermined(winner) // Trigger the callback with the winner's name
        }
    }
}
