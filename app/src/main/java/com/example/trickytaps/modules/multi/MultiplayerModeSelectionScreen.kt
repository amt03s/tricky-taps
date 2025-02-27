// MultiplayerModeSelectionScreen.kt
package com.example.trickytaps.modules.multi

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button (Aligned to Top Start)
        IconButton(
            onClick = {
                navController.navigate("landingPage") {
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

            Button(onClick = {
                val names = if (playerCount == 3) listOf(player1, player2, player3) else listOf(player1, player2)
                viewModel.setPlayers(names)
                navController.navigate("rotateScreen/$playerCount")
            }) {
                Text(text = "Start Game")
            }
        }
    }
}
