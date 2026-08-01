package jp.co.kirokuai.app.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jp.co.kirokuai.app.model.Meeting
import jp.co.kirokuai.app.model.MeetingStatus
import jp.co.kirokuai.app.util.formatDuration
import jp.co.kirokuai.app.viewmodel.HistoryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
) {
    val meetings by viewModel.meetings.collectAsStateWithLifecycle()

    if (meetings.isEmpty()) {
        EmptyHistory(modifier = modifier)
    } else {
        MeetingList(
            meetings = meetings,
            modifier = modifier,
        )
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("No meetings yet")
    }
}

@Composable
private fun MeetingList(
    meetings: List<Meeting>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "会議履歴",
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        items(
            items = meetings,
            key = Meeting::id,
        ) { meeting ->
            MeetingCard(meeting = meeting)
        }
    }
}

@Composable
private fun MeetingCard(
    meeting: Meeting,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "作成日時: ${formatCreatedAt(meeting.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "録音時間: ${formatDuration(meeting.duration)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "状態: ${meeting.status.displayName()}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

internal fun formatCreatedAt(createdAt: Long): String =
    DATE_TIME_FORMATTER.format(
        Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()),
    )

private fun MeetingStatus.displayName(): String = when (this) {
    MeetingStatus.RECORDING -> "録音中"
    MeetingStatus.TRANSCRIBING -> "文字起こし中"
    MeetingStatus.COMPLETED -> "完了"
    MeetingStatus.ERROR -> "エラー"
}

private val DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm", Locale.JAPAN)
