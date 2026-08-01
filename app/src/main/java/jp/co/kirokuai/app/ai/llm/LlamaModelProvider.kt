package jp.co.kirokuai.app.ai.llm

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

internal fun interface LlamaModelProvider {
    fun getModelFile(): File
}

internal class BundledLlamaModelProvider(
    private val context: Context,
) : LlamaModelProvider {
    @Synchronized
    override fun getModelFile(): File {
        val modelDirectory = File(context.filesDir, MODEL_DIRECTORY)
        val modelFile = File(modelDirectory, MODEL_FILE_NAME)
        if (modelFile.isFile && modelFile.sha256() == MODEL_SHA256) {
            return modelFile
        }

        modelDirectory.mkdirs()
        val temporaryFile = File(modelDirectory, "$MODEL_FILE_NAME.tmp")
        temporaryFile.delete()
        try {
            context.assets.open(MODEL_ASSET_PATH).use { input ->
                temporaryFile.outputStream().use(input::copyTo)
            }
        } catch (error: Exception) {
            temporaryFile.delete()
            throw LlmException.ModelMissing(MODEL_ASSET_PATH)
        }

        if (temporaryFile.sha256() != MODEL_SHA256) {
            temporaryFile.delete()
            throw LlmException.ModelInvalid()
        }
        modelFile.delete()
        if (!temporaryFile.renameTo(modelFile)) {
            temporaryFile.delete()
            throw LlmException.ModelMissing(modelFile.absolutePath)
        }
        return modelFile
    }

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance(SHA_256)
        FileInputStream(this).use { input ->
            val buffer = ByteArray(HASH_BUFFER_SIZE)
            while (true) {
                val bytesRead = input.read(buffer)
                if (bytesRead < 0) break
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private companion object {
        const val MODEL_DIRECTORY = "models"
        const val MODEL_FILE_NAME = "stories15M-q4_0.gguf"
        const val MODEL_ASSET_PATH = "$MODEL_DIRECTORY/$MODEL_FILE_NAME"
        const val MODEL_SHA256 =
            "6151b1929d7f5aa3385d9ddef3393e55587c0a55de661562322bc51dfda93a04"
        const val SHA_256 = "SHA-256"
        const val HASH_BUFFER_SIZE = 8 * 1_024
    }
}
