package jp.co.kirokuai.app.ai.speech

import java.io.File

class FakeSpeechRecognizer : SpeechRecognizer {
    override suspend fun transcribe(audioFile: File): Transcript = Transcript(
        text = SAMPLE_TRANSCRIPT,
    )

    private companion object {
        const val SAMPLE_TRANSCRIPT = "This is a sample transcript."
    }
}
