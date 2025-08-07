package com.roadrelief.app.ui.common

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.util.PermissionManager

@Composable
fun HandlePermissions(
    permissions: List<String>,
    content: @Composable () -> Unit
) {
    val activity = LocalContext.current as Activity
    var permissionsGranted by remember { mutableStateOf(PermissionManager.arePermissionsGranted(activity, permissions)) }
    var shouldShowRationale by remember { mutableStateOf(PermissionManager.shouldShowRequestPermissionRationale(activity, permissions)) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissionsMap ->
        if (permissionsMap.values.all { it }) {
            permissionsGranted = true
        } else {
            shouldShowRationale = PermissionManager.shouldShowRequestPermissionRationale(activity, permissions)
            if (!shouldShowRationale) {
                permanentlyDenied = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!permissionsGranted) {
            launcher.launch(permissions.toTypedArray())
        }
    }

    when {
        permissionsGranted -> content()
        shouldShowRationale -> RationaleUI(onConfirm = { launcher.launch(permissions.toTypedArray()) })
        permanentlyDenied -> PermanentlyDeniedUI()
        else -> {
            // This is a fallback, in a real app you might want to show a loading indicator
            // or some other placeholder UI.
        }
    }
}

@Composable
private fun RationaleUI(onConfirm: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "We need permissions to use this feature. Please grant them to continue.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoadReliefButton(onClick = onConfirm, text = "Grant Permissions")
    }
}

@Composable
private fun PermanentlyDeniedUI() {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You have permanently denied the required permissions. Please enable them in the app settings to use this feature.",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        RoadReliefButton(onClick = {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }, text = "Open Settings")
    }
}