package jp.co.kirokuai.app.domain

import java.time.Instant
import java.time.ZoneOffset
import jp.co.kirokuai.app.export.PdfExportException
import jp.co.kirokuai.app.export.PdfExporter
import jp.co.kirokuai.app.export.PdfGenerator
import jp.co.kirokuai.app.export.SafPdfExporter
import jp.co.kirokuai.app.model.MeetingSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfExportUseCaseTest {
    @Test
    fun `export loads existing summary and calls exporter`() = runTest {
        val summary = summary()
        val exporter = RecordingPdfExporter()
        val useCase = PdfExportUseCase(FakePdfSummaryRepository(summary), exporter)

        val result = useCase.export(summary.meetingId, "content://document/export")

        assertTrue(result.isSuccess)
        assertEquals(summary, exporter.exportedSummary)
        assertEquals("content://document/export", exporter.destination)
    }

    @Test
    fun `export fails when summary does not exist`() = runTest {
        val useCase = PdfExportUseCase(FakePdfSummaryRepository(null), RecordingPdfExporter())

        val error = useCase.export(1, "destination").exceptionOrNull()

        assertTrue(error is PdfExportException.NoSummary)
    }

    @Test
    fun `export fails when file creation is cancelled`() = runTest {
        val useCase = PdfExportUseCase(FakePdfSummaryRepository(summary()), RecordingPdfExporter())

        val error = useCase.export(1, null).exceptionOrNull()

        assertTrue(error is PdfExportException.FileCreationCancelled)
    }

    @Test
    fun `default filename uses required format`() {
        val exporter = SafPdfExporter(
            generator = PdfGenerator(ZoneOffset.UTC),
            openOutputStream = { error("not used") },
            currentInstant = { Instant.parse("2026-08-02T19:30:00Z") },
            zoneId = ZoneOffset.UTC,
        )
        val useCase = PdfExportUseCase(FakePdfSummaryRepository(summary()), exporter)

        assertEquals("meeting-20260802-1930.pdf", useCase.defaultFileName())
    }
}

private class RecordingPdfExporter : PdfExporter {
    var exportedSummary: MeetingSummary? = null
    var destination: String? = null

    override fun defaultFileName(): String = "meeting-20260802-1930.pdf"

    override fun export(summary: MeetingSummary, destination: String) {
        exportedSummary = summary
        this.destination = destination
    }
}

private class FakePdfSummaryRepository(summary: MeetingSummary?) : MeetingSummaryRepository {
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
