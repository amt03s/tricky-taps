// OnlineMultiplayerViewmodel.kt
// OnlineMultiplayerViewModel.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trickytaps.TrickQuestion
import com.example.trickytaps.generateTrickQuestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OnlineMultiplayerViewModel : ViewModel() {
    private val firebaseFirestoreService = FirebaseFirestoreService()

    // Make _gameState mutable internally
    private val _gameState = MutableStateFlow<GameState?>(null)

    // Expose gameState as immutable StateFlow to the outside world
    val gameState: StateFlow<GameState?> = _gameState

    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId

    private val _availableGames = MutableStateFlow<List<GameInfo>>(emptyList())
    val availableGames: StateFlow<List<GameInfo>> = _availableGames

    // Fetch available games from Firestore
    fun fetchAvailableGames() {
        firebaseFirestoreService.fetchAvailableGames { games ->
            Log.d("ViewModel", "Fetched games: ${games.size}")
            _availableGames.value = games
        }
    }

    // Listen for the Host's ready status
    fun listenForHostReadyStatus(gameId: String, onHostReady: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for game updates", error)
                    return@addSnapshotListener
                }

                snapshot?.let {
                    val players = it.get("players") as? Map<String, Map<String, Any>> ?: emptyMap()
                    val hostReady = players["host"]?.get("ready") as? Boolean ?: false
                    onHostReady(hostReady)
                }
            }
    }

    // Create a new game
    fun createGame(playerName: String): String {
        val gameId = firebaseFirestoreService.createGame(playerName)

        _gameId.value = gameId

        // Generate an initial question
        val initialQuestion = generateTrickQuestion()

        // Initialize the game state with Player 1 (the creator)
        _gameState.value = GameState(
            gameId = gameId,
            players = mapOf(playerName to Player(name = playerName, score = 0, isReady = false)),
            status = "waiting",
            currentQuestion = initialQuestion
        )

        // Save the game document to Firestore with the initial game data
        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("games").document(gameId)

        gameRef.set(mapOf(
            "players" to mapOf(playerName to mapOf("score" to 0, "ready" to false)),
            "status" to "waiting",
            "currentQuestion" to initialQuestion // Add the initial question to Firestore
        ))
            .addOnSuccessListener {
                Log.d("Firestore", "Game created and question initialized")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error creating game", e)
            }

        return gameId
    }

    // Join an existing game
    fun joinGame(gameId: String, playerName: String) {
        val playerData = hashMapOf(
            "name" to playerName,
            "score" to 0,
            "ready" to false
        )

        firebaseFirestoreService.joinGame(gameId, playerName, playerData)

        listenForGameUpdates(gameId) { secondPlayerName ->
            _gameState.value?.let {
                val updatedGame = it.copy(
                    players = it.players + (secondPlayerName to Player(
                        name = secondPlayerName,
                        score = 0,
                        isReady = false
                    ))
                )
                _gameState.value = updatedGame // Now this will work as _gameState is mutable
            }
        }
    }

    // Listen for game updates such as player joining and both players being ready
    var hasNavigated = false // Prevent infinite navigation loops

    fun listenForGameUpdates(gameId: String, onSecondPlayerJoined: (String) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("games").document(gameId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firestore", "Error listening for game updates", error)
                    return@addSnapshotListener
                }

                snapshot?.let { doc ->
                    val players = doc.get("players") as? Map<String, Map<String, Any>> ?: emptyMap()
                    val status = doc.getString("status") ?: "waiting"
                    val questionData = doc.get("currentQuestion") as? Map<String, Any>

                    // Parse the current question
                    val currentQuestion = if (questionData != null) {
                        TrickQuestion(
                            question = questionData["question"] as? String ?: "No question",
                            options = (questionData["options"] as? List<String>) ?: emptyList(),
                            correctAnswer = questionData["correctAnswer"] as? String ?: "",
                            displayedColor = Color.Black // Default color
                        )
                    } else null

                    Log.d("Firestore", "Game status updated: $status, Question: ${currentQuestion?.question}")

                    _gameState.value = GameState(
                        gameId = gameId,
                        players = players.mapValues { entry ->
                            Player(
                                name = entry.key,
                                score = entry.value["score"] as? Int ?: 0,
                                isReady = entry.value["ready"] as? Boolean ?: false
                            )
                        },
                        status = status,
                        currentQuestion = currentQuestion
                    )

                    val secondPlayerName = players.keys.firstOrNull { it != _gameState.value?.players?.keys?.first() }
                    secondPlayerName?.let {
                        Log.d("GameState", "Second player joined: $it")
                        onSecondPlayerJoined(it)
                    }
                }
            }
    }

    // Update player ready status and check if both players are ready
    fun updatePlayerReadyStatus(gameId: String, playerName: String, isReady: Boolean) {
        viewModelScope.launch {
            firebaseFirestoreService.updatePlayerReadyStatus(gameId, playerName, isReady)

            val db = FirebaseFirestore.getInstance()
            val gameRef = db.collection("games").document(gameId)

            gameRef.get().addOnSuccessListener { document ->
                val players = document.get("players") as? Map<String, Map<String, Any>> ?: emptyMap()

                val allReady = players.values.all { (it["ready"] as? Boolean) == true }

                if (allReady) {
                    // Update the game status to 'ready'
                    gameRef.update("status", "ready")
                        .addOnSuccessListener {
                            Log.d("Firestore", "Game status updated to 'ready'")
                            // Set the new status in the game state
                            _gameState.value = _gameState.value?.copy(status = "ready")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Error updating game status", e)
                        }
                }
            }
        }
    }

    fun updateGameStatusToReady(gameId: String) {
        viewModelScope.launch {
            val newQuestion = generateTrickQuestion() // Generate a new question when the game is ready

            val db = FirebaseFirestore.getInstance()
            val gameRef = db.collection("games").document(gameId)

            // Ensure the question is set in Firestore
            gameRef.update("currentQuestion", newQuestion)
                .addOnSuccessListener {
                    Log.d("Firestore", "New question updated in Firestore")
                    _gameState.value?.let { game ->
                        _gameState.value = game.copy(currentQuestion = newQuestion) // Update the game state locally
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating question", e)
                }

            // Now update game status to 'ready'
            gameRef.update("status", "ready")
                .addOnSuccessListener {
                    Log.d("Firestore", "Game status updated to 'ready'")
                    _gameState.value?.let { game ->
                        _gameState.value = game.copy(status = "ready")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Error updating game status", e)
                }
        }
    }

    // Update the player's score in Firestore
    fun updateScore(gameId: String, playerName: String, scoreChange: Int) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val gameRef = db.collection("games").document(gameId)

            gameRef.get().addOnSuccessListener { document ->
                val players = document.get("players") as? MutableMap<String, Map<String, Any>> ?: return@addOnSuccessListener
                val currentScore = players[playerName]?.get("score") as? Long ?: 0
                val newScore = currentScore + scoreChange

                // Update the score in Firestore
                gameRef.update("players.$playerName.score", newScore)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Score updated successfully for $playerName")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating score", e)
                    }
            }
        }
    }

    // Update the question in Firestore
    fun updateQuestion(gameId: String) {
        val newQuestion = generateTrickQuestion() // Generate a new question

        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("games").document(gameId)

        // Update the current question field in Firestore
        gameRef.update("currentQuestion", newQuestion)
            .addOnSuccessListener {
                Log.d("Firestore", "New question updated in Firestore")
                _gameState.value?.let { game ->
                    _gameState.value = game.copy(currentQuestion = newQuestion) // Update the local game state
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error updating question", e)
            }
    }

    // Update game status in Firestore
    fun updateGameStatus(gameId: String, newStatus: String) {
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val gameRef = db.collection("games").document(gameId)

            // Fetch the current game document
            gameRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    // Set question if not already set
                    val currentQuestion = document.get("currentQuestion") as? TrickQuestion
                    if (currentQuestion == null) {
                        val newQuestion = generateTrickQuestion() // Generate a new question
                        gameRef.update("currentQuestion", newQuestion) // Update the Firestore document with the new question
                    }
                }

                // Update game status to 'ready'
                gameRef.update("status", newStatus)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Game status updated to '$newStatus'")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating game status", e)
                    }
            }
        }
    }
}
