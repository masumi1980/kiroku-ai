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
) : MeetingRepository {
    override suspend fun save(meeting: Meeting) {
        val entity = meeting.toEntity()
        if (entity.id == 0L) {
            meetingDao.insert(entity)
        } else {
            meetingDao.update(entity)
        }
    }

    override fun getAll(): Flow<List<Meeting>> =
        meetingDao.getAll().map { entities ->
            entities.map(MeetingEntity::toModel)
        }
}

internal fun Meeting.toEntity(): MeetingEntity = MeetingEntity(
    id = id,
    title = title,
    createdAt = createdAt,
    duration = duration,
    audioPath = requireNotNull(audioPath) { "A saved meeting requires an audio path" },
    status = status.name,
)

internal fun MeetingEntity.toModel(): Meeting = Meeting(
    id = id,
    title = title,
    createdAt = createdAt,
    duration = duration,
    audioPath = audioPath,
    status = runCatching { MeetingStatus.valueOf(status) }.getOrDefault(MeetingStatus.ERROR),
)
