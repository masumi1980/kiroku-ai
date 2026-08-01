package jp.co.kirokuai.app.viewmodel

import jp.co.kirokuai.app.audio.AudioRecorder
import jp.co.kirokuai.app.ai.speech.SpeechRecognizer
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import jp.co.kirokuai.app.ai.speech.Transcript
import jp.co.kirokuai.app.domain.MeetingRepository
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordingViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stopRecording_recognizesAndPersistsCompletedMeeting() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val audioRecorder = FakeAudioRecorder()
        val repository = FakeMeetingRepository()
        val viewModel = RecordingViewModel(
            audioRecorder = audioRecorder,
            meetingRepository = repository,
            speechRepository = SpeechRepository(
                speechRecognizer = SuccessfulSpeechRecognizer(),
                meetingRepository = repository,
            ),
            outputPathProvider = { "recording.m4a" },
            currentTimeMillis = { 123L },
        )

        viewModel.updateMeetingTitle(" Weekly Meeting ")
        viewModel.startRecording()
        advanceTimeBy(2_000L)
        runCurrent()
        viewModel.stopRecording()
        runCurrent()

        assertTrue(audioRecorder.started)
        assertTrue(audioRecorder.stopped)
        assertFalse(viewModel.uiState.value.isRecording)
        assertEquals(
            Meeting(
                id = 1,
                title = "Weekly Meeting",
                createdAt = 123L,
                duration = 2L,
                audioPath = "recording.m4a",
                status = MeetingStatus.COMPLETED,
                transcript = "Persistent transcript",
            ),
            repository.savedMeeting,
        )
        assertEquals("Persistent transcript", repository.savedTranscript)
    }

    @Test
    fun stopRecording_exposesReadableErrorWhenRecognitionFails() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val repository = FakeMeetingRepository()
        val viewModel = RecordingViewModel(
            audioRecorder = FakeAudioRecorder(),
            meetingRepository = repository,
            speechRepository = SpeechRepository(
                speechRecognizer = RecordingFailingSpeechRecognizer(),
                meetingRepository = repository,
            ),
            outputPathProvider = { "recording.wav" },
        )

        viewModel.startRecording()
        viewModel.stopRecording()
        runCurrent()

        assertEquals("文字起こしを保存できませんでした", viewModel.uiState.value.errorMessage)
        assertEquals(null, repository.savedTranscript)
    }
}

private class FakeAudioRecorder : AudioRecorder {
    var started = false
    var stopped = false

    override fun start(outputPath: String) {
        started = true
    }

    override fun stop() {
        stopped = true
    }
}

private class FakeMeetingRepository : MeetingRepository {
    private val meetings = MutableStateFlow<List<Meeting>>(emptyList())
    var savedMeeting: Meeting? = null

    var savedTranscript: String? = null

    override suspend fun save(meeting: Meeting): Long {
        savedMeeting = meeting
        meetings.value += meeting
        return 1L
    }

    override suspend fun saveTranscript(meetingId: Long, transcript: String) {
        savedTranscript = transcript
        savedMeeting = savedMeeting?.copy(
            id = meetingId,
            transcript = transcript,
            status = MeetingStatus.COMPLETED,
        )
    }

    override suspend fun loadTranscript(meetingId: Long): String? = savedTranscript

    override fun getAll(): Flow<List<Meeting>> = meetings
}

private class SuccessfulSpeechRecognizer : SpeechRecognizer {
    override suspend fun transcribe(audioFile: File): Transcript =
        Transcript(text = "Persistent transcript", language = "en")
}

private class RecordingFailingSpeechRecognizer : SpeechRecognizer {
    override suspend fun transcribe(audioFile: File): Transcript = error("Recognition failed")
}
