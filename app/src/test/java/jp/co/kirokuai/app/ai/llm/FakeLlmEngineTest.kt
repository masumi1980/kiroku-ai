package jp.co.kirokuai.app.ai.llm

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeLlmEngineTest {
    @Test
    fun generate_returnsConfiguredResponse() = runTest {
        val engine = FakeLlmEngine(response = "test response")

        assertEquals("test response", engine.generate("prompt"))
    }
}
