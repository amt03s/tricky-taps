// OnlineOpponentMultiplayerGameScreen
package com.example.trickytaps.modules.multi.online

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OnlineOpponentMultiplayerGameScreen(
    navController: NavController,
    gameId: String,
    playerName: String
) {
    val viewModel: OnlineMultiplayerViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()

    // Listen for game updates
    LaunchedEffect(gameId) {
        viewModel.listenForGameUpdates(gameId) { secondPlayerName ->
            // This callback will be triggered when the second player joins
            Log.d("Game", "Second player joined: $secondPlayerName")
            // You can handle any updates you want to make when the second player joins here
        }
    }

    // Show loading screen while waiting for game state
    if (gameState == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Text(text = "Waiting for game to start...", fontSize = 20.sp)
        }
    } else {
        // Extract data from game state
        val players = gameState!!.players
        val status = gameState!!.status
        val currentQuestion = gameState!!.currentQuestion // Use the question from game state

        // Check if both players are ready
        val bothPlayersReady = players.values.all { it.isReady }

        if (bothPlayersReady && status != "ready") {
            // If both players are ready, update the status and navigate to the game screen
            viewModel.updatePlayerReadyStatus(gameId, playerName, true)
            viewModel.updatePlayerReadyStatus(gameId, gameState!!.players.keys.first { it != playerName }, true) // Update second player

            // Update the game status to "ready"
            viewModel.updateGameStatus(gameId, "ready")
        }

        // Proceed to the game screen if both players are ready
        if (gameState!!.status == "ready") {
            navController.navigate("onlineMultiplayerGame/${gameState!!.gameId}/${playerName}")
        } else {
            // Display the waiting screen
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Waiting for the other player to be ready",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Display the game content
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Game Status: $status",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Show the player's name and score
            Text(
                text = "Player: $playerName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Score: ${players[playerName]?.score ?: 0}",
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Display the current question
            Text(
                text = currentQuestion?.question ?: "No question available",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Display the answer options
            currentQuestion?.options?.let { options ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    options.forEach { option ->
                        AnswerButton(
                            option = option,
                            correctAnswer = currentQuestion.correctAnswer,
                            gameId = gameId,
                            playerName = playerName,
                            viewModel = viewModel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Button to leave the game or go back
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "Exit Game")
            }
        }
    }
}


