package jp.co.kirokuai.app.data

import jp.co.kirokuai.app.database.MeetingDao
import jp.co.kirokuai.app.database.MeetingEntity
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RoomMeetingRepositoryTest {
    @Test
    fun getAll_convertsEntitiesAndObservesUpdates() = runTest {
        val dao = FakeMeetingDao()
        val repository = RoomMeetingRepository(dao)
        val entity = MeetingEntity(
            id = 7,
            title = "Project Review",
            createdAt = 100L,
            duration = 65L,
            audioPath = "meeting.m4a",
            status = MeetingStatus.COMPLETED.name,
        )

        dao.entities.value = listOf(entity)

        assertEquals(
            listOf(
                Meeting(
                    id = 7,
                    title = "Project Review",
                    createdAt = 100L,
                    duration = 65L,
                    audioPath = "meeting.m4a",
                    status = MeetingStatus.COMPLETED,
                ),
            ),
            repository.getAll().first(),
        )
    }
}

private class FakeMeetingDao : MeetingDao {
    val entities = MutableStateFlow<List<MeetingEntity>>(emptyList())

    override suspend fun insert(meeting: MeetingEntity): Long {
        entities.value += meeting.copy(id = 1)
        return 1
    }

    override suspend fun update(meeting: MeetingEntity) {
        entities.value = entities.value.map { if (it.id == meeting.id) meeting else it }
    }

    override suspend fun delete(meeting: MeetingEntity) {
        entities.value = entities.value.filterNot { it.id == meeting.id }
    }

    override fun getAll(): Flow<List<MeetingEntity>> = entities
}
