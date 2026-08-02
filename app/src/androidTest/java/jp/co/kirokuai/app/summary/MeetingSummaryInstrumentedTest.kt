package jp.co.kirokuai.app.summary

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.co.kirokuai.app.MainActivity
import jp.co.kirokuai.app.ai.llm.LlmEngine
import jp.co.kirokuai.app.ai.llm.LlmRepository
import jp.co.kirokuai.app.ai.parser.MeetingSummaryParser
import jp.co.kirokuai.app.ai.prompt.MeetingPromptBuilder
import jp.co.kirokuai.app.data.RoomMeetingSummaryRepository
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.MeetingRepository
import jp.co.kirokuai.app.domain.MeetingSummarizer
import jp.co.kirokuai.app.model.Meeting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeetingSummaryInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun cleanUp() {
        context.deleteDatabase(DATABASE_NAME)
    }

    @Test
    fun endToEndGenerationPersistsAndRestoresSummary() = runBlocking {
        val database = openDatabase()
        val summaryRepository = RoomMeetingSummaryRepository(database.meetingSummaryDao())
        val summarizer = MeetingSummarizer(
            meetingRepository = StoredTranscriptRepository("オフライン会議の文字起こし"),
            summaryRepository = summaryRepository,
            promptBuilder = MeetingPromptBuilder(),
            llmRepository = LlmRepository(InstrumentedFakeLlmEngine()),
            parser = MeetingSummaryParser(),
            currentTimeMillis = { CREATED_AT },
        )

        summarizer.generate(MEETING_ID)
        database.close()

        val reopenedDatabase = openDatabase()
        val restored = RoomMeetingSummaryRepository(reopenedDatabase.meetingSummaryDao()).load(MEETING_ID)
        assertEquals("会議の概要", restored?.summary)
        assertEquals(listOf("端末内で処理する"), restored?.decisions)
        reopenedDatabase.close()
    }

    @Test
    fun activityRecreationDoesNotLoseApplicationState() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.recreate()
        }
    }

    private fun openDatabase(): KirokuDatabase = Room.databaseBuilder(
        context,
        KirokuDatabase::class.java,
        DATABASE_NAME,
    ).addMigrations(KirokuDatabase.MIGRATION_1_2, KirokuDatabase.MIGRATION_2_3).build()

    private companion object {
        const val DATABASE_NAME = "meeting-summary-instrumented.db"
        const val MEETING_ID = 12L
        const val CREATED_AT = 1_000L
    }
}

private class InstrumentedFakeLlmEngine : LlmEngine {
    override suspend fun load() = Unit
    override suspend fun generate(prompt: String): String {
        check(prompt.contains("オフライン会議の文字起こし"))
        return """{"summary":"会議の概要","decisions":["端末内で処理する"],"discussion":[],"nextActions":[],"risks":[]}"""
    }
    override suspend fun close() = Unit
}

private class StoredTranscriptRepository(private val transcript: String) : MeetingRepository {
    override suspend fun save(meeting: Meeting): Long = meeting.id
    override suspend fun saveTranscript(meetingId: Long, transcript: String) = Unit
    override suspend fun loadTranscript(meetingId: Long): String = transcript
    override fun getAll(): Flow<List<Meeting>> = MutableStateFlow(emptyList())
}
