// GameState.kt
package com.example.trickytaps.modules.multi.online

import com.example.trickytaps.TrickQuestion

data class GameState(
    val gameId: String = "",
    val status: String = "",
    val players: Map<String, Player> = mapOf(),
    val currentQuestion: TrickQuestion? = null  // Changed to TrickQuestion
)

