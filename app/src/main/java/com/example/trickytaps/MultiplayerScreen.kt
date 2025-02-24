package com.example.trickytaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay

class MultiplayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: MultiplayerViewModel = viewModel()

            MultiplayerScreen(navController = navController, viewModel = viewModel)
        }
    }
}

@Composable
fun MultiplayerScreen(navController: NavController, viewModel: MultiplayerViewModel) {
    val playerNames by viewModel.playerNames.collectAsState()
    val scores by viewModel.scores.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState() // Retrieve player count correctly

    var timeLeft by remember { mutableIntStateOf(5) } // 5-second gameplay
    var gameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    val currentQuestion = remember { mutableStateOf(generateTrickQuestion()) }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    LaunchedEffect(isPortrait) {
        isPaused = isPortrait
    }

    LaunchedEffect(timeLeft, isPaused) {
        while (timeLeft > 0 && !isPaused && !gameOver) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0) gameOver = true
    }

    if (gameOver) {
        MultiplayerGameOverScreen(scores, playerNames, navController, playerCount, viewModel)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    Text(
                        text = "Time Left: $timeLeft",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = buildAnnotatedString {
                            val questionText = currentQuestion.value.question
                            val colorText = questionText.substringAfter("**").substringBefore("**") // Extracts the word to be colored
                            val colorStartIndex = questionText.indexOf(colorText)

                            append(questionText)

                            if (colorStartIndex != -1) {
                                addStyle(
                                    style = SpanStyle(color = currentQuestion.value.displayedColor),
                                    start = colorStartIndex,
                                    end = colorStartIndex + colorText.length
                                )
                            }
                        },
                        fontSize = 22.sp,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        playerNames.forEachIndexed { index, playerName ->
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = playerName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                val options = currentQuestion.value.options.chunked(2)
                                options.forEach { rowOptions ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        rowOptions.forEach { option ->
                                            AnswerButton(
                                                option,
                                                index,
                                                currentQuestion,
                                                viewModel,
                                                isPortrait
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { isPaused = !isPaused },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Text(if (isPaused) "Resume" else "Pause")
            }
        }
    }
}

@Composable
fun AnswerButton(
    option: String,
    playerIndex: Int,
    currentQuestion: MutableState<TrickQuestion>,
    viewModel: MultiplayerViewModel,
    isPortrait: Boolean
) {
    // Get player name safely
    val playerNames = viewModel.playerNames.collectAsState().value
    val playerName = playerNames.getOrNull(playerIndex) ?: "Unknown Player" // Avoid crashes

    Box(
        modifier = Modifier
            .width(if (isPortrait) 250.dp else 160.dp)
            .height(60.dp)
            .padding(4.dp)
            .background(Color.Gray, shape = RoundedCornerShape(8.dp))
            .clickable {
                if (option == currentQuestion.value.correctAnswer) {
                    viewModel.updateScore(playerName)
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
fun MultiplayerGameOverScreen(
    scores: Map<String, Int>,
    playerNames: List<String>,
    navController: NavController,
    playerCount: Int,
    viewModel: MultiplayerViewModel
) {
    val maxScore = scores.values.maxOrNull() ?: 0
    val winners = scores.filter { it.value == maxScore }.keys

    LaunchedEffect(Unit) {
        viewModel.updateWinCount()
    }

    val winCounts by viewModel.winCounts.collectAsState() // Get updated win counts

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(
            onClick = {
                navController.navigate("multiplayerModeSelection") {
                    popUpTo("landingPage") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Back")
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Game Over!", fontSize = 28.sp, fontWeight = FontWeight.Bold)

            if (winners.size == 1) {
                Text(text = "Winner: ${winners.first()} with $maxScore points!", fontSize = 24.sp)
            } else {
                Text(
                    text = "It's a tie between ${winners.joinToString(", ")} with $maxScore points!",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Display Games Won Leaderboard
            Text(text = "🏆 Games Won:", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            winCounts.forEach { (player, wins) ->
                Text(text = "$player: $wins wins", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                viewModel.resetScores() // Reset round scores, not win count
                navController.navigate("rotateScreen/$playerCount")
            }) {
                Text(text = "Play Again")
            }
        }
    }
}




