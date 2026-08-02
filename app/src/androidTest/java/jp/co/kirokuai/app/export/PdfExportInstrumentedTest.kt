package jp.co.kirokuai.app.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import jp.co.kirokuai.app.data.RoomMeetingRepository
import jp.co.kirokuai.app.data.RoomMeetingSummaryRepository
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.PdfExportUseCase
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingSummary
import jp.co.kirokuai.app.model.MeetingStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfExportInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private var exportedUri: Uri? = null

    @After
    fun cleanUp() {
        context.deleteDatabase(DATABASE_NAME)
        exportedUri?.let { uri -> context.contentResolver.delete(uri, null, null) }
    }

    @Test
    @SdkSuppress(minSdkVersion = 35)
    fun exportsExistingSummaryAsMultipagePdfThroughDocumentUriWithoutMutation() = runBlocking {
        context.deleteDatabase(DATABASE_NAME)
        val database = Room.databaseBuilder(context, KirokuDatabase::class.java, DATABASE_NAME).build()
        try {
            val repository = RoomMeetingSummaryRepository(database.meetingSummaryDao())
            val meetingRepository = RoomMeetingRepository(database.meetingDao())
            val meetingId = meetingRepository.save(meeting())
            val originalSummary = largeSummary(meetingId)
            repository.save(originalSummary)
            val createDocumentIntent = ActivityResultContracts.CreateDocument(PDF_MIME_TYPE)
                .createIntent(context, REQUIRED_FILE_NAME)
            assertEquals(Intent.ACTION_CREATE_DOCUMENT, createDocumentIntent.action)
            assertEquals(PDF_MIME_TYPE, createDocumentIntent.type)
            assertEquals(REQUIRED_FILE_NAME, createDocumentIntent.getStringExtra(Intent.EXTRA_TITLE))
            val documentUri = requireNotNull(
                context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, REQUIRED_FILE_NAME)
                        put(MediaStore.MediaColumns.MIME_TYPE, PDF_MIME_TYPE)
                    },
                ),
            )
            exportedUri = documentUri
            val generator = PdfGenerator()
            val exporter = SafPdfExporter(
                generator = generator,
                openOutputStream = { destination ->
                    context.contentResolver.openOutputStream(Uri.parse(destination), "wt")
                },
            )
            val useCase = PdfExportUseCase(repository, meetingRepository, exporter)

            val result = useCase.export(meetingId, documentUri.toString())

            assertTrue(result.exceptionOrNull()?.stackTraceToString(), result.isSuccess)
            val bytes = context.contentResolver.openInputStream(documentUri).use { input ->
                requireNotNull(input).readBytes()
            }
            assertTrue(bytes.size > PDF_HEADER.length)
            assertEquals(PDF_HEADER, bytes.copyOfRange(0, PDF_HEADER.length).toString(Charsets.US_ASCII))
            val descriptor = requireNotNull(context.contentResolver.openFileDescriptor(documentUri, "r"))
            PdfRenderer(descriptor).use { renderer ->
                assertTrue(renderer.pageCount > 1)
                val exportedText = buildString {
                    repeat(renderer.pageCount) { pageIndex ->
                        renderer.openPage(pageIndex).use { page ->
                            if (pageIndex == 0) {
                                assertEquals(A4_PAGE_WIDTH, page.width)
                                assertEquals(A4_PAGE_HEIGHT, page.height)
                            }
                            page.textContents.forEach { content -> appendLine(content.text) }
                        }
                    }
                }
                assertTrue(exportedText.contains("Meeting Summary"))
                assertTrue(exportedText.contains("Meeting date: 2026-08-02 19:30"))
                assertTrue(exportedText.contains("Meeting summary details"))
                assertTrue(exportedText.contains("Decisions"))
                assertTrue(exportedText.contains("Store PDF on device"))
                assertTrue(exportedText.contains("Discussion"))
                assertTrue(exportedText.contains("Use an A4 layout"))
                assertTrue(exportedText.contains("Next Actions"))
                assertTrue(exportedText.contains("Review exported content"))
                assertTrue(exportedText.contains("Risks"))
                assertTrue(exportedText.contains("Do not upload to cloud"))
            }
            assertEquals(originalSummary, repository.load(meetingId))
        } finally {
            database.close()
        }
    }

    private fun meeting() = Meeting(
        id = 0,
        title = "PDFエクスポート会議",
        createdAt = MEETING_DATE_MILLIS,
        duration = 60_000,
        audioPath = "meeting.wav",
        status = MeetingStatus.COMPLETED,
    )

    private fun largeSummary(meetingId: Long) = MeetingSummary(
        id = meetingId,
        meetingId = meetingId,
        summary = "Meeting summary details ".repeat(LARGE_SUMMARY_REPETITIONS),
        decisions = listOf("Store PDF on device"),
        discussion = listOf("Use an A4 layout"),
        nextActions = listOf("Review exported content"),
        risks = listOf("Do not upload to cloud"),
        createdAt = SUMMARY_GENERATED_AT_MILLIS,
    )

    private companion object {
        const val DATABASE_NAME = "pdf-export-instrumented.db"
        const val LARGE_SUMMARY_REPETITIONS = 20_000
        const val PDF_MIME_TYPE = "application/pdf"
        const val REQUIRED_FILE_NAME = "meeting-20260802-1930.pdf"
        const val PDF_HEADER = "%PDF-"
        const val A4_PAGE_WIDTH = 595
        const val A4_PAGE_HEIGHT = 842
        const val MEETING_DATE_MILLIS = 1_785_699_000_000
        const val SUMMARY_GENERATED_AT_MILLIS = 1_785_702_600_000
    }
}
