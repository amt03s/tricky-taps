package com.example.trickytaps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "landingPage") {
        composable("landingPage") {
            MainScreen(navController)
        }
        composable("multiplayerModeSelection") {
            MultiplayerModeSelectionScreen(navController)
        }
        composable("rotateScreen/{playerCount}") { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getString("playerCount")?.toInt() ?: 2
            RotateToLandscapeScreen(navController, playerCount)
        }
        composable("multiplayerScreen/{playerCount}") { backStackEntry ->
            val playerCount = backStackEntry.arguments?.getString("playerCount")?.toInt() ?: 2
            MultiplayerScreen(playerCount)
        }
        composable("authScreen") { AuthScreen(navController) }
        composable("usernameScreen/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            UsernameScreen(navController, userId)
        }
        composable("gameScreen/{username}") { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Player"
            GameScreen(navController, username, FirebaseFirestore.getInstance())
        }
    }
}

@Composable
fun GameScreen(navController: NavController, username: String, db: FirebaseFirestore) {
    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) } // 30-second gameplay
    var highScore by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf(generateTrickQuestion()) }
    var paused by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

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

    // Countdown Timer (Only runs if not paused)
    LaunchedEffect(timeLeft, paused) {
        while (timeLeft > 0 && !paused && !gameOver) {
            delay(1000L)
            timeLeft--
        }
        if (timeLeft == 0) gameOver = true
    }

    if (gameOver) {
        GameOverScreen(score, navController, username, db)
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Player: $username", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Blue)
                Text(text = "High Score: $highScore", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Green)
                Spacer(modifier = Modifier.height(12.dp))

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

@Composable
fun GameOverScreen(score: Int, navController: NavController, username: String, db: FirebaseFirestore) {
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

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Game Over!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(text = "Final Score: $score", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("gameScreen/$username") }) {
            Text(text = "Play Again")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            // ✅ Clear all previous screens before navigating to the login screen
            navController.navigate("authScreen") {
                popUpTo("landingPage") { inclusive = true } // Clears previous screens from the stack
            }
        }) {
            Text(text = "Quit")
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
