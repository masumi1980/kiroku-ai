package jp.co.kirokuai.app.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingSummaryDao {
    @Upsert
    @Transaction
    suspend fun save(summary: MeetingSummaryEntity)

    @Query("SELECT * FROM meeting_summaries WHERE meetingId = :meetingId")
    suspend fun load(meetingId: Long): MeetingSummaryEntity?

    @Query("SELECT * FROM meeting_summaries WHERE meetingId = :meetingId")
    fun observe(meetingId: Long): Flow<MeetingSummaryEntity?>
}
