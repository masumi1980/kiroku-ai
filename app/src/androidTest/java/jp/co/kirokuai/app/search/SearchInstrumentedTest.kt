package jp.co.kirokuai.app.search

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.co.kirokuai.app.data.RoomMeetingRepository
import jp.co.kirokuai.app.data.RoomMeetingSummaryRepository
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private lateinit var database: KirokuDatabase
    private lateinit var meetingRepository: RoomMeetingRepository
    private lateinit var summaryRepository: RoomMeetingSummaryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(context, KirokuDatabase::class.java).build()
        meetingRepository = RoomMeetingRepository(database.meetingDao())
        summaryRepository = RoomMeetingSummaryRepository(database.meetingSummaryDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun searchesTranscript() = runBlocking {
        val meetingId = saveMeeting(transcript = "Discuss the offline roadmap")

        val result = meetingRepository.search("offline roadmap")

        assertEquals(listOf(meetingId), result.map(Meeting::id))
    }

    @Test
    fun searchesTitle() = runBlocking {
        val meetingId = saveMeeting(title = "Quarterly Planning")

        val result = meetingRepository.search("Quarterly")

        assertEquals(listOf(meetingId), result.map(Meeting::id))
    }

    @Test
    fun searchesPersistedSummary() = runBlocking {
        val meetingId = saveMeeting()
        summaryRepository.save(summary(meetingId, "Release candidate approved"))

        val result = meetingRepository.search("candidate approved")

        assertEquals(listOf(meetingId), result.map(Meeting::id))
    }

    @Test
    fun returnsEmptyResult() = runBlocking {
        saveMeeting(title = "Known meeting", transcript = "Known transcript")

        val result = meetingRepository.search("missing")

        assertTrue(result.isEmpty())
    }

    @Test
    fun searchIsCaseInsensitive() = runBlocking {
        val meetingId = saveMeeting(title = "Project Review")

        val result = meetingRepository.search("PROJECT REVIEW")

        assertEquals(listOf(meetingId), result.map(Meeting::id))
    }

    private suspend fun saveMeeting(
        title: String = "Meeting",
        transcript: String = "Transcript",
    ): Long = meetingRepository.save(
        Meeting(
            id = 0,
            title = title,
            createdAt = 100,
            duration = 1_000,
            audioPath = "meeting.wav",
            status = MeetingStatus.COMPLETED,
            transcript = transcript,
        ),
    )

    private fun summary(meetingId: Long, text: String) = MeetingSummary(
        id = meetingId,
        meetingId = meetingId,
        summary = text,
        decisions = emptyList(),
        discussion = emptyList(),
        nextActions = emptyList(),
        risks = emptyList(),
        createdAt = 200,
    )
}
