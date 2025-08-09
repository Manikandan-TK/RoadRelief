package com.roadrelief.app.ui.screens.fullscreenimage

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageScreen(
    navController: NavController,
    imageUriString: String?
) {
    val decodedUri = imageUriString?.let {
        try {
            Uri.parse(URLDecoder.decode(it, "UTF-8"))
        } catch (e: Exception) {
            // Fallback to parsing without decoding if it fails
            Uri.parse(it)
        }
    }

    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { /* No title needed for full screen image view */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White // Ensure icon is visible on dark background
                        )
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f) // Semi-transparent app bar
                )
            )
        },
        containerColor = Color.Black // Background for the image
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .clickable { navController.popBackStack() }, // Optional: click anywhere on image to go back
            contentAlignment = Alignment.Center
        ) {
            if (decodedUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = decodedUri),
                    contentDescription = "Full Screen Evidence Photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
