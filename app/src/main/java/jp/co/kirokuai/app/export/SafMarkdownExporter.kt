package jp.co.kirokuai.app.export

import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import jp.co.kirokuai.app.model.MeetingSummary

class SafMarkdownExporter(
    private val openOutputStream: (String) -> OutputStream?,
    private val currentInstant: () -> Instant = Instant::now,
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) : MarkdownExporter {
    override fun defaultFileName(): String =
        FILE_NAME_FORMATTER.withZone(zoneId).format(currentInstant())

    override fun export(summary: MeetingSummary, destination: String) {
        try {
            val outputStream = requireNotNull(openOutputStream(destination)) {
                "Storage Access Framework returned no output stream"
            }
            outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(summary.toMarkdown())
            }
        } catch (exception: SecurityException) {
            throw MarkdownExportException.PermissionDenied(exception)
        } catch (exception: MarkdownExportException) {
            throw exception
        } catch (exception: Exception) {
            throw MarkdownExportException.WriteFailure(exception)
        }
    }

    private fun MeetingSummary.toMarkdown(): String = listOf(
        "# Meeting Summary",
        "## Summary",
        summary.toMarkdownParagraph(),
        "## Decisions",
        decisions.toMarkdownList(),
        "## Discussion",
        discussion.toMarkdownList(),
        "## Next Actions",
        nextActions.toMarkdownList(),
        "## Risks",
        risks.toMarkdownList(),
    ).joinToString(SECTION_SEPARATOR, postfix = LINE_FEED)

    private fun String.toMarkdownParagraph(): String =
        normalizeLineEndings().ifBlank { EMPTY_SECTION }

    private fun List<String>.toMarkdownList(): String = if (isEmpty()) {
        EMPTY_SECTION
    } else {
        joinToString(ITEM_SEPARATOR) { item -> "- ${item.normalizeLineEndings()}" }
    }

    private fun String.normalizeLineEndings(): String =
        replace(CARRIAGE_RETURN_LINE_FEED, LINE_FEED).replace(CARRIAGE_RETURN, LINE_FEED)

    private companion object {
        const val EMPTY_SECTION = "None"
        const val LINE_FEED = "\n"
        const val CARRIAGE_RETURN = "\r"
        const val CARRIAGE_RETURN_LINE_FEED = "\r\n"
        const val SECTION_SEPARATOR = "\n\n"
        const val ITEM_SEPARATOR = "\n\n"
        val FILE_NAME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("'Meeting_'yyyyMMdd_HHmmss'.md'")
    }
}
