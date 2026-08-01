package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.model.Meeting

interface MeetingRepository {
    suspend fun save(meeting: Meeting)

    suspend fun getAll(): List<Meeting>
}
