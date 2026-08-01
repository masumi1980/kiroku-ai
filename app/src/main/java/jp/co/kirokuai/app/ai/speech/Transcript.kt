package jp.co.kirokuai.app.ai.speech

data class Transcript(
    val text: String,
    val language: String? = null,
    val durationMillis: Long = 0,
)
