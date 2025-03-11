// OnlineMultiplayerModeSelectionScreen.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot

@Composable
fun OnlineMultiplayerModeSelectionScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // State to hold the username
    var playerName by remember { mutableStateOf<String>("Player") }

    // Fetch the username from Firestore
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            fetchUsernameFromFirestore(currentUser.uid) { username ->
                playerName = username
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
            text = "Online Multiplayer",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Create Game Button
        Button(
            onClick = {
                // Remove this call, as the game creation logic is now handled in the ViewModel
                navController.navigate("createOnlineGame/$playerName")
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Create Game")
        }

        // Join Game Button
        Button(
            onClick = {
                navController.navigate("joinOnlineGame/$playerName")
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Join Game")
        }
    }
}


fun fetchUsernameFromFirestore(userId: String, onUsernameFetched: (String) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Fetch username from Firestore "users" collection
    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            val username = document.getString("username") ?: "Player" // Default to "Player" if username is null
            onUsernameFetched(username)
        }
        .addOnFailureListener { exception ->
            // Handle failure (e.g., user not found)
            onUsernameFetched("Player") // Default to "Player" in case of error
        }
}


// Helper function to generate a unique player ID
fun generatePlayerId(): String {
    return "player_${System.currentTimeMillis()}"
}