package jp.co.kirokuai.app.ai.speech

import android.content.Context
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhisperSpeechRecognizer internal constructor(
    private val modelProvider: WhisperModelProvider,
    private val nativeApi: WhisperNativeApi,
    private val dispatcher: CoroutineDispatcher,
) : SpeechRecognizer {
    constructor(context: Context) : this(
        modelProvider = BundledWhisperModelProvider(context.applicationContext),
        nativeApi = WhisperNativeBridge(),
        dispatcher = Dispatchers.Default,
    )

    override suspend fun transcribe(audioFile: File): Transcript = withContext(dispatcher) {
        validateAudioFile(audioFile)
        val modelFile = modelProvider.getModelFile()
        val text = try {
            nativeApi.transcribe(
                modelPath = modelFile.absolutePath,
                audioPath = audioFile.absolutePath,
            )
        } catch (error: UnsatisfiedLinkError) {
            throw SpeechRecognitionException.NativeInitialization(error)
        } catch (error: Exception) {
            if (error is SpeechRecognitionException) throw error
            throw SpeechRecognitionException.NativeTranscription(error)
        }

        Transcript(
            text = text.trim(),
            language = INITIAL_MODEL_LANGUAGE,
        )
    }

    private fun validateAudioFile(audioFile: File) {
        if (!audioFile.exists()) {
            throw SpeechRecognitionException.AudioFileMissing(audioFile.absolutePath)
        }
        if (!audioFile.isFile || !audioFile.canRead()) {
            throw SpeechRecognitionException.AudioFileUnreadable(audioFile.absolutePath)
        }
        if (!audioFile.extension.equals(WAV_EXTENSION, ignoreCase = true)) {
            throw SpeechRecognitionException.InvalidAudio("Audio file must be WAV")
        }
    }

    private companion object {
        const val WAV_EXTENSION = "wav"
        const val INITIAL_MODEL_LANGUAGE = "en"
    }
}
