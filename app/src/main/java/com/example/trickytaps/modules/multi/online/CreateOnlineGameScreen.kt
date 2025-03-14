// CreateOnlineGameScreen.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CreateOnlineGameScreen(navController: NavController, playerName: String) {
    val viewModel: OnlineMultiplayerViewModel = viewModel()
    val gameState by viewModel.gameState.collectAsState()

    var gameId by rememberSaveable { mutableStateOf<String?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) }
    var isBothPlayersReady by remember { mutableStateOf(false) }
    var newPlayerName by remember { mutableStateOf<String?>(null) } // Store the name of the new player who joins

    // Ensure game creation only happens once
    LaunchedEffect(Unit) {
        if (gameId == null) {
            gameId = viewModel.createGame(playerName)

            gameId?.let {
                viewModel.listenForGameUpdates(it) { playerName ->
                    newPlayerName = playerName // Set the new player name when they join
                    val state = viewModel.gameState.value
                    if (state?.players?.values?.all { it.isReady } == true) {
                        isBothPlayersReady = true
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button (Aligned to Top Start)
        IconButton(
            onClick = {
                navController.navigate("onlineMultiplayerModeSelection") {
                    popUpTo("onlineMultiplayerModeSelection") { inclusive = true } // Clears the back stack correctly
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (gameId == null) {
                CircularProgressIndicator()
                Text(text = "Creating Game...", fontSize = 24.sp)
            } else {
                Text(text = "Game ID: $gameId", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "Waiting for player to join...", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(20.dp))

                // Show the new player who joined
                newPlayerName?.let {
                    Text(
                        text = "$it has joined the game!",
                        fontSize = 20.sp,
                        color = Color.Blue,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // If the player is ready, show a "Ready" button
                if (!isPlayerReady) {
                    Button(
                        onClick = {
                            isPlayerReady = true
                            viewModel.updatePlayerReadyStatus(gameId!!, playerName, true)
                        },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(text = "Ready")
                    }
                } else {
                    Text(
                        text = "You are ready!",
                        fontSize = 20.sp,
                        color = Color.Green,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                // If both players are ready, navigate to the next screen
                if (isBothPlayersReady) {
                    LaunchedEffect(Unit) {
                        navController.navigate("onlineMultiplayerGame/$gameId/$playerName")
                    }
                }
            }
        }
    }
}






