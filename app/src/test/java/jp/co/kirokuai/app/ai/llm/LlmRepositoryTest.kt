package jp.co.kirokuai.app.ai.llm

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LlmRepositoryTest {
    @Test
    fun generate_delegatesToEngine() = runTest {
        var receivedPrompt: String? = null
        val repository = LlmRepository(
            engine = object : LlmEngine {
                override suspend fun load() = Unit

                override suspend fun generate(prompt: String): String {
                    receivedPrompt = prompt
                    return "response"
                }

                override suspend fun close() = Unit
            },
        )

        assertEquals("response", repository.generate("prompt"))
        assertEquals("prompt", receivedPrompt)
    }

    @Test
    fun lifecycle_delegatesLoadAndCloseToEngine() = runTest {
        val engine = RecordingLlmEngine()
        val repository = LlmRepository(engine)

        repository.load()
        repository.close()

        assertEquals(1, engine.loadCalls)
        assertEquals(1, engine.closeCalls)
    }
}

private class RecordingLlmEngine : LlmEngine {
    var loadCalls = 0
    var closeCalls = 0

    override suspend fun load() {
        loadCalls += 1
    }

    override suspend fun generate(prompt: String): String = "response"

    override suspend fun close() {
        closeCalls += 1
    }
}
