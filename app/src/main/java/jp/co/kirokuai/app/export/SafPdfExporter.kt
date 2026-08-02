package jp.co.kirokuai.app.export

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import jp.co.kirokuai.app.model.MeetingSummary

class SafPdfExporter(
    private val generator: PdfGenerator,
    private val openOutputStream: (String) -> OutputStream?,
    private val currentInstant: () -> Instant = Instant::now,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : PdfExporter {
    override fun defaultFileName(): String =
        FILE_NAME_FORMATTER.withZone(zoneId).format(currentInstant())

    override fun export(summary: MeetingSummary, destination: String) {
        try {
            val outputStream = requireNotNull(openOutputStream(destination)) {
                "Storage Access Framework returned no output stream"
            }
            outputStream.use { output ->
                val document = PdfDocument()
                try {
                    generator.generate(summary).pages.forEachIndexed { index, page ->
                        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
                        val pdfPage = document.startPage(pageInfo)
                        page.lines.forEach { line ->
                            pdfPage.canvas.drawText(line.text, line.x, line.baseline, line.style.toPaint())
                        }
                        document.finishPage(pdfPage)
                    }
                    document.writeTo(output)
                } finally {
                    document.close()
                }
            }
        } catch (exception: SecurityException) {
            throw PdfExportException.PermissionDenied(exception)
        } catch (exception: PdfExportException) {
            throw exception
        } catch (exception: Exception) {
            throw PdfExportException.WriteFailure(exception)
        }
    }

    private fun PdfTextStyle.toPaint(): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = this@toPaint.textSize
        typeface = Typeface.create("sans-serif", if (isBold) Typeface.BOLD else Typeface.NORMAL)
    }

    private companion object {
        const val PAGE_WIDTH = 595
        const val PAGE_HEIGHT = 842
        val FILE_NAME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("'meeting-'yyyyMMdd-HHmm'.pdf'")
    }
}
