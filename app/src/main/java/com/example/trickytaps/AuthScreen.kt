// AuthScreen.kt
package com.example.trickytaps

import android.content.Intent
import android.widget.Toast
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
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore

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
            firebaseAuthWithGoogle(account.idToken!!, navController)
        } catch (e: ApiException) {
            Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
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

                // Store email in Firestore before asking for a username
                val newUser = mapOf(
                    "email" to email,
                    "highScore" to 0 // Default high score
                )

                db.collection("users").document(userId)
                    .set(newUser) // Store email before username setup
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
                            navController.navigate("gameScreen/$username")
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
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Choose a Username", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            isError = errorMessage != null
        )

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (username.isBlank()) {
                    errorMessage = "Username cannot be empty!"
                } else {
                    checkUsernameAvailability(db, username, userId, navController) { isAvailable ->
                        if (isAvailable) {
                            saveUsername(db, username, userId, navController, context)
                        } else {
                            errorMessage = "Username already taken! Try another."
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text(text = "Save Username")
        }
    }
}

fun checkUsernameAvailability(
    db: FirebaseFirestore,
    username: String,
    userId: String,
    navController: NavController,
    callback: (Boolean) -> Unit
) {
    db.collection("users")
        .whereEqualTo("username", username)
        .get()
        .addOnSuccessListener { result ->
            callback(result.isEmpty) // If no results, username is available
        }
        .addOnFailureListener {
            callback(false) // Assume unavailable if query fails
        }
}

fun saveUsername(
    db: FirebaseFirestore,
    username: String,
    userId: String,
    navController: NavController,
    context: android.content.Context
) {
    db.collection("users").document(userId)
        .update("username", username)
        .addOnSuccessListener {
            Toast.makeText(context, "Username saved!", Toast.LENGTH_SHORT).show()
            navController.navigate("gameScreen/$username")
        }
        .addOnFailureListener {
            Toast.makeText(context, "Error saving username!", Toast.LENGTH_SHORT).show()
        }
}

fun firebaseAuthWithGoogle(idToken: String, navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val userId = user!!.uid
                val email = user.email ?: ""

                // Check if user already exists in Firestore
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // ✅ User exists, retrieve username and go to game
                            val username = document.getString("username") ?: "Player"
                            navController.navigate("gameScreen/$username")
                        } else {
                            // 🚀 New User: Store email, high score, and redirect to username setup
                            val newUser = mapOf(
                                "email" to email,
                                "highScore" to 0
                            )

                            db.collection("users").document(userId)
                                .set(newUser) // ✅ Store new user data
                                .addOnSuccessListener {
                                    navController.navigate("usernameScreen/$userId") // Redirect to username setup
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        println("Firestore error: $e")
                    }
            } else {
                println("Google sign-in failed: ${task.exception?.message}")
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



