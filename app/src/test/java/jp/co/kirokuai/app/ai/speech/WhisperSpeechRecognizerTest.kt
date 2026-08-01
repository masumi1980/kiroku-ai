package jp.co.kirokuai.app.ai.speech

import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WhisperSpeechRecognizerTest {
    @Test
    fun transcribe_returnsTranscriptFromNativeApi() = runTest {
        val audioFile = createTemporaryWaveFile()
        val modelFile = File(audioFile.parentFile, "ggml-tiny.en.bin")
        val nativeApi = FakeWhisperNativeApi(text = " Sample transcript. ")
        val recognizer = WhisperSpeechRecognizer(
            modelProvider = WhisperModelProvider { modelFile },
            nativeApi = nativeApi,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val transcript = recognizer.transcribe(audioFile)

        assertEquals(
            Transcript(
                text = "Sample transcript.",
                language = "en",
            ),
            transcript,
        )
        assertEquals(modelFile.absolutePath, nativeApi.receivedModelPath)
        assertEquals(audioFile.absolutePath, nativeApi.receivedAudioPath)
    }

    @Test
    fun transcribe_rejectsMissingAudioFileBeforeCallingNativeApi() = runTest {
        val nativeApi = FakeWhisperNativeApi(text = "unused")
        val recognizer = WhisperSpeechRecognizer(
            modelProvider = WhisperModelProvider { File("model.bin") },
            nativeApi = nativeApi,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val error = runCatching {
            recognizer.transcribe(File("missing.wav"))
        }.exceptionOrNull()

        assertTrue(error is SpeechRecognitionException.AudioFileMissing)
        assertFalse(nativeApi.wasCalled)
    }

    @Test
    fun transcribe_rejectsNonWaveAudio() = runTest {
        val audioFile = File.createTempFile("meeting", ".m4a").apply { deleteOnExit() }
        val recognizer = WhisperSpeechRecognizer(
            modelProvider = WhisperModelProvider { File("model.bin") },
            nativeApi = FakeWhisperNativeApi(text = "unused"),
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val error = runCatching { recognizer.transcribe(audioFile) }.exceptionOrNull()

        assertTrue(error is SpeechRecognitionException.InvalidAudio)
    }

    private fun createTemporaryWaveFile(): File =
        File.createTempFile("meeting", ".wav").apply {
            writeBytes(byteArrayOf(0))
            deleteOnExit()
        }
}

private class FakeWhisperNativeApi(
    private val text: String,
) : WhisperNativeApi {
    var receivedModelPath: String? = null
    var receivedAudioPath: String? = null
    val wasCalled: Boolean
        get() = receivedModelPath != null

    override fun transcribe(modelPath: String, audioPath: String): String {
        receivedModelPath = modelPath
        receivedAudioPath = audioPath
        return text
    }
}
