package com.roadrelief.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.roadrelief.app.ui.components.RoadReliefCard
import com.roadrelief.app.ui.components.RoadReliefFAB
import com.roadrelief.app.ui.components.RoadReliefTopAppBar
import com.roadrelief.app.ui.nav.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cases by viewModel.cases.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.US) }

    Scaffold(
        topBar = {
            RoadReliefTopAppBar(
                title = "RoadRelief Cases",
                canNavigateBack = false, // Added
                onNavigateUp = {} // Added
            )
        },
        floatingActionButton = {
            RoadReliefFAB(
                onClick = { navController.navigate(Screen.NewCase.route) },
                icon = Icons.Default.Add,
                contentDescription = "Add New Case"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (cases.isEmpty()) {
                Text("No cases found. Click the + button to add a new case.")
            } else {
                LazyColumn {
                    items(cases) { case ->
                        RoadReliefCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                            // Removed onClick from RoadReliefCard itself
                        ) {
                            ListItem(
                                headlineContent = { Text("Case ID: ${case.id}") },
                                supportingContent = { Text("Status: ${case.status}") },
                                trailingContent = { Text(dateFormat.format(Date(case.incidentDate))) },
                                modifier = Modifier.clickable { // Click listener is on ListItem
                                    navController.navigate(Screen.CaseDetail.createRoute(case.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}