package com.roadrelief.app.ui.screens.newcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadrelief.app.data.database.dao.CaseDao
import com.roadrelief.app.data.database.dao.EvidenceDao
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewCaseViewModel @Inject constructor(
    private val caseDao: CaseDao,
    private val evidenceDao: EvidenceDao,
) : ViewModel() {

    private val _incidentDate = MutableStateFlow(System.currentTimeMillis())
    val incidentDate: StateFlow<Long> = _incidentDate.asStateFlow()

    private val _authority = MutableStateFlow("")
    val authority: StateFlow<String> = _authority.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _compensation = MutableStateFlow("")
    val compensation: StateFlow<String> = _compensation.asStateFlow()

    private val _status = MutableStateFlow("Pending")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _evidenceList = MutableStateFlow<List<EvidenceEntity>>(emptyList())
    val evidenceList: StateFlow<List<EvidenceEntity>> = _evidenceList.asStateFlow()

    fun onIncidentDateChange(date: Long) {
        _incidentDate.value = date
    }

    fun onAuthorityChange(auth: String) {
        _authority.value = auth
    }

    fun onDescriptionChange(desc: String) {
        _description.value = desc
    }

    fun onCompensationChange(comp: String) {
        _compensation.value = comp
    }

    fun addEvidence(uriString: String, latitude: Double, longitude: Double) {
        val timestamp = System.currentTimeMillis()
        val newEvidence = EvidenceEntity(
            caseId = 0, // Will be updated after case is saved
            photoUri = uriString,
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp
        )
        _evidenceList.value = _evidenceList.value + newEvidence
    }

    fun saveCase() {
        viewModelScope.launch {
            val case = CaseEntity(
                incidentDate = _incidentDate.value,
                authority = _authority.value,
                description = _description.value,
                compensation = _compensation.value.toDoubleOrNull() ?: 0.0,
                status = _status.value
            )
            val caseId = caseDao.insertCase(case)

            _evidenceList.value.forEach { evidence ->
                evidenceDao.insertEvidence(evidence.copy(caseId = caseId))
            }
            // Clear form after saving
            _incidentDate.value = System.currentTimeMillis()
            _authority.value = ""
            _description.value = ""
            _compensation.value = ""
            _evidenceList.value = emptyList()
        }
    }
}