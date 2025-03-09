package com.example.trickytaps.modules.multi.online

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.trickytaps.TrickQuestion
import com.example.trickytaps.generateTrickQuestion
import com.example.trickytaps.ui.theme.TrickyTapsTheme

@Composable
fun OnlineMultiplayerGameScreen(
    navController: NavController,
    gameId: String,
    playerName: String
) {
    val viewModel: OnlineMultiplayerViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()

    // Handle the game's state and listen for updates
    LaunchedEffect(gameId) {
        // Listen for game updates and handle when the second player joins
        viewModel.listenForGameUpdates(gameId) { secondPlayerName ->
            // If second player name is updated, show it
            if (secondPlayerName != null) {
                // Update the game state with the second player
                // You can either show a message, update a UI state, etc.
                // Example: You might want to update a UI element to reflect second player joining
                Log.d("OnlineMultiplayerGameScreen", "Second player joined: $secondPlayerName")
            }
        }
    }

    // If the game state is null, it means we're waiting for data, show a loading screen
    if (gameState == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Waiting for the game to start...", fontSize = 20.sp)
        }
    } else {
        // Extract data from game state
        val players = gameState!!.players
        val status = gameState!!.status
        val currentQuestion = gameState!!.currentQuestion // Use the question from game state

        // Display game screen
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Show the game status
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

@Composable
fun AnswerButton(
    option: String,
    correctAnswer: String,
    gameId: String,
    playerName: String,
    viewModel: OnlineMultiplayerViewModel
) {
    Button(
        onClick = {
            // Check if the answer is correct or incorrect
            if (option == correctAnswer) {
                viewModel.updateScore(gameId, playerName, 10) // Add score for correct answer
            } else {
                viewModel.updateScore(gameId, playerName, -5) // Deduct score for incorrect answer
            }

            // Generate the next question from TrickQuestion (dynamically)
            val newQuestion = generateTrickQuestion() // Get a new random question

            // Update the question in the game (without storing in Firestore)
            viewModel.updateQuestion(gameId)
        },
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(vertical = 8.dp)
    ) {
        Text(text = option)
    }
}


