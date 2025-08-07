package com.roadrelief.app.ui.screens.newcase

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.roadrelief.app.ui.components.RoadReliefButton
import com.roadrelief.app.ui.nav.Screen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission") // Permissions handled by launcher
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
    val userNamePlaceholder by viewModel.userNamePlaceholder.collectAsState()
    val incidentLatitude by viewModel.incidentLatitude.collectAsState()
    val incidentLongitude by viewModel.incidentLongitude.collectAsState()

    var isFetchingLocation by remember { mutableStateOf(false) }


    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    // Initialize FusedLocationProviderClient
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            isFetchingLocation = true
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token // Used to cancel the request if needed
            ).addOnSuccessListener { location ->
                if (location != null) {
                    viewModel.onIncidentLocationChange(location.latitude, location.longitude)
                }
                isFetchingLocation = false
            }.addOnFailureListener {
                isFetchingLocation = false
                // TODO: Show a Toast or Snackbar message to the user about the failure
            }
        } else {
            // TODO: Handle permission denial (e.g., show a Snackbar or guide user to settings)
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
            val photoLatitude = handle.get<Double>("latitude") // Renamed to avoid confusion
            val photoLongitude = handle.get<Double>("longitude") // Renamed to avoid confusion

            if (photoUri != null && photoLatitude != null && photoLongitude != null) {
                viewModel.addEvidence(photoUri, photoLatitude, photoLongitude)
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .verticalScroll(rememberScrollState())
        ) {
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

            // Incident Date Picker
            Box(modifier = Modifier.clickable { datePickerDialog.show() }) {
                OutlinedTextField(
                    value = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(incidentDate)),
                    onValueChange = { },
                    label = { Text("Incident Date") },
                    readOnly = true,
                    enabled = false, // Keep it not directly editable, only via picker
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Date Picker") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Incident Location Section
            Text("Incident Location", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = if (incidentLatitude != null && incidentLongitude != null) {
                        String.format(Locale.US, "Lat: %.5f, Lon: %.5f", incidentLatitude, incidentLongitude)
                    } else {
                        "Location not set"
                    },
                    onValueChange = { /* Location is set via button */ },
                    label = { Text("Coordinates") },
                    readOnly = true,
                    modifier = Modifier.weight(1f),
                     colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    enabled = false // Display only
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    enabled = !isFetchingLocation,
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding
                ) {
                    if (isFetchingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Get Current Location")
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Get Location")
                    }
                }
            }
            Text(
                text = "Tap 'Get Location' to record the incident's location. This should be the actual place where the incident occurred.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )


            // Responsible Authority Dropdown
            var authorityExpanded by remember { mutableStateOf(false) }
            val authorities = listOf("City Council", "State Highway Dept", "National Highways Authority", "Other") // Example list
            Box {
                OutlinedTextField(
                    value = authority,
                    onValueChange = { }, // Not directly editable
                    label = { Text("Responsible Authority") },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { authorityExpanded = true },
                    trailingIcon = { Icon(Icons.Filled.ArrowDropDown, "Select Authority") }
                )
                DropdownMenu(
                    expanded = authorityExpanded,
                    onDismissRequest = { authorityExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    authorities.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                viewModel.onAuthorityChange(selectionOption)
                                authorityExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Road Condition Description
            OutlinedTextField(
                value = roadConditionDescription,
                onValueChange = viewModel::onRoadConditionDescriptionChange,
                label = { Text("Road Condition Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Vehicle Damage Description
            OutlinedTextField(
                value = vehicleDamageDescription,
                onValueChange = viewModel::onVehicleDamageDescriptionChange,
                label = { Text("Vehicle Damage Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Compensation Amount
            OutlinedTextField(
                value = compensation,
                onValueChange = viewModel::onCompensationChange,
                label = { Text("Compensation Amount Requested") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Add Photo Evidence
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
                        // You could add a small overlay here with evidence.latitude, evidence.longitude if needed
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Save Claim Button
            RoadReliefButton(
                onClick = {
                    viewModel.saveCase()
                    navController.popBackStack() // Navigate back after saving
                },
                text = "Save Claim",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp)) // Added some padding at the bottom
        }
    }
}
