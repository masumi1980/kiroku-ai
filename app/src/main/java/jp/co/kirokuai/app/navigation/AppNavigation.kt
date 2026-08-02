package jp.co.kirokuai.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.co.kirokuai.app.KirokuApplication
import jp.co.kirokuai.app.data.PcmWavAudioRecorder
import jp.co.kirokuai.app.ui.history.HistoryScreen
import jp.co.kirokuai.app.ui.home.HomeScreen
import jp.co.kirokuai.app.ui.meeting.MeetingDetailScreen
import jp.co.kirokuai.app.ui.recording.RecordingScreen
import jp.co.kirokuai.app.ui.settings.SettingsScreen
import jp.co.kirokuai.app.viewmodel.HistoryViewModel
import jp.co.kirokuai.app.viewmodel.MeetingViewModel
import jp.co.kirokuai.app.viewmodel.RecordingViewModel
import java.io.File

private const val HOME_ROUTE = "home"
private const val HISTORY_ROUTE = "history"
private const val SETTINGS_ROUTE = "settings"
private const val RECORDING_ROUTE = "recording"
private const val MEETING_ROUTE = "meeting"
private const val MEETING_ID_ARGUMENT = "meetingId"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as KirokuApplication
    val meetingRepository = application.appContainer.meetingRepository
    val speechRepository = application.appContainer.speechRepository

    NavHost(
        navController = navController,
        startDestination = HOME_ROUTE,
    ) {
        composable(HOME_ROUTE) {
            HomeScreen(
                onNewMeetingClick = { navController.navigate(RECORDING_ROUTE) },
                onHistoryClick = { navController.navigate(HISTORY_ROUTE) },
                onSettingsClick = { navController.navigate(SETTINGS_ROUTE) },
            )
        }
        composable(HISTORY_ROUTE) {
            val historyViewModel = viewModel {
                HistoryViewModel(meetingRepository = meetingRepository)
            }
            HistoryScreen(
                viewModel = historyViewModel,
                onMeetingClick = { meetingId -> navController.navigate("$MEETING_ROUTE/$meetingId") },
            )
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen()
        }
        composable(RECORDING_ROUTE) {
            val context = LocalContext.current.applicationContext
            val audioRecorder = remember(context) { PcmWavAudioRecorder(context) }
            val recordingViewModel = viewModel {
                RecordingViewModel(
                    audioRecorder = audioRecorder,
                    meetingRepository = meetingRepository,
                    speechRepository = speechRepository,
                    outputPathProvider = {
                        val recordingDirectory = File(context.filesDir, "recordings")
                        recordingDirectory.mkdirs()
                        File(recordingDirectory, "${System.currentTimeMillis()}.wav").absolutePath
                    },
                )
            }
            RecordingScreen(viewModel = recordingViewModel)
        }
        composable("$MEETING_ROUTE/{$MEETING_ID_ARGUMENT}") { backStackEntry ->
            val meetingId = requireNotNull(backStackEntry.arguments?.getString(MEETING_ID_ARGUMENT)).toLong()
            val meetingViewModel = viewModel {
                MeetingViewModel(
                    meetingId = meetingId,
                    summarizer = application.appContainer.meetingSummarizer,
                    summaryRepository = application.appContainer.meetingSummaryRepository,
                )
            }
            MeetingDetailScreen(viewModel = meetingViewModel)
        }
    }
}
