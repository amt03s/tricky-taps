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
import kotlinx.coroutines.*

@Composable
fun GameOverScreen(navController: NavController, gameState: GameState, viewModel: OnlineMultiplayerViewModel) {
    var winner by remember { mutableStateOf("No Winner") }
    var deleteCompleted by remember { mutableStateOf(false) }

    // ✅ Fetch winner asynchronously using coroutine
    LaunchedEffect(gameState) {
        winner = try {
            getWinner(gameState, viewModel) // Use the suspend function
        } catch (e: Exception) {
            Log.e("GameOverScreen", "Failed to get winner: ${e.message}")
            "Error"
        }
    }

    // ✅ Handle navigation after session deletion
    LaunchedEffect(deleteCompleted) {
        if (deleteCompleted) {
            try {
                navController.navigate("onlineMultiplayerModeSelection") {
                    popUpTo("onlineMultiplayerModeSelection") { inclusive = false }
                }
            } catch (e: Exception) {
                Log.e("GameOverScreen", "Navigation failed: ${e.message}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        viewModel.deleteGameSession(gameState.gameId) // Delete game session
                        Log.d("GameOverScreen", "Game session deleted for ID: ${gameState.gameId}")
                        deleteCompleted = true
                    } catch (e: Exception) {
                        Log.e("GameOverScreen", "Failed to delete session: ${e.message}")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = "Exit Game")
        }
    }
}

// ✅ Suspend function to determine the winner using coroutines
suspend fun getWinner(gameState: GameState, viewModel: OnlineMultiplayerViewModel): String {
    return withContext(Dispatchers.IO) {
        try {
            val playerNames = gameState.players.keys.toList()
            val playerName1 = playerNames[0]
            val playerName2 = playerNames[1]

            // Use the suspend version of getPlayerScore()
            val player1Score = viewModel.getPlayerScoreSuspend(gameState.gameId, playerName1)
            val player2Score = viewModel.getPlayerScoreSuspend(gameState.gameId, playerName2)

            if (player1Score > player2Score) playerName1 else playerName2
        } catch (e: Exception) {
            Log.e("GameOverScreen", "Failed to determine winner: ${e.message}")
            "Unknown"
        }
    }
}