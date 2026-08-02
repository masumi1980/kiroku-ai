package jp.co.kirokuai.app.ai.llm

import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LlamaCppEngineInstrumentedTest {
    @Test
    fun bundledModel_loadsAndGeneratesResponse() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val engine = LlamaCppEngine(context)

        val response = try {
            engine.load()
            engine.generate(SIMPLE_INSTRUCTION_PROMPT)
        } finally {
            engine.close()
        }
        val extractedModel = File(context.filesDir, "models/$MODEL_FILE_NAME")

        Log.i(LOG_TAG, "Generated response: $response")
        assertTrue("Expected the bundled GGUF model to generate text", response.isNotBlank())
        assertEquals(MODEL_SHA256, extractedModel.sha256())
    }

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(this).use { input ->
            val buffer = ByteArray(8 * 1_024)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead < 0) break
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    private companion object {
        const val LOG_TAG = "LlamaCppEngineTest"
        const val MODEL_FILE_NAME = "qwen3-4b-instruct-q4_k_m.gguf"
        const val MODEL_SHA256 =
            "7485fe6f11af29433bc51cab58009521f205840f5b4ae3a32fa7f92e8534fdf5"
        const val SIMPLE_INSTRUCTION_PROMPT =
            "<|im_start|>user\n/no_think Reply with only the word OK.<|im_end|>\n<|im_start|>assistant\n"
    }
}
