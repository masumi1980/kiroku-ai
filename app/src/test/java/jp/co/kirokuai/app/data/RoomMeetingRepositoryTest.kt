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
            updatedAt = 110L,
            durationMillis = 65L,
            audioPath = "meeting.m4a",
            status = MeetingStatus.COMPLETED.name,
            transcript = "Meeting transcript",
            summary = null,
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
                    updatedAt = 110L,
                    transcript = "Meeting transcript",
                ),
            ),
            repository.getAll().first(),
        )
    }

    @Test
    fun saveTranscript_updatesTranscriptTimestampAndStatusAtomically() = runTest {
        val dao = FakeMeetingDao()
        val repository = RoomMeetingRepository(dao, currentTimeMillis = { 500L })
        dao.entities.value = listOf(
            MeetingEntity(
                id = 9,
                title = "Meeting",
                createdAt = 100L,
                updatedAt = 100L,
                durationMillis = 10L,
                audioPath = "meeting.wav",
                status = MeetingStatus.TRANSCRIBING.name,
                transcript = "",
                summary = null,
            ),
        )

        repository.saveTranscript(9, "Persistent transcript")

        assertEquals("Persistent transcript", repository.loadTranscript(9))
        assertEquals(500L, dao.entities.value.single().updatedAt)
        assertEquals(MeetingStatus.COMPLETED.name, dao.entities.value.single().status)
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

    override suspend fun getTranscript(meetingId: Long): String? =
        entities.value.firstOrNull { it.id == meetingId }?.transcript

    override suspend fun search(keyword: String): List<MeetingEntity> =
        entities.value.filter { entity ->
            entity.title.contains(keyword, ignoreCase = true) ||
                entity.transcript.contains(keyword, ignoreCase = true) ||
                entity.summary?.contains(keyword, ignoreCase = true) == true
        }

    override suspend fun saveTranscript(
        meetingId: Long,
        transcript: String,
        updatedAt: Long,
        status: String,
    ): Int {
        var updated = false
        entities.value = entities.value.map { entity ->
            if (entity.id == meetingId) {
                updated = true
                entity.copy(transcript = transcript, updatedAt = updatedAt, status = status)
            } else {
                entity
            }
        }
        return if (updated) 1 else 0
    }
}
