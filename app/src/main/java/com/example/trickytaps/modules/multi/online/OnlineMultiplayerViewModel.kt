// OnlineMultiplayerViewmodel.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.trickytaps.TrickQuestion
import com.example.trickytaps.generateTrickQuestion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OnlineMultiplayerViewModel : ViewModel() {
    private val firebaseFirestoreService = FirebaseFirestoreService()

    private val _gameState = MutableStateFlow<Game?>(null)
    val gameState: StateFlow<Game?> = _gameState

    private val _gameId = MutableStateFlow<String?>(null)
    val gameId: StateFlow<String?> = _gameId

    private val _availableGames = MutableStateFlow<List<GameInfo>>(emptyList())
    val availableGames: StateFlow<List<GameInfo>> = _availableGames

    // Fetch available games from Firestore
    fun fetchAvailableGames() {
        firebaseFirestoreService.fetchAvailableGames { games ->
            Log.d("ViewModel", "Fetched games: ${games.size}")  // Log the size of fetched games
            _availableGames.value = games // This triggers the UI to update
        }
    }


    // Create Game
    fun createGame(playerName: String): String {
        // Create a new game and add Player 1 (the host)
        val gameId = firebaseFirestoreService.createGame(playerName)
        _gameId.value = gameId

        // Initialize the game state with Player 1 (the creator)
        _gameState.value = Game(
            gameId = gameId,
            players = mapOf(playerName to Player(name = playerName, score = 0, isReady = false)), // Player 1 is the first player
            status = "waiting",
            currentQuestion = null
        )

        // Listen for the second player joining the game
        listenForGameUpdates(gameId) { secondPlayerName ->
            // Add Player 2 when they join
            _gameState.value = _gameState.value?.copy(
                players = _gameState.value?.players?.plus(
                    secondPlayerName to Player(name = secondPlayerName, score = 0, isReady = false) // Player 2 joins the game
                ) ?: emptyMap()
            )
        }

        return gameId
    }

    // Join Game
    fun joinGame(gameId: String, playerName: String) {
        // Create a new player entry
        val playerData = hashMapOf(
            "name" to playerName,
            "score" to 0,
            "ready" to false // Initially, the player is not ready
        )

        // Add Player 2 to Firestore in the game document
        firebaseFirestoreService.joinGame(gameId, playerName, playerData)

        // Listen for updates when the second player joins
        listenForGameUpdates(gameId) { secondPlayerName ->
            _gameState.value?.let {
                val updatedGame = it.copy(
                    players = it.players + (secondPlayerName to Player(name = secondPlayerName, score = 0, isReady = false)) // Add Player 2
                )
                _gameState.value = updatedGame
            }
        }
    }




    // Listen for game updates such as player joining and both players being ready
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

                    // Proceed if we have exactly two players
                    if (players.size == 2) {
                        // Find the second player (the one who is not the host)
                        val hostPlayer = doc.getString("hostPlayer")
                        val secondPlayerName = players.keys.firstOrNull { it != hostPlayer }

                        // If we find the second player, trigger the callback
                        secondPlayerName?.let {
                            onSecondPlayerJoined(it)
                        }
                    }
                }
            }
    }



    // Update Player Ready Status
    fun updatePlayerReadyStatus(gameId: String, playerName: String, isReady: Boolean) {
        viewModelScope.launch {
            firebaseFirestoreService.updatePlayerReadyStatus(gameId, playerName, isReady)
        }
    }

    fun updateScore(gameId: String, playerName: String, newScore: Int) {
        viewModelScope.launch {
            // Update score in Firestore using FirebaseFirestoreService
            firebaseFirestoreService.updateScore(gameId, playerName, newScore)

            // Optionally, update game state locally after score update
            _gameState.value?.let { game ->
                val updatedPlayer = game.players[playerName]?.copy(score = newScore)
                if (updatedPlayer != null) {
                    val updatedPlayers = game.players.toMutableMap()
                    updatedPlayers[playerName] = updatedPlayer
                    _gameState.value = game.copy(players = updatedPlayers)
                }
            }
        }
    }

    fun updateQuestion(gameId: String) {
        viewModelScope.launch {
            val newQuestion = generateTrickQuestion() // Get a new random question

            // Update the game state with the new question
            _gameState.value?.let { game ->
                _gameState.value = game.copy(currentQuestion = newQuestion)
            }

            // Optionally, you can update the Firestore with the new question if required
            // firebaseFirestoreService.updateQuestion(gameId, newQuestion)
        }
    }
}


