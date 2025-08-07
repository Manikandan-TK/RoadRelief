package com.roadrelief.app.ui.screens.newcase

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.nav.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NewCaseScreen(
    navController: NavController,
    viewModel: NewCaseViewModel = hiltViewModel()
) {
    val incidentDate by viewModel.incidentDate.collectAsState()
    val authority by viewModel.authority.collectAsState()
    val description by viewModel.description.collectAsState()
    val compensation by viewModel.compensation.collectAsState()
    val evidenceList by viewModel.evidenceList.collectAsState()

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.let { handle ->
            val photoUri = handle.get<String>("photoUri")
            val latitude = handle.get<Double>("latitude")
            val longitude = handle.get<Double>("longitude")

            if (photoUri != null && latitude != null && longitude != null) {
                viewModel.addEvidence(photoUri, latitude, longitude)
                // Clear the saved state handle to avoid re-triggering
                handle.remove<String>("photoUri")
                handle.remove<Double>("latitude")
                handle.remove<Double>("longitude")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars) // Apply status bar insets first
            .padding(16.dp) // Then apply uniform content padding
    ) {
        OutlinedTextField(
            value = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(incidentDate)),
            onValueChange = { /* Not directly editable */ },
            label = { Text("Incident Date") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = authority,
            onValueChange = { viewModel.onAuthorityChange(it) },
            label = { Text("Authority") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.onDescriptionChange(it) },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = compensation,
            onValueChange = { viewModel.onCompensationChange(it) },
            label = { Text("Compensation") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        RoadReliefButton(
            onClick = { navController.navigate(Screen.Camera.route) },
            modifier = Modifier.fillMaxWidth(),
            text = "Add Evidence"
        )
        // Increased spacer for better separation
        Spacer(modifier = Modifier.height(16.dp))

        if (evidenceList.isNotEmpty()) {
            Text("Evidence Photos:")
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow {
                items(evidenceList) {
                    Image(
                        painter = rememberAsyncImagePainter(Uri.parse(it.photoUri)),
                        contentDescription = null,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp)) // Spacer after evidence, before Save
        }

        RoadReliefButton(
            onClick = {
                viewModel.saveCase()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            text = "Save Case"
        )
    }
}