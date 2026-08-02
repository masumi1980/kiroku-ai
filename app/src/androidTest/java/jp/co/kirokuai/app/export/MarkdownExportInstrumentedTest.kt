package jp.co.kirokuai.app.export

import android.content.Context
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.co.kirokuai.app.data.RoomMeetingSummaryRepository
import jp.co.kirokuai.app.database.KirokuDatabase
import jp.co.kirokuai.app.domain.MarkdownExportUseCase
import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MarkdownExportInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private var exportedUri: Uri? = null

    @After
    fun cleanUp() {
        context.deleteDatabase(DATABASE_NAME)
        exportedUri?.let { uri -> context.contentResolver.delete(uri, null, null) }
    }

    @Test
    fun exportsLargeExistingSummaryThroughStorageAccessFrameworkWithoutMutation() = runBlocking {
        context.deleteDatabase(DATABASE_NAME)
        val database = Room.databaseBuilder(context, KirokuDatabase::class.java, DATABASE_NAME).build()
        val repository = RoomMeetingSummaryRepository(database.meetingSummaryDao())
        val originalSummary = largeSummary()
        repository.save(originalSummary)
        val createDocumentIntent = ActivityResultContracts.CreateDocument(MARKDOWN_MIME_TYPE)
            .createIntent(context, REQUIRED_FILE_NAME)
        assertEquals(Intent.ACTION_CREATE_DOCUMENT, createDocumentIntent.action)
        assertEquals(MARKDOWN_MIME_TYPE, createDocumentIntent.type)
        assertEquals(REQUIRED_FILE_NAME, createDocumentIntent.getStringExtra(Intent.EXTRA_TITLE))
        val documentUri = requireNotNull(
            context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, REQUIRED_FILE_NAME)
                    put(MediaStore.MediaColumns.MIME_TYPE, MARKDOWN_MIME_TYPE)
                },
            ),
        )
        exportedUri = documentUri
        val exporter = SafMarkdownExporter(
            openOutputStream = { destination ->
                context.contentResolver.openOutputStream(Uri.parse(destination), "wt")
            },
        )
        val useCase = MarkdownExportUseCase(repository, exporter)

        val result = useCase.export(MEETING_ID, documentUri.toString())

        assertTrue(result.exceptionOrNull()?.stackTraceToString(), result.isSuccess)
        val bytes = context.contentResolver.openInputStream(documentUri).use { input ->
            requireNotNull(input).readBytes()
        }
        val markdown = bytes.toString(Charsets.UTF_8)
        assertTrue(markdown.startsWith("# Meeting Summary\n\n## Summary\n\n日本語"))
        assertTrue(markdown.contains("## Next Actions\n\nNone"))
        assertFalse(markdown.contains('\r'))
        assertEquals(markdown, bytes.toString(Charsets.UTF_8))
        assertEquals(originalSummary, repository.load(MEETING_ID))
        database.close()
    }

    private fun largeSummary() = MeetingSummary(
        id = MEETING_ID,
        meetingId = MEETING_ID,
        summary = "日本語".repeat(LARGE_SUMMARY_REPETITIONS),
        decisions = listOf("端末内に保存する"),
        discussion = listOf("UTF-8とLFを使用する"),
        nextActions = emptyList(),
        risks = listOf("クラウドへ送信しない"),
        createdAt = 1_000,
    )

    private companion object {
        const val DATABASE_NAME = "markdown-export-instrumented.db"
        const val MEETING_ID = 13L
        const val LARGE_SUMMARY_REPETITIONS = 20_000
        const val MARKDOWN_MIME_TYPE = "text/markdown"
        const val REQUIRED_FILE_NAME = "Meeting_20260802_193000.md"
    }
}
