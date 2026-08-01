package jp.co.kirokuai.app.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Insert
    suspend fun insert(meeting: MeetingEntity): Long

    @Update
    suspend fun update(meeting: MeetingEntity)

    @Delete
    suspend fun delete(meeting: MeetingEntity)

    @Query("SELECT * FROM meetings ORDER BY createdAt DESC")
    fun getAll(): Flow<List<MeetingEntity>>

    @Query("SELECT transcript FROM meetings WHERE id = :meetingId")
    suspend fun getTranscript(meetingId: Long): String?

    @Transaction
    @Query(
        """
        UPDATE meetings
        SET transcript = :transcript, updatedAt = :updatedAt, status = :status
        WHERE id = :meetingId
        """,
    )
    suspend fun saveTranscript(
        meetingId: Long,
        transcript: String,
        updatedAt: Long,
        status: String,
    ): Int
}
