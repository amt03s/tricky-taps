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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trickytaps.generateTrickQuestion

// OnlineOpponentMultiplayerGameScreen.kt
@Composable
fun OnlineOpponentMultiplayerGameScreen(
    navController: NavController,
    gameId: String,
    playerName: String
) {
    val viewModel: OnlineMultiplayerViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()
    val playerScore by viewModel.playerScore.collectAsState()
    val timer by viewModel.timer.collectAsState() // Observe the timer
    val gameOver by viewModel.gameOver.collectAsState() // Observe game over state

    // Listen for game updates
    LaunchedEffect(gameId) {
        viewModel.listenForGameUpdates(gameId) { secondPlayerName ->
            Log.d("Game", "Second player joined: $secondPlayerName")
        }
    }

    // Fetch the player's score when the game starts
    LaunchedEffect(gameId, playerName) {
        viewModel.getPlayerScore(gameId, playerName) { score ->
            viewModel.updateScore(gameId, playerName, score)  // Update the score in the ViewModel
        }

        // Start the 45-second game timer
        viewModel.startGameTimer()
    }

    // If the game is over, show the Game Over screen
    if (gameOver) {
        // Pass the viewModel to GameOverScreen
        GameOverScreen(navController, gameState!!, viewModel)
    } else {
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
                viewModel.updatePlayerReadyStatus(gameId, playerName, true)
                viewModel.updatePlayerReadyStatus(gameId, gameState!!.players.keys.first { it != playerName }, true)
                viewModel.updateGameStatus(gameId, "ready")
            }

            if (gameState!!.status == "ready") {
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

                    Text(
                        text = "Player: $playerName",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Score: $playerScore",
                        fontSize = 20.sp
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    // Timer
                    Text(
                        text = "Time Remaining: $timer seconds",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    Text(
                        text = currentQuestion?.question ?: "No question available",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    currentQuestion?.options?.let { options ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            options.forEach { option ->
                                Button(
                                    onClick = {
                                        if (option == currentQuestion.correctAnswer) {
                                            viewModel.updateScore(gameId, playerName, 10)
                                        }

                                        val newQuestion = generateTrickQuestion()
                                        viewModel.updateQuestion(gameId)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth(0.8f)
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(text = option)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text(text = "Exit Game")
                    }
                }
            } else {
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
        }
    }
}








