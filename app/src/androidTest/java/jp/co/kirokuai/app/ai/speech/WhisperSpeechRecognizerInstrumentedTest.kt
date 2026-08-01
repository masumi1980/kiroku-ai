package jp.co.kirokuai.app.ai.speech

import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhisperSpeechRecognizerInstrumentedTest {
    @Test
    fun bundledModel_transcribesWaveFileThroughJni() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val targetContext = instrumentation.targetContext
        val audioFile = File(targetContext.cacheDir, TEST_AUDIO_FILE)
        instrumentation.context.assets.open(TEST_AUDIO_FILE).use { input ->
            audioFile.outputStream().use(input::copyTo)
        }

        val transcript = WhisperSpeechRecognizer(targetContext).transcribe(audioFile)

        assertEquals("en", transcript.language)
        assertTrue(transcript.text.isNotBlank())
        assertTrue(
            transcript.text.lowercase().contains(EXPECTED_TRANSCRIPT_PHRASE),
        )
    }

    private companion object {
        const val TEST_AUDIO_FILE = "jfk.wav"
        const val EXPECTED_TRANSCRIPT_PHRASE = "ask not what your country can do for you"
    }
}
