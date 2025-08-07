package com.roadrelief.app.ui.screens.newcase

import android.app.DatePickerDialog
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.nav.Screen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCaseScreen(
    navController: NavController,
    viewModel: NewCaseViewModel = hiltViewModel()
) {
    val incidentDate by viewModel.incidentDate.collectAsState()
    val authority by viewModel.authority.collectAsState()
    val roadConditionDescription by viewModel.roadConditionDescription.collectAsState()
    val vehicleDamageDescription by viewModel.vehicleDamageDescription.collectAsState()
    val compensation by viewModel.compensation.collectAsState()
    val evidenceList by viewModel.evidenceList.collectAsState()
    val userNamePlaceholder by viewModel.userNamePlaceholder.collectAsState() // Collect user name placeholder

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            viewModel.onIncidentDateChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.let { handle ->
            val photoUri = handle.get<String>("photoUri")
            val latitude = handle.get<Double>("latitude")
            val longitude = handle.get<Double>("longitude")

            if (photoUri != null && latitude != null && longitude != null) {
                viewModel.addEvidence(photoUri, latitude, longitude)
                handle.remove<String>("photoUri")
                handle.remove<Double>("latitude")
                handle.remove<Double>("longitude")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Damage Claim") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Added for scrollability
        ) {
            // Reminder Text if profile is incomplete
            if (userNamePlaceholder == "[Your Name]") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Your profile is incomplete. Please fill it out from the Home screen's Profile tab for accurate claim details.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Box(modifier = Modifier.clickable { datePickerDialog.show() }) {
                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(incidentDate)),
                    onValueChange = { },
                    label = { Text("Incident Date") },
                    readOnly = true,
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            Box {
                OutlinedTextField(
                    value = authority,
                    onValueChange = { }, // Not directly editable
                    label = { Text("Responsible Authority") },
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    viewModel.authorities.forEach { auth ->
                        DropdownMenuItem(
                            text = { Text(auth) },
                            onClick = {
                                viewModel.onAuthorityChange(auth)
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = roadConditionDescription,
                onValueChange = { viewModel.onRoadConditionDescriptionChange(it) },
                label = { Text("Describe the road condition (e.g., deep pothole, debris)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = vehicleDamageDescription,
                onValueChange = { viewModel.onVehicleDamageDescriptionChange(it) },
                label = { Text("Describe the damage to your vehicle") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = compensation,
                onValueChange = { viewModel.onCompensationChange(it) },
                label = { Text("Requested Compensation Amount (₹)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Photo Evidence", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "Important: Add photos of the road hazard AND the damage to your vehicle.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(evidenceList) { evidence ->
                        Image(
                            painter = rememberAsyncImagePainter(Uri.parse(evidence.photoUri)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                        .clickable { navController.navigate(Screen.Camera.route) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = "Add Photo")
                        Text("Add Photo")
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    "Location is recorded automatically with your first photo.",
                    style = MaterialTheme.typography.bodySmall
                )
            }


            Spacer(modifier = Modifier.weight(1f, fill = false)) // Adjusted weight for scrollable content

            RoadReliefButton(
                onClick = {
                    viewModel.saveCase()
                    navController.popBackStack() // Navigate back after saving
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp), // Added padding for better spacing at the bottom
                text = "Save Claim"
            )
        }
    }
}
