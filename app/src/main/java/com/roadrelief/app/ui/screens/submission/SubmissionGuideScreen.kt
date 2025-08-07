package com.roadrelief.app.ui.screens.submission

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.components.RoadReliefTopAppBar

@Composable
fun SubmissionGuideScreen(navController: NavController) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            RoadReliefTopAppBar(
                title = "Submission Guide",
                canNavigateBack = true,
                onNavigateUp = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Follow these steps to file your claim:",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            GuideStep(
                icon = Icons.AutoMirrored.Outlined.ListAlt,
                title = "1. Gather Documents",
                description = "Collect your generated PDF report, photos, and other supporting evidence."
            )
            GuideStep(
                icon = Icons.Filled.Create,
                title = "2. Visit the E-Daakhil Portal",
                description = "Visit the official portal to file your complaint. A stable internet connection is recommended."
            )
            GuideStep(
                icon = Icons.Filled.Info,
                title = "3. Fill Out the Online Form",
                description = "Accurately fill out the online form and attach all required documents."
            )
            GuideStep(
                icon = Icons.Filled.CheckCircle,
                title = "4. Keep Your Complaint Number",
                description = "Save your complaint number for future reference and tracking."
            )

            Spacer(modifier = Modifier.height(24.dp))

            RoadReliefButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, "https://edaakhil.nic.in/".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
                text = "Go to E-Daakhil Portal"
            )
        }
    }
}

@Composable
private fun GuideStep(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
