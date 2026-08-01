package jp.co.kirokuai.app.model

data class Meeting(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val duration: Long,
    val audioPath: String?,
    val status: MeetingStatus,
    val updatedAt: Long = createdAt,
    val transcript: String = "",
    val summary: String? = null,
)
