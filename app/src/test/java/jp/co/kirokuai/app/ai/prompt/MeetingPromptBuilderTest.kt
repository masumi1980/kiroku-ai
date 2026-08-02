package jp.co.kirokuai.app.ai.prompt

import org.junit.Assert.assertEquals
import org.junit.Test

class MeetingPromptBuilderTest {
    @Test
    fun `build applies the fixed prompt and safely escapes transcript`() {
        val prompt = MeetingPromptBuilder().build("議事録\\資料\u0000")

        assertEquals(
            """
                You are an AI meeting assistant.

                Summarize the meeting below.

                Return ONLY valid JSON.

                {
                  "summary":"",
                  "decisions":[],
                  "discussion":[],
                  "nextActions":[],
                  "risks":[]
                }

                Transcript:

                議事録\\資料

                No markdown.

                No explanation.

                No extra text.
            """.trimIndent(),
            prompt,
        )
    }
}
