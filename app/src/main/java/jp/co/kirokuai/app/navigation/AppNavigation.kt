package jp.co.kirokuai.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import jp.co.kirokuai.app.ui.history.HistoryScreen
import jp.co.kirokuai.app.ui.home.HomeScreen
import jp.co.kirokuai.app.ui.recording.RecordingScreen
import jp.co.kirokuai.app.ui.settings.SettingsScreen
import jp.co.kirokuai.app.data.MediaRecorderAudioRecorder
import jp.co.kirokuai.app.viewmodel.RecordingViewModel
import java.io.File

private const val HOME_ROUTE = "home"
private const val HISTORY_ROUTE = "history"
private const val SETTINGS_ROUTE = "settings"
private const val RECORDING_ROUTE = "recording"

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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
            HistoryScreen()
        }
        composable(SETTINGS_ROUTE) {
            SettingsScreen()
        }
        composable(RECORDING_ROUTE) {
            val context = LocalContext.current.applicationContext
            val audioRecorder = remember(context) { MediaRecorderAudioRecorder(context) }
            val recordingViewModel = viewModel {
                RecordingViewModel(
                    audioRecorder = audioRecorder,
                    outputPathProvider = {
                        val recordingDirectory = File(context.filesDir, "recordings")
                        recordingDirectory.mkdirs()
                        File(recordingDirectory, "${System.currentTimeMillis()}.m4a").absolutePath
                    },
                )
            }
            RecordingScreen(viewModel = recordingViewModel)
        }
    }
}
