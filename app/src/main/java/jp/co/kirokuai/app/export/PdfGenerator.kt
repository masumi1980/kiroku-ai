package jp.co.kirokuai.app.export

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import jp.co.kirokuai.app.model.MeetingSummary

class PdfGenerator(
    private val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    fun generate(summary: MeetingSummary, meetingDateMillis: Long): PdfLayout {
        val blocks = buildList {
            add(PdfBlock("Meeting Summary", PdfTextStyle.TITLE))
            add(PdfBlock("Meeting date: ${formatDate(meetingDateMillis)}", PdfTextStyle.BODY))
            addSection("Summary", listOf(summary.summary), isList = false)
            addSection("Decisions", summary.decisions)
            addSection("Discussion", summary.discussion)
            addSection("Next Actions", summary.nextActions)
            addSection("Risks", summary.risks)
        }
        return PdfLayout(paginate(blocks))
    }

    private fun MutableList<PdfBlock>.addSection(
        title: String,
        values: List<String>,
        isList: Boolean = true,
    ) {
        add(PdfBlock(title, PdfTextStyle.HEADING, spaceBefore = SECTION_SPACE))
        if (values.isEmpty() || (!isList && values.all(String::isBlank))) {
            add(PdfBlock(EMPTY_SECTION, PdfTextStyle.BODY))
        } else {
            values.forEach { value ->
                add(PdfBlock(if (isList) "- $value" else value, PdfTextStyle.BODY))
            }
        }
    }

    private fun paginate(blocks: List<PdfBlock>): List<PdfPage> {
        val pages = mutableListOf<PdfPage>()
        var pageLines = mutableListOf<PdfLine>()
        var y = TOP_MARGIN

        fun finishPage() {
            pages += PdfPage(pageLines)
            pageLines = mutableListOf()
            y = TOP_MARGIN
        }

        blocks.forEach { block ->
            val wrappedLines = wrap(block.text, block.style.maximumLineUnits)
            val followingLineHeight = if (block.style == PdfTextStyle.HEADING) {
                PdfTextStyle.BODY.lineHeight
            } else {
                0f
            }
            val requiredHeight = block.spaceBefore + block.style.lineHeight + followingLineHeight
            if (pageLines.isNotEmpty() && y + requiredHeight > PAGE_HEIGHT - BOTTOM_MARGIN) {
                finishPage()
            }
            y += block.spaceBefore
            wrappedLines.forEach { text ->
                if (pageLines.isNotEmpty() && y + block.style.lineHeight > PAGE_HEIGHT - BOTTOM_MARGIN) {
                    finishPage()
                }
                y += block.style.lineHeight
                pageLines += PdfLine(text = text, x = LEFT_MARGIN, baseline = y, style = block.style)
            }
        }
        if (pageLines.isNotEmpty()) finishPage()
        return pages
    }

    private fun wrap(text: String, maximumLineUnits: Int): List<String> =
        text.normalizeLineEndings().split(LINE_FEED).flatMap { sourceLine ->
            if (sourceLine.isEmpty()) {
                listOf("")
            } else {
                sourceLine.wrapLine(maximumLineUnits)
            }
        }

    private fun String.wrapLine(maximumLineUnits: Int): List<String> {
        val lines = mutableListOf<String>()
        val currentLine = StringBuilder()
        var currentUnits = 0
        codePoints().forEach { codePoint ->
            val codePointUnits = if (codePoint <= ASCII_MAX_CODE_POINT) 1 else 2
            if (currentLine.isNotEmpty() && currentUnits + codePointUnits > maximumLineUnits) {
                lines += currentLine.toString()
                currentLine.clear()
                currentUnits = 0
            }
            currentLine.appendCodePoint(codePoint)
            currentUnits += codePointUnits
        }
        if (currentLine.isNotEmpty()) lines += currentLine.toString()
        return lines
    }

    private fun formatDate(epochMillis: Long): String =
        DATE_FORMATTER.withZone(zoneId).format(Instant.ofEpochMilli(epochMillis))

    private fun String.normalizeLineEndings(): String =
        replace(CARRIAGE_RETURN_LINE_FEED, LINE_FEED).replace(CARRIAGE_RETURN, LINE_FEED)

    private data class PdfBlock(
        val text: String,
        val style: PdfTextStyle,
        val spaceBefore: Float = 0f,
    )

    private companion object {
        const val EMPTY_SECTION = "None"
        const val LINE_FEED = "\n"
        const val CARRIAGE_RETURN = "\r"
        const val CARRIAGE_RETURN_LINE_FEED = "\r\n"
        const val LEFT_MARGIN = 48f
        const val TOP_MARGIN = 48f
        const val BOTTOM_MARGIN = 48f
        const val SECTION_SPACE = 12f
        const val PAGE_HEIGHT = 842
        const val ASCII_MAX_CODE_POINT = 0x7F
        val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    }
}

data class PdfLayout(val pages: List<PdfPage>)

data class PdfPage(val lines: List<PdfLine>)

data class PdfLine(
    val text: String,
    val x: Float,
    val baseline: Float,
    val style: PdfTextStyle,
)

enum class PdfTextStyle(
    val textSize: Float,
    val lineHeight: Float,
    val maximumLineUnits: Int,
    val isBold: Boolean,
) {
    TITLE(textSize = 24f, lineHeight = 32f, maximumLineUnits = 38, isBold = true),
    HEADING(textSize = 16f, lineHeight = 24f, maximumLineUnits = 56, isBold = true),
    BODY(textSize = 11f, lineHeight = 17f, maximumLineUnits = 90, isBold = false),
}
