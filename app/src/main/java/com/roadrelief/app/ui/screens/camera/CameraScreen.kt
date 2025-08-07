package com.roadrelief.app.ui.screens.camera

import android.Manifest
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon // Icon is used by RoadReliefExtendedFAB
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // Ensure this import is present
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadrelief.app.ui.common.HandlePermissions
import com.roadrelief.app.ui.components.RoadReliefExtendedFAB
import com.roadrelief.app.ui.components.RoadReliefTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel = hiltViewModel()
) {
    // CRITICAL: Ensure CameraViewModel exposes these states:
    // val isCapturing: StateFlow<Boolean>
    // val capturedImageUri: StateFlow<Uri?>
    // val currentLocation: StateFlow<Location?>
    val isCapturing by viewModel.isCapturing.collectAsState()
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()

    HandlePermissions(permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraController = remember { LifecycleCameraController(context) }

        LaunchedEffect(Unit) {
            cameraController.bindToLifecycle(lifecycleOwner)
            cameraController.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
            cameraController.cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA
            cameraController.setEnabledUseCases(LifecycleCameraController.IMAGE_CAPTURE)
            cameraController.imageCaptureFlashMode = ImageCapture.FLASH_MODE_AUTO
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                RoadReliefTopAppBar(
                    title = "Take Evidence Photo",
                    canNavigateBack = true,
                    onNavigateUp = { navController.popBackStack() }
                )
            },
            floatingActionButton = {
                RoadReliefExtendedFAB(
                    text = if (isCapturing) "Capturing..." else "Take photo",
                    icon = Icons.Filled.Camera,
                    onClick = {
                        // The enabled parameter on RoadReliefExtendedFAB will handle preventing clicks
                        viewModel.takePhoto(cameraController)
                    },
                    enabled = !isCapturing, // Use the new enabled parameter
                    modifier = Modifier.padding(),
                    contentDescription = "Take photo"
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { previewContext ->
                        PreviewView(previewContext).apply {
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
                if (isCapturing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        LaunchedEffect(capturedImageUri, currentLocation) {
            val uri = capturedImageUri
            val location = currentLocation
            if (uri != null && location != null) {
                navController.previousBackStackEntry?.savedStateHandle?.set("photoUri", uri.toString())
                navController.previousBackStackEntry?.savedStateHandle?.set("latitude", location.latitude)
                navController.previousBackStackEntry?.savedStateHandle?.set("longitude", location.longitude)
                // Optional: viewModel.clearCapturedData() // To prevent re-triggering if screen recomposes before pop
                navController.popBackStack()
            }
        }
    }
}
