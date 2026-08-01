package jp.co.kirokuai.app.viewmodel

import jp.co.kirokuai.app.ai.speech.SpeechRecognizer
import jp.co.kirokuai.app.ai.speech.SpeechRepository
import jp.co.kirokuai.app.ai.speech.Transcript
import java.io.File
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TranscriptionViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startTranscription_exposesLoadingThenSuccess() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val result = CompletableDeferred<Transcript>()
        val viewModel = TranscriptionViewModel(
            speechRepository = SpeechRepository(DeferredSpeechRecognizer(result)),
        )

        viewModel.startTranscription(File("meeting.m4a"))
        runCurrent()
        assertEquals(TranscriptionUiState.Loading, viewModel.uiState.value)

        val transcript = Transcript(text = "文字起こし結果", language = "ja")
        result.complete(transcript)
        runCurrent()

        assertEquals(
            TranscriptionUiState.Success(transcript),
            viewModel.uiState.value,
        )
    }

    @Test
    fun startTranscription_exposesErrorWhenRecognizerFails() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = TranscriptionViewModel(
            speechRepository = SpeechRepository(FailingSpeechRecognizer()),
        )

        viewModel.startTranscription(File("meeting.m4a"))
        runCurrent()

        assertEquals(
            TranscriptionUiState.Error("文字起こしに失敗しました"),
            viewModel.uiState.value,
        )
    }
}

private class DeferredSpeechRecognizer(
    private val result: CompletableDeferred<Transcript>,
) : SpeechRecognizer {
    override suspend fun transcribe(audioFile: File): Transcript = result.await()
}

private class FailingSpeechRecognizer : SpeechRecognizer {
    override suspend fun transcribe(audioFile: File): Transcript {
        error("Transcription failed")
    }
}
