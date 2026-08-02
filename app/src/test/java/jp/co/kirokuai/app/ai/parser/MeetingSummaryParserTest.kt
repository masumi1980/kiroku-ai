package jp.co.kirokuai.app.ai.parser

import jp.co.kirokuai.app.ai.summary.MeetingSummaryException
import org.junit.Assert.assertEquals
import org.junit.Test

class MeetingSummaryParserTest {
    private val parser = MeetingSummaryParser()

    @Test
    fun `parse validates and creates meeting summary`() {
        val result = parser.parse(
            """{"summary":"概要","decisions":["決定"],"discussion":["議論"],"nextActions":["対応"],"risks":["リスク"]}""",
            meetingId = 7,
            createdAt = 100,
        )

        assertEquals(7, result.id)
        assertEquals("概要", result.summary)
        assertEquals(listOf("決定"), result.decisions)
        assertEquals(listOf("対応"), result.nextActions)
    }

    @Test(expected = MeetingSummaryException.InvalidJson::class)
    fun `parse rejects missing required fields`() {
        parser.parse("""{"summary":"概要","decisions":[]}""", 7, 100)
    }

    @Test(expected = MeetingSummaryException.InvalidJson::class)
    fun `parse rejects malformed json`() {
        parser.parse("not json", 7, 100)
    }
}
