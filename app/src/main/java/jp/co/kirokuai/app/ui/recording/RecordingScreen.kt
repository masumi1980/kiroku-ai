package jp.co.kirokuai.app.ui.recording

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.kirokuai.app.viewmodel.RecordingUiState
import jp.co.kirokuai.app.viewmodel.RecordingViewModel
import jp.co.kirokuai.app.util.formatDuration

@Composable
fun RecordingScreen(
    viewModel: RecordingViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) viewModel.startRecording()
    }

    RecordingContent(
        uiState = uiState,
        onMeetingTitleChange = viewModel::updateMeetingTitle,
        onStartClick = {
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO,
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                viewModel.startRecording()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        onStopClick = viewModel::stopRecording,
        modifier = modifier,
    )
}

@Composable
private fun RecordingContent(
    uiState: RecordingUiState,
    onMeetingTitleChange: (String) -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "会議を録音",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = uiState.meetingTitle,
            onValueChange = onMeetingTitleChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isRecording,
            label = { Text("会議タイトル") },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = formatElapsedTime(uiState.elapsedSeconds),
            style = MaterialTheme.typography.displayMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = onStartClick,
                enabled = !uiState.isRecording,
            ) {
                Text("録音開始")
            }
            Button(
                onClick = onStopClick,
                enabled = uiState.isRecording,
            ) {
                Text("停止")
            }
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

internal fun formatElapsedTime(elapsedSeconds: Long): String {
    return formatDuration(elapsedSeconds)
}
