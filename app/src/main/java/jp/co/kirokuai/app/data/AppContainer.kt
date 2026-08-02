package jp.co.kirokuai.app.data

import android.content.Context
import androidx.core.net.toUri
import jp.co.kirokuai.app.ai.llm.LlamaCppEngine
import jp.co.kirokuai.app.ai.llm.LlmRepository
import jp.co.kirokuai.app.ai.parser.MeetingSummaryParser
import jp.co.kirokuai.app.ai.prompt.MeetingPromptBuilder
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import jp.co.kirokuai.app.ai.speech.WhisperSpeechRecognizer
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.MeetingRepository
import jp.co.kirokuai.app.domain.MarkdownExportUseCase
import jp.co.kirokuai.app.domain.MeetingSummarizer
import jp.co.kirokuai.app.domain.MeetingSummaryRepository
import jp.co.kirokuai.app.export.SafMarkdownExporter

class AppContainer(context: Context) {
    private val database = KirokuDatabase.create(context)
    private val speechRecognizer = WhisperSpeechRecognizer(context)
    private val llmEngine = LlamaCppEngine(context)

    val meetingRepository: MeetingRepository = RoomMeetingRepository(database.meetingDao())
    val speechRepository = SpeechRepository(speechRecognizer, meetingRepository)
    val llmRepository = LlmRepository(llmEngine)
    val meetingSummaryRepository: MeetingSummaryRepository =
        RoomMeetingSummaryRepository(database.meetingSummaryDao())
    val meetingSummarizer = MeetingSummarizer(
        meetingRepository = meetingRepository,
        summaryRepository = meetingSummaryRepository,
        promptBuilder = MeetingPromptBuilder(),
        llmRepository = llmRepository,
        parser = MeetingSummaryParser(),
    )
    val markdownExportUseCase = MarkdownExportUseCase(
        summaryRepository = meetingSummaryRepository,
        exporter = SafMarkdownExporter(
            openOutputStream = { destination ->
                context.contentResolver.openOutputStream(destination.toUri(), "wt")
            },
        ),
    )
}
