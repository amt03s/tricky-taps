// OnlineMultiplayerViewmodel.kt
// OnlineMultiplayerViewModel.kt
package com.example.trickytaps.modules.multi.online

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.trickytaps.TrickQuestion
import com.example.trickytaps.generateTrickQuestion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

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

    private val _playerScore = MutableStateFlow(0)  // Initialize with a default score of 0
    val playerScore: StateFlow<Int> get() = _playerScore

    private val _timer = MutableStateFlow(45) // Timer for 45 seconds
    val timer: StateFlow<Int> get() = _timer

    private val _gameOver = MutableStateFlow(false) // Track if the game is over
    val gameOver: StateFlow<Boolean> get() = _gameOver

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
    fun joinGame(gameId: String, playerName: String, context: Context, navController: NavController) {
        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("games").document(gameId)

        gameRef.get().addOnSuccessListener { document ->
            val players = document.get("players") as? Map<String, Map<String, Any>> ?: emptyMap()

            // Log the players to check what's being fetched
            Log.d("GameCheck", "Players in the game: ${players.keys}")

            // Check if the current user is already in the game (i.e., the player has joined)
            if (players.containsKey(playerName)) {
                // Show a Toast message if the user tries to join their own game
                Toast.makeText(context, "You cannot join your own game!", Toast.LENGTH_SHORT).show()

                // Log the user out
                FirebaseAuth.getInstance().signOut()

                // Navigate to the Landing Page
                navController.popBackStack()  // This removes the current screen from the navigation stack
                navController.navigate("landingPage")  // Navigate to the Landing Page
                return@addOnSuccessListener
            }

            // Proceed with joining the game
            val playerData = hashMapOf(
                "name" to playerName,
                "score" to 0,
                "ready" to false
            )

            firebaseFirestoreService.joinGame(gameId, playerName, playerData)

            // Once the player has joined, navigate to the Ready Screen
            Toast.makeText(context, "Joined Game $gameId", Toast.LENGTH_SHORT).show()

            // Navigate after Firestore data has been updated
            navController.navigate("readyScreen/$gameId/$playerName")
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to join the game", Toast.LENGTH_SHORT).show()
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

    fun updateScore(gameId: String, playerName: String, scoreChange: Int) {
        Log.d("ViewModel", "Entering updateScore method for $playerName with score change: $scoreChange")
        viewModelScope.launch {
            val db = FirebaseFirestore.getInstance()
            val gameRef = db.collection("games").document(gameId)

            // Log the score change attempt
            Log.d("ViewModel", "Updating score for $playerName in game $gameId. Current score change: $scoreChange")

            try {
                // Fetch the current game state
                val document = gameRef.get().await()  // Await Firestore document
                Log.d("ViewModel", "Fetched game data: ${document.data}")

                val players = document.get("players") as? Map<String, Map<String, Any>> ?: return@launch
                val currentScore = players[playerName]?.get("score") as? Long ?: 0
                val newScore = currentScore + scoreChange

                // Log the calculated new score
                Log.d("ViewModel", "Current score for $playerName: $currentScore. New score: $newScore")

                // Update the score in Firestore
                gameRef.update("players.$playerName.score", newScore)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Score updated successfully for $playerName")

                        // Update local state
                        _playerScore.value = newScore.toInt()

                        // Fetch the updated game state after updating the score
                        gameRef.get().addOnSuccessListener { updatedDocument ->
                            Log.d("ViewModel", "Fetched updated game data: ${updatedDocument.data}")

                            val updatedPlayers = updatedDocument.get("players") as? Map<String, Map<String, Any>> ?: return@addOnSuccessListener
                            val updatedStatus = updatedDocument.getString("status") ?: "waiting"
                            val updatedQuestion = updatedDocument.get("currentQuestion") as? Map<String, Any>

                            // Parse the updated question
                            val currentQuestion = if (updatedQuestion != null) {
                                TrickQuestion(
                                    question = updatedQuestion["question"] as? String ?: "No question",
                                    options = (updatedQuestion["options"] as? List<String>) ?: emptyList(),
                                    correctAnswer = updatedQuestion["correctAnswer"] as? String ?: "",
                                    displayedColor = Color.Black // Default color
                                )
                            } else null

                            // Log the updated players' state
                            Log.d("ViewModel", "Updated players: $updatedPlayers")

                            // Update the game state locally
                            _gameState.value = GameState(
                                gameId = gameId,
                                players = updatedPlayers.mapValues { entry ->
                                    Player(
                                        name = entry.key,
                                        score = entry.value["score"] as? Int ?: 0,
                                        isReady = entry.value["ready"] as? Boolean ?: false
                                    )
                                },
                                status = updatedStatus,
                                currentQuestion = currentQuestion
                            )

                            // Log the updated game state
                            Log.d("ViewModel", "Updated game state: ${_gameState.value}")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error updating score", e)
                    }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error fetching game data", e)
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
    fun getPlayerScore(gameId: String, playerName: String, onScoreFetched: (Int) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("games").document(gameId)

        gameRef.get()
            .addOnSuccessListener { document ->
                val players = document.get("players") as? Map<String, Map<String, Any>> ?: return@addOnSuccessListener
                val score = players[playerName]?.get("score") as? Long ?: 0
                onScoreFetched(score.toInt())  // Callback with the fetched score
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching player score", e)
            }
    }


    fun startGameTimer() {
        viewModelScope.launch {
            for (i in 45 downTo 1) {
                delay(1000) // Wait for 1 second
                _timer.value = i // Update the timer
            }
            // After 45 seconds, set game over
            _gameOver.value = true
        }
    }

    fun deleteGameSession(gameId: String) {
        val db = FirebaseFirestore.getInstance()
        val gameRef = db.collection("games").document(gameId)

        gameRef.delete()
            .addOnSuccessListener {
                Log.d("Firestore", "Game session deleted successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error deleting game session", e)
            }
    }


}
