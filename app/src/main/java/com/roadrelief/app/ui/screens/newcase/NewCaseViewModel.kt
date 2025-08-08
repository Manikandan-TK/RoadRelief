package com.roadrelief.app.ui.screens.newcase

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.roadrelief.app.data.database.dao.CaseDao
import com.roadrelief.app.data.database.dao.EvidenceDao
import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import com.roadrelief.app.data.database.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NewCaseViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val caseDao: CaseDao,
    private val evidenceDao: EvidenceDao,
    userDao: UserDao
) : ViewModel() {

    // User data for placeholders
    private val user: StateFlow<UserEntity?> = userDao.getUser()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val userNamePlaceholder: StateFlow<String> = user.map {
        if (it?.name.isNullOrEmpty()) "[Your Name]" else it.name
    }.stateIn(viewModelScope, SharingStarted.Lazily, "[Your Name]")

    private val _incidentDate = MutableStateFlow(System.currentTimeMillis())
    val incidentDate: StateFlow<Long> = _incidentDate.asStateFlow()

    private val _roadConditionDescription = MutableStateFlow("")
    val roadConditionDescription: StateFlow<String> = _roadConditionDescription.asStateFlow()

    private val _vehicleDamageDescription = MutableStateFlow("")
    val vehicleDamageDescription: StateFlow<String> = _vehicleDamageDescription.asStateFlow()

    private val _compensation = MutableStateFlow("")
    val compensation: StateFlow<String> = _compensation.asStateFlow()

    private val _evidenceList = MutableStateFlow<List<EvidenceEntity>>(emptyList())
    val evidenceList: StateFlow<List<EvidenceEntity>> = _evidenceList.asStateFlow()

    private val _incidentLatitude = MutableStateFlow<Double?>(null)
    val incidentLatitude: StateFlow<Double?> = _incidentLatitude.asStateFlow()

    private val _incidentLongitude = MutableStateFlow<Double?>(null)
    val incidentLongitude: StateFlow<Double?> = _incidentLongitude.asStateFlow()

    private val _incidentAddress = MutableStateFlow<String?>(null)
    val incidentAddress: StateFlow<String?> = _incidentAddress.asStateFlow()

    // State for loading indicators
    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    // State for the final, selected authority
    private val _selectedAuthority = MutableStateFlow("")
    val selectedAuthority: StateFlow<String> = _selectedAuthority.asStateFlow()

    // State for the list of choices in the dropdown
    private val _authorityOptions = MutableStateFlow<List<String>>(emptyList())
    val authorityOptions: StateFlow<List<String>> = _authorityOptions.asStateFlow()

    // State for the "Other" text field
    private val _customAuthority = MutableStateFlow("")
    val customAuthority: StateFlow<String> = _customAuthority.asStateFlow()

    // Channel for one-time UI events (e.g., Snackbars)
    private val _uiEventChannel = Channel<UiEvent>()
    val uiEvents = _uiEventChannel.receiveAsFlow()

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }


    fun onIncidentDateChange(date: Long) {
        _incidentDate.value = date
    }

    // Modify onAuthorityChange
    fun onAuthorityChange(auth: String) {
        _selectedAuthority.value = auth
        if (auth != "Other") {
            _customAuthority.value = ""
        }
    }

    // Add onCustomAuthorityChange
    fun onCustomAuthorityChange(text: String) {
        _customAuthority.value = text
    }

    fun onRoadConditionDescriptionChange(desc: String) {
        _roadConditionDescription.value = desc
    }

    fun onVehicleDamageDescriptionChange(desc: String) {
        _vehicleDamageDescription.value = desc
    }

    fun onCompensationChange(comp: String) {
        _compensation.value = comp
    }

    fun onIncidentLocationChange(latitude: Double?, longitude: Double?) {
        _incidentLatitude.value = latitude
        _incidentLongitude.value = longitude
        if (latitude != null && longitude != null) {
            detectAuthorityFromLocation(latitude, longitude)
        } else {
            _incidentAddress.value = null
        }
    }

    // New public function for the UI to call
    fun onGetLocationClicked() {
        viewModelScope.launch {
            // Check for Google Play Services
            val googleApiAvailability = GoogleApiAvailability.getInstance()
            val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
            if (resultCode != ConnectionResult.SUCCESS) {
                _uiEventChannel.send(UiEvent.ShowSnackbar("Google Play Services are not available on this device."))
                return@launch
            }

            if (!isNetworkAvailable()) {
                _uiEventChannel.send(UiEvent.ShowSnackbar("No internet connection. Please connect and try again."))
                return@launch
            }
            fetchAndProcessLocation()
        }
    }

    fun onLocationPermissionDenied() {
        viewModelScope.launch {
            _uiEventChannel.send(UiEvent.ShowSnackbar("Location permission denied. Please enable it in settings and try again."))
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun fetchAndProcessLocation() {
        Log.i("LocationCrash", "fetchAndProcessLocation() invoked")

        if (!hasLocationPermission()) {
            // Should never happen if your launcher is correct—but guard anyway!
            viewModelScope.launch {
                _uiEventChannel.send(UiEvent.ShowSnackbar(
                    "Please grant location permission first."
                ))
            }
            _isDetectingLocation.value = false // Ensure loading state is reset
            return
        }

        _isDetectingLocation.value = true
        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        onIncidentLocationChange(location.latitude, location.longitude) // This will trigger geocoding
                    } else {
                        viewModelScope.launch {
                            _isDetectingLocation.value = false
                            _uiEventChannel.send(UiEvent.ShowSnackbar("Failed to get location. Please check GPS and try again."))
                        }
                    }
                }
                .addOnFailureListener {
                    // this will only catch async failures
                    Log.e("LocationCrash", "async failure", it)
                    viewModelScope.launch {
                        _isDetectingLocation.value = false
                        // _uiEventChannel.send(UiEvent.ShowSnackbar("Location permission denied or error occurred.")) // More specific messages are now in place
                    }
                }
        } catch (e: SecurityException) {
            Log.e("LocationCrash", "sync SecurityException", e)
            viewModelScope.launch {
                _uiEventChannel.send(UiEvent.ShowSnackbar(
                    "Location permission missing at runtime—please grant it and try again."
                ))
            }
            _isDetectingLocation.value = false
        } catch (e: Exception) {
            Log.e("LocationCrash", "unexpected exception", e)
            viewModelScope.launch {
                _uiEventChannel.send(UiEvent.ShowSnackbar(
                    "Unexpected error initializing location services."
                ))
            }
            _isDetectingLocation.value = false
        }
    }

    private fun detectAuthorityFromLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isDetectingLocation.value = true // Keep true while geocoding
            val geocoder = Geocoder(context, Locale.getDefault())
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val generatedOptions = mutableListOf<String>()
                var topSuggestion = ""

                if (addresses.isNullOrEmpty()) {
                    throw IOException("No address found for location")
                }

                val address = addresses[0]
                val state = address.adminArea
                val city = address.locality
                val roadName = address.thoroughfare ?: ""

                // Set human-readable address
                _incidentAddress.value = address.getAddressLine(0)

                if (roadName.contains("National Highway", ignoreCase = true) || roadName.contains("NH", ignoreCase = true)) {
                    topSuggestion = "National Highways Authority of India"
                } else if (city != null) {
                    topSuggestion = "Municipal Corporation of $city"
                } else if (state != null) {
                    topSuggestion = "$state State Highway Dept"
                }

                if (city != null) generatedOptions.add("Municipal Corporation of $city")
                if (state != null) generatedOptions.add("$state State Highway Dept")
                generatedOptions.add("National Highways Authority of India")
                generatedOptions.add("Other")

                _authorityOptions.value = generatedOptions.distinct()
                if (_selectedAuthority.value.isEmpty()) {
                    _selectedAuthority.value = topSuggestion
                }

            } catch (e: Exception) {
                _uiEventChannel.send(UiEvent.ShowSnackbar("Could not auto-detect authority. Please select manually."))
                val defaults = listOf("State Highway Dept", "National Highways Authority of India", "Municipal Corporation", "Other")
                _authorityOptions.value = defaults
                _selectedAuthority.value = "" // Force manual selection
                _incidentAddress.value = "Could not determine address."
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    fun addEvidence(uriString: String, latitude: Double, longitude: Double) {
        val timestamp = System.currentTimeMillis()
        val newEvidence = EvidenceEntity(
            caseId = 0, // Will be updated after case is saved
            photoUri = uriString,
            latitude = latitude, // This is the photo's location
            longitude = longitude, // This is the photo's location
            timestamp = timestamp
        )
        _evidenceList.value = _evidenceList.value + newEvidence
    }

    fun saveCase() {
        viewModelScope.launch {
            val case = CaseEntity(
                incidentDate = _incidentDate.value,
                authority = if (_selectedAuthority.value == "Other") _customAuthority.value.trim() else _selectedAuthority.value,
                description = _roadConditionDescription.value,
                vehicleDamageDescription = _vehicleDamageDescription.value,
                compensation = _compensation.value.toDoubleOrNull() ?: 0.0,
                status = "Pending",
                incidentLatitude = _incidentLatitude.value,
                incidentLongitude = _incidentLongitude.value
            )
            val caseId = caseDao.insertCase(case)

            _evidenceList.value.forEach { evidence ->
                evidenceDao.insertEvidence(evidence.copy(caseId = caseId))
            }
            // Clear form after saving
            _incidentDate.value = System.currentTimeMillis()
            _selectedAuthority.value = ""
            _customAuthority.value = ""
            _authorityOptions.value = emptyList()
            _roadConditionDescription.value = ""
            _vehicleDamageDescription.value = ""
            _compensation.value = ""
            _incidentLatitude.value = null
            _incidentLongitude.value = null
            _incidentAddress.value = null
            _evidenceList.value = emptyList()
        }
    }
}
