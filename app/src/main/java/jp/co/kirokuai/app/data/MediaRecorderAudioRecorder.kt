package jp.co.kirokuai.app.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import jp.co.kirokuai.app.audio.AudioRecorder

class MediaRecorderAudioRecorder(
    private val context: Context,
) : AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null

    override fun start(outputPath: String) {
        check(mediaRecorder == null) { "Recording is already in progress" }

        val recorder = createMediaRecorder()
        try {
            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputPath)
                prepare()
                start()
            }
            mediaRecorder = recorder
        } catch (error: Exception) {
            recorder.release()
            throw error
        }
    }

    override fun stop() {
        val recorder = mediaRecorder ?: return
        mediaRecorder = null
        try {
            recorder.stop()
        } finally {
            recorder.release()
        }
    }

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
}
