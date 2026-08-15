package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchMeetingsUseCaseTest {
    @Test
    fun `search trims keyword and returns repository matches`() = runTest {
        val expected = listOf(meeting())
        val repository = RecordingSearchRepository(expected)
        val useCase = SearchMeetingsUseCase(repository)

        val result = useCase("  project review  ")

        assertEquals("project review", repository.keyword)
        assertEquals(expected, result)
    }

    @Test
    fun `blank keyword returns empty without searching repository`() = runTest {
        val repository = RecordingSearchRepository(listOf(meeting()))
        val useCase = SearchMeetingsUseCase(repository)

        val result = useCase("   ")

        assertEquals(emptyList<Meeting>(), result)
        assertEquals(null, repository.keyword)
    }
}

private class RecordingSearchRepository(
    private val results: List<Meeting>,
) : MeetingRepository {
    var keyword: String? = null

    override suspend fun search(keyword: String): List<Meeting> {
        this.keyword = keyword
        return results
    }

    override suspend fun save(meeting: Meeting): Long = meeting.id
    override suspend fun saveTranscript(meetingId: Long, transcript: String) = Unit
    override suspend fun loadTranscript(meetingId: Long): String? = null
    override fun getAll(): Flow<List<Meeting>> = emptyFlow()
}

private fun meeting() = Meeting(
    id = 1,
    title = "Project Review",
    createdAt = 100,
    duration = 1_000,
    audioPath = "meeting.wav",
    status = MeetingStatus.COMPLETED,
)
