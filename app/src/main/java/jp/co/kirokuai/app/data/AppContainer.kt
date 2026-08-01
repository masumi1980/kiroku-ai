package jp.co.kirokuai.app.data

import android.content.Context
import jp.co.kirokuai.app.ai.llm.LlamaCppEngine
import jp.co.kirokuai.app.ai.llm.LlmRepository
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import jp.co.kirokuai.app.ai.speech.WhisperSpeechRecognizer
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.MeetingRepository

class AppContainer(context: Context) {
    private val database = KirokuDatabase.create(context)
    private val speechRecognizer = WhisperSpeechRecognizer(context)
    private val llmEngine = LlamaCppEngine(context)

    val meetingRepository: MeetingRepository = RoomMeetingRepository(database.meetingDao())
    val speechRepository = SpeechRepository(speechRecognizer, meetingRepository)
    val llmRepository = LlmRepository(llmEngine)
}
