package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.model.Meeting
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    suspend fun save(meeting: Meeting): Long

    suspend fun saveTranscript(meetingId: Long, transcript: String)

    suspend fun loadTranscript(meetingId: Long): String?

    suspend fun search(keyword: String): List<Meeting>

    fun getAll(): Flow<List<Meeting>>
}
