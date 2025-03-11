// MultiplayerScreen.kt
package com.example.trickytaps.modules.multi.local

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.trickytaps.MediaPlayerManager
import com.example.trickytaps.R
import com.example.trickytaps.TrickQuestion
import com.example.trickytaps.generateTrickQuestion
import com.example.trickytaps.modules.single.PauseDialog
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import kotlinx.coroutines.launch


class MultiplayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val viewModel: MultiplayerViewModel = viewModel()
            val onVolumeChange = { newVolume: Float ->
                MediaPlayerManager.setVolume(newVolume)
            }

            MultiplayerScreen(navController = navController, viewModel = viewModel, onVolumeChange = onVolumeChange)
        }
    }
}

@Composable
fun MultiplayerScreen(navController: NavController,
                      viewModel: MultiplayerViewModel,
                      onVolumeChange: (Float) -> Unit // Receive volume function
) {
    val playerNames by viewModel.playerNames.collectAsState()
    val scores by viewModel.scores.collectAsState()
    val playerCount by viewModel.playerCount.collectAsState() // Retrieve player count correctly
    val gameTime by viewModel.gameTime.collectAsState() // Retrieve time from ViewModel
    var timeLeft by remember { mutableIntStateOf(gameTime) } // Use set time
    var gameOver by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    val currentQuestion = remember { mutableStateOf(generateTrickQuestion()) }
    var showPauseDialog by remember { mutableStateOf(false) } // State to show the PauseDialog

    val configuration = LocalConfiguration.current
    val isPortrait =
        configuration.orientation == android.content.res.Configuration.ORIENTATION_PORTRAIT

    val context = LocalContext.current
    val mediaPlayerRight = remember { MediaPlayer.create(context, R.raw.right) }
    val mediaPlayerWrong = remember { MediaPlayer.create(context, R.raw.wrong) }
    val mediaPlayerOver = remember { MediaPlayer.create(context, R.raw.over) }

    //var volumeLevel by remember { mutableStateOf(1f) } // Default max volume

    var sfxVolume by remember { mutableStateOf(1f) }  // Default volume for sound effects
    var bgmVolume by remember { mutableStateOf(1f) }  // Default volume for background music

    val coroutineScope = rememberCoroutineScope()
    val selectedAnswers = remember { mutableStateMapOf<Int, String?>() }
    val correctAnswers = remember { mutableStateMapOf<Int, Boolean?>() }

    LaunchedEffect(sfxVolume) {
        mediaPlayerRight.setVolume(sfxVolume, sfxVolume)
        mediaPlayerWrong.setVolume(sfxVolume, sfxVolume)
    }

    LaunchedEffect(bgmVolume) {
        onVolumeChange(bgmVolume) // Update BGM volume in MainActivity
    }

    DisposableEffect(Unit) {
        // Lock the orientation to landscape when entering this screen
        (context as? ComponentActivity)?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // Cleanup: Reset orientation to unspecified (default) when leaving this screen
        onDispose {
            (context as? ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // Remove fixed orientation
        }
    }

    LaunchedEffect(isPortrait) {
        isPaused = isPortrait
    }

    LaunchedEffect(timeLeft, isPaused) {
        while (timeLeft > 0 && !isPaused && !gameOver) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0) {
            mediaPlayerOver.start()
            gameOver = true
        }
    }

    if (gameOver) {
        MultiplayerGameOverScreen(scores, playerNames, navController, playerCount, viewModel)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Time Left: $timeLeft",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = buildAnnotatedString {
                        val questionText = currentQuestion.value.question
                        val colorText = questionText.substringAfter("**")
                            .substringBefore("**") // Extracts the word to be colored
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
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowOptions.forEach { option ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(60.dp)
                                                .padding(4.dp)
                                                .background(
                                                    when {
                                                        selectedAnswers[index] == option && correctAnswers[index] == true -> Color.Green.copy(alpha = 0.2f)
                                                        selectedAnswers[index] == option && correctAnswers[index] == false -> Color.Red.copy(alpha = 0.2f)
                                                        else -> Color.Gray
                                                    },shape = RoundedCornerShape(8.dp)
                                                )
                                                .border(
                                                    width = 2.dp,
                                                    color = when {
                                                        selectedAnswers[index] == option && correctAnswers[index] == true -> Color.Green
                                                        selectedAnswers[index] == option && correctAnswers[index] == false -> Color.Red
                                                        else -> Color.Transparent
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    if (selectedAnswers[index] == null) { // Prevent multiple clicks
                                                        selectedAnswers[index] = option
                                                        if (option == currentQuestion.value.correctAnswer) {
                                                            correctAnswers[index] = true
                                                            mediaPlayerRight.start()
                                                            viewModel.updateScore(playerNames[index])
                                                        } else {
                                                            correctAnswers[index] = false
                                                            mediaPlayerWrong.start()
                                                        }

                                                        // Wait a bit to show the feedback, then reset
                                                        coroutineScope.launch {
                                                            delay(500)
                                                            selectedAnswers[index] = null
                                                            correctAnswers[index] = null
                                                            currentQuestion.value = generateTrickQuestion()
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = option,
                                                fontSize = 18.sp,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = {
                    isPaused = !isPaused
                    showPauseDialog = true
                          },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pause",
                    modifier = Modifier.size(32.dp)
                )
            }
            // Show Pause Dialog if paused
            if (showPauseDialog) {
                PauseDialog(
                    onResume = {
                        showPauseDialog = false
                        isPaused = false
                               },
                    onBgmVolumeChange = { newVolume -> bgmVolume = newVolume },
                    onSfxVolumeChange = { newVolume -> sfxVolume = newVolume },
                    bgmVolume = bgmVolume,
                    sfxVolume = sfxVolume,
                    navController = navController
                )
            }
        }
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

    val context = LocalContext.current

    // State to track the action (whether "Play Again" or "Exit" was clicked)
    var clickedPlayAgain by remember { mutableStateOf(false) }
    // Lock orientation to landscape when the screen is first shown
    DisposableEffect(Unit) {
        (context as? ComponentActivity)?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            // This will be reset when exiting the screen or navigating away
            if (!clickedPlayAgain) {
                // If Exit is clicked, reset to portrait
                (context as? ComponentActivity)?.requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }
    }

    // Logic for Play Again button click
    val handlePlayAgainClick: () -> Unit = {
        clickedPlayAgain = true
        // Reset the scores
        viewModel.resetScores()

        // Navigate to "Play Again" screen
        navController.navigate("rotateScreen/$playerCount") {
            // Reset orientation back to landscape if player clicked Play Again
            (context as? ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }
    // Logic for Exit button click
    val handleExitClick: () -> Unit = {
        clickedPlayAgain = false
        // Navigate to the landing page or multiplayer mode
        navController.navigate("landingPage") {
            // When exit is clicked, reset orientation to portrait
            (context as? ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
    LaunchedEffect(Unit) {
        // Update win count when the screen is shown
        viewModel.updateWinCount()
    }

    val winCounts by viewModel.winCounts.collectAsState() // Get updated win counts

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = handleExitClick, // Exit action
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Back")
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Game Over!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            if (winners.size == 1) {
                Text(
                    text = "Winner: ${winners.first()} with $maxScore points!",
                    fontSize = 24.sp
                )
            } else {
                Text(
                    text = "It's a tie between ${winners.joinToString(", ")} with $maxScore points!",
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Display Games Won Leaderboard
            Text(
                text = "🏆 Games Won:",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            winCounts.forEach { (player, wins) ->
                // Use a conditional check to decide between "win" or "wins"
                val winText = if (wins == 1) "win" else "wins"
                Text(text = "$player: $wins $winText", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = handlePlayAgainClick) { // Play Again action
            Text(text = "Play Again")
            }
        }
    }
}