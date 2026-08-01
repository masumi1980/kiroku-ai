package jp.co.kirokuai.app.ui.recording

import org.junit.Assert.assertEquals
import org.junit.Test

class RecordingScreenTest {
    @Test
    fun formatElapsedTime_formatsMinutesAndSeconds() {
        assertEquals("00:00", formatElapsedTime(0))
        assertEquals("01:05", formatElapsedTime(65))
    }
}
