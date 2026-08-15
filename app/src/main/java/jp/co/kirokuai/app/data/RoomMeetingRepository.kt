package jp.co.kirokuai.app.data

import jp.co.kirokuai.app.database.MeetingDao
import jp.co.kirokuai.app.database.MeetingEntity
import jp.co.kirokuai.app.domain.MeetingRepository
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMeetingRepository(
    private val meetingDao: MeetingDao,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) : MeetingRepository {
    override suspend fun save(meeting: Meeting): Long {
        val entity = meeting.toEntity()
        return if (entity.id == 0L) {
            meetingDao.insert(entity)
        } else {
            meetingDao.update(entity)
            entity.id
        }
    }

    override suspend fun saveTranscript(meetingId: Long, transcript: String) {
        require(transcript.isNotBlank()) { "Transcript must not be empty" }
        val updatedRows = meetingDao.saveTranscript(
            meetingId = meetingId,
            transcript = transcript,
            updatedAt = currentTimeMillis(),
            status = MeetingStatus.COMPLETED.name,
        )
        check(updatedRows == 1) { "Meeting not found: $meetingId" }
    }

    override suspend fun loadTranscript(meetingId: Long): String? =
        meetingDao.getTranscript(meetingId)

    override suspend fun search(keyword: String): List<Meeting> =
        meetingDao.search(keyword).map(MeetingEntity::toModel)

    override fun getAll(): Flow<List<Meeting>> =
        meetingDao.getAll().map { entities ->
            entities.map(MeetingEntity::toModel)
        }
}

internal fun Meeting.toEntity(): MeetingEntity = MeetingEntity(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    durationMillis = duration,
    audioPath = requireNotNull(audioPath) { "A saved meeting requires an audio path" },
    status = status.name,
    transcript = transcript,
    summary = summary,
)

internal fun MeetingEntity.toModel(): Meeting = Meeting(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    duration = durationMillis,
    audioPath = audioPath,
    status = runCatching { MeetingStatus.valueOf(status) }.getOrDefault(MeetingStatus.ERROR),
    transcript = transcript,
    summary = summary,
)
