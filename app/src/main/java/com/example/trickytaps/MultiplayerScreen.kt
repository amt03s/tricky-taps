package com.example.trickytaps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun MultiplayerScreen(navController: NavController) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    var timeLeft by remember { mutableStateOf(30) } // Game timer
    var gameOver by remember { mutableStateOf(false) }
    var paused by remember { mutableStateOf(false) } // Pause state
    val currentQuestion = remember { mutableStateOf(generateTrickQuestion()) }
    val playerScores = remember { mutableStateListOf(0, 0, 0) } // Scores for 3 players

    // Countdown Timer
    LaunchedEffect(timeLeft, paused) {
        while (timeLeft > 0 && !gameOver) {
            if (!paused) {
                delay(1000L)
                timeLeft--
            }
            else {
                // Keep checking if the game is resumed
                while (paused) {
                    delay(100L)
                }
            }
        }
        if (timeLeft == 0) gameOver = true
    }

    if (gameOver) {
        MultiplayerGameOverScreen(playerScores, navController) {
            timeLeft = 30
            gameOver = false
            playerScores.fill(0) // Reset scores
            currentQuestion.value = generateTrickQuestion()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Timer Display
                Text(text = "Time Left: $timeLeft", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "${currentQuestion.value.question}", fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                // Answer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (playerIndex in 0 until 3) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Player ${playerIndex + 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isPortrait) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currentQuestion.value.options.forEach { option ->
                                        AnswerButton(option, playerIndex, currentQuestion, playerScores, isPortrait)
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val options = currentQuestion.value.options.chunked(2)
                                    options.forEach { row ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            row.forEach { option ->
                                                AnswerButton(option, playerIndex, currentQuestion, playerScores, isPortrait)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Pause Button (Top Right)
            Button(
                onClick = { paused = !paused },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(if (paused) "Resume" else "Pause")
            }
        }
    }
}

@Composable
fun AnswerButton(option: String, playerIndex: Int, currentQuestion: MutableState<TrickQuestion>, playerScores: MutableList<Int>, isPortrait: Boolean) {
    Box(
        modifier = Modifier
            .width(if (isPortrait) 250.dp else 160.dp)
            .height(60.dp) // Ensuring equal height and appropriate width
            .padding(4.dp)
            .background(Color.Gray, shape = RoundedCornerShape(8.dp))
            .clickable {
                if (option == currentQuestion.value.correctAnswer) {
                    playerScores[playerIndex] += 10 // Update specific player score
                }
                currentQuestion.value = generateTrickQuestion() // Load new question
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = option, fontSize = 18.sp, color = Color.White, textAlign = TextAlign.Center)
    }
}
@Composable
fun MultiplayerGameOverScreen(scores: List<Int>, navController: NavController, onRestart: () -> Unit) {
    val winner = scores.indices.maxByOrNull { scores[it] } ?: 0

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Game Over!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Winner: Player ${winner + 1} with ${scores[winner]} points!", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRestart() }) {
            Text(text = "Play Again")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("landingPage"){
                popUpTo("landingPage") { inclusive = true }
            }
        }) {
            Text(text = "Quit")
        }
    }
}
