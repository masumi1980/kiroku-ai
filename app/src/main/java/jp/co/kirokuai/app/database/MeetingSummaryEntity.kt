package jp.co.kirokuai.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meeting_summaries")
data class MeetingSummaryEntity(
    @PrimaryKey
    val meetingId: Long,
    val summary: String,
    val decisionsJson: String,
    val discussionJson: String,
    val nextActionsJson: String,
    val risksJson: String,
    val createdAt: Long,
)
