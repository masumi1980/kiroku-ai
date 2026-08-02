package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.export.PdfExportException
import jp.co.kirokuai.app.export.PdfExporter

class PdfExportUseCase(
    private val summaryRepository: MeetingSummaryRepository,
    private val exporter: PdfExporter,
) {
    fun defaultFileName(): String = exporter.defaultFileName()

    suspend fun export(meetingId: Long, destination: String?): Result<Unit> = runCatching {
        val outputDestination = destination ?: throw PdfExportException.FileCreationCancelled()
        val summary = summaryRepository.load(meetingId) ?: throw PdfExportException.NoSummary()
        exporter.export(summary, outputDestination)
    }
}
