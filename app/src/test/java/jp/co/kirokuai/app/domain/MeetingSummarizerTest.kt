package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.ai.llm.LlmEngine
import jp.co.kirokuai.app.ai.llm.LlmRepository
import jp.co.kirokuai.app.ai.parser.MeetingSummaryParser
import jp.co.kirokuai.app.ai.prompt.MeetingPromptBuilder
import jp.co.kirokuai.app.ai.summary.MeetingSummaryException
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class MeetingSummarizerTest {
    @Test
    fun `generate loads transcript invokes model parses and saves`() = runTest {
        val summaryRepository = FakeSummaryRepository()
        val engine = RecordingLlmEngine()
        val summarizer = MeetingSummarizer(
            meetingRepository = FakeMeetingRepository("会議の文字起こし"),
            summaryRepository = summaryRepository,
            promptBuilder = MeetingPromptBuilder(),
            llmRepository = LlmRepository(engine),
            parser = MeetingSummaryParser(),
            currentTimeMillis = { 500 },
        )

        val result = summarizer.generate(3)

        assertEquals("概要", result.summary)
        assertEquals(result, summaryRepository.saved.value)
        check(engine.prompt.contains("会議の文字起こし"))
    }

    @Test(expected = MeetingSummaryException.EmptyTranscript::class)
    fun `generate rejects empty transcript before inference`() = runTest {
        MeetingSummarizer(
            FakeMeetingRepository(""),
            FakeSummaryRepository(),
            MeetingPromptBuilder(),
            LlmRepository(RecordingLlmEngine()),
            MeetingSummaryParser(),
        ).generate(3)
    }
}

private class RecordingLlmEngine : LlmEngine {
    var prompt = ""
    override suspend fun load() = Unit
    override suspend fun generate(prompt: String): String {
        this.prompt = prompt
        return """{"summary":"概要","decisions":[],"discussion":[],"nextActions":[],"risks":[]}"""
    }
    override suspend fun close() = Unit
}

private class FakeSummaryRepository : MeetingSummaryRepository {
    val saved = MutableStateFlow<MeetingSummary?>(null)
    override suspend fun save(summary: MeetingSummary) { saved.value = summary }
    override suspend fun load(meetingId: Long): MeetingSummary? = saved.value
    override fun observe(meetingId: Long): Flow<MeetingSummary?> = saved
}

private class FakeMeetingRepository(private val transcript: String?) : MeetingRepository {
    override suspend fun save(meeting: Meeting): Long = meeting.id
    override suspend fun saveTranscript(meetingId: Long, transcript: String) = Unit
    override suspend fun loadTranscript(meetingId: Long): String? = transcript
    override fun getAll(): Flow<List<Meeting>> = MutableStateFlow(emptyList())
}
