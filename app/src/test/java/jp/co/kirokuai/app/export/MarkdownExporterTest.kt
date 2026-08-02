package jp.co.kirokuai.app.export

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.time.Instant
import java.time.ZoneOffset
import jp.co.kirokuai.app.model.MeetingSummary
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MarkdownExporterTest {
    @Test
    fun `export writes exact UTF-8 Markdown format with LF endings`() {
        val output = ByteArrayOutputStream()
        val exporter = SafMarkdownExporter(openOutputStream = { output })

        exporter.export(summary(), "content://document/meeting")

        val expected = """
            # Meeting Summary

            ## Summary

            日本語の概要
            2行目

            ## Decisions

            - 決定1

            - 決定2

            ## Discussion

            - 議論

            ## Next Actions

            - 対応

            ## Risks

            - リスク
        """.trimIndent() + "\n"
        assertArrayEquals(expected.toByteArray(Charsets.UTF_8), output.toByteArray())
        assertFalse(output.toString(Charsets.UTF_8.name()).contains('\r'))
    }

    @Test
    fun `export writes None for every empty section`() {
        val output = ByteArrayOutputStream()
        val exporter = SafMarkdownExporter(openOutputStream = { output })

        exporter.export(summary(summary = "", decisions = emptyList(), discussion = emptyList(), nextActions = emptyList(), risks = emptyList()), "destination")

        assertEquals(5, Regex("(?m)^None$").findAll(output.toString(Charsets.UTF_8.name())).count())
    }

    @Test
    fun `default filename uses required timestamp format`() {
        val exporter = SafMarkdownExporter(
            openOutputStream = { ByteArrayOutputStream() },
            currentInstant = { Instant.parse("2026-08-02T10:30:00Z") },
            zoneId = ZoneOffset.ofHours(9),
        )

        assertEquals("Meeting_20260802_193000.md", exporter.defaultFileName())
    }

    @Test(expected = MarkdownExportException.WriteFailure::class)
    fun `export maps write failures`() {
        SafMarkdownExporter(openOutputStream = { throw IOException("disk failure") })
            .export(summary(), "destination")
    }

    @Test(expected = MarkdownExportException.PermissionDenied::class)
    fun `export maps permission failures`() {
        SafMarkdownExporter(openOutputStream = { throw SecurityException("denied") })
            .export(summary(), "destination")
    }

    private fun summary(
        summary: String = "日本語の概要\r\n2行目",
        decisions: List<String> = listOf("決定1", "決定2"),
        discussion: List<String> = listOf("議論"),
        nextActions: List<String> = listOf("対応"),
        risks: List<String> = listOf("リスク"),
    ) = MeetingSummary(
        id = 1,
        meetingId = 1,
        summary = summary,
        decisions = decisions,
        discussion = discussion,
        nextActions = nextActions,
        risks = risks,
        createdAt = 0,
    )
}
