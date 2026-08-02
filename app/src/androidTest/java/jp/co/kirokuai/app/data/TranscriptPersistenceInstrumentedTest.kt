package jp.co.kirokuai.app.data

import android.Manifest
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import jp.co.kirokuai.app.ai.speech.WhisperSpeechRecognizer
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TranscriptPersistenceInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun recorder_createsWhisperCompatibleWaveFile() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.grantRuntimePermission(
            context.packageName,
            Manifest.permission.RECORD_AUDIO,
        )
        val audioFile = File(context.cacheDir, "recording-flow-test.wav")
        val recorder = PcmWavAudioRecorder(context)
        try {
            recorder.start(audioFile.absolutePath)
            Thread.sleep(RECORDING_TEST_DURATION_MILLIS)
            recorder.stop()

            val header = audioFile.inputStream().use { input -> ByteArray(12).also(input::read) }
            assertEquals("RIFF", header.copyOfRange(0, 4).toString(Charsets.US_ASCII))
            assertEquals("WAVE", header.copyOfRange(8, 12).toString(Charsets.US_ASCII))
            assertTrue(audioFile.length() > WAV_HEADER_SIZE)
        } finally {
            audioFile.delete()
        }
    }

    @Test
    fun whisperTranscript_persistsAfterDatabaseReopen() = runBlocking {
        context.deleteDatabase(PERSISTENCE_DATABASE_NAME)
        val audioFile = copyTestAudio()
        var database = openDatabase(PERSISTENCE_DATABASE_NAME)
        try {
            var meetingRepository = RoomMeetingRepository(database.meetingDao())
            val meetingId = meetingRepository.save(testMeeting())
            val speechRepository = SpeechRepository(
                speechRecognizer = WhisperSpeechRecognizer(context),
                meetingRepository = meetingRepository,
            )

            val transcript = speechRepository.transcribeAndPersist(meetingId, audioFile)
            database.close()

            database = openDatabase(PERSISTENCE_DATABASE_NAME)
            meetingRepository = RoomMeetingRepository(database.meetingDao())
            assertTrue(transcript.text.lowercase().contains(EXPECTED_TRANSCRIPT_PHRASE))
            assertEquals(transcript.text, meetingRepository.loadTranscript(meetingId))
            assertEquals(MeetingStatus.COMPLETED, meetingRepository.getAll().first().single().status)
        } finally {
            database.close()
            audioFile.delete()
            context.deleteDatabase(PERSISTENCE_DATABASE_NAME)
        }
    }

    @Test
    fun migrationFromVersionOne_preservesExistingRecording() = runBlocking {
        context.deleteDatabase(MIGRATION_DATABASE_NAME)
        createVersionOneDatabase()
        val database = openDatabase(MIGRATION_DATABASE_NAME)
        try {
            val meeting = RoomMeetingRepository(database.meetingDao()).getAll().first().single()

            assertEquals(LEGACY_TITLE, meeting.title)
            assertEquals(LEGACY_AUDIO_PATH, meeting.audioPath)
            assertEquals(LEGACY_CREATED_AT, meeting.updatedAt)
            assertEquals("", meeting.transcript)
        } finally {
            database.close()
            context.deleteDatabase(MIGRATION_DATABASE_NAME)
        }
    }

    private fun openDatabase(name: String): KirokuDatabase =
        Room.databaseBuilder(context, KirokuDatabase::class.java, name)
            .addMigrations(KirokuDatabase.MIGRATION_1_2, KirokuDatabase.MIGRATION_2_3)
            .build()

    private fun createVersionOneDatabase() {
        context.openOrCreateDatabase(MIGRATION_DATABASE_NAME, Context.MODE_PRIVATE, null).use { db ->
            db.execSQL(
                """
                CREATE TABLE meetings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    duration INTEGER NOT NULL,
                    audioPath TEXT NOT NULL,
                    status TEXT NOT NULL
                )
                """.trimIndent(),
            )
            db.execSQL(
                """
                INSERT INTO meetings (id, title, createdAt, duration, audioPath, status)
                VALUES (1, ?, ?, 15, ?, ?)
                """.trimIndent(),
                arrayOf<Any>(
                    LEGACY_TITLE,
                    LEGACY_CREATED_AT,
                    LEGACY_AUDIO_PATH,
                    MeetingStatus.COMPLETED.name,
                ),
            )
            db.version = 1
        }
    }

    private fun copyTestAudio(): File =
        File(context.cacheDir, "transcript-persistence-jfk.wav").also { destination ->
            InstrumentationRegistry.getInstrumentation().context.assets.open("jfk.wav").use { input ->
                destination.outputStream().use(input::copyTo)
            }
        }

    private fun testMeeting(): Meeting = Meeting(
        id = 0,
        title = "Transcript persistence test",
        createdAt = 100L,
        duration = 11L,
        audioPath = "jfk.wav",
        status = MeetingStatus.TRANSCRIBING,
    )

    private companion object {
        const val PERSISTENCE_DATABASE_NAME = "transcript-persistence-test.db"
        const val MIGRATION_DATABASE_NAME = "transcript-migration-test.db"
        const val EXPECTED_TRANSCRIPT_PHRASE = "ask not what your country can do for you"
        const val LEGACY_TITLE = "Existing recording"
        const val LEGACY_AUDIO_PATH = "recording.m4a"
        const val LEGACY_CREATED_AT = 123L
        const val RECORDING_TEST_DURATION_MILLIS = 500L
        const val WAV_HEADER_SIZE = 44L
    }
}
