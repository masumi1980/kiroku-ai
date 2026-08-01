package jp.co.kirokuai.app.ai.speech

import java.io.File
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeSpeechRecognizerTest {
    @Test
    fun transcribe_returnsFixedTranscript() = runTest {
        val recognizer = FakeSpeechRecognizer()

        val transcript = recognizer.transcribe(File("meeting.m4a"))

        assertEquals(
            Transcript(text = "This is a sample transcript."),
            transcript,
        )
    }
}
