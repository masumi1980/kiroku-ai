package jp.co.kirokuai.app.ai.llm

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.util.Log
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LlamaCppEngineInstrumentedTest {
    @Test
    fun bundledModel_loadsAndGeneratesResponse() = runBlocking {
        val engine = LlamaCppEngine(ApplicationProvider.getApplicationContext())

        val response = engine.generate("Once upon a time,")

        Log.i(LOG_TAG, "Generated response: $response")
        assertTrue("Expected the bundled GGUF model to generate text", response.isNotBlank())
    }

    private companion object {
        const val LOG_TAG = "LlamaCppEngineTest"
    }
}
