// Game.kt
package com.example.trickytaps.modules.multi.online

import com.example.trickytaps.TrickQuestion

data class Game(
    val gameId: String,
    val players: Map<String, Player>,
    val status: String, // "waiting", "in_progress", "finished"
    val currentQuestion: TrickQuestion? // It can be null when not set
)
