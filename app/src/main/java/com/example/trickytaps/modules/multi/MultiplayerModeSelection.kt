// MultiplayerModeSelection.kt
package com.example.trickytaps.modules.multi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun MultiplayerModeSelection(navController: NavController) {
    // UI to choose between Local or Online Multiplayer
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose Multiplayer Mode",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Local Multiplayer Button
        Button(
            onClick = {
                // Navigate to local multiplayer screen
                navController.navigate("MultiplayerModeSelectionScreen")
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Local Multiplayer")
        }

        // Online Multiplayer Button
        Button(
            onClick = {
                // Navigate to online multiplayer screen
                navController.navigate("multiplayerAuthScreen")
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Online Multiplayer")
        }
    }
}
