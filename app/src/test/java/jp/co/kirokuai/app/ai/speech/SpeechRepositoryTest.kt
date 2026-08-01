package jp.co.kirokuai.app.ai.speech

import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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
