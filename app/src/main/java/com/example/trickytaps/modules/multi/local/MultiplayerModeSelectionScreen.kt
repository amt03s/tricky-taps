// MultiplayerModeSelectionScreen.kt
package com.example.trickytaps.modules.multi.local

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MultiplayerModeSelectionScreen(navController: NavController, viewModel: MultiplayerViewModel) {
    var player1 by remember { mutableStateOf("") }
    var player2 by remember { mutableStateOf("") }
    var player3 by remember { mutableStateOf("") }
    var playerCount by remember { mutableIntStateOf(2) } // Default to 2 players

    var showTimer by remember { mutableStateOf(false) } // State to show the PauseDialog

    val context = LocalContext.current
    DisposableEffect(Unit) {
        // Lock the orientation to PORTRAIT when entering this screen
        (context as? ComponentActivity)?.requestedOrientation =
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Cleanup: Reset orientation to unspecified (default) when leaving this screen
        onDispose {
            (context as? ComponentActivity)?.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED // Remove fixed orientation
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button (Aligned to Top Start)
        IconButton(
            onClick = {
                navController.navigate("multiplayerModeSelection") {
                    popUpTo("authScreen") { inclusive = true }
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
            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Enter Player Names", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = player1,
                onValueChange = {
                    if (it.length <= 12 && it.matches(Regex("^[a-zA-Z0-9_]*$"))) { // Only allow letters, numbers & underscore
                        player1 = it
                    }
                },
                label = { Text("Player 1 Name") },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = player2,
                onValueChange = {
                    if (it.length <= 12 && it.matches(Regex("^[a-zA-Z0-9_]*$"))) {
                        player2 = it
                    }
                },
                label = { Text("Player 2 Name") },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (playerCount == 3) {
                OutlinedTextField(
                    value = player3,
                    onValueChange = {
                        if (it.length <= 12 && it.matches(Regex("^[a-zA-Z0-9_]*$"))) {
                            player3 = it
                        }
                    },
                    label = { Text("Player 3 Name") },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row {
                Button(onClick = { playerCount = 2 }) {
                    Text(text = "2 Players")
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(onClick = { playerCount = 3 }) {
                    Text(text = "3 Players")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

//            Button(onClick = {
//                val names = if (playerCount == 3) listOf(player1, player2, player3) else listOf(player1, player2)
//                viewModel.setPlayers(names)
//                navController.navigate("rotateScreen/$playerCount")
//            }) {
//                Text(text = "Start Game")
//            }
//            Button(onClick = {
//                val safePlayer1 = player1.ifEmpty { "Player1" }
//                val safePlayer2 = player2.ifEmpty { "Player2" }
//                val safePlayer3 = if (playerCount == 3) player3.ifEmpty { "null" } else "null"
//
//                navController.navigate("modeSelection/$playerCount/$safePlayer1/$safePlayer2/$safePlayer3")
//            }) {
//                Text(text = "Set Timer!")
//            }

            Button(
                onClick = { showTimer = true },
            ) {
                Text(text = "Set Timer")
            }
        }

        /// Show Timer Dialog when `showTimer` is true
        if (showTimer) {
            Timer(
                onResume = { showTimer = false },
                navController = navController,
                viewModel = viewModel,
                player1 = player1,
                player2 = player2,
                player3 = player3,
                playerCount = playerCount,
            )
        }
    }
}

@Composable
fun Timer(
    onResume: () -> Unit,
    navController: NavController,
    viewModel: MultiplayerViewModel,
    player1: String,
    player2: String,
    player3: String,
    playerCount: Int
) {
    var time by remember { mutableIntStateOf(30) } // Default time in seconds


    AlertDialog(
        onDismissRequest = { onResume() }, // Close dialog on outside click
        confirmButton = {},
        title = {
            Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                IconButton(
                    onClick = onResume,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
                Text(text = "Set Game Timer",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = { if (time > 5) time -= 5 }) {
                        Text(text = "-5s")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "$time seconds", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = { if (time < 300) time += 5 }) {
                        Text(text = "+5s")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(onClick = {
                    viewModel.setGameTime(time) // Store time in ViewModel
                    onResume() // Close the dialog
//                    val names = if (playerCount == 3) listOf(player1, player2, player3) else listOf(player1, player2)
                    val names = listOf(player1.ifBlank { "Player1" }, player2.ifBlank { "Player2" }) +
                            if (playerCount == 3) listOf(player3.ifBlank { "Player3" }) else emptyList()

                    viewModel.setPlayers(names)
                    navController.navigate("rotateScreen/$playerCount")
                }) {
                    Text(text = "Start Game")
                }
            }
        }
    )
}