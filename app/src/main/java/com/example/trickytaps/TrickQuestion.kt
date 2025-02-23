package com.example.trickytaps

import androidx.compose.ui.graphics.Color

data class TrickQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val displayedColor: Color
)

fun generateTrickQuestion(): TrickQuestion {
    val questions = listOf(
        "Choose the **color** of the text",
        "Select the **odd** number",
        "Tap the **largest** shape",
        "Which one is **not** a fruit?",
        "Pick the word spelled **incorrectly**",
        "Choose the **color**"
    )

    val randomQuestion = questions.random()

    return when (randomQuestion) {
        "Choose the **color** of the text" -> {
            val colorNames = listOf("Red", "Blue", "Green", "Yellow")
            val colorMap = mapOf(
                "Red" to Color.Red,
                "Blue" to Color.Blue,
                "Green" to Color.Green,
                "Yellow" to Color.Yellow
            )

            val textWord = colorNames.random() // This is the displayed word
            var textColor: String

            do {
                textColor = colorNames.random() // This is the actual text color
            } while (textColor == textWord) // Ensure they are different

            TrickQuestion(
                question = "Choose the color of the text: **$textWord**",
                options = colorNames.shuffled(),
                correctAnswer = textColor, // The correct answer is the actual color
                displayedColor = colorMap[textColor]!! // Get the actual color value
            )
        }
        "Choose the **color**" -> {
            val colorNames = listOf("Red", "Blue", "Green", "Yellow")
            val colorMap = mapOf(
                "Red" to Color.Red,
                "Blue" to Color.Blue,
                "Green" to Color.Green,
                "Yellow" to Color.Yellow
            )

            val displayedWord = colorNames.random() // The misleading word (e.g., "Green")
            var displayedColorName: String

            do {
                displayedColorName = colorNames.random() // The actual displayed color (e.g., Yellow)
            } while (displayedColorName == displayedWord) // Ensure they are different

            TrickQuestion(
                question = "Choose the color **$displayedWord**",
                options = colorNames.shuffled(),
                correctAnswer = displayedWord, // Correct answer is the displayed word (not the color)
                displayedColor = colorMap[displayedColorName]!! // The actual color applied to text
            )
        }
        "Select the **odd** number" -> {
            val numbers = listOf("12", "7", "4", "8")
            TrickQuestion(randomQuestion, numbers, "7", Color.Black)
        }
        "Tap the **largest** shape" -> {
            val shapes = listOf("Small Circle", "Large Square", "Medium Triangle", "Tiny Rectangle")
            TrickQuestion(randomQuestion, shapes, "Large Square", Color.Black)
        }
        "Which one is **not** a fruit?" -> {
            val items = listOf("Apple", "Banana", "Tomato", "Carrot")
            TrickQuestion(randomQuestion, items, "Carrot", Color.Black)
        }
        "Pick the word spelled **incorrectly**" -> {
            val words = listOf("Recieve", "Receive", "Accomodate", "Accommodate")
            TrickQuestion(randomQuestion, words, "Recieve", Color.Black)
        }
        else -> TrickQuestion("Error loading question", listOf("N/A"), "N/A", Color.Black)
    }
}
