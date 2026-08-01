package jp.co.kirokuai.app.ai.speech

import android.content.Context
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

internal fun interface WhisperModelProvider {
    fun getModelFile(): File
}

internal class BundledWhisperModelProvider(
    private val context: Context,
) : WhisperModelProvider {
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
            throw SpeechRecognitionException.ModelMissing(MODEL_ASSET_PATH)
        }

        if (temporaryFile.sha256() != MODEL_SHA256) {
            temporaryFile.delete()
            throw SpeechRecognitionException.ModelInvalid()
        }
        modelFile.delete()
        if (!temporaryFile.renameTo(modelFile)) {
            temporaryFile.delete()
            throw SpeechRecognitionException.ModelMissing(modelFile.absolutePath)
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
        const val MODEL_FILE_NAME = "ggml-tiny.en.bin"
        const val MODEL_ASSET_PATH = "$MODEL_DIRECTORY/$MODEL_FILE_NAME"
        const val MODEL_SHA256 =
            "921e4cf8686fdd993dcd081a5da5b6c365bfde1162e72b08d75ac75289920b1f"
        const val SHA_256 = "SHA-256"
        const val HASH_BUFFER_SIZE = 8 * 1_024
    }
}
