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
        "Select the odd number",
        "Tap the largest shape",
        "Which one is not a fruit?",
        "Pick the word spelled incorrectly",
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
        "Which number is prime?",
        "Pick the animal that cannot fly",
        "Which planet is closest to the Sun?",
        "Choose the fastest mode of transportation",
        "What comes next? (2, 4, 8, 16, ?)",
        "Which one is a mammal?",
        "Which shape is a rectangle?",
        "Which word is a synonym for 'happy'?",
        "Which is an even number?",
        "What is the capital of France?",
        "Which is the tallest mountain?",
        "Pick the chemical symbol for gold",
        "Which organ pumps blood?",
        "Which day comes after Monday?",
        "What color is an emerald?",
        "Pick the smallest prime number",
        "How many sides does a hexagon have?",
        "Which month has 28 days?",
        "Which animal is a reptile?",
        "Find the hidden shape"
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
        "Select the odd number" -> {
            val numbers = listOf("12", "7", "4", "8")
            TrickQuestion(randomQuestion, numbers, "7", Color.Black)
        }
        "Tap the largest shape" -> {
            val shapes = listOf("Small Circle", "Large Square", "Medium Triangle", "Tiny Rectangle")
            TrickQuestion(randomQuestion, shapes, "Large Square", Color.Black)
        }
        "Which one is not a fruit?" -> {
            val items = listOf("Apple", "Banana", "Tomato", "Carrot")
            TrickQuestion(randomQuestion, items, "Carrot", Color.Black)
        }
        "Pick the word spelled incorrectly" -> {
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
        "Which is the tallest mountain?" -> TrickQuestion(
            "Which is the tallest mountain?",
            listOf("K2", "Kilimanjaro", "Everest", "Makalu"),
            "Everest",
            Color.Black
        )

        "Pick the chemical symbol for gold" -> TrickQuestion(
            "Pick the chemical symbol for gold",
            listOf("Go", "Au", "Ag", "Fe"),
            "Au",
            Color.Black
        )

        "Which organ pumps blood?" -> TrickQuestion(
            "Which organ pumps blood?",
            listOf("Brain", "Liver", "Heart", "Lungs"),
            "Heart",
            Color.Black
        )

        "Which day comes after Monday?" -> TrickQuestion(
            "Which day comes after Monday?",
            listOf("Sunday", "Wednesday", "Friday", "Tuesday"),
            "Tuesday",
            Color.Black
        )

        "What color is an emerald?" -> TrickQuestion(
            "What color is an emerald?",
            listOf("Red", "Green", "Blue", "Yellow"),
            "Green",
            Color.Black
        )

        "Pick the smallest prime number" -> TrickQuestion(
            "Pick the smallest prime number",
            listOf("0", "1", "2", "3"),
            "2",
            Color.Black
        )

        "How many sides does a hexagon have?" -> TrickQuestion(
            "How many sides does a hexagon have?",
            listOf("5", "6", "7", "8"),
            "6",
            Color.Black
        )

        "Which month has 28 days?" -> TrickQuestion(
            "Which month has 28 days?",
            listOf("February", "April", "All of them", "November"),
            "All of them",
            Color.Black
        )

        "Which animal is a reptile?" -> TrickQuestion(
            "Which animal is a reptile?",
            listOf("Snake", "Dog", "Whale", "Eagle"),
            "Snake",
            Color.Black
        )

        "Find the hidden shape" -> TrickQuestion(
            "Find the hidden shape",
            listOf("Circle", "Square", "Triangle", "Hexagon"),
            "Triangle",
            Color.Black
        )
        "Which number is prime?" -> TrickQuestion("Which number is prime?", listOf("15", "17", "21", "25"), "17", Color.Black)
        "Pick the animal that cannot fly" -> TrickQuestion("Pick the animal that cannot fly", listOf("Penguin", "Bat", "Owl", "Eagle"), "Penguin", Color.Black)
        "Which planet is closest to the Sun?" -> TrickQuestion("Which planet is closest to the Sun?", listOf("Venus", "Earth", "Mercury", "Mars"), "Mercury", Color.Black)
        "Choose the fastest mode of transportation" -> TrickQuestion("Choose the fastest mode of transportation", listOf("Train", "Plane", "Car", "Bicycle"), "Plane", Color.Black)
        "What comes next? (2, 4, 8, 16, ?)" -> TrickQuestion("What comes next? (2, 4, 8, 16, ?)", listOf("24", "32", "40", "48"), "32", Color.Black)
        "Which one is a mammal?" -> TrickQuestion("Which one is a mammal?", listOf("Shark", "Dolphin", "Octopus", "Crocodile"), "Dolphin", Color.Black)
        "Which shape is a rectangle?" -> TrickQuestion("Which shape is a rectangle?", listOf("Circle", "Triangle", "Square", "Parallelogram"), "Parallelogram", Color.Black)
        "Which word is a synonym for 'happy'?" -> TrickQuestion("Which word is a synonym for 'happy'?", listOf("Sad", "Angry", "Cheerful", "Sleepy"), "Cheerful", Color.Black)
        "Which is an even number?" -> TrickQuestion("Which is an even number?", listOf("3", "7", "10", "15"), "10", Color.Black)
        "What is the capital of France?" -> TrickQuestion("What is the capital of France?", listOf("Berlin", "Madrid", "Paris", "Rome"), "Paris", Color.Black)

        else -> TrickQuestion("Error loading question", listOf("N/A"), "N/A", Color.Black)
    }
}
