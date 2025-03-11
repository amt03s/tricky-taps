// OnlineMultiplayerViewmodel.kt
// OnlineMultiplayerViewModel.kt
package com.example.trickytaps.modules.multi.online

import android.util.Log
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
        Log.d("ViewModel", "createGame method in ViewModel triggered")
        val gameId = firebaseFirestoreService.createGame(playerName)

        _gameId.value = gameId

        // Initialize the game state with Player 1 (the creator)
        _gameState.value = GameState(
            gameId = gameId,
            players = mapOf(playerName to Player(name = playerName, score = 0, isReady = false)),
            status = "waiting",
            currentQuestion = null
        )

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
                    val status = doc.getString("status")
                    val currentQuestion = doc.get("currentQuestion") as? TrickQuestion

                    // Update local game state when Firestore status is ready
                    if (status == "ready") {
                        Log.d("GameState", "Both players are ready, game status: $status")
                        _gameState.value = _gameState.value?.copy(status = "ready", currentQuestion = currentQuestion)
                    } else {
                        // Update the game state without overriding the "ready" status if it's not yet "ready"
                        _gameState.value = _gameState.value?.copy(
                            players = players.mapValues { entry ->
                                Player(
                                    name = entry.key,
                                    score = entry.value["score"] as? Int ?: 0,
                                    isReady = entry.value["ready"] as? Boolean ?: false
                                )
                            },
                            currentQuestion = currentQuestion
                        )
                    }

                    // If second player joins, we need to trigger the update to reflect that
                    val secondPlayerName = players.keys.firstOrNull { it != _gameState.value?.players?.keys?.first() }
                    secondPlayerName?.let {
                        Log.d("GameState", "Second player joined: $it")
                        onSecondPlayerJoined(it) // Call the callback for second player
                    }
                }
            }
    }

    // Update player ready status and check if both players are ready
    fun updatePlayerReadyStatus(gameId: String, playerName: String, isReady: Boolean) {
        viewModelScope.launch {
            firebaseFirestoreService.updatePlayerReadyStatus(gameId, playerName, isReady)

            _gameState.value?.let { game ->
                val updatedPlayer = game.players[playerName]?.copy(isReady = isReady)
                if (updatedPlayer != null) {
                    val updatedPlayers = game.players.toMutableMap()
                    updatedPlayers[playerName] = updatedPlayer

                    // Check if both players are ready
                    val allReady = updatedPlayers.values.all { it.isReady }

                    // If both players are ready, set the status to "ready"
                    if (allReady) {
                        // Ensure the game status is updated to "ready"
                        updateGameStatusToReady(gameId)

                        // Now update local game state with "ready"
                        _gameState.value = game.copy(status = "ready", players = updatedPlayers)
                    } else {
                        _gameState.value = game.copy(players = updatedPlayers)
                    }
                }
            }
        }
    }

    // Update game status to "ready"
    fun updateGameStatusToReady(gameId: String) {
        viewModelScope.launch {
            firebaseFirestoreService.updateGameStatus(gameId, "ready")

            _gameState.value?.let { game ->
                _gameState.value = game.copy(status = "ready")
            }
        }
    }
}