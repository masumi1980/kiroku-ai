package jp.co.kirokuai.app.database

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(name = "duration")
    val durationMillis: Long,
    val audioPath: String,
    val status: String,
    val transcript: String,
    val summary: String?,
)
