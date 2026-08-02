package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.export.PdfExportException
import jp.co.kirokuai.app.export.PdfExporter
import kotlinx.coroutines.flow.first

class PdfExportUseCase(
    private val summaryRepository: MeetingSummaryRepository,
    private val meetingRepository: MeetingRepository,
    private val exporter: PdfExporter,
) {
    fun defaultFileName(): String = exporter.defaultFileName()

    suspend fun export(meetingId: Long, destination: String?): Result<Unit> = runCatching {
        val outputDestination = destination ?: throw PdfExportException.FileCreationCancelled()
        val summary = summaryRepository.load(meetingId) ?: throw PdfExportException.NoSummary()
        val meetingDateMillis = checkNotNull(
            meetingRepository.getAll().first().firstOrNull { meeting -> meeting.id == meetingId }?.createdAt,
        ) { "Meeting not found: $meetingId" }
        exporter.export(summary, meetingDateMillis, outputDestination)
    }
}
