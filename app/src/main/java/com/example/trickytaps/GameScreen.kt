package com.example.trickytaps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "mainScreen") {
        composable("mainScreen") { MainScreen(navController) }
        composable("gameScreen") { GameScreen(navController) }
    }
}

@Composable
fun GameScreen(navController: NavController) {
    var score by remember { mutableStateOf(0) }
    var timeLeft by remember { mutableStateOf(30) } // 30-second gameplay
    var currentQuestion by remember { mutableStateOf(generateTrickQuestion()) }
    var paused by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }

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
        GameOverScreen(score, navController) {
            score = 0
            timeLeft = 30
            gameOver = false
            currentQuestion = generateTrickQuestion()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Time Left: $timeLeft", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Score: $score", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(32.dp))

                // Display Trick Question
                Text(
                    text = currentQuestion.question,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Display Answer Choices
                currentQuestion.options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.Gray)
                            .clickable {
                                if (option == currentQuestion.correctAnswer) {
                                    score += 10
                                }
                                currentQuestion = generateTrickQuestion()
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = option, fontSize = 18.sp, color = Color.White)
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

// Game Over Screen
@Composable
fun GameOverScreen(score: Int, navController: NavController, onRestart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Game Over!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Final Score: $score", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRestart() }) {
            Text(text = "Play Again")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            navController.navigate("landingPage") {
                popUpTo("landingPage") { inclusive = true } // Clears previous screens
            }
        }){
            Text(text = "Quit")
        }
    }
}
