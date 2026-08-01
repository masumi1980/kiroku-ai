package jp.co.kirokuai.app.ai.speech

import java.io.File

class SpeechRepository(
    private val speechRecognizer: SpeechRecognizer,
) {
    suspend fun transcribe(audioFile: File): Transcript =
        speechRecognizer.transcribe(audioFile)
}
