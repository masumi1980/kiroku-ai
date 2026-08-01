package jp.co.kirokuai.app.ai.llm

class FakeLlmEngine(
    private val response: String = DEFAULT_RESPONSE,
) : LlmEngine {
    override suspend fun generate(prompt: String): String = response

    private companion object {
        const val DEFAULT_RESPONSE = "Generated response"
    }
}
