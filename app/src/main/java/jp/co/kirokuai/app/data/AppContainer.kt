package jp.co.kirokuai.app.data

import android.content.Context
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import jp.co.kirokuai.app.ai.speech.WhisperSpeechRecognizer
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.MeetingRepository

class AppContainer(context: Context) {
    private val database = KirokuDatabase.create(context)
    private val speechRecognizer = WhisperSpeechRecognizer(context)

    val meetingRepository: MeetingRepository = RoomMeetingRepository(database.meetingDao())
    val speechRepository = SpeechRepository(speechRecognizer)
}
