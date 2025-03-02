// GameScreen.kt
package com.example.trickytaps.modules.single

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.trickytaps.R
import com.example.trickytaps.generateTrickQuestion

@Composable
fun GameScreen(navController: NavController,
               initialTime: Int,
               username: String,
               db: FirebaseFirestore,
               onVolumeChange: (Float) -> Unit, // Receive volume function
               mode: String) {

    var score by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableStateOf(initialTime) } // Use initialTime instead of difficulty string
    var highScore by remember { mutableIntStateOf(0) }
    var gameOver by remember { mutableStateOf(false) }
    var currentQuestion by remember { mutableStateOf(generateTrickQuestion()) }
    var paused by remember { mutableStateOf(false) }
    var questionCount by remember { mutableIntStateOf(0) } // Track number of questions
    var showPauseDialog by remember { mutableStateOf(false) } // State to show the PauseDialog

    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    val context = LocalContext.current
    val mediaPlayerRight = remember { MediaPlayer.create(context, R.raw.right) }
    val mediaPlayerWrong = remember { MediaPlayer.create(context, R.raw.wrong) }
    val mediaPlayerOver = remember { MediaPlayer.create(context, R.raw.over) }

    var sfxVolume by remember { mutableStateOf(1f) }  // Default full volume for sound effects
    var bgmVolume by remember { mutableStateOf(1f) }  // Default full volume for background music

    LaunchedEffect(sfxVolume) {
        mediaPlayerRight.setVolume(sfxVolume, sfxVolume)
        mediaPlayerWrong.setVolume(sfxVolume, sfxVolume)
    }

    LaunchedEffect(bgmVolume) {
        onVolumeChange(bgmVolume) // Update BGM volume in MainActivity
    }

    LaunchedEffect(true) {
        (context as? ComponentActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    // Retrieve User's High Score from Firestore
    LaunchedEffect(userId, mode) {
        if (userId != null) {
            // Retrieve the high score based on the selected mode
            val scoreType = if (mode == "easy") "easyHighScore" else "hardHighScore"
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    highScore = document.getLong(scoreType)?.toInt() ?: 0
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
            timeLeft--  // Decrease timeLeft every second
        }

        if (timeLeft == 0 && !gameOver) {
            if (questionCount < 9) {
                currentQuestion = generateTrickQuestion()
                timeLeft = initialTime // Reset the timer to initialTime
                questionCount++  // Move to next question
            } else {
                gameOver = true
                mediaPlayerOver.start()  // Play end of game sound
            }
        }
    }

    if (gameOver) {
        GameOverScreen(score = score, navController = navController, username = username, db = db, initialTime = initialTime, mode = mode)
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
                                    if (timeLeft == 5){
                                        score += 50
                                        mediaPlayerRight.start()
                                    }
                                    else if (timeLeft == 4){
                                        score += 40
                                        mediaPlayerRight.start()
                                    }
                                    else if (timeLeft == 3){
                                        score += 30
                                        mediaPlayerRight.start()
                                    }
                                    else if (timeLeft == 20){
                                        score += 40
                                        mediaPlayerRight.start()
                                    }
                                    else{
                                        score += 10
                                        mediaPlayerRight.start() // Play correct answer sound
                                    }
                                } else {
                                    mediaPlayerWrong.start() // Play wrong answer sound
                                }

                                if (questionCount < 10) {
                                    currentQuestion = generateTrickQuestion()
                                    timeLeft = initialTime // Reset timer for next question
                                    questionCount++
                                } else {
                                    gameOver = true
                                    mediaPlayerOver.start()
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
                onBgmVolumeChange = { newVolume -> bgmVolume = newVolume },
                onSfxVolumeChange = { newVolume -> sfxVolume = newVolume },
                bgmVolume = bgmVolume,
                sfxVolume = sfxVolume
            )
        }
    }
}

@Composable
fun GameOverScreen(navController: NavController, username: String, score: Int, db: FirebaseFirestore, initialTime: Int, mode: String) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    // Determine the mode based on initialTime
    val mode = if (initialTime == 5) "easy" else "hard"

    // Update High Score in Firestore
    LaunchedEffect(score) {
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val currentHighScore = document.getLong(mode) ?: 0
                    if (score > currentHighScore) {
                        db.collection("users").document(userId)
                            .update(mode, score) // Update the correct mode score
                            .addOnSuccessListener {
                                println("$mode updated to $score")
                            }
                            .addOnFailureListener { e ->
                                println("Error updating $mode: $e")
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
                navController.navigate("modeScreen/$username") {
                    popUpTo("modeScreen") { inclusive = true } // Clears the back stack correctly
                }
            }) {
                Text(text = "Play Again")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                // Pass the selected mode (easy or hard) to the leaderboard screen
                navController.navigate("leaderboardScreen/$username/$score/$initialTime/$mode")
                //navController.navigate("leaderboardScreen/$username/$score/$mode") // Pass mode here as well
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
//fun LeaderboardScreen(navController: NavController, db: FirebaseFirestore, username: String, score: Int, mode: String) {
fun LeaderboardScreen(navController: NavController, db: FirebaseFirestore, username: String, score: Int, initialTime: Int, mode: String) {
    var leaderboard by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedMode by remember { mutableStateOf(mode) } // Default mode passed from GameOverScreen

    // Fetch only the top 10 players from Firestore based on selected mode
    LaunchedEffect(selectedMode) {
        val leaderboardField = if (selectedMode == "easy") "easyHighScore" else "hardHighScore"

        db.collection("users")
            .orderBy(leaderboardField, com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10) // Limit to Top 10 players
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    val user = doc.getString("username") ?: "Unknown"
                    val highScore = doc.getLong(leaderboardField)?.toInt() ?: 0
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
                // Navigate back to GameOverScreen with the score and mode
                //navController.popBackStack() // This will navigate back safely
                navController.navigate("gameOverScreen/$username/$score/$initialTime/$mode") {
                    popUpTo("leaderboardScreen") { inclusive = true } // Clears the Leaderboard from stack
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

            // Buttons to toggle between Easy and Hard mode leaderboards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { selectedMode = "easy" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == "easy") Color.Gray else Color.LightGray
                    )
                ) {
                    Text(text = "Easy Mode")
                }
                Button(
                    onClick = { selectedMode = "hard" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedMode == "hard") Color.Gray else Color.LightGray
                    )
                ) {
                    Text(text = "Hard Mode")
                }
            }

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

// Pause Dialog with Two Volume Sliders
@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onBgmVolumeChange: (Float) -> Unit,
    onSfxVolumeChange: (Float) -> Unit,
    bgmVolume: Float,
    sfxVolume: Float
) {
    AlertDialog(
        onDismissRequest = { onResume() },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { onResume() }) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = "Resume",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
        },
        title = { Text("Game Paused") },
        text = {
            Column {
                // Background Music Slider
                Row{
                    IconButton(onClick = {})
                    {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "music",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                    Slider(
                        value = bgmVolume,
                        onValueChange = onBgmVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Row{
                    IconButton(onClick = {})
                    {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "music",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                    Slider(
                        value = sfxVolume,
                        onValueChange = onSfxVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    )
}