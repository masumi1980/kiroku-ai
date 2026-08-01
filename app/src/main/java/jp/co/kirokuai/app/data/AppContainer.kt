package jp.co.kirokuai.app.data

import android.content.Context
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.MeetingRepository

class AppContainer(context: Context) {
    private val database = KirokuDatabase.create(context)

    val meetingRepository: MeetingRepository = RoomMeetingRepository(database.meetingDao())
}
