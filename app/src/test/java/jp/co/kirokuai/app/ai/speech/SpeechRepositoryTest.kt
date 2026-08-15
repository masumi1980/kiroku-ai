package jp.co.kirokuai.app.ai.speech

import java.io.File
import jp.co.kirokuai.app.domain.MeetingRepository
import jp.co.kirokuai.app.model.Meeting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpeechRepositoryTest {
    @Test
    fun transcribe_delegatesToSpeechRecognizer() = runTest {
        val audioFile = File("meeting.m4a")
        val expectedTranscript = Transcript(
            text = "テスト文字起こし",
            language = "ja",
            durationMillis = 1_000L,
        )
        val recognizer = RecordingSpeechRecognizer(expectedTranscript)
        val repository = SpeechRepository(recognizer)

        val transcript = repository.transcribe(audioFile)

        assertEquals(audioFile, recognizer.receivedAudioFile)
        assertEquals(expectedTranscript, transcript)
    }

    @Test
    fun transcribeAndPersist_savesRecognizerResultForMeeting() = runTest {
        val transcript = Transcript(text = "保存する文字起こし", language = "ja")
        val meetingRepository = RecordingMeetingRepository()
        val repository = SpeechRepository(
            speechRecognizer = RecordingSpeechRecognizer(transcript),
            meetingRepository = meetingRepository,
        )

        val result = repository.transcribeAndPersist(42L, File("meeting.wav"))

        assertEquals(transcript, result)
        assertEquals(42L, meetingRepository.savedMeetingId)
        assertEquals(transcript.text, meetingRepository.savedTranscript)
    }

    @Test
    fun transcribeAndPersist_rejectsEmptyTranscriptWithoutSaving() = runTest {
        val meetingRepository = RecordingMeetingRepository()
        val repository = SpeechRepository(
            speechRecognizer = RecordingSpeechRecognizer(Transcript(text = "  ")),
            meetingRepository = meetingRepository,
        )

        val error = runCatching {
            repository.transcribeAndPersist(42L, File("meeting.wav"))
        }.exceptionOrNull()

        assertTrue(error is SpeechRecognitionException.EmptyTranscript)
        assertNull(meetingRepository.savedTranscript)
    }
}

private class RecordingSpeechRecognizer(
    private val transcript: Transcript,
) : SpeechRecognizer {
    var receivedAudioFile: File? = null

    override suspend fun transcribe(audioFile: File): Transcript {
        receivedAudioFile = audioFile
        return transcript
    }
}

private class RecordingMeetingRepository : MeetingRepository {
    var savedMeetingId: Long? = null
    var savedTranscript: String? = null

    override suspend fun save(meeting: Meeting): Long = meeting.id

    override suspend fun saveTranscript(meetingId: Long, transcript: String) {
        savedMeetingId = meetingId
        savedTranscript = transcript
    }

    override suspend fun loadTranscript(meetingId: Long): String? = savedTranscript

    override suspend fun search(keyword: String): List<Meeting> = emptyList()

    override fun getAll(): Flow<List<Meeting>> = emptyFlow()
}
