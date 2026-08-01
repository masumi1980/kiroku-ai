package jp.co.kirokuai.app.audio

interface AudioRecorder {
    fun start(outputPath: String)

    fun stop()
}
