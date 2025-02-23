package com.example.trickytaps

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MultiplayerViewModel : ViewModel() {
    private val _playerNames = MutableStateFlow(listOf<String>())
    val playerNames: StateFlow<List<String>> = _playerNames

    private val _scores = MutableStateFlow(mapOf<String, Int>())
    val scores: StateFlow<Map<String, Int>> = _scores

    private val _winCounts = MutableStateFlow(mapOf<String, Int>()) // Track Wins
    val winCounts: StateFlow<Map<String, Int>> = _winCounts

    private val _playerCount = MutableStateFlow(2) // Default to 2 players
    val playerCount: StateFlow<Int> = _playerCount

    fun setPlayers(names: List<String>) {
        _playerNames.value = names
        _scores.value = names.associateWith { 0 } // Reset scores
        _winCounts.value = names.associateWith { 0 } // Wins persist across rounds
        _playerCount.value = names.size
    }

    fun updateScore(playerName: String) { // Updates only in-game points
        _scores.value = _scores.value.mapValues {
            if (it.key == playerName) it.value + 10 else it.value
        }
    }

    fun updateWinCount() { // Updates winner count when round ends
        val maxScore = _scores.value.values.maxOrNull() ?: 0
        val winners = _scores.value.filter { it.value == maxScore }.keys

        _winCounts.value = _winCounts.value.mapValues {
            if (it.key in winners) it.value + 1 else it.value
        }
    }

    fun resetScores() { // Resets round scores, NOT total wins
        _scores.value = _playerNames.value.associateWith { 0 }
    }
}




