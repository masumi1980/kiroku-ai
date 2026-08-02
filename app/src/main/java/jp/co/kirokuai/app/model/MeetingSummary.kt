package jp.co.kirokuai.app.model

data class MeetingSummary(
    val id: Long,
    val meetingId: Long,
    val summary: String,
    val decisions: List<String>,
    val discussion: List<String>,
    val nextActions: List<String>,
    val risks: List<String>,
    val createdAt: Long,
)
