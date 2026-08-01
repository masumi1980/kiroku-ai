package jp.co.kirokuai.app.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import jp.co.kirokuai.app.audio.AudioRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PcmWavAudioRecorder(
    private val context: Context,
) : AudioRecorder {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null

    @Volatile
    private var isRecording = false

    @Volatile
    private var recordingFailure: RuntimeException? = null

    override fun start(outputPath: String) {
        check(audioRecord == null) { "Recording is already in progress" }
        check(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED,
        ) { "Microphone permission is required" }
        val outputFile = File(outputPath)
        outputFile.parentFile?.mkdirs()
        val bufferSize = recordingBufferSize()
        val recorder = createAudioRecord(bufferSize)
        check(recorder.state == AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            "Audio recorder could not be initialized"
        }

        recordingFailure = null
        isRecording = true
        try {
            recorder.startRecording()
            audioRecord = recorder
            recordingThread = Thread(
                { writeAudio(recorder, outputFile, bufferSize) },
                RECORDING_THREAD_NAME,
            ).apply { start() }
        } catch (error: RuntimeException) {
            isRecording = false
            recorder.release()
            throw error
        }
    }

    override fun stop() {
        val recorder = audioRecord ?: return
        audioRecord = null
        isRecording = false
        try {
            recorder.stop()
            recordingThread?.join()
            recordingFailure?.let { throw it }
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Audio recording did not stop cleanly", error)
        } finally {
            recordingThread = null
            recorder.release()
        }
    }

    private fun writeAudio(recorder: AudioRecord, outputFile: File, bufferSize: Int) {
        try {
            FileOutputStream(outputFile).use { output ->
                output.write(ByteArray(WAV_HEADER_SIZE))
                val buffer = ByteArray(bufferSize)
                while (isRecording) {
                    val bytesRead = recorder.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) output.write(buffer, 0, bytesRead)
                }
            }
            writeWaveHeader(outputFile)
        } catch (error: Exception) {
            recordingFailure = IllegalStateException("Audio recording could not be written", error)
        }
    }

    private fun writeWaveHeader(outputFile: File) {
        val audioDataSize = outputFile.length() - WAV_HEADER_SIZE
        val header = ByteBuffer.allocate(WAV_HEADER_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                put("RIFF".toByteArray(Charsets.US_ASCII))
                putInt((audioDataSize + WAV_HEADER_SIZE - RIFF_HEADER_SIZE).toInt())
                put("WAVE".toByteArray(Charsets.US_ASCII))
                put("fmt ".toByteArray(Charsets.US_ASCII))
                putInt(PCM_FORMAT_CHUNK_SIZE)
                putShort(PCM_AUDIO_FORMAT.toShort())
                putShort(CHANNEL_COUNT.toShort())
                putInt(SAMPLE_RATE_HZ)
                putInt(BYTE_RATE)
                putShort(BLOCK_ALIGNMENT.toShort())
                putShort(BITS_PER_SAMPLE.toShort())
                put("data".toByteArray(Charsets.US_ASCII))
                putInt(audioDataSize.toInt())
            }
            .array()
        RandomAccessFile(outputFile, "rw").use { file ->
            file.seek(0)
            file.write(header)
        }
    }

    private fun recordingBufferSize(): Int {
        val minimum = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        check(minimum > 0) { "No supported microphone buffer size" }
        return minimum * BUFFER_MULTIPLIER
    }

    @SuppressLint("MissingPermission")
    private fun createAudioRecord(bufferSize: Int): AudioRecord = AudioRecord.Builder()
        .setAudioSource(MediaRecorder.AudioSource.MIC)
        .setAudioFormat(
            AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE_HZ)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build(),
        )
        .setBufferSizeInBytes(bufferSize)
        .build()

    private companion object {
        const val SAMPLE_RATE_HZ = 16_000
        const val CHANNEL_COUNT = 1
        const val BITS_PER_SAMPLE = 16
        const val BYTES_PER_SAMPLE = BITS_PER_SAMPLE / 8
        const val BYTE_RATE = SAMPLE_RATE_HZ * CHANNEL_COUNT * BYTES_PER_SAMPLE
        const val BLOCK_ALIGNMENT = CHANNEL_COUNT * BYTES_PER_SAMPLE
        const val PCM_AUDIO_FORMAT = 1
        const val PCM_FORMAT_CHUNK_SIZE = 16
        const val WAV_HEADER_SIZE = 44
        const val RIFF_HEADER_SIZE = 8
        const val BUFFER_MULTIPLIER = 2
        const val RECORDING_THREAD_NAME = "KirokuPcmRecorder"
    }
}
