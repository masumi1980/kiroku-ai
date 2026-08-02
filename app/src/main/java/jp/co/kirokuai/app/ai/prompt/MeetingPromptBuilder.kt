package jp.co.kirokuai.app.ai.prompt

class MeetingPromptBuilder {
    fun build(transcript: String): String = TEMPLATE.replace(TRANSCRIPT_PLACEHOLDER, escape(transcript))

    private fun escape(transcript: String): String = transcript
        .replace("\\", "\\\\")
        .replace("\u0000", "")

    private companion object {
        const val TRANSCRIPT_PLACEHOLDER = "{TRANSCRIPT}"
        val TEMPLATE = """
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

            {TRANSCRIPT}

            No markdown.

            No explanation.

            No extra text.
        """.trimIndent()
    }
}
