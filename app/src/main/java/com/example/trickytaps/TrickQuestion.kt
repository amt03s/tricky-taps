// TrickQuestion.kt
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
        "Choose the **color**",
        "Which shape has more sides?",
        "What is 2 + 2? (Trick question)",
        "Which one is heavier?",
        "Pick the word that is a palindrome",
        "Which glass has more water?",
        "Tap the fastest animal",
        "Which letter comes next? (A, C, E, ?)",
        "Choose the correct spelling",
        "Which number is missing? (1, 3, 5, ?)",
        "Which is the coldest place?",
        "Select the word that is an antonym of 'hot'",
        "Pick the odd one out",
        "Find the hidden number",
        "Tap the longest word",
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

            val textWord = colorNames.random()
            var textColor: String

            do {
                textColor = colorNames.random()
            } while (textColor == textWord)

            TrickQuestion(
                question = "Choose the color of the text: **$textWord**",
                options = colorNames.shuffled(),
                correctAnswer = textColor,
                displayedColor = colorMap[textColor]!!
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

            val displayedWord = colorNames.random()
            var displayedColorName: String

            do {
                displayedColorName = colorNames.random()
            } while (displayedColorName == displayedWord)

            TrickQuestion(
                question = "Choose the color **$displayedWord**",
                options = colorNames.shuffled(),
                correctAnswer = displayedWord,
                displayedColor = colorMap[displayedColorName]!!
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
        "Which shape has more sides?" -> {
            val shapes = listOf("Square", "Triangle", "Pentagon", "Hexagon")
            TrickQuestion(randomQuestion, shapes, "Hexagon", Color.Black)
        }
        "What is 2 + 2? (Trick question)" -> {
            val answers = listOf("4", "Fish", "22", "5")
            TrickQuestion(randomQuestion, answers, "Fish", Color.Black)
        }
        "Which one is heavier?" -> {
            val weights = listOf("1 kg of feathers", "1 kg of bricks", "10 kg of paper", "5 kg of steel")
            TrickQuestion(randomQuestion, weights, "10 kg of paper", Color.Black)
        }
        "Pick the word that is a palindrome" -> {
            val words = listOf("Racecar", "Table", "Laptop", "Mirror")
            TrickQuestion(randomQuestion, words, "Racecar", Color.Black)
        }
        "Which glass has more water?" -> {
            val options = listOf("Tall Glass", "Wide Glass", "Same", "Short Glass")
            TrickQuestion(randomQuestion, options, "Same", Color.Black)
        }
        "Tap the fastest animal" -> {
            val options = listOf("Cheetah", "Falcon", "Horse", "Rabbit")
            TrickQuestion(randomQuestion, options, "Falcon", Color.Black)
        }
        "Which letter comes next? (A, C, E, ?)" -> {
            val options = listOf("F", "G", "H", "I")
            TrickQuestion(randomQuestion, options, "G", Color.Black)
        }
        "Choose the correct spelling" -> {
            val options = listOf("Definately", "Definitely", "Defanitely", "Defenitely")
            TrickQuestion(randomQuestion, options, "Definitely", Color.Black)
        }
        "Which number is missing? (1, 3, 5, ?)" -> {
            val options = listOf("6", "7", "8", "9")
            TrickQuestion(randomQuestion, options, "7", Color.Black)
        }
        "Which is the coldest place?" -> {
            val options = listOf("Antarctica", "Siberia", "Iceland", "Alaska")
            TrickQuestion(randomQuestion, options, "Antarctica", Color.Black)
        }
        "Select the word that is an antonym of 'hot'" -> {
            val options = listOf("Cold", "Warm", "Freezing", "Chilly")
            TrickQuestion(randomQuestion, options, "Cold", Color.Black)
        }
        "Pick the odd one out" -> {
            val options = listOf("Dog", "Cat", "Fish", "Cow")
            TrickQuestion(randomQuestion, options, "Fish", Color.Black)
        }
        "Find the hidden number" -> {
            val options = listOf("5", "7", "9", "11")
            TrickQuestion(randomQuestion, options, "7", Color.Black)
        }
        "Tap the longest word" -> {
            val options = listOf("Elephant", "Hippopotamus", "Butterfly", "Giraffe")
            TrickQuestion(randomQuestion, options, "Hippopotamus", Color.Black)
        }
        else -> TrickQuestion("Error loading question", listOf("N/A"), "N/A", Color.Black)
    }
}
