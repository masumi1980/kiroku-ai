package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.flow.Flow

interface MeetingSummaryRepository {
    suspend fun save(summary: MeetingSummary)

    suspend fun load(meetingId: Long): MeetingSummary?

    fun observe(meetingId: Long): Flow<MeetingSummary?>
}
