package jp.co.kirokuai.app.ai.llm

internal fun interface LlamaNativeApi {
    fun generate(modelPath: String, prompt: String): String
}

internal class LlamaNativeBridge : LlamaNativeApi {
    override fun generate(modelPath: String, prompt: String): String {
        NativeLibrary.ensureLoaded()
        return generateNative(modelPath, prompt)
    }

    private external fun generateNative(modelPath: String, prompt: String): String

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
