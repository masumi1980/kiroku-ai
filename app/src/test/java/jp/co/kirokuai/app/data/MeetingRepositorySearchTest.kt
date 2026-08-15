package jp.co.kirokuai.app.data

import jp.co.kirokuai.app.database.MeetingDao
import jp.co.kirokuai.app.database.MeetingEntity
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MeetingRepositorySearchTest {
    @Test
    fun `search delegates keyword to DAO and maps meetings`() = runTest {
        val entity = MeetingEntity(
            id = 15,
            title = "Search Result",
            createdAt = 200,
            updatedAt = 300,
            durationMillis = 400,
            audioPath = "meeting.wav",
            status = MeetingStatus.COMPLETED.name,
            transcript = "offline transcript",
            summary = "stored summary",
        )
        val dao = SearchRecordingDao(listOf(entity))
        val repository = RoomMeetingRepository(dao)

        val result = repository.search("offline")

        assertEquals("offline", dao.keyword)
        assertEquals(
            listOf(
                Meeting(
                    id = 15,
                    title = "Search Result",
                    createdAt = 200,
                    updatedAt = 300,
                    duration = 400,
                    audioPath = "meeting.wav",
                    status = MeetingStatus.COMPLETED,
                    transcript = "offline transcript",
                    summary = "stored summary",
                ),
            ),
            result,
        )
    }
}

private class SearchRecordingDao(
    private val searchResults: List<MeetingEntity>,
) : MeetingDao {
    var keyword: String? = null

    override suspend fun search(keyword: String): List<MeetingEntity> {
        this.keyword = keyword
        return searchResults
    }

    override suspend fun insert(meeting: MeetingEntity): Long = meeting.id
    override suspend fun update(meeting: MeetingEntity) = Unit
    override suspend fun delete(meeting: MeetingEntity) = Unit
    override fun getAll(): Flow<List<MeetingEntity>> = emptyFlow()
    override suspend fun getTranscript(meetingId: Long): String? = null
    override suspend fun saveTranscript(
        meetingId: Long,
        transcript: String,
        updatedAt: Long,
        status: String,
    ): Int = 0
}
