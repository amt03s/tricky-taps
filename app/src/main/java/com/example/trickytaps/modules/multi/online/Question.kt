package com.example.trickytaps.modules.multi.online

data class Question(
    val question: String = "",
    val options: List<String> = listOf(),
    val correctAnswer: String = ""
)