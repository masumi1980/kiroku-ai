package jp.co.kirokuai.app.ai.llm

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlamaCppEngine internal constructor(
    private val modelProvider: LlamaModelProvider,
    private val nativeApi: LlamaNativeApi,
    private val dispatcher: CoroutineDispatcher,
) : LlmEngine {
    constructor(context: Context) : this(
        modelProvider = BundledLlamaModelProvider(context.applicationContext),
        nativeApi = LlamaNativeBridge(),
        dispatcher = Dispatchers.Default,
    )

    override suspend fun generate(prompt: String): String = withContext(dispatcher) {
        if (prompt.isBlank()) {
            throw LlmException.InvalidPrompt("Prompt must not be blank")
        }

        val modelFile = modelProvider.getModelFile()
        try {
            nativeApi.generate(modelFile.absolutePath, prompt).trim()
        } catch (error: UnsatisfiedLinkError) {
            throw LlmException.NativeInitialization(error)
        } catch (error: OutOfMemoryError) {
            throw LlmException.OutOfMemory(error)
        } catch (error: Exception) {
            if (error is LlmException) throw error
            throw LlmException.NativeGeneration(error)
        }
    }
}
