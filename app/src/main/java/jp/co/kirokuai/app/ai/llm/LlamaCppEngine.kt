package jp.co.kirokuai.app.ai.llm

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class LlamaCppEngine internal constructor(
    private val modelProvider: LlamaModelProvider,
    private val nativeApi: LlamaNativeApi,
    private val dispatcher: CoroutineDispatcher,
) : LlmEngine {
    private val mutex = Mutex()
    private var nativeHandle: Long = NO_NATIVE_HANDLE

    constructor(context: Context) : this(
        modelProvider = BundledLlamaModelProvider(context.applicationContext),
        nativeApi = LlamaNativeBridge(),
        dispatcher = Dispatchers.Default,
    )

    override suspend fun load() = withContext(dispatcher) {
        mutex.withLock { loadIfNecessary() }
    }

    override suspend fun generate(prompt: String): String = withContext(dispatcher) {
        if (prompt.isBlank()) {
            throw LlmException.InvalidPrompt("Prompt must not be blank")
        }

        mutex.withLock {
            loadIfNecessary()
            try {
                nativeApi.generate(nativeHandle, prompt).trim()
            } catch (error: OutOfMemoryError) {
                throw LlmException.OutOfMemory(error)
            } catch (error: IllegalArgumentException) {
                throw LlmException.ContextOverflow(error)
            } catch (error: Exception) {
                if (error is LlmException) throw error
                throw LlmException.NativeGeneration(error)
            }
        }
    }

    override suspend fun close() = withContext(dispatcher) {
        mutex.withLock {
            if (nativeHandle != NO_NATIVE_HANDLE) {
                nativeApi.close(nativeHandle)
                nativeHandle = NO_NATIVE_HANDLE
            }
        }
    }

    private fun loadIfNecessary() {
        if (nativeHandle != NO_NATIVE_HANDLE) return
        val modelFile = modelProvider.getModelFile()
        nativeHandle = try {
            nativeApi.load(modelFile.absolutePath)
        } catch (error: UnsatisfiedLinkError) {
            throw LlmException.NativeInitialization(error)
        } catch (error: OutOfMemoryError) {
            throw LlmException.OutOfMemory(error)
        } catch (error: Exception) {
            if (error is LlmException) throw error
            throw LlmException.NativeInitialization(error)
        }
        check(nativeHandle != NO_NATIVE_HANDLE) { "Native model returned an invalid handle" }
    }

    private companion object {
        const val NO_NATIVE_HANDLE = 0L
    }
}
