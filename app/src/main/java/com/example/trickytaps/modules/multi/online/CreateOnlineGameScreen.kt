// CreateOnlineGameScreen.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
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

    // Ensure game creation only happens once
    LaunchedEffect(Unit) {
        if (gameId == null) {
            gameId = viewModel.createGame(playerName)

            gameId?.let {
                viewModel.listenForGameUpdates(it) {
                    val state = viewModel.gameState.value
                    if (state?.players?.values?.all { it.isReady } == true) {
                        isBothPlayersReady = true
                    }
                }
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
        if (gameId == null) {
            CircularProgressIndicator()
            Text(text = "Creating Game...", fontSize = 24.sp)
        } else {
            Text(text = "Game ID: $gameId", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Waiting for player to join...", fontSize = 20.sp)
            Spacer(modifier = Modifier.height(20.dp))

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

            if (isBothPlayersReady) {
                LaunchedEffect(Unit) {
                    navController.navigate("onlineMultiplayerGame/$gameId/$playerName")
                }
            }
        }
    }
}






