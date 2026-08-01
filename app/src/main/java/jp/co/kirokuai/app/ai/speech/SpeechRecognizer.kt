package jp.co.kirokuai.app.ai.speech

import java.io.File

interface SpeechRecognizer {
    suspend fun transcribe(audioFile: File): Transcript
}
