package jp.co.kirokuai.app.ai.parser

import jp.co.kirokuai.app.ai.summary.MeetingSummaryException
import jp.co.kirokuai.app.model.MeetingSummary

class MeetingSummaryParser {
    fun parse(json: String, meetingId: Long, createdAt: Long): MeetingSummary = try {
        val value = JsonReader(json).readObject()
        MeetingSummary(
            id = meetingId,
            meetingId = meetingId,
            summary = value.requiredString("summary"),
            decisions = value.requiredStringList("decisions"),
            discussion = value.requiredStringList("discussion"),
            nextActions = value.requiredStringList("nextActions"),
            risks = value.requiredStringList("risks"),
            createdAt = createdAt,
        )
    } catch (exception: MeetingSummaryException.InvalidJson) {
        throw exception
    } catch (exception: RuntimeException) {
        throw MeetingSummaryException.InvalidJson(exception)
    }

    private fun Map<String, Any>.requiredString(key: String): String =
        this[key] as? String ?: throw MeetingSummaryException.InvalidJson()

    private fun Map<String, Any>.requiredStringList(key: String): List<String> {
        val values = this[key] as? List<*> ?: throw MeetingSummaryException.InvalidJson()
        return values.map { it as? String ?: throw MeetingSummaryException.InvalidJson() }
    }
}

private class JsonReader(private val source: String) {
    private var position = 0

    fun readObject(): Map<String, Any> {
        skipWhitespace()
        expect('{')
        val result = linkedMapOf<String, Any>()
        skipWhitespace()
        if (consume('}')) return finish(result)
        while (true) {
            val key = readString()
            skipWhitespace()
            expect(':')
            skipWhitespace()
            result[key] = if (peek() == '"') readString() else readStringArray()
            skipWhitespace()
            if (consume('}')) return finish(result)
            expect(',')
            skipWhitespace()
        }
    }

    private fun finish(result: Map<String, Any>): Map<String, Any> {
        skipWhitespace()
        check(position == source.length) { "Unexpected trailing JSON content" }
        return result
    }

    private fun readStringArray(): List<String> {
        expect('[')
        val result = mutableListOf<String>()
        skipWhitespace()
        if (consume(']')) return result
        while (true) {
            result += readString()
            skipWhitespace()
            if (consume(']')) return result
            expect(',')
            skipWhitespace()
        }
    }

    private fun readString(): String {
        expect('"')
        val result = StringBuilder()
        while (position < source.length) {
            when (val character = source[position++]) {
                '"' -> return result.toString()
                '\\' -> result.append(readEscape())
                else -> {
                    check(character >= ' ') { "Control character in string" }
                    result.append(character)
                }
            }
        }
        error("Unterminated string")
    }

    private fun readEscape(): Char {
        check(position < source.length) { "Incomplete escape" }
        return when (val escaped = source[position++]) {
            '"', '\\', '/' -> escaped
            'b' -> '\b'
            'f' -> '\u000C'
            'n' -> '\n'
            'r' -> '\r'
            't' -> '\t'
            'u' -> readUnicodeEscape()
            else -> error("Invalid escape")
        }
    }

    private fun readUnicodeEscape(): Char {
        check(position + UNICODE_LENGTH <= source.length) { "Incomplete unicode escape" }
        return source.substring(position, position + UNICODE_LENGTH)
            .also { position += UNICODE_LENGTH }
            .toInt(HEX_RADIX)
            .toChar()
    }

    private fun skipWhitespace() {
        while (position < source.length && source[position].isWhitespace()) position++
    }

    private fun peek(): Char {
        check(position < source.length) { "Unexpected end of JSON" }
        return source[position]
    }

    private fun consume(expected: Char): Boolean =
        if (position < source.length && source[position] == expected) {
            position++
            true
        } else {
            false
        }

    private fun expect(expected: Char) {
        check(consume(expected)) { "Expected $expected" }
    }

    private companion object {
        const val UNICODE_LENGTH = 4
        const val HEX_RADIX = 16
    }
}
