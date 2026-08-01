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
            nativeApi = LlamaNativeApi { _, _ -> error("native failed") },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        val error = runCatching { engine.generate("prompt") }.exceptionOrNull()

        assertTrue(error is LlmException.NativeGeneration)
    }
}

private class RecordingLlamaNativeApi(
    private val response: String,
) : LlamaNativeApi {
    var receivedModelPath: String? = null
    var receivedPrompt: String? = null
    val wasCalled: Boolean
        get() = receivedPrompt != null

    override fun generate(modelPath: String, prompt: String): String {
        receivedModelPath = modelPath
        receivedPrompt = prompt
        return response
    }
}
