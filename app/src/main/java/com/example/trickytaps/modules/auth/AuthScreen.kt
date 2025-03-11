// AuthScreen.kt
package com.example.trickytaps.modules.auth

import android.content.Intent
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.trickytaps.R
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    val googleSignInClient = remember { getGoogleSignInClient(context) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!, navController, context)
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(true) {
        (context as? ComponentActivity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button (Aligned to Top Start)
        IconButton(
            onClick = {
                if (!isLogin) {
                    isLogin = true // Reset to login mode if in sign-up mode
                }
                else if (isLogin){
                    navController.navigate("landingPage")
                }
                else {
                    navController.popBackStack() // Go back only if already in login mode
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                Text(text = if (isLogin) "Login" else "Sign Up", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true, // Prevents multi-line expansion
                    modifier = Modifier
                        .fillMaxWidth(0.85f), // Limits width to 85% of the screen
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(12.dp))

                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    maxLines = 1,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide Password" else "Show Password"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.85f)
                )


                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            if (isLogin) {
                                loginUser(email, password, navController, context)
                            } else {
                                signUpUser(email, password, navController, context)
                            }
                        } else {
                            Toast.makeText(
                                context,
                                "Email and password cannot be empty",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text(text = if (isLogin) "Login" else "Sign Up")
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Google Sign-In Button
                if (isLogin) {
                    Button(
                        onClick = {
                            val googleSignInClient = getGoogleSignInClient(context)

                            // Sign out first to force account selection
                            googleSignInClient.signOut().addOnCompleteListener {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text(text = "Continue with Google")
                    }
                }
            }

            TextButton(
                onClick = { isLogin = !isLogin },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp) // Optional padding
            ) {
                Text(text = if (isLogin) "Create an account" else "Already have an account? Log in")
            }
        }
    }
}

fun signUpUser(email: String, password: String, navController: NavController, context: android.content.Context) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser!!.uid

                // Initialize both easy and hard high scores
                val newUser = mapOf(
                    "email" to email,
                    "easy" to 0,  // Default easy mode high score
                    "hard" to 0,  // Default hard mode high score
                    "score" to 0  // Multiplayer hard mode score
                )

                db.collection("users").document(userId)
                    .set(newUser, SetOptions.merge())
                    .addOnSuccessListener {
                        navController.navigate("usernameScreen/$userId")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Firestore error!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

fun loginUser(email: String, password: String, navController: NavController, context: android.content.Context) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()


    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser!!.uid

                // Retrieve username from Firestore
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val username = document.getString("username")
                        if (username != null) {
                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            navController.navigate("modeScreen/$username")
                        } else {
                            Toast.makeText(context, "Username not found, please set it up.", Toast.LENGTH_SHORT).show()
                            navController.navigate("usernameScreen/$userId")
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error retrieving username!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

@Composable
fun UsernameScreen(navController: NavController, userId: String) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }

    // List of adjectives and animals for fun username generation
    val adjectives = listOf("Wild", "Happy", "Mighty", "Sneaky", "Charming", "Brave", "Funky", "Silly")
    val animals = listOf("Lion", "Panda", "Tiger", "Wolf", "Penguin", "Elephant", "Koala", "Giraffe")
    val places = listOf("Atlantis", "Wonderland", "Narnia", "Eldorado", "Oz", "Hogwarts", "Neverland")

    // Function to generate a fun username
    fun generateFunUsername(): String {
        val randomAdjective = adjectives.random()
        val randomAnimal = animals.random()
        val randomPlace = places.random()
        val randomNumber = (100..999).random()

        // Combine them into a fun format
        return "$randomAdjective$randomAnimal$randomNumber"
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Back Button (Aligned to Top Start)
        IconButton(
            onClick = {
                navController.navigate("authScreen") {
                    popUpTo("landingPage") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Choose a Username", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    if (it.length <= 12 && it.matches(Regex("^[a-zA-Z0-9_]*$"))) { // Allow only letters, numbers & underscore
                        username = it
                    }
                },
                label = { Text("Username") },
                singleLine = true,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth(0.85f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Button to generate a fun random username
            Button(
                onClick = {
                    username = generateFunUsername() // Set the generated fun username
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "Generate Fun Username")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (username.isNotBlank()) {
                        db.collection("users").document(userId)
                            .set(mapOf("username" to username), SetOptions.merge())
                            .addOnSuccessListener {
                                Toast.makeText(context, "Username saved!", Toast.LENGTH_SHORT).show()
                                navController.navigate("modeScreen/$username")
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Error saving username!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Username cannot be empty!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = "Save Username")
            }
        }
    }
}

fun firebaseAuthWithGoogle(idToken: String, navController: NavController, context: android.content.Context) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    val userId = user.uid
                    val email = user.email ?: ""

                    // Retrieve user data from Firestore
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val username = document.getString("username")
                                // Check if the username exists
                                if (username != null) {
                                    // If the user is already registered and is on the multiplayer auth screen, navigate accordingly
                                    val currentDestination = navController.currentBackStackEntry?.destination?.route
                                    if (currentDestination?.contains("multiplayerAuthScreen") == true) {
                                        navController.navigate("onlineMultiplayerModeSelection") // Navigate to online multiplayer mode selection
                                    } else {
                                        navController.navigate("modeScreen/$username") // Navigate to mode screen if not on multiplayer auth
                                    }
                                } else {
                                    // If the username does not exist, navigate to multiplayer username screen
                                    navController.navigate("multiplayerUsernameScreen/$userId")
                                }
                            } else {
                                // If the user is not found in Firestore, create a new user and navigate to username screen
                                val newUser = mapOf(
                                    "email" to email,
                                    "easy" to 0,
                                    "hard" to 0
                                )

                                db.collection("users").document(userId)
                                    .set(newUser)
                                    .addOnSuccessListener {
                                        navController.navigate("multiplayerUsernameScreen/$userId")
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(context, "Firestore error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error retrieving user data: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            } else {
                Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

fun getGoogleSignInClient(context: android.content.Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // Request Web Client ID
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(context, gso)
}



