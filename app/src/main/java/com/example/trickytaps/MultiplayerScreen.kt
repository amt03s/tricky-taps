package com.example.trickytaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import kotlinx.coroutines.delay

class MultiplayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultiplayerScreen(playerCount = 2)
        }
    }
}

@Composable
fun MultiplayerScreen(playerCount: Int) {
    var timeLeft by remember { mutableStateOf(60) }
    var gameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    val currentQuestion = remember { mutableStateOf(generateTrickQuestion()) }
    val playerScores = remember { mutableStateListOf(*IntArray(playerCount) { 0 }.toTypedArray()) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    LaunchedEffect(isPortrait) {
        isPaused = isPortrait
    }

    LaunchedEffect(timeLeft) {
        while (timeLeft > 0 && !isPaused) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0) gameOver = true
    }

    if (gameOver) {
        MultiplayerGameOverScreen(playerScores)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (isPaused) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Rotate your device back to Landscape to continue!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                }
            } else {
                Text(text = "Time Left: $timeLeft", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = currentQuestion.value.question, fontSize = 22.sp, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (playerIndex in 0 until playerCount) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Player ${playerIndex + 1}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))

                            val options = currentQuestion.value.options.chunked(2)
                            options.forEach { rowOptions ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowOptions.forEach { option ->
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
fun MultiplayerGameOverScreen(scores: List<Int>) {
    val winner = scores.indices.maxByOrNull { scores[it] } ?: 0
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Game Over!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Winner: Player ${winner + 1} with ${scores[winner]} points!", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* Restart Game */ }) {
            Text(text = "Play Again")
        }
    }
}
