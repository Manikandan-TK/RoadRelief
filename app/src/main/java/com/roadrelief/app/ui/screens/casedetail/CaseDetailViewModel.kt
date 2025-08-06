package com.roadrelief.app.ui.screens.casedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadrelief.app.data.database.dao.CaseDao
import com.roadrelief.app.data.database.dao.EvidenceDao
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import com.roadrelief.app.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val caseDao: CaseDao,
    private val evidenceDao: EvidenceDao,
    private val pdfGenerator: PdfGenerator
) : ViewModel() {

    private val caseId: Long = savedStateHandle.get<Long>("caseId") ?: -1L

    private val _case = MutableStateFlow<CaseEntity?>(null)
    private val _evidence = MutableStateFlow<List<EvidenceEntity>>(emptyList())

    val caseWithEvidence: StateFlow<Pair<CaseEntity?, List<EvidenceEntity>>?> = combine(_case, _evidence) { case, evidence ->
        case?.let { Pair(it, evidence) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        if (caseId != -1L) {
            viewModelScope.launch {
                caseDao.getCaseById(caseId).collect { case ->
                    _case.value = case
                }
            }
            viewModelScope.launch {
                evidenceDao.getEvidenceForCase(caseId).collect { evidence ->
                    _evidence.value = evidence
                }
            }
        }
    }

    fun generatePdfReport() {
        viewModelScope.launch {
            _case.value?.let { case ->
                val pdfUri = pdfGenerator.generatePdfReport(case, _evidence.value)
                // TODO: Handle the generated PDF URI (e.g., show a success message, share it)
            }
        }
    }
}
