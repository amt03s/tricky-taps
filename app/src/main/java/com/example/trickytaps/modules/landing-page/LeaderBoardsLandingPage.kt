package com.example.trickytaps.modules.landingpage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LeaderBoardsLandingPage(navController: NavController, db: FirebaseFirestore, username: String, score: Int, initialTime: Int, mode: String) {
    var leaderboard by remember { mutableStateOf<List<Pair<String, Int>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedMode by remember { mutableStateOf(mode) } // Default mode passed from GameOverScreen

    // Fetch only the top 10 players from Firestore based on selected mode
    LaunchedEffect(selectedMode) {
        val leaderboardField = if (selectedMode == "easy") "easy" else "hard"

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
//                navController.popBackStack() // This will navigate back safely
                navController.navigate("landingPage")
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
