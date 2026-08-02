package jp.co.kirokuai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.kirokuai.app.domain.MarkdownExportUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MarkdownExportViewModel(
    private val meetingId: Long,
    private val exportUseCase: MarkdownExportUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(MarkdownExportUiState())
    val uiState: StateFlow<MarkdownExportUiState> = mutableUiState.asStateFlow()

    fun defaultFileName(): String = exportUseCase.defaultFileName()

    fun export(destination: String?) {
        if (mutableUiState.value.isExporting) return
        viewModelScope.launch {
            mutableUiState.value = MarkdownExportUiState(isExporting = true)
            exportUseCase.export(meetingId, destination)
                .onSuccess { mutableUiState.value = MarkdownExportUiState(message = SUCCESS_MESSAGE) }
                .onFailure { error ->
                    mutableUiState.value = MarkdownExportUiState(
                        message = error.message ?: FAILURE_MESSAGE,
                    )
                }
        }
    }

    fun messageShown() {
        mutableUiState.value = mutableUiState.value.copy(message = null)
    }

    private companion object {
        const val SUCCESS_MESSAGE = "Markdownをエクスポートしました"
        const val FAILURE_MESSAGE = "Markdownをエクスポートできませんでした"
    }
}
