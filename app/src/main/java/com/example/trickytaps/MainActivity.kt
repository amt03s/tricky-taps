package com.example.trickytaps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.trickytaps.ui.theme.TrickyTapsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrickyTapsTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
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

@Composable
fun MultiplayerModeSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 🔙 Back Button (Top Left)
        Button(
            onClick = { navController.popBackStack() },  // Navigate back to Main Menu
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text(text = "⬅ Back")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Choose Multiplayer Mode", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { navController.navigate("rotateScreen/2") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = "2 Players", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { navController.navigate("rotateScreen/3") },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = "3 Players", fontSize = 18.sp, color = Color.White)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val navController = rememberNavController()
    MainScreen(navController)
}
