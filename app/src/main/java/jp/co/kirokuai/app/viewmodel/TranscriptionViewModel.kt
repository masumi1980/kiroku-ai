package jp.co.kirokuai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranscriptionViewModel(
    private val speechRepository: SpeechRepository,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow<TranscriptionUiState>(
        TranscriptionUiState.Idle,
    )
    val uiState: StateFlow<TranscriptionUiState> = mutableUiState.asStateFlow()

    fun startTranscription(audioFile: File) {
        if (mutableUiState.value == TranscriptionUiState.Loading) return

        viewModelScope.launch {
            mutableUiState.value = TranscriptionUiState.Loading
            try {
                val transcript = speechRepository.transcribe(audioFile)
                mutableUiState.value = TranscriptionUiState.Success(transcript)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                mutableUiState.value = TranscriptionUiState.Error(
                    message = TRANSCRIPTION_ERROR_MESSAGE,
                )
            }
        }
    }

    private companion object {
        const val TRANSCRIPTION_ERROR_MESSAGE = "文字起こしに失敗しました"
    }
}
