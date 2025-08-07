package com.roadrelief.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
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
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            RoadReliefTopAppBar(
                title = "My Reports",
                canNavigateBack = false,
                onNavigateUp = {}
            )
        },
        floatingActionButton = {
            RoadReliefFAB(
                onClick = { navController.navigate(Screen.NewCase.route) },
                icon = Icons.Default.Add,
                contentDescription = "Add New Case"
            )
        },
        bottomBar = {
            BottomAppBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Reports") },
                    icon = { Icon(Icons.Outlined.Article, contentDescription = "Reports") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { navController.navigate(Screen.Profile.route) },
                    label = { Text("Profile") },
                    icon = { Icon(Icons.Outlined.AccountCircle, contentDescription = "Profile") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (cases.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = "No Reports",
                        tint = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "No Reports Yet",
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Tap the + button to create your first report.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn {
                    items(cases) { case ->
                        RoadReliefCard(
                            modifier = Modifier
                                .padding(vertical = 4.dp),
                            onClick = { navController.navigate(Screen.CaseDetail.createRoute(case.id)) } // Added onClick navigation
                        ) {
                            ListItem(
                                headlineContent = { Text("Case ID: ${case.id}") },
                                supportingContent = { Text("Status: ${case.status}") },
                                trailingContent = { Text(dateFormat.format(Date(case.incidentDate))) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
