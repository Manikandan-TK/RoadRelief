package com.roadrelief.app.ui.screens.casedetail

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.components.RoadReliefCard
import com.roadrelief.app.ui.nav.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaseDetailScreen(
    navController: NavController, // Added NavController
    viewModel: CaseDetailViewModel = hiltViewModel()
    // Removed onBackClicked, will use navController
) {
    val caseWithEvidence by viewModel.caseWithEvidence.collectAsState()
    val shareIntent by viewModel.shareIntent.collectAsState()
    val caseEntity = caseWithEvidence?.first
    val evidenceList = caseWithEvidence?.second ?: emptyList()
    val context = LocalContext.current

    LaunchedEffect(shareIntent) {
        shareIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.onShareIntentHandled()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Claim Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Use NavController
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            caseEntity?.let {
                Surface(shadowElevation = 4.dp) { // Add elevation for separation
                    RoadReliefButton(
                        onClick = { viewModel.generatePdfReport() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        text = "Generate PDF Report"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            caseEntity?.let { case ->
                Spacer(modifier = Modifier.height(16.dp))

                RoadReliefCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow(label = "Responsible Authority", value = case.authority)
                        DetailRow(label = "Incident Date", value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(case.incidentDate)))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status", style = MaterialTheme.typography.bodyMedium)
                            AssistChip(onClick = { /* No action needed */ }, label = { Text(case.status) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                RoadReliefCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Road Condition Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = case.description,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Text(
                            text = "Vehicle Damage Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = case.vehicleDamageDescription,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Text(
                            text = "Requested Compensation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹ ${case.compensation}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (evidenceList.isNotEmpty()) {
                    Text(
                        "Evidence Photos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tap a photo to view full screen.",
                         style = MaterialTheme.typography.bodySmall,
                         modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(evidenceList) { evidence ->
                            Image(
                                painter = rememberAsyncImagePainter(Uri.parse(evidence.photoUri)),
                                contentDescription = "Evidence Photo",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clickable { navController.navigate(Screen.FullScreenImage.createRoute(evidence.photoUri)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // Space before the bottom bar
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Case not found.")
                }
            }
            Spacer(modifier = Modifier.height(72.dp)) // Extra space to ensure content is above the bottom bar
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
