package jp.co.kirokuai.app.data

import jp.co.kirokuai.app.database.MeetingSummaryDao
import jp.co.kirokuai.app.database.MeetingSummaryEntity
import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MeetingSummaryRepositoryTest {
    @Test
    fun `save and load preserve structured summary`() = runTest {
        val repository = RoomMeetingSummaryRepository(InMemorySummaryDao())
        val expected = MeetingSummary(4, 4, "概要", listOf("決定"), listOf("議論"), listOf("対応"), listOf("懸念"), 50)

        repository.save(expected)

        assertEquals(expected, repository.load(4))
    }
}

private class InMemorySummaryDao : MeetingSummaryDao {
    private val entity = MutableStateFlow<MeetingSummaryEntity?>(null)

    override suspend fun save(summary: MeetingSummaryEntity) {
        entity.value = summary
    }

    override suspend fun load(meetingId: Long): MeetingSummaryEntity? =
        entity.value?.takeIf { it.meetingId == meetingId }

    override fun observe(meetingId: Long): Flow<MeetingSummaryEntity?> = entity
}
