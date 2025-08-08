package com.roadrelief.app.ui.screens.newcase

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.nav.Screen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCaseScreen(
    navController: NavController,
    viewModel: NewCaseViewModel = hiltViewModel()
) {
    val incidentDate by viewModel.incidentDate.collectAsState()
    val roadConditionDescription by viewModel.roadConditionDescription.collectAsState()
    val vehicleDamageDescription by viewModel.vehicleDamageDescription.collectAsState()
    val compensation by viewModel.compensation.collectAsState()
    val evidenceList by viewModel.evidenceList.collectAsState()
    val userNamePlaceholder by viewModel.userNamePlaceholder.collectAsState()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsState()
    val selectedAuthority by viewModel.selectedAuthority.collectAsState()
    val authorityOptions by viewModel.authorityOptions.collectAsState()
    val customAuthority by viewModel.customAuthority.collectAsState()
    val incidentLatitude by viewModel.incidentLatitude.collectAsState()
    val incidentLongitude by viewModel.incidentLongitude.collectAsState()
    val incidentAddress by viewModel.incidentAddress.collectAsState()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.onGetLocationClicked()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

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
            val photoLatitude = handle.get<Double>("latitude")
            val photoLongitude = handle.get<Double>("longitude")

            if (photoUri != null && photoLatitude != null && photoLongitude != null) {
                viewModel.addEvidence(photoUri, photoLatitude, photoLongitude)
                handle.remove<String>("photoUri")
                handle.remove<Double>("latitude")
                handle.remove<Double>("longitude")
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is NewCaseViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Damage Claim") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                RoadReliefButton(
                    onClick = {
                        viewModel.saveCase()
                        navController.popBackStack()
                    },
                    text = "Save Claim",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
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
            Spacer(modifier = Modifier.height(16.dp))
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(modifier = Modifier.clickable { datePickerDialog.show() }) {
                        OutlinedTextField(
                            value = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(incidentDate)),
                            onValueChange = { },
                            label = { Text("Incident Date") },
                            readOnly = true,
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ),
                            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Date Picker") }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            })
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Location Icon", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Incident Location", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (incidentLatitude != null && incidentLongitude != null) {
                                    String.format(Locale.US, "Lat: %.5f, Lon: %.5f", incidentLatitude, incidentLongitude)
                                } else {
                                    "Tap to get location"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (incidentLatitude != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            incidentAddress?.let {
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        if (isDetectingLocation) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 16.dp))

                    var authorityExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = authorityExpanded,
                        onExpandedChange = {
                            authorityExpanded = !authorityExpanded && (authorityOptions.isNotEmpty() || !isDetectingLocation)
                        }
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            value = selectedAuthority.ifEmpty { if (isDetectingLocation) "Detecting..." else "Select Authority" },
                            onValueChange = {},
                            label = { Text("Responsible Authority") },
                            trailingIcon = {
                                if (isDetectingLocation && selectedAuthority.isEmpty()) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = authorityExpanded)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(),
                        )
                        ExposedDropdownMenu(
                            expanded = authorityExpanded,
                            onDismissRequest = { authorityExpanded = false },
                        ) {
                            authorityOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        viewModel.onAuthorityChange(selectionOption)
                                        authorityExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }
            }

            if (selectedAuthority == "Other") {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customAuthority,
                    onValueChange = viewModel::onCustomAuthorityChange,
                    label = { Text("Please specify the authority") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(16.dp))


            OutlinedTextField(
                value = roadConditionDescription,
                onValueChange = viewModel::onRoadConditionDescriptionChange,
                label = { Text("Road Condition Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = vehicleDamageDescription,
                onValueChange = viewModel::onVehicleDamageDescriptionChange,
                label = { Text("Vehicle Damage Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = compensation,
                onValueChange = viewModel::onCompensationChange,
                label = { Text("Compensation Amount Requested") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Photo Evidence", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.navigate(Screen.Camera.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = "Add Photo")
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("Add Photo from Camera")
            }
            Text(
                text = "Location for photos is recorded automatically with each photo. This is separate from the incident location above.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(evidenceList) { evidence ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = evidence.photoUri.toUri()),
                            contentDescription = "Evidence Photo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
