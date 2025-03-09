// JoinOnlineGameScreen.kt
package com.example.trickytaps.modules.multi.online

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun JoinOnlineGameScreen(navController: NavController, playerName: String) {
    val viewModel: OnlineMultiplayerViewModel = viewModel() // Initialize the viewModel
    val availableGames by viewModel.availableGames.collectAsState()

    var gameId by remember { mutableStateOf<String?>(null) }

    // Fetch available games when the screen is launched
    LaunchedEffect(Unit) {
        viewModel.fetchAvailableGames()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Available Games",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Show a message when no games are available
        if (availableGames.isEmpty()) {
            Text(
                text = "No available games to join.",
                fontSize = 20.sp,
                color = Color.Gray,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Display available games in a scrollable list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(availableGames) { game ->
                    GameCard(
                        gameId = game.gameId,
                        playerCount = game.playerCount,
                        onClick = {
                            gameId = game.gameId
                            // Join the game by calling joinGame() in ViewModel
                            viewModel.joinGame(game.gameId, playerName)

                            // Navigate to the Ready Screen
                            navController.navigate("readyScreen/$gameId/$playerName")
                        }
                    )
                }
            }
        }
    }
}



@Composable
fun GameCard(gameId: String, playerCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick), // Make it clickable
        shape = RoundedCornerShape(12.dp), // Rounded corners for the card
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Game ID: $gameId",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "$playerCount players",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            // Show an arrow icon to indicate that the card is clickable
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Join Game",
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
        }
    }
}

@Composable
fun ReadyScreen(navController: NavController, gameId: String, playerName: String) {
    // Initialize ViewModel directly in the composable
    val viewModel: OnlineMultiplayerViewModel = viewModel()

    var isReady by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Are You Ready to Play?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Button to mark player as ready
        Button(
            onClick = {
                // Mark the player as ready in Firestore
                // Call the ViewModel's method to update the player's ready status
                viewModel.updatePlayerReadyStatus(gameId, playerName, true)

                isReady = true
                // Navigate to the next screen or game screen
                navController.navigate("gameScreen/$gameId/$playerName")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = if (isReady) "Ready!" else "Mark as Ready")
        }

        // Show message when player is ready
        if (isReady) {
            Text(
                text = "You are ready!",
                fontSize = 20.sp,
                color = Color.Green,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}


