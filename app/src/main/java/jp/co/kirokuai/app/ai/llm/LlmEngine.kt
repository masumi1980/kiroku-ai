package jp.co.kirokuai.app.ai.llm

interface LlmEngine {
    suspend fun load()

    suspend fun generate(prompt: String): String

    suspend fun close()
}
