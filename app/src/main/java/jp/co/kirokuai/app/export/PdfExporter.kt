package jp.co.kirokuai.app.export

import jp.co.kirokuai.app.model.MeetingSummary

interface PdfExporter {
    fun defaultFileName(): String

    fun export(summary: MeetingSummary, meetingDateMillis: Long, destination: String)
}
