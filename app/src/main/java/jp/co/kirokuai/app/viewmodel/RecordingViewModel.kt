package jp.co.kirokuai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import jp.co.kirokuai.app.audio.AudioRecorder
import jp.co.kirokuai.app.domain.MeetingRepository
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class RecordingUiState(
    val meetingTitle: String = "",
    val elapsedSeconds: Long = 0,
    val isRecording: Boolean = false,
    val errorMessage: String? = null,
)

class RecordingViewModel(
    private val audioRecorder: AudioRecorder,
    private val meetingRepository: MeetingRepository,
    private val speechRepository: SpeechRepository,
    private val outputPathProvider: () -> String,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = mutableUiState.asStateFlow()

    private var timerJob: Job? = null
    private var recordingOutputPath: String? = null
    private var recordingStartedAt: Long? = null

    fun updateMeetingTitle(title: String) {
        mutableUiState.update { it.copy(meetingTitle = title) }
    }

    fun startRecording() {
        if (mutableUiState.value.isRecording) return

        try {
            val outputPath = outputPathProvider()
            audioRecorder.start(outputPath)
            recordingOutputPath = outputPath
            recordingStartedAt = currentTimeMillis()
            mutableUiState.update {
                it.copy(
                    elapsedSeconds = 0,
                    isRecording = true,
                    errorMessage = null,
                )
            }
            startTimer()
        } catch (_: Exception) {
            mutableUiState.update {
                it.copy(errorMessage = "録音を開始できませんでした")
            }
        }
    }

    fun stopRecording() {
        if (!mutableUiState.value.isRecording) return

        timerJob?.cancel()
        timerJob = null
        try {
            audioRecorder.stop()
            mutableUiState.update { it.copy(isRecording = false, errorMessage = null) }
            saveMeeting()
        } catch (_: RuntimeException) {
            mutableUiState.update {
                it.copy(
                    isRecording = false,
                    errorMessage = "録音を停止できませんでした",
                )
            }
        }
    }

    private fun saveMeeting() {
        val outputPath = recordingOutputPath ?: return
        val startedAt = recordingStartedAt ?: return
        val state = mutableUiState.value
        recordingOutputPath = null
        recordingStartedAt = null

        viewModelScope.launch {
            try {
                val meetingId = meetingRepository.save(
                    Meeting(
                        id = 0,
                        title = state.meetingTitle.trim().ifBlank { DEFAULT_MEETING_TITLE },
                        createdAt = startedAt,
                        duration = state.elapsedSeconds,
                        audioPath = outputPath,
                        status = MeetingStatus.TRANSCRIBING,
                    ),
                )
                speechRepository.transcribeAndPersist(meetingId, File(outputPath))
            } catch (_: Exception) {
                mutableUiState.update {
                    it.copy(errorMessage = "文字起こしを保存できませんでした")
                }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(TIMER_INTERVAL_MILLIS)
                mutableUiState.update { state ->
                    state.copy(elapsedSeconds = state.elapsedSeconds + 1)
                }
            }
        }
    }

    override fun onCleared() {
        if (mutableUiState.value.isRecording) {
            runCatching { audioRecorder.stop() }
        }
    }

    private companion object {
        const val TIMER_INTERVAL_MILLIS = 1_000L
        const val DEFAULT_MEETING_TITLE = "無題の会議"
    }
}
