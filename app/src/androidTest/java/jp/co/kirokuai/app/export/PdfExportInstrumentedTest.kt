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
import jp.co.kirokuai.app.data.RoomMeetingSummaryRepository
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.PdfExportUseCase
import jp.co.kirokuai.app.model.MeetingSummary
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
    fun exportsExistingSummaryAsMultipagePdfThroughDocumentUriWithoutMutation() = runBlocking {
        context.deleteDatabase(DATABASE_NAME)
        val database = Room.databaseBuilder(context, KirokuDatabase::class.java, DATABASE_NAME).build()
        try {
            val repository = RoomMeetingSummaryRepository(database.meetingSummaryDao())
            val originalSummary = largeSummary()
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
            val useCase = PdfExportUseCase(repository, exporter)

            val result = useCase.export(MEETING_ID, documentUri.toString())

            assertTrue(result.exceptionOrNull()?.stackTraceToString(), result.isSuccess)
            val bytes = context.contentResolver.openInputStream(documentUri).use { input ->
                requireNotNull(input).readBytes()
            }
            assertTrue(bytes.size > PDF_HEADER.length)
            assertEquals(PDF_HEADER, bytes.copyOfRange(0, PDF_HEADER.length).toString(Charsets.US_ASCII))
            val content = generator.generate(originalSummary).pages.flatMap(PdfPage::lines).map(PdfLine::text)
            assertTrue(content.contains("Meeting Summary"))
            assertTrue(content.any { line -> line.contains("日本語") })
            assertTrue(content.contains("Decisions"))
            assertTrue(content.contains("Discussion"))
            assertTrue(content.contains("Next Actions"))
            assertTrue(content.contains("Risks"))
            val descriptor = requireNotNull(context.contentResolver.openFileDescriptor(documentUri, "r"))
            PdfRenderer(descriptor).use { renderer ->
                assertTrue(renderer.pageCount > 1)
                renderer.openPage(0).use { page ->
                    assertEquals(A4_PAGE_WIDTH, page.width)
                    assertEquals(A4_PAGE_HEIGHT, page.height)
                }
            }
            assertEquals(originalSummary, repository.load(MEETING_ID))
        } finally {
            database.close()
        }
    }

    private fun largeSummary() = MeetingSummary(
        id = MEETING_ID,
        meetingId = MEETING_ID,
        summary = "日本語の会議要約".repeat(LARGE_SUMMARY_REPETITIONS),
        decisions = listOf("端末内でPDFを作成する"),
        discussion = listOf("A4レイアウトを使用する"),
        nextActions = listOf("出力内容を確認する"),
        risks = listOf("クラウドへ送信しない"),
        createdAt = 1_785_699_000_000,
    )

    private companion object {
        const val DATABASE_NAME = "pdf-export-instrumented.db"
        const val MEETING_ID = 14L
        const val LARGE_SUMMARY_REPETITIONS = 20_000
        const val PDF_MIME_TYPE = "application/pdf"
        const val REQUIRED_FILE_NAME = "meeting-20260802-1930.pdf"
        const val PDF_HEADER = "%PDF-"
        const val A4_PAGE_WIDTH = 595
        const val A4_PAGE_HEIGHT = 842
    }
}
