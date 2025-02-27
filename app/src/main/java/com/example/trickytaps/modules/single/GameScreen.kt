// GameScreen.kt
package com.example.trickytaps.modules.single

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.example.trickytaps.generateTrickQuestion

@Composable
fun GameScreen(navController: NavController, initialTime: Int, username: String, db: FirebaseFirestore) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(initialTime) } // Use `initialTime` instead of difficulty string
    var highScore by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf(generateTrickQuestion()) }
    var paused by remember { mutableStateOf(false) }
    var questionCount by remember { mutableIntStateOf(0) } // Track number of questions
    var showPauseDialog by remember { mutableStateOf(false) } // State to show the PauseDialog

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val context = LocalContext.current
    LaunchedEffect(true) {
        (context as? ComponentActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    LaunchedEffect(initialTime) {
        timeLeft = initialTime
    }

    // Retrieve User's High Score from Firestore
    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    highScore = document.getLong("highScore")?.toInt() ?: 0
                }
                .addOnFailureListener {
                    println("Failed to load high score")
                }
        }
    }

    // Question Timer
    LaunchedEffect(timeLeft, paused) {
        while (timeLeft > 0 && !paused && !gameOver) {
            delay(1000L)
            timeLeft--
        }

        if (timeLeft == 0 && !gameOver) {
            if (questionCount < 9) {
                // Move to next question
                currentQuestion = generateTrickQuestion()
                timeLeft = initialTime // Reset timer for next question
                questionCount++
            } else {
                gameOver = true // End game after 10 questions
            }
        }
    }

    if (gameOver) {
        GameOverScreen(score = score, navController = navController, username = username, db = db, initialTime = initialTime)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Player: $username",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )
                Text(
                    text = "High Score: $highScore",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Green
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Time Left: $timeLeft", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Score: $score", fontSize = 20.sp)
//                Text(
//                    text = "Question ${questionCount + 1} of 10",
//                    fontSize = 18.sp
//                ) // Show progress
                Spacer(modifier = Modifier.height(32.dp))

                // Display Trick Question
                Text(
                    text = buildAnnotatedString {
                        val questionText = currentQuestion.question
                        val colorText = questionText.substringAfter("**")
                            .substringBefore("**") // Extract color word
                        val colorStartIndex = questionText.indexOf(colorText)

                        append(questionText)

                        if (colorStartIndex != -1) {
                            addStyle(
                                style = SpanStyle(color = currentQuestion.displayedColor),
                                start = colorStartIndex,
                                end = colorStartIndex + colorText.length
                            )
                        }
                    },
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

                                if (questionCount < 10) {
                                    currentQuestion = generateTrickQuestion()
                                    timeLeft = initialTime // Reset timer for next question
                                    questionCount++
                                } else {
                                    gameOver = true
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = option, fontSize = 18.sp, color = Color.White)
                    }
                }
            }

            // Pause Button
            IconButton(
                onClick = {
                    paused = !paused
                    showPauseDialog = true
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Pause",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Show Pause Dialog if paused
        if (showPauseDialog) {
            PauseDialog(
                onResume = {
                    showPauseDialog = false
                    paused = false
                },
                onToggleMute = {
                    // Handle mute logic here
                }
            )
        }
    }
}

@Composable
fun GameOverScreen(navController: NavController, username: String, score: Int, db: FirebaseFirestore, initialTime: Int) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Update High Score in Firestore
    LaunchedEffect(score) {
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val currentHighScore = document.getLong("highScore") ?: 0
                    if (score > currentHighScore) {
                        db.collection("users").document(userId)
                            .update("highScore", score)
                            .addOnSuccessListener {
                                println("High score updated to $score")
                            }
                            .addOnFailureListener { e ->
                                println("Error updating high score: $e")
                            }
                    }
                }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(
            onClick = {
                navController.navigate("landingPage")
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
            Text(text = "Final Score: $score", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
//                navController.popBackStack("gameScreen", inclusive = true) // Clears previous game instance
//                navController.navigate("gameScreen/$initialTime/$username") // Restart game
                navController.navigate("gameScreen/$initialTime/$username") {
                    popUpTo("gameScreen") { inclusive = true }
                }
            }) {
                Text(text = "Play Again")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                navController.navigate("leaderboardScreen/$username/$score") // Navigate to Leaderboard with username & score
            }) {
                Text(text = "Show Leaderboard")
            }
        }
    }
}


@Composable
fun RotateToLandscapeScreen(navController: NavController, playerCount: Int) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // If already in landscape mode, navigate to the game
    LaunchedEffect(isLandscape) {
        if (isLandscape) {
            navController.navigate("multiplayerScreen/$playerCount") {
                popUpTo("rotateScreen/$playerCount") { inclusive = true }
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
            text = "Please rotate your device to Landscape mode",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "The game will start automatically when you rotate your device.",
            fontSize = 16.sp
        )
    }
}


@Composable
fun LeaderboardScreen(navController: NavController, db: FirebaseFirestore, username: String, score: Int) {
    var leaderboard by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch only the top 10 players from Firestore
    LaunchedEffect(Unit) {
        db.collection("users")
            .orderBy("highScore", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10) // Limit to Top 10 players
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    val user = doc.getString("username") ?: "Unknown"
                    val highScore = doc.getLong("highScore")?.toInt() ?: 0
                    user to highScore
                }
                leaderboard = users
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(
            onClick = {
                navController.navigate("gameOverScreen/$username/$score") {
                    popUpTo("leaderboardScreen") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Leaderboard", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // Lazy Grid layout for responsive display
                val gridState = rememberLazyGridState()
                val columns = when {
                    LocalConfiguration.current.screenWidthDp < 600 -> 1 // For small screens
                    LocalConfiguration.current.screenWidthDp < 900 -> 2 // For medium screens
                    else -> 3 // For large screens
                }

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(leaderboard) { (user, highScore) ->
                        // Determine the position (rank)
                        val rank = leaderboard.indexOfFirst { it.first == user } + 1
                        val isTop3 = rank <= 3 // For top 3 users

                        Card(
                            modifier = Modifier.padding(8.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isTop3) Color(0xFFD4AF37) else Color.Gray), // Gold for top 3
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left side: Column for Username, Medal, and Place
                                Column(
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    // Username and Medal
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = user,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isTop3) Color.White else Color.Black
                                        )
                                        if (isTop3) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // Display "1st", "2nd", or "3rd" as the medal
                                            Text(
                                                text = when (rank) {
                                                    1 -> "🥇"
                                                    2 -> "🥈"
                                                    3 -> "🥉"
                                                    else -> ""
                                                },
                                                fontSize = 18.sp,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    // Place under the username
                                    Text(
                                        text = "${rank}${when (rank) {
                                            1 -> "st"
                                            2 -> "nd"
                                            3 -> "rd"
                                            else -> "th"
                                        }} Place",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isTop3) Color.White else Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                // Right side: Points
                                Text(
                                    text = "$highScore points",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isTop3) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun PauseDialog(onResume: () -> Unit, onToggleMute: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onResume() },
        title = { Text(text = "Game Paused") },
        text = { Text(text = "Would you like to resume the game?") },
        confirmButton = {
            Button(onClick = onResume) {
                Text("Resume")
            }
        },
        dismissButton = {
            Button(onClick = onToggleMute) {
                Text("Toggle Mute")
            }
        }
    )
}