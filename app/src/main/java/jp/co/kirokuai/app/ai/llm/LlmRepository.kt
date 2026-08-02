package jp.co.kirokuai.app.ai.llm

class LlmRepository(
    private val engine: LlmEngine,
) {
    suspend fun load() = engine.load()

    suspend fun generate(prompt: String): String = engine.generate(prompt)

    suspend fun close() = engine.close()
}
