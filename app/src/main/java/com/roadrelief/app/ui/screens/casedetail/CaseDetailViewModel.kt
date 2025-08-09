package com.roadrelief.app.ui.screens.casedetail

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.roadrelief.app.data.database.dao.CaseDao
import com.roadrelief.app.data.database.dao.EvidenceDao
import com.roadrelief.app.data.database.dao.UserDao
import com.roadrelief.app.data.database.entity.CaseEntity
import com.roadrelief.app.data.database.entity.EvidenceEntity
import com.roadrelief.app.util.PdfGenerationException
import com.roadrelief.app.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// Represents the different states of the PDF generation process for the UI
sealed interface PdfGenerationState {
    object Idle : PdfGenerationState
    object Loading : PdfGenerationState
    data class Error(val message: String) : PdfGenerationState
}

@HiltViewModel
class CaseDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val caseDao: CaseDao,
    private val evidenceDao: EvidenceDao,
    private val userDao: UserDao,
    private val pdfGenerator: PdfGenerator
) : ViewModel() {

    private val caseId: Long = savedStateHandle.get<Long>("caseId") ?: -1L

    private val _case = MutableStateFlow<CaseEntity?>(null)
    private val _evidence = MutableStateFlow<List<EvidenceEntity>>(emptyList())

    val caseWithEvidence: StateFlow<Pair<CaseEntity?, List<EvidenceEntity>>?> = combine(_case, _evidence) { case, evidence ->
        case?.let { Pair(it, evidence) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _shareIntent = MutableStateFlow<Intent?>(null)
    val shareIntent: StateFlow<Intent?> = _shareIntent.asStateFlow()

    private val _pdfGenerationState = MutableStateFlow<PdfGenerationState>(PdfGenerationState.Idle)
    val pdfGenerationState: StateFlow<PdfGenerationState> = _pdfGenerationState.asStateFlow()

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
            _pdfGenerationState.value = PdfGenerationState.Loading
            val user = userDao.getUser().first()
            _case.value?.let { case ->
                try {
                    val pdfUri = pdfGenerator.generatePdfReport(user, case, _evidence.value)
                    val authority = "${context.packageName}.provider"
                    val contentUri = FileProvider.getUriForFile(context, authority, pdfUri.toFile())

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, contentUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooserIntent = Intent.createChooser(shareIntent, "Share PDF Report")
                    _shareIntent.value = chooserIntent
                    _pdfGenerationState.value = PdfGenerationState.Idle
                } catch (e: PdfGenerationException) {
                    _pdfGenerationState.value = PdfGenerationState.Error("Failed to generate PDF: ${e.message}")
                } catch (e: Exception) {
                    // Catch any other unexpected errors
                    _pdfGenerationState.value = PdfGenerationState.Error("An unexpected error occurred.")
                }
            } ?: run {
                _pdfGenerationState.value = PdfGenerationState.Error("Case details not available.")
            }
        }
    }

    fun onShareIntentHandled() {
        _shareIntent.value = null
    }

    fun dismissPdfError() {
        _pdfGenerationState.value = PdfGenerationState.Idle
    }
}
