//TrickyTapsLandingPage.kt
package com.example.trickytaps.modules.auth

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun TrickyTapsLandingPage(navController: NavController) {
    val context = LocalContext.current
    LaunchedEffect(true) {
        (context as? ComponentActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button (Aligned to Top Start)
        IconButton(
            onClick = {
                navController.navigate("help") {
                    popUpTo("authScreen") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(imageVector = Icons.Default.HelpOutline,
                contentDescription = "Help",
                modifier = Modifier.size(32.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(text = "Tricky Taps", fontSize = 32.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { navController.navigate("authScreen") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "Single Player", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { navController.navigate("multiplayerModeSelection") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "Multiplayer", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}

