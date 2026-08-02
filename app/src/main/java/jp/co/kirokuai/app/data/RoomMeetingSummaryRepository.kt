package jp.co.kirokuai.app.data

import jp.co.kirokuai.app.ai.parser.MeetingSummaryParser
import jp.co.kirokuai.app.ai.summary.MeetingSummaryException
import jp.co.kirokuai.app.database.MeetingSummaryDao
import jp.co.kirokuai.app.database.MeetingSummaryEntity
import jp.co.kirokuai.app.domain.MeetingSummaryRepository
import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomMeetingSummaryRepository(
    private val dao: MeetingSummaryDao,
    private val parser: MeetingSummaryParser = MeetingSummaryParser(),
) : MeetingSummaryRepository {
    override suspend fun save(summary: MeetingSummary) {
        try {
            dao.save(summary.toEntity())
        } catch (exception: Exception) {
            throw MeetingSummaryException.DatabaseError(exception)
        }
    }

    override suspend fun load(meetingId: Long): MeetingSummary? = try {
        dao.load(meetingId)?.toModel(parser)
    } catch (exception: MeetingSummaryException) {
        throw exception
    } catch (exception: Exception) {
        throw MeetingSummaryException.DatabaseError(exception)
    }

    override fun observe(meetingId: Long): Flow<MeetingSummary?> =
        dao.observe(meetingId).map { entity -> entity?.toModel(parser) }
}

private fun MeetingSummary.toEntity() = MeetingSummaryEntity(
    meetingId = meetingId,
    summary = summary,
    decisionsJson = decisions.toJson(),
    discussionJson = discussion.toJson(),
    nextActionsJson = nextActions.toJson(),
    risksJson = risks.toJson(),
    createdAt = createdAt,
)

private fun MeetingSummaryEntity.toModel(parser: MeetingSummaryParser): MeetingSummary = parser.parse(
    json = """{"summary":${summary.toJson()},"decisions":$decisionsJson,"discussion":$discussionJson,"nextActions":$nextActionsJson,"risks":$risksJson}""",
    meetingId = meetingId,
    createdAt = createdAt,
)

private fun List<String>.toJson(): String = joinToString(prefix = "[", postfix = "]") { it.toJson() }

private fun String.toJson(): String = buildString {
    append('"')
    this@toJson.forEach { character ->
        when (character) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\b' -> append("\\b")
            '\u000C' -> append("\\f")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (character < ' ') {
                append("\\u")
                append(character.code.toString(16).padStart(4, '0'))
            } else {
                append(character)
            }
        }
    }
    append('"')
}
