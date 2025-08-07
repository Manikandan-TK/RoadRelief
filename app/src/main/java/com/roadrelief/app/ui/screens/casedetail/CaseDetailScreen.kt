package com.roadrelief.app.ui.screens.casedetail

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.components.RoadReliefCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaseDetailScreen(
    viewModel: CaseDetailViewModel = hiltViewModel()
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        caseEntity?.let { case ->
            RoadReliefCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Case ID: ${case.id}")
                    Text(text = "Incident Date: ${SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(case.incidentDate))}")
                    Text(text = "Authority: ${case.authority}")
                    Text(text = "Description: ${case.description}")
                    Text(text = "Compensation: ${case.compensation}")
                    Text(text = "Status: ${case.status}")
                }
            }
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
                Spacer(modifier = Modifier.height(16.dp))
            }

            RoadReliefButton(
                onClick = { viewModel.generatePdfReport() },
                modifier = Modifier.fillMaxWidth(),
                text = "Generate PDF Report"
            )
        } ?: run {
            Text(text = "Case not found.")
        }
    }
}