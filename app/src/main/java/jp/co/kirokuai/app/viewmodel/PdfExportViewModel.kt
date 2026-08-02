package jp.co.kirokuai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.kirokuai.app.domain.PdfExportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PdfExportViewModel(
    private val meetingId: Long,
    private val exportUseCase: PdfExportUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(PdfExportUiState())
    val uiState: StateFlow<PdfExportUiState> = mutableUiState.asStateFlow()

    fun defaultFileName(): String = exportUseCase.defaultFileName()

    fun export(destination: String?) {
        if (mutableUiState.value.isExporting) return
        viewModelScope.launch {
            mutableUiState.value = PdfExportUiState(isExporting = true)
            exportUseCase.export(meetingId, destination)
                .onSuccess { mutableUiState.value = PdfExportUiState(message = SUCCESS_MESSAGE) }
                .onFailure { error ->
                    mutableUiState.value = PdfExportUiState(
                        message = error.message ?: FAILURE_MESSAGE,
                    )
                }
        }
    }

    fun messageShown() {
        mutableUiState.value = mutableUiState.value.copy(message = null)
    }

    private companion object {
        const val SUCCESS_MESSAGE = "PDFをエクスポートしました"
        const val FAILURE_MESSAGE = "PDFをエクスポートできませんでした"
    }
}
