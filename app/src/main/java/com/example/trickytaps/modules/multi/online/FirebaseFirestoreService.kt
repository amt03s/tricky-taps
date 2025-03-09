// FirebaseFirestoreService.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseFirestoreService {
    private val db = FirebaseFirestore.getInstance()

    // Create Game
    fun createGame(playerName: String): String {
        val gameId = db.collection("games").document().id
        val gameData = hashMapOf(
            "status" to "waiting", // Ensure status is a String
            "players" to hashMapOf(
                playerName to hashMapOf( // Use playerName directly as the key
                    "name" to playerName,
                    "score" to 0,
                    "ready" to false
                )
            )
        )

        // Store the game data in Firestore under the 'games' collection
        db.collection("games").document(gameId).set(gameData)
        return gameId
    }

    // Join Game
    fun joinGame(gameId: String, playerName: String, playerData: Map<String, Any>) {
        // Update the Firestore document for the game
        db.collection("games").document(gameId)
            .update("players.$playerName", playerData) // Add Player 2 to the "players" field

        // Optionally, you can listen for updates here, to trigger changes when a player joins
    }




    // Listen for Game Updates
    fun listenForGameUpdates(gameId: String, onGameUpdated: (Map<String, Any>) -> Unit) {
        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error listening for game updates: ${e.message}")
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val gameData = it.data ?: return@addSnapshotListener
                    onGameUpdated(gameData)
                }
            }
    }

    // Listen for Player Join
    fun listenForPlayerJoin(gameId: String, onPlayerJoined: (Boolean) -> Unit) {
        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("Firestore", "Error getting game data: ${e.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val players = snapshot.get("players") as? Map<String, Any>
                    val secondPlayerJoined = players?.size == 2  // Check if there are 2 players

                    onPlayerJoined(secondPlayerJoined)
                }
            }
    }

    // Update Score
    // FirebaseFirestoreService.kt

    fun updateScore(gameId: String, playerName: String, newScore: Int) {
        db.collection("games").document(gameId)
            .update("players.$playerName.score", newScore)
            .addOnSuccessListener {
                Log.d("Firestore", "Player $playerName score updated to $newScore")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating player score: ${e.message}")
            }
    }

    // Fetch Available Games
    fun fetchAvailableGames(onGamesFetched: (List<GameInfo>) -> Unit) {
        db.collection("games")
            .whereEqualTo("status", "waiting") // Only games with "waiting" status
            .get()
            .addOnSuccessListener { result ->
                val availableGames = mutableListOf<GameInfo>()
                for (document in result) {
                    val gameId = document.id
                    val players = document.get("players") as? Map<String, Any> ?: emptyMap()
                    availableGames.add(GameInfo(gameId, players.size)) // Add game info to the list
                }
                Log.d("Firestore", "Fetched ${availableGames.size} available games.") // Log the number of games
                onGamesFetched(availableGames) // Update the state
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching available games: ${exception.message}")
            }
    }



    fun updatePlayerReadyStatus(gameId: String, playerName: String, isReady: Boolean) {
        // Update the player's ready status in Firestore
        db.collection("games").document(gameId)
            .update("players.$playerName.ready", isReady)
            .addOnSuccessListener {
                Log.d("Firestore", "Player $playerName ready status updated to $isReady")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating player ready status: ${e.message}")
            }
    }


}



