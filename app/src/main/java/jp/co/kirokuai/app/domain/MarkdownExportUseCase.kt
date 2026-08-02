package jp.co.kirokuai.app.domain

import jp.co.kirokuai.app.export.MarkdownExportException
import jp.co.kirokuai.app.export.MarkdownExporter

class MarkdownExportUseCase(
    private val summaryRepository: MeetingSummaryRepository,
    private val exporter: MarkdownExporter,
) {
    fun defaultFileName(): String = exporter.defaultFileName()

    suspend fun export(meetingId: Long, destination: String?): Result<Unit> = runCatching {
        val outputDestination = destination ?: throw MarkdownExportException.FileCreationCancelled()
        val summary = summaryRepository.load(meetingId) ?: throw MarkdownExportException.NoSummary()
        exporter.export(summary, outputDestination)
    }
}
