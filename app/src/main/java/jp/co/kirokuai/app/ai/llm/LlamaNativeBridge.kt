package jp.co.kirokuai.app.ai.llm

internal interface LlamaNativeApi {
    fun load(modelPath: String): Long
    fun generate(handle: Long, prompt: String): String
    fun close(handle: Long)
}

internal class LlamaNativeBridge : LlamaNativeApi {
    override fun load(modelPath: String): Long {
        NativeLibrary.ensureLoaded()
        return loadNative(modelPath)
    }

    override fun generate(handle: Long, prompt: String): String = generateNative(handle, prompt)

    override fun close(handle: Long) = closeNative(handle)

    private external fun loadNative(modelPath: String): Long
    private external fun generateNative(handle: Long, prompt: String): String
    private external fun closeNative(handle: Long)

    private object NativeLibrary {
        @Volatile
        private var loaded = false

        fun ensureLoaded() {
            if (loaded) return
            synchronized(this) {
                if (!loaded) {
                    System.loadLibrary("kiroku_llama")
                    loaded = true
                }
            }
        }
    }
}
