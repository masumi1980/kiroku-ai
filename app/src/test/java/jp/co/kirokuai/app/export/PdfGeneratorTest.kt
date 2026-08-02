package jp.co.kirokuai.app.export

import java.time.ZoneOffset
import jp.co.kirokuai.app.model.MeetingSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfGeneratorTest {
    @Test
    fun `generate includes title date and every summary section`() {
        val layout = PdfGenerator(zoneId = ZoneOffset.UTC).generate(summary(), MEETING_DATE_MILLIS)
        val text = layout.pages.flatMap(PdfPage::lines).map(PdfLine::text)

        assertEquals("Meeting Summary", text.first())
        assertTrue(text.contains("Meeting date: 2026-08-02 19:30"))
        assertContainsInOrder(
            text,
            "Summary",
            "日本語の要約",
            "Decisions",
            "- 決定事項",
            "Discussion",
            "- 議論内容",
            "Next Actions",
            "- 次の対応",
            "Risks",
            "- リスク",
        )
    }

    @Test
    fun `generate renders None for empty sections`() {
        val emptySummary = summary(
            summaryText = "",
            decisions = emptyList(),
            discussion = emptyList(),
            nextActions = emptyList(),
            risks = emptyList(),
        )

        val text = PdfGenerator(ZoneOffset.UTC).generate(emptySummary, MEETING_DATE_MILLIS)
            .pages.flatMap(PdfPage::lines).map(PdfLine::text)

        assertEquals(5, text.count { it == "None" })
    }

    @Test
    fun `generate creates automatic page breaks for large summaries`() {
        val largeSummary = summary(summaryText = "日本語".repeat(20_000))

        val layout = PdfGenerator(ZoneOffset.UTC).generate(largeSummary, MEETING_DATE_MILLIS)

        assertTrue(layout.pages.size > 1)
        assertTrue(layout.pages.all { page -> page.lines.isNotEmpty() })
        assertTrue(layout.pages.flatMap(PdfPage::lines).all { line -> line.baseline < 842f })
    }

    private fun assertContainsInOrder(actual: List<String>, vararg expected: String) {
        var searchFrom = 0
        expected.forEach { value ->
            val index = actual.indexOfFirstFrom(searchFrom) { it == value }
            assertTrue("Missing or out-of-order PDF text: $value", index >= 0)
            searchFrom = index + 1
        }
    }

    private fun <T> List<T>.indexOfFirstFrom(startIndex: Int, predicate: (T) -> Boolean): Int {
        for (index in startIndex until size) if (predicate(this[index])) return index
        return -1
    }

    private fun summary(
        summaryText: String = "日本語の要約",
        decisions: List<String> = listOf("決定事項"),
        discussion: List<String> = listOf("議論内容"),
        nextActions: List<String> = listOf("次の対応"),
        risks: List<String> = listOf("リスク"),
    ) = MeetingSummary(
        id = 1,
        meetingId = 1,
        summary = summaryText,
        decisions = decisions,
        discussion = discussion,
        nextActions = nextActions,
        risks = risks,
        createdAt = 1_785_699_000_000,
    )

    private companion object {
        const val MEETING_DATE_MILLIS = 1_785_699_000_000
    }
}
