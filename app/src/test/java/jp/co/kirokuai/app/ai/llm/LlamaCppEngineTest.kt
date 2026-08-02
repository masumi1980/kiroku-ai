package jp.co.kirokuai.app.ai.llm

import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LlamaCppEngineTest {
    @Test
    fun generate_returnsTrimmedNativeResponse() = runTest {
        val modelFile = File("model.gguf")
        val nativeApi = RecordingLlamaNativeApi(" generated text ")
        val engine = LlamaCppEngine(
            modelProvider = LlamaModelProvider { modelFile },
            nativeApi = nativeApi,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val response = engine.generate("Once upon a time,")

        assertEquals("generated text", response)
        assertEquals(modelFile.absolutePath, nativeApi.receivedModelPath)
        assertEquals("Once upon a time,", nativeApi.receivedPrompt)
        assertEquals(TEST_NATIVE_HANDLE, nativeApi.receivedHandle)
    }

    @Test
    fun generate_rejectsBlankPromptBeforeLoadingModel() = runTest {
        var modelWasRequested = false
        val nativeApi = RecordingLlamaNativeApi("unused")
        val engine = LlamaCppEngine(
            modelProvider = LlamaModelProvider {
                modelWasRequested = true
                File("model.gguf")
            },
            nativeApi = nativeApi,
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val error = runCatching { engine.generate("   ") }.exceptionOrNull()

        assertTrue(error is LlmException.InvalidPrompt)
        assertFalse(modelWasRequested)
        assertFalse(nativeApi.wasCalled)
    }

    @Test
    fun generate_wrapsNativeFailure() = runTest {
        val engine = LlamaCppEngine(
            modelProvider = LlamaModelProvider { File("model.gguf") },
            nativeApi = FailingGenerateNativeApi(),
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val error = runCatching { engine.generate("prompt") }.exceptionOrNull()

        assertTrue(error is LlmException.NativeGeneration)
    }

    @Test
    fun generate_mapsContextOverflow() = runTest {
        val engine = LlamaCppEngine(
            modelProvider = LlamaModelProvider { File("model.gguf") },
            nativeApi = ContextOverflowNativeApi(),
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val error = runCatching { engine.generate("prompt") }.exceptionOrNull()

        assertTrue(error is LlmException.ContextOverflow)
    }
}

private class RecordingLlamaNativeApi(
    private val response: String,
) : LlamaNativeApi {
    var receivedModelPath: String? = null
    var receivedHandle: Long? = null
    var receivedPrompt: String? = null
    val wasCalled: Boolean
        get() = receivedPrompt != null

    override fun load(modelPath: String): Long {
        receivedModelPath = modelPath
        return TEST_NATIVE_HANDLE
    }

    override fun generate(handle: Long, prompt: String): String {
        receivedHandle = handle
        receivedPrompt = prompt
        return response
    }

    override fun close(handle: Long) = Unit
}

private class FailingGenerateNativeApi : LlamaNativeApi {
    override fun load(modelPath: String): Long = TEST_NATIVE_HANDLE

    override fun generate(handle: Long, prompt: String): String = error("native failed")

    override fun close(handle: Long) = Unit
}

private class ContextOverflowNativeApi : LlamaNativeApi {
    override fun load(modelPath: String): Long = TEST_NATIVE_HANDLE

    override fun generate(handle: Long, prompt: String): String =
        throw IllegalArgumentException("Prompt exceeds context")

    override fun close(handle: Long) = Unit
}

private const val TEST_NATIVE_HANDLE = 7L
