package com.example.trickytaps

data class TrickQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)

fun generateTrickQuestion(): TrickQuestion {
    val questions = listOf(
        "Choose the **color** of the text",
        "Select the **odd** number",
        "Tap the **largest** shape",
        "Which one is **not** a fruit?",
        "Pick the word spelled **incorrectly**"
    )

    val randomQuestion = questions.random()

    return when (randomQuestion) {
        "Choose the **color** of the text" -> {
            val colors = listOf("Red", "Blue", "Green", "Yellow")
            val displayedColor = colors.random()
            val correctAnswer = displayedColor
            TrickQuestion("Choose the color: **$displayedColor**", colors.shuffled(), correctAnswer)
        }
        "Select the **odd** number" -> {
            val numbers = listOf("12", "7", "4", "8")
            TrickQuestion(randomQuestion, numbers, "7")
        }
        "Tap the **largest** shape" -> {
            val shapes = listOf("Small Circle", "Large Square", "Medium Triangle", "Tiny Rectangle")
            TrickQuestion(randomQuestion, shapes, "Large Square")
        }
        "Which one is **not** a fruit?" -> {
            val items = listOf("Apple", "Banana", "Tomato", "Carrot")
            TrickQuestion(randomQuestion, items, "Carrot")
        }
        "Pick the word spelled **incorrectly**" -> {
            val words = listOf("Recieve", "Receive", "Accomodate", "Accommodate")
            TrickQuestion(randomQuestion, words, "Recieve")
        }
        else -> TrickQuestion("Error loading question", listOf("N/A"), "N/A")
    }
}
