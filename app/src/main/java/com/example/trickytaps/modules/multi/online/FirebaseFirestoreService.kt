// FirebaseFirestoreService.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import com.example.trickytaps.TrickQuestion
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseFirestoreService {
    private val db = FirebaseFirestore.getInstance()


    // Join Game
    fun joinGame(gameId: String, playerName: String, playerData: Map<String, Any>) {
        // Update the Firestore document for the game
        db.collection("games").document(gameId)
            .update("players.$playerName", playerData) // Add Player 2 to the "players" field

        // Optionally, you can listen for updates here, to trigger changes when a player joins
    }

    fun createGame(playerName: String): String {
        val gameId = db.collection("games").document().id // Generate unique game ID
        val gameData = hashMapOf(
            "status" to "waiting",  // Set the game status to "waiting"
            "players" to hashMapOf(
                playerName to hashMapOf(
                    "name" to playerName,
                    "score" to 0,
                    "ready" to false
                )
            )
        )

        // Create the game in Firestore
        db.collection("games").document(gameId).set(gameData)
        Log.d("Firestore", "Game created with ID: $gameId")
        Log.d("Firestore", "createGame method called") // Add this for debugging


        // Return the generated game ID
        return gameId
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

    fun updateQuestion(gameId: String, newQuestion: TrickQuestion) {
        // Update the Firestore document with the new question
        db.collection("games").document(gameId)
            .update("currentQuestion", newQuestion)
            .addOnSuccessListener {
                Log.d("Firestore", "Question updated successfully!")
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error updating question", exception)
            }
    }

    fun updateGameStatus(gameId: String, status: String) {
        db.collection("games").document(gameId)
            .update("status", status)
            .addOnSuccessListener {
                Log.d("Firestore", "Game status updated successfully to: $status")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating game status", e)
            }
    }

    fun updateGameStatusToReady(gameId: String) {
        // Get a reference to the game document in Firestore
        val gameRef = db.collection("games").document(gameId)

        // Update the status field to "ready"
        gameRef.update("status", "ready")
            .addOnSuccessListener {
                Log.d("Firestore", "Game status updated to 'ready' successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating game status", e)
            }
    }



}



