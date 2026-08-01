package jp.co.kirokuai.app.ai.speech

import java.io.File
import jp.co.kirokuai.app.domain.MeetingRepository

class SpeechRepository(
    private val speechRecognizer: SpeechRecognizer,
    private val meetingRepository: MeetingRepository? = null,
) {
    suspend fun transcribe(audioFile: File): Transcript =
        speechRecognizer.transcribe(audioFile)

    suspend fun transcribeAndPersist(meetingId: Long, audioFile: File): Transcript {
        val transcript = speechRecognizer.transcribe(audioFile)
        if (transcript.text.isBlank()) {
            throw SpeechRecognitionException.EmptyTranscript()
        }
        val repository = checkNotNull(meetingRepository) {
            "MeetingRepository is required for transcript persistence"
        }
        repository.saveTranscript(meetingId, transcript.text)
        return transcript
    }
}
