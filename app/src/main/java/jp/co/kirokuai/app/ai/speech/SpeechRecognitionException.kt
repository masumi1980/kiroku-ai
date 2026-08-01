package jp.co.kirokuai.app.ai.speech

sealed class SpeechRecognitionException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class AudioFileMissing(path: String) : SpeechRecognitionException(
        message = "Audio file does not exist: $path",
    )

    class AudioFileUnreadable(path: String) : SpeechRecognitionException(
        message = "Audio file is not readable: $path",
    )

    class InvalidAudio(message: String) : SpeechRecognitionException(message)

    class EmptyTranscript : SpeechRecognitionException("Transcript must not be empty")

    class ModelMissing(path: String) : SpeechRecognitionException(
        message = "Bundled Whisper model is missing: $path",
    )

    class ModelInvalid : SpeechRecognitionException(
        message = "Bundled Whisper model failed integrity verification",
    )

    class NativeInitialization(cause: Throwable) : SpeechRecognitionException(
        message = "Whisper native library could not be initialized",
        cause = cause,
    )

    class NativeTranscription(cause: Throwable) : SpeechRecognitionException(
        message = "Whisper native transcription failed",
        cause = cause,
    )
}
