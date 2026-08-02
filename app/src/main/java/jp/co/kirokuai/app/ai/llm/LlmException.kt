package jp.co.kirokuai.app.ai.llm

sealed class LlmException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class InvalidPrompt(message: String) : LlmException(message)
    class ContextOverflow(cause: Throwable) : LlmException("Prompt exceeds model context", cause)
    class ModelMissing(path: String) : LlmException("LLM model is missing: $path")
    class ModelInvalid : LlmException("LLM model integrity check failed")
    class NativeInitialization(cause: Throwable) :
        LlmException("Native LLM initialization failed", cause)
    class NativeGeneration(cause: Throwable) : LlmException("Native text generation failed", cause)
    class OutOfMemory(cause: Throwable) : LlmException("Insufficient memory for text generation", cause)
}
