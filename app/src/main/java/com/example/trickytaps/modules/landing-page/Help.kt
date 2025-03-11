// Help.kt
package com.example.trickytaps.modules.`landing-page`

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.trickytaps.R

@Composable
fun Help(navController: NavController){
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        IconButton(
            onClick = {
                navController.navigate("landingPage") {
                    popUpTo("landingPage") { inclusive = true }
                }
            },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(50.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            )  {
                Text(text = "How to Play\n", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.help)
                        .decoderFactory(ImageDecoderDecoder.Factory())
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit, // Prevent cropping
                    modifier = Modifier
                        .fillMaxWidth() // Stretches to fill the width without cropping
                        .aspectRatio(8f / 9f) // Adjust aspect ratio to fit the content properly
                )

                Text(
                    text = "\n1. Tap the box of your choice to answer before time runs out.\n" +
                            "\n" +
                            "2. Earn points by answering correctly and quickly.\n" +
                            "\n" +
                            "3. Compete with friends or aim for the high score in this fast-paced challenge!"
                )
            }
        }
    }
}
