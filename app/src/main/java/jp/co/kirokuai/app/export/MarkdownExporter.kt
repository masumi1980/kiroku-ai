package jp.co.kirokuai.app.export

import jp.co.kirokuai.app.model.MeetingSummary

interface MarkdownExporter {
    fun defaultFileName(): String

    fun export(summary: MeetingSummary, destination: String)
}
