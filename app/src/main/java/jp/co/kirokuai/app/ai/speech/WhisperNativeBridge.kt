package jp.co.kirokuai.app.ai.speech

internal interface WhisperNativeApi {
    fun transcribe(modelPath: String, audioPath: String): String
}

internal class WhisperNativeBridge : WhisperNativeApi {
    override fun transcribe(modelPath: String, audioPath: String): String {
        loadLibrary()
        return transcribeNative(modelPath, audioPath)
    }

    private external fun transcribeNative(modelPath: String, audioPath: String): String

    private companion object {
        private const val NATIVE_LIBRARY_NAME = "kiroku_whisper"

        @Volatile
        private var isLibraryLoaded = false

        @Synchronized
        fun loadLibrary() {
            if (isLibraryLoaded) return
            System.loadLibrary(NATIVE_LIBRARY_NAME)
            isLibraryLoaded = true
        }
    }
}
