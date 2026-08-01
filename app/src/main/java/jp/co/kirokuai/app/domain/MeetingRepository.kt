package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.model.Meeting
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    suspend fun save(meeting: Meeting)

    fun getAll(): Flow<List<Meeting>>
}
