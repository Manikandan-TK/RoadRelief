package com.roadrelief.app.ui.screens.profile

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.components.RoadReliefTopAppBar
import com.roadrelief.app.ui.nav.Screen
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    sharedPreferences: SharedPreferences, // Re-added this parameter
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation event
    LaunchedEffect(Unit) {
        viewModel.navigateToHome.collectLatest {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            RoadReliefTopAppBar(
                title = "Profile",
                canNavigateBack = navController.previousBackStackEntry != null,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from Scaffold
                .padding(16.dp) // Keep original content padding
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.vehicleNumber,
                onValueChange = { viewModel.onVehicleNumberChange(it) },
                label = { Text("Vehicle Number") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            RoadReliefButton(
                onClick = { viewModel.saveProfile() },
                modifier = Modifier.fillMaxWidth(),
                text = "Save Profile"
            )
        }
    }
}