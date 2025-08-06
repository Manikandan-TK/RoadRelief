package com.roadrelief.app.ui.screens.newcase

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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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

        Button(
            onClick = { navController.navigate(Screen.Camera.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Evidence")
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

        Button(
            onClick = {
                viewModel.saveCase()
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Case")
        }
    }
}