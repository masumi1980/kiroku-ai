package jp.co.kirokuai.app.viewmodel

import jp.co.kirokuai.app.ai.speech.Transcript

sealed interface TranscriptionUiState {
    data object Idle : TranscriptionUiState

    data object Loading : TranscriptionUiState

    data class Success(
        val transcript: Transcript,
    ) : TranscriptionUiState

    data class Error(
        val message: String,
    ) : TranscriptionUiState
}
