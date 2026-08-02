package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.export.MarkdownExportException
import jp.co.kirokuai.app.export.MarkdownExporter
import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownExportUseCaseTest {
    @Test
    fun `export loads existing summary and calls exporter`() = runTest {
        val summary = summary()
        val exporter = RecordingMarkdownExporter()
        val useCase = MarkdownExportUseCase(FakeMarkdownSummaryRepository(summary), exporter)

        val result = useCase.export(summary.meetingId, "content://document/export")

        assertTrue(result.isSuccess)
        assertEquals(summary, exporter.exportedSummary)
        assertEquals("content://document/export", exporter.destination)
    }

    @Test
    fun `export fails when summary does not exist`() = runTest {
        val useCase = MarkdownExportUseCase(FakeMarkdownSummaryRepository(null), RecordingMarkdownExporter())

        val error = useCase.export(1, "destination").exceptionOrNull()

        assertTrue(error is MarkdownExportException.NoSummary)
    }

    @Test
    fun `export fails when file creation is cancelled`() = runTest {
        val useCase = MarkdownExportUseCase(FakeMarkdownSummaryRepository(summary()), RecordingMarkdownExporter())

        val error = useCase.export(1, null).exceptionOrNull()

        assertTrue(error is MarkdownExportException.FileCreationCancelled)
    }

    @Test
    fun `default filename is supplied by exporter`() {
        val useCase = MarkdownExportUseCase(FakeMarkdownSummaryRepository(summary()), RecordingMarkdownExporter())

        assertEquals("Meeting_20260802_193000.md", useCase.defaultFileName())
    }
}

private class RecordingMarkdownExporter : MarkdownExporter {
    var exportedSummary: MeetingSummary? = null
    var destination: String? = null

    override fun defaultFileName(): String = "Meeting_20260802_193000.md"

    override fun export(summary: MeetingSummary, destination: String) {
        exportedSummary = summary
        this.destination = destination
    }
}

private class FakeMarkdownSummaryRepository(summary: MeetingSummary?) : MeetingSummaryRepository {
    private val storedSummary = MutableStateFlow(summary)
    override suspend fun save(summary: MeetingSummary) { storedSummary.value = summary }
    override suspend fun load(meetingId: Long): MeetingSummary? = storedSummary.value
    override fun observe(meetingId: Long): Flow<MeetingSummary?> = storedSummary
}

private fun summary() = MeetingSummary(
    id = 1,
    meetingId = 1,
    summary = "概要",
    decisions = emptyList(),
    discussion = emptyList(),
    nextActions = emptyList(),
    risks = emptyList(),
    createdAt = 0,
)
