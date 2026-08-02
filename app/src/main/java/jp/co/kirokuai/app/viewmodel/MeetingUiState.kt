package jp.co.kirokuai.app.viewmodel

import jp.co.kirokuai.app.model.MeetingSummary

data class MeetingUiState(
    val summary: MeetingSummary? = null,
    val isGenerating: Boolean = false,
    val errorMessage: String? = null,
)
