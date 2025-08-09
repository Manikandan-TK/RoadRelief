package com.roadrelief.app.ui.screens.casedetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.roadrelief.app.ui.components.RoadReliefTopAppBar
import com.roadrelief.app.ui.nav.Screen
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaseDetailScreen(
    navController: NavController,
    viewModel: CaseDetailViewModel = hiltViewModel()
) {
    val caseWithEvidence by viewModel.caseWithEvidence.collectAsState()
    val shareIntent by viewModel.shareIntent.collectAsState()
    val pdfGenerationState by viewModel.pdfGenerationState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(shareIntent) {
        shareIntent?.let { intent ->
            context.startActivity(intent)
            viewModel.onShareIntentHandled()
        }
    }

    LaunchedEffect(pdfGenerationState) {
        val state = pdfGenerationState
        if (state is PdfGenerationState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    actionLabel = "Dismiss"
                )
                viewModel.dismissPdfError()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            RoadReliefTopAppBar(
                title = "Case Details",
                canNavigateBack = true,
                onNavigateUp = { navController.navigateUp() }
            )
        },
        bottomBar = {
            BottomAppBar {
                Button(
                    onClick = { viewModel.generatePdfReport() },
                    enabled = pdfGenerationState !is PdfGenerationState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    if (pdfGenerationState is PdfGenerationState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Generate PDF Report")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                caseWithEvidence?.let { (case, evidence) ->
                    case?.let {
                        DetailCard("Incident Details") {
                            DetailItem("Incident Date", it.incidentDate.formatDate())
                            HorizontalDivider()
                            DetailItem("Road Authority", it.authority)
                            it.incidentLatitude?.let { lat ->
                                it.incidentLongitude?.let { lon ->
                                    HorizontalDivider()
                                    DetailItem("Incident Location", "Lat: ${"%.5f".format(lat)}, Lon: ${"%.5f".format(lon)}")
                                }
                            }
                        }
                    }
                }
            }

            item {
                caseWithEvidence?.let { (case, _) ->
                    case?.let {
                        DetailCard("Claim Details") {
                            DetailItem("Road Condition", it.description)
                            HorizontalDivider()
                            DetailItem("Vehicle Damage", it.vehicleDamageDescription)
                            HorizontalDivider()
                            DetailItem("Compensation", "₹ ${String.format(Locale.getDefault(), "%,.2f", it.compensation)}")
                        }
                    }
                }
            }

            item {
                caseWithEvidence?.let { (_, evidence) ->
                    if (evidence.isNotEmpty()) {
                        DetailCard("Evidence") {
                            LazyRow(
                                contentPadding = PaddingValues(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(evidence) { photo ->
                                    val encodedUrl = URLEncoder.encode(photo.photoUri, "UTF-8")
                                    AsyncImage(
                                        model = photo.photoUri,
                                        contentDescription = "Evidence Photo",
                                        modifier = Modifier
                                            .size(150.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                navController.navigate(Screen.FullScreenImage.createRoute(encodedUrl))
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                 }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun Long.formatDate(): String {
    val sdf = SimpleDateFormat("dd MMMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(this))
}
