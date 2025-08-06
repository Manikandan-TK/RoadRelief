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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("RoadRelief Cases") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.NewCase.route) }) {
                Icon(Icons.Default.Add, contentDescription = "Add New Case")
            }
        }
    ) {
        paddingValues ->
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
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { navController.navigate(Screen.CaseDetail.createRoute(case.id)) },
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text("Case ID: ${case.id}") },
                                supportingContent = { Text("Status: ${case.status}") },
                                trailingContent = { Text(SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(case.incidentDate)))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}