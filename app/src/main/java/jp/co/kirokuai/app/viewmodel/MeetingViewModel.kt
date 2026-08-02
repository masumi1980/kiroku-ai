package jp.co.kirokuai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.kirokuai.app.domain.MeetingSummarizer
import jp.co.kirokuai.app.domain.MeetingSummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MeetingViewModel(
    private val meetingId: Long,
    private val summarizer: MeetingSummarizer,
    summaryRepository: MeetingSummaryRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(MeetingUiState())
    val uiState: StateFlow<MeetingUiState> = mutableUiState.asStateFlow()

    init {
        summaryRepository.observe(meetingId)
            .onEach { summary -> mutableUiState.value = mutableUiState.value.copy(summary = summary) }
            .catch { error -> showError(error) }
            .launchIn(viewModelScope)
    }

    fun generateSummary() {
        if (mutableUiState.value.isGenerating) return
        viewModelScope.launch {
            mutableUiState.value = mutableUiState.value.copy(isGenerating = true, errorMessage = null)
            runCatching { summarizer.generate(meetingId) }
                .onSuccess { summary ->
                    mutableUiState.value = MeetingUiState(summary = summary)
                }
                .onFailure(::showError)
        }
    }

    private fun showError(error: Throwable) {
        mutableUiState.value = mutableUiState.value.copy(
            isGenerating = false,
            errorMessage = error.message ?: "要約を生成できませんでした",
        )
    }
}
