package jp.co.kirokuai.app.ai.summary

sealed class MeetingSummaryException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class EmptyTranscript : MeetingSummaryException("Transcript is empty")
    class InvalidJson(cause: Throwable? = null) : MeetingSummaryException("Meeting summary JSON is invalid", cause)
    class ModelError(cause: Throwable) : MeetingSummaryException("Meeting summary generation failed", cause)
    class DatabaseError(cause: Throwable) : MeetingSummaryException("Meeting summary persistence failed", cause)
}
