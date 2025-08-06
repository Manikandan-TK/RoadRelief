package com.roadrelief.app.ui.screens.camera

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = remember { LifecycleCameraController(context) }

    LaunchedEffect(Unit) {
        cameraController.bindToLifecycle(lifecycleOwner)
        cameraController.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
        cameraController.cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
        cameraController.setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE or LifecycleCameraController.IMAGE_ANALYSIS)
        cameraController.imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Take photo") },
                icon = { Icon(Icons.Filled.Camera, contentDescription = "Take photo") },
                onClick = {
                    val outputDirectory: File = context.filesDir
                    val executor: Executor = ContextCompat.getMainExecutor(context)
                    viewModel.takePhoto(cameraController, outputDirectory, executor)
                },
                modifier = Modifier.padding()
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PreviewView(it).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setBackgroundColor(android.graphics.Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        previewView.controller = cameraController
                    }
                }
            )
        }
    }

    LaunchedEffect(viewModel.capturedImageUri.value, viewModel.currentLocation.value) {
        val uri = viewModel.capturedImageUri.value
        val location = viewModel.currentLocation.value
        if (uri != null && location != null) {
            navController.previousBackStackEntry?.savedStateHandle?.set("photoUri", uri.toString())
            navController.previousBackStackEntry?.savedStateHandle?.set("latitude", location.latitude)
            navController.previousBackStackEntry?.savedStateHandle?.set("longitude", location.longitude)
            navController.popBackStack()
        }
    }
}