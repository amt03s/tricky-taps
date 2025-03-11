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
fun MultiplayerAuthScreen(navController: NavController) {
    val context = LocalContext.current
    val googleSignInClient = remember { getMultiplayerGoogleSignInClient(context) }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        IconButton(
            onClick = {
                if (!isLogin) {
                    isLogin = true // Reset to login mode if in sign-up mode
                } else if (isLogin) {
                    navController.navigate("landingPage")
                } else {
                    navController.popBackStack() // Go back only if already in login mode
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }

        // Center the content vertically and horizontally
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .align(Alignment.Center)  // Center the Column inside the Box
        ) {
            Text(
                text = if (isLogin) "Multiplayer Login" else "Multiplayer Sign Up",
                fontSize = 24.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.85f),
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
                            multiplayerLoginUser(email, password, navController, context)
                        } else {
                            multiplayerSignUpUser(email, password, navController, context)
                        }
                    } else {
                        Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text(text = if (isLogin) "Login" else "Sign Up")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLogin) {
                Button(
                    onClick = {
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

        // Correctly align the TextButton to the bottom of the screen by separating it
        TextButton(
            onClick = { isLogin = !isLogin },
            modifier = Modifier
                .align(Alignment.BottomCenter)  // Align it to the bottom center
                .padding(bottom = 20.dp)
        ) {
            Text(text = if (isLogin) "Create an account" else "Already have an account? Log in")
        }
    }
}

fun multiplayerSignUpUser(email: String, password: String, navController: NavController, context: android.content.Context) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser!!.uid

                val newUser = mapOf(
                    "email" to email,
                    "easy" to 0,  // Default easy mode high score
                    "hard" to 0,   // Default hard mode high score
                    "score" to 0  // Multiplayer hard mode score
                )

                db.collection("users").document(userId)
                    .set(newUser, SetOptions.merge())
                    .addOnSuccessListener {
                        navController.navigate("OnlineMultiplayerModeSelectionScreen")
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Firestore error!", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

fun multiplayerLoginUser(email: String, password: String, navController: NavController, context: android.content.Context) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser!!.uid

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        val username = document.getString("username")
                        if (username != null) {
                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            navController.navigate("OnlineMultiplayerModeSelectionScreen")
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

fun getMultiplayerGoogleSignInClient(context: android.content.Context): GoogleSignInClient {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)) // Request Web Client ID
        .requestEmail()
        .build()

    return GoogleSignIn.getClient(context, gso)
}
