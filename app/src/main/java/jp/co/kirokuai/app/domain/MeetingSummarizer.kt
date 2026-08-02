package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.ai.llm.LlmRepository
import jp.co.kirokuai.app.ai.parser.MeetingSummaryParser
import jp.co.kirokuai.app.ai.prompt.MeetingPromptBuilder
import jp.co.kirokuai.app.ai.summary.MeetingSummaryException
import jp.co.kirokuai.app.model.MeetingSummary

class MeetingSummarizer(
    private val meetingRepository: MeetingRepository,
    private val summaryRepository: MeetingSummaryRepository,
    private val promptBuilder: MeetingPromptBuilder,
    private val llmRepository: LlmRepository,
    private val parser: MeetingSummaryParser,
    private val currentTimeMillis: () -> Long = System::currentTimeMillis,
) {
    suspend fun generate(meetingId: Long): MeetingSummary {
        val transcript = meetingRepository.loadTranscript(meetingId)
        if (transcript.isNullOrBlank()) throw MeetingSummaryException.EmptyTranscript()

        val generatedJson = try {
            llmRepository.load()
            llmRepository.generate(promptBuilder.build(transcript))
        } catch (exception: Exception) {
            throw MeetingSummaryException.ModelError(exception)
        }

        val summary = parser.parse(generatedJson, meetingId, currentTimeMillis())
        summaryRepository.save(summary)
        return summary
    }
}
