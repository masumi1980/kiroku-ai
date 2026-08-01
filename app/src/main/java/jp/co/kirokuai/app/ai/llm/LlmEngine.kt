package jp.co.kirokuai.app.ai.llm

interface LlmEngine {
    suspend fun generate(prompt: String): String
}
