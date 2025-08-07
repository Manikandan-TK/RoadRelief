package com.roadrelief.app.ui.screens.newcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadrelief.app.data.database.dao.CaseDao
import com.roadrelief.app.data.database.dao.EvidenceDao
import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import com.roadrelief.app.data.database.entity.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewCaseViewModel @Inject constructor(
    private val caseDao: CaseDao,
    private val evidenceDao: EvidenceDao,
    private val userDao: UserDao
) : ViewModel() {

    // User data for placeholders
    private val user: StateFlow<UserEntity?> = userDao.getUser()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val userNamePlaceholder: StateFlow<String> = user.map {
        if (it?.name.isNullOrEmpty()) "[Your Name]" else it!!.name
    }.stateIn(viewModelScope, SharingStarted.Lazily, "[Your Name]")

    val userAddressPlaceholder: StateFlow<String> = user.map {
        if (it?.address.isNullOrEmpty()) "[Your Address]" else it!!.address
    }.stateIn(viewModelScope, SharingStarted.Lazily, "[Your Address]")

    val userVehicleNumberPlaceholder: StateFlow<String> = user.map {
        if (it?.vehicleNumber.isNullOrEmpty()) "[Your Vehicle Number]" else it!!.vehicleNumber
    }.stateIn(viewModelScope, SharingStarted.Lazily, "[Your Vehicle Number]")


    private val _incidentDate = MutableStateFlow(System.currentTimeMillis())
    val incidentDate: StateFlow<Long> = _incidentDate.asStateFlow()

    private val _authority = MutableStateFlow("")
    val authority: StateFlow<String> = _authority.asStateFlow()

    private val _roadConditionDescription = MutableStateFlow("")
    val roadConditionDescription: StateFlow<String> = _roadConditionDescription.asStateFlow()

    private val _vehicleDamageDescription = MutableStateFlow("")
    val vehicleDamageDescription: StateFlow<String> = _vehicleDamageDescription.asStateFlow()

    private val _compensation = MutableStateFlow("")
    val compensation: StateFlow<String> = _compensation.asStateFlow()

    private val _status = MutableStateFlow("Pending")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _evidenceList = MutableStateFlow<List<EvidenceEntity>>(emptyList())
    val evidenceList: StateFlow<List<EvidenceEntity>> = _evidenceList.asStateFlow()

    private val _incidentLatitude = MutableStateFlow<Double?>(null)
    val incidentLatitude: StateFlow<Double?> = _incidentLatitude.asStateFlow()

    private val _incidentLongitude = MutableStateFlow<Double?>(null)
    val incidentLongitude: StateFlow<Double?> = _incidentLongitude.asStateFlow()

    val authorities = listOf("City Council", "State Highway Dept", "Other")

    fun onIncidentDateChange(date: Long) {
        _incidentDate.value = date
    }

    fun onAuthorityChange(auth: String) {
        _authority.value = auth
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
                authority = _authority.value,
                description = _roadConditionDescription.value,
                vehicleDamageDescription = _vehicleDamageDescription.value,
                compensation = _compensation.value.toDoubleOrNull() ?: 0.0,
                status = _status.value,
                incidentLatitude = _incidentLatitude.value,
                incidentLongitude = _incidentLongitude.value
            )
            val caseId = caseDao.insertCase(case)

            _evidenceList.value.forEach { evidence ->
                evidenceDao.insertEvidence(evidence.copy(caseId = caseId))
            }
            // Clear form after saving
            _incidentDate.value = System.currentTimeMillis()
            _authority.value = ""
            _roadConditionDescription.value = ""
            _vehicleDamageDescription.value = ""
            _compensation.value = ""
            _incidentLatitude.value = null
            _incidentLongitude.value = null
            _evidenceList.value = emptyList()
        }
    }
}