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
                override suspend fun generate(prompt: String): String {
                    receivedPrompt = prompt
                    return "response"
                }
            },
        )

        assertEquals("response", repository.generate("prompt"))
        assertEquals("prompt", receivedPrompt)
    }
}
